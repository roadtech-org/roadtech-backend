package com.roadtech.service;

import com.roadtech.dto.mechanic.AvailabilityDto;
import com.roadtech.dto.mechanic.LocationUpdateDto;
import com.roadtech.dto.mechanic.MechanicProfileDto;
import com.roadtech.dto.mechanic.UpdateMechanicProfileDto;
import com.roadtech.dto.request.ServiceRequestDto;
import com.roadtech.entity.MechanicProfile;
import com.roadtech.entity.ServiceRequest;
import com.roadtech.entity.ServiceRequest.RequestStatus;
import com.roadtech.exception.BadRequestException;
import com.roadtech.exception.ForbiddenException;
import com.roadtech.exception.ResourceNotFoundException;
import com.roadtech.repository.MechanicProfileRepository;
import com.roadtech.repository.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MechanicService {

    private final MechanicProfileRepository mechanicProfileRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final NotificationService notificationService;
    private final LocationService locationService;

    @Transactional(readOnly = true)
    public MechanicProfileDto getProfile(Long userId) {
        MechanicProfile profile = mechanicProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic profile not found"));
        return MechanicProfileDto.fromEntity(profile);
    }

    @Transactional
    public MechanicProfileDto updateProfile(Long userId, UpdateMechanicProfileDto dto) {
        MechanicProfile profile = mechanicProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic profile not found"));

        if (dto.getSpecializations() != null) {
            profile.setSpecializations(dto.getSpecializations());
        }
        profile.setIsAvailable(dto.getIsAvailable());

        profile = mechanicProfileRepository.save(profile);
        return MechanicProfileDto.fromEntity(profile);
    }

    @Transactional
    public MechanicProfileDto toggleAvailability(Long userId, AvailabilityDto dto) {
        MechanicProfile profile = mechanicProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic profile not found"));

        profile.setIsAvailable(dto.getIsAvailable());
        profile = mechanicProfileRepository.save(profile);

        return MechanicProfileDto.fromEntity(profile);
    }

    @Transactional
    public void updateLocation(Long userId, LocationUpdateDto dto) {
        MechanicProfile profile = mechanicProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic profile not found"));

        profile.setCurrentLatitude(dto.getLatitude());
        profile.setCurrentLongitude(dto.getLongitude());
        profile.setLocationUpdatedAt(LocalDateTime.now());

        mechanicProfileRepository.save(profile);

        // Send location update to active requests
        List<ServiceRequest> activeRequests = serviceRequestRepository
                .findActiveRequestsByMechanicId(userId);

        for (ServiceRequest request : activeRequests) {
            notificationService.notifyLocationUpdate(
                    request.getId(),
                    userId,
                    dto.getLatitude(),
                    dto.getLongitude()
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestDto> getPendingRequests() {
        return serviceRequestRepository.findPendingRequestsWithoutMechanic()
                .stream()
                .map(ServiceRequestDto::fromEntityWithDetails)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestDto> getAssignedRequests(Long userId) {
        return serviceRequestRepository.findByMechanicIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ServiceRequestDto::fromEntityWithDetails)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestDto> getActiveRequests(Long userId) {
        return serviceRequestRepository.findActiveRequestsByMechanicId(userId)
                .stream()
                .map(ServiceRequestDto::fromEntityWithDetails)
                .toList();
    }

    @Transactional
    public ServiceRequestDto acceptRequest(Long requestId, Long mechanicUserId) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request", requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Request is no longer pending");
        }

        if (request.getMechanic() != null) {
            throw new BadRequestException("Request already has an assigned mechanic");
        }

        MechanicProfile profile = mechanicProfileRepository.findByUserId(mechanicUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic profile not found"));

        if (!profile.getIsAvailable()) {
            throw new BadRequestException("You must be available to accept requests");
        }

        request.setMechanic(profile.getUser());
        request.setStatus(RequestStatus.ACCEPTED);
        request.setAcceptedAt(LocalDateTime.now());

        // Calculate ETA if mechanic has location
        if (profile.getCurrentLatitude() != null && profile.getCurrentLongitude() != null) {
            double distance = locationService.calculateDistance(
                    profile.getCurrentLatitude(),
                    profile.getCurrentLongitude(),
                    request.getLatitude(),
                    request.getLongitude()
            );
            int etaMinutes = locationService.estimateArrivalMinutes(distance);
            request.setEstimatedArrival(LocalDateTime.now().plusMinutes(etaMinutes));
        }

        request = serviceRequestRepository.save(request);

        // Notify user about status change
        notificationService.notifyRequestStatusUpdate(request);

        return ServiceRequestDto.fromEntityWithDetails(request);
    }

    @Transactional
    public ServiceRequestDto rejectRequest(Long requestId, Long mechanicUserId) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request", requestId));

        // If mechanic was assigned, unassign them
        if (request.getMechanic() != null && request.getMechanic().getId().equals(mechanicUserId)) {
            request.setMechanic(null);
            request.setStatus(RequestStatus.PENDING);
            request.setAcceptedAt(null);
            request.setEstimatedArrival(null);

            request = serviceRequestRepository.save(request);
            notificationService.notifyRequestStatusUpdate(request);
        }

        return ServiceRequestDto.fromEntity(request);
    }

    @Transactional
    public ServiceRequestDto startService(Long requestId, Long mechanicUserId) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request", requestId));

        validateMechanicAccess(request, mechanicUserId);

        if (request.getStatus() != RequestStatus.ACCEPTED) {
            throw new BadRequestException("Can only start an accepted request");
        }

        request.setStatus(RequestStatus.IN_PROGRESS);
        request.setStartedAt(LocalDateTime.now());

        request = serviceRequestRepository.save(request);
        notificationService.notifyRequestStatusUpdate(request);

        return ServiceRequestDto.fromEntityWithDetails(request);
    }

    @Transactional
    public ServiceRequestDto completeService(Long requestId, Long mechanicUserId) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request", requestId));

        validateMechanicAccess(request, mechanicUserId);

        if (request.getStatus() != RequestStatus.IN_PROGRESS) {
            throw new BadRequestException("Can only complete an in-progress request");
        }

        request.setStatus(RequestStatus.COMPLETED);
        request.setCompletedAt(LocalDateTime.now());

        request = serviceRequestRepository.save(request);

        // Update mechanic stats
        MechanicProfile profile = mechanicProfileRepository.findByUserId(mechanicUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic profile not found"));
        profile.setTotalJobs(profile.getTotalJobs() + 1);
        mechanicProfileRepository.save(profile);

        notificationService.notifyRequestStatusUpdate(request);

        return ServiceRequestDto.fromEntityWithDetails(request);
    }

    private void validateMechanicAccess(ServiceRequest request, Long mechanicUserId) {
        if (request.getMechanic() == null || !request.getMechanic().getId().equals(mechanicUserId)) {
            throw new ForbiddenException("You are not assigned to this request");
        }
    }
}
