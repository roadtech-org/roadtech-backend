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

    // Repository for mechanic profile related DB operations
    private final MechanicProfileRepository mechanicProfileRepository;

    // Repository for service request related DB operations
    private final ServiceRequestRepository serviceRequestRepository;

    // Handles notifications sent to users/mechanics
    private final NotificationService notificationService;

    // Handles distance calculation and ETA estimation
    private final LocationService locationService;

    /**
     * Fetch mechanic profile for the given user ID
     */
    @Transactional(readOnly = true)
    public MechanicProfileDto getProfile(Long userId) {
        MechanicProfile profile = mechanicProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic profile not found"));
        return MechanicProfileDto.fromEntity(profile);
    }

    /**
     * Update mechanic profile details like specializations and availability
     */
    @Transactional
    public MechanicProfileDto updateProfile(Long userId, UpdateMechanicProfileDto dto) {
        MechanicProfile profile = mechanicProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic profile not found"));

        // Update specializations only if provided
        if (dto.getSpecializations() != null) {
            profile.setSpecializations(dto.getSpecializations());
        }

        // Update availability status
        profile.setIsAvailable(dto.getIsAvailable());

        profile = mechanicProfileRepository.save(profile);
        return MechanicProfileDto.fromEntity(profile);
    }

    /**
     * Toggle mechanic availability (online/offline)
     */
    @Transactional
    public MechanicProfileDto toggleAvailability(Long userId, AvailabilityDto dto) {
        MechanicProfile profile = mechanicProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic profile not found"));

        profile.setIsAvailable(dto.getIsAvailable());
        profile = mechanicProfileRepository.save(profile);

        return MechanicProfileDto.fromEntity(profile);
    }

    /**
     * Update mechanic's live location and notify active service requests
     */
    @Transactional
    public void updateLocation(Long userId, LocationUpdateDto dto) {
        MechanicProfile profile = mechanicProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic profile not found"));

        // Update current latitude and longitude
        profile.setCurrentLatitude(dto.getLatitude());
        profile.setCurrentLongitude(dto.getLongitude());
        profile.setLocationUpdatedAt(LocalDateTime.now());

        mechanicProfileRepository.save(profile);

        // Fetch all active service requests assigned to this mechanic
        List<ServiceRequest> activeRequests = serviceRequestRepository
                .findActiveRequestsByMechanicId(userId);

        // Notify users about mechanic's updated location
        for (ServiceRequest request : activeRequests) {
            notificationService.notifyLocationUpdate(
                    request.getId(),
                    userId,
                    dto.getLatitude(),
                    dto.getLongitude()
            );
        }
    }

    /**
     * Fetch all pending service requests that are not yet assigned to any mechanic
     */
    @Transactional(readOnly = true)
    public List<ServiceRequestDto> getPendingRequests() {
        return serviceRequestRepository.findPendingRequestsWithoutMechanic()
                .stream()
                .map(ServiceRequestDto::fromEntityWithDetails)
                .toList();
    }

    /**
     * Fetch all requests assigned to the mechanic (any status)
     */
    @Transactional(readOnly = true)
    public List<ServiceRequestDto> getAssignedRequests(Long userId) {
        return serviceRequestRepository.findByMechanicIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ServiceRequestDto::fromEntityWithDetails)
                .toList();
    }

    /**
     * Fetch only active requests (accepted or in-progress) for a mechanic
     */
    @Transactional(readOnly = true)
    public List<ServiceRequestDto> getActiveRequests(Long userId) {
        return serviceRequestRepository.findActiveRequestsByMechanicId(userId)
                .stream()
                .map(ServiceRequestDto::fromEntityWithDetails)
                .toList();
    }

    /**
     * Mechanic accepts a pending service request
     */
    @Transactional
    public ServiceRequestDto acceptRequest(Long requestId, Long mechanicUserId) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request", requestId));

        // Request must be pending
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Request is no longer pending");
        }

        // Request should not already have a mechanic assigned
        if (request.getMechanic() != null) {
            throw new BadRequestException("Request already has an assigned mechanic");
        }

        MechanicProfile profile = mechanicProfileRepository.findByUserId(mechanicUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic profile not found"));

        // Mechanic must be available to accept requests
        if (!profile.getIsAvailable()) {
            throw new BadRequestException("You must be available to accept requests");
        }

        // Assign mechanic and update request status
        request.setMechanic(profile.getUser());
        request.setStatus(RequestStatus.ACCEPTED);
        request.setAcceptedAt(LocalDateTime.now());

        // Calculate ETA if mechanic's location is available
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

        // Notify user about acceptance
        notificationService.notifyRequestStatusUpdate(request);

        return ServiceRequestDto.fromEntityWithDetails(request);
    }

    /**
     * Mechanic rejects an assigned request and makes it pending again
     */
    @Transactional
    public ServiceRequestDto rejectRequest(Long requestId, Long mechanicUserId) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request", requestId));

        // Only unassign if this mechanic is currently assigned
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

    /**
     * Mark a service request as started
     */
    @Transactional
    public ServiceRequestDto startService(Long requestId, Long mechanicUserId) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request", requestId));

        // Ensure mechanic is authorized
        validateMechanicAccess(request, mechanicUserId);

        // Can only start an accepted request
        if (request.getStatus() != RequestStatus.ACCEPTED) {
            throw new BadRequestException("Can only start an accepted request");
        }

        request.setStatus(RequestStatus.IN_PROGRESS);
        request.setStartedAt(LocalDateTime.now());

        request = serviceRequestRepository.save(request);
        notificationService.notifyRequestStatusUpdate(request);

        return ServiceRequestDto.fromEntityWithDetails(request);
    }

    /**
     * Mark a service request as completed and update mechanic stats
     */
    @Transactional
    public ServiceRequestDto completeService(Long requestId, Long mechanicUserId) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request", requestId));

        // Ensure mechanic is authorized
        validateMechanicAccess(request, mechanicUserId);

        // Can only complete an in-progress request
        if (request.getStatus() != RequestStatus.IN_PROGRESS) {
            throw new BadRequestException("Can only complete an in-progress request");
        }

        request.setStatus(RequestStatus.COMPLETED);
        request.setCompletedAt(LocalDateTime.now());

        request = serviceRequestRepository.save(request);

        // Increment mechanic's total completed jobs
        MechanicProfile profile = mechanicProfileRepository.findByUserId(mechanicUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic profile not found"));
        profile.setTotalJobs(profile.getTotalJobs() + 1);
        mechanicProfileRepository.save(profile);

        notificationService.notifyRequestStatusUpdate(request);

        return ServiceRequestDto.fromEntityWithDetails(request);
    }

    /**
     * Validate that the request is assigned to the current mechanic
     */
    private void validateMechanicAccess(ServiceRequest request, Long mechanicUserId) {
        if (request.getMechanic() == null || !request.getMechanic().getId().equals(mechanicUserId)) {
            throw new ForbiddenException("You are not assigned to this request");
        }
    }
}
