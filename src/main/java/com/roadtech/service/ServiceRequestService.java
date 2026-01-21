package com.roadtech.service;

import com.roadtech.dto.request.CreateServiceRequestDto;
import com.roadtech.dto.request.ServiceRequestDto;
import com.roadtech.entity.ServiceRequest;
import com.roadtech.entity.ServiceRequest.RequestStatus;
import com.roadtech.entity.User;
import com.roadtech.exception.BadRequestException;
import com.roadtech.exception.ForbiddenException;
import com.roadtech.exception.ResourceNotFoundException;
import com.roadtech.repository.ServiceRequestRepository;
import com.roadtech.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public ServiceRequestDto createRequest(Long userId, CreateServiceRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Check if user already has an active request
        serviceRequestRepository.findActiveRequestByUserId(userId)
                .ifPresent(r -> {
                    throw new BadRequestException("You already have an active service request");
                });

        ServiceRequest request = ServiceRequest.builder()
                .user(user)
                .issueType(dto.getIssueType())
                .description(dto.getDescription())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .address(dto.getAddress())
                .status(RequestStatus.PENDING)
                .build();

        request = serviceRequestRepository.save(request);

        // Notify available mechanics about new request
        notificationService.notifyNewRequest(request);

        return ServiceRequestDto.fromEntity(request);
    }

    @Transactional(readOnly = true)
    public ServiceRequestDto getRequestById(Long requestId, Long userId) {
        ServiceRequest request = serviceRequestRepository.findByIdWithDetails(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request", requestId));

        // Verify user has access to this request
        if (!request.getUser().getId().equals(userId) &&
            (request.getMechanic() == null || !request.getMechanic().getId().equals(userId))) {
            throw new ForbiddenException("You don't have access to this request");
        }

        return ServiceRequestDto.fromEntityWithDetails(request);
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestDto> getUserRequests(Long userId) {
        return serviceRequestRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ServiceRequestDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceRequestDto getActiveRequest(Long userId) {
        return serviceRequestRepository.findActiveRequestByUserId(userId)
                .map(ServiceRequestDto::fromEntityWithDetails)
                .orElse(null);
    }

    @Transactional
    public ServiceRequestDto cancelRequest(Long requestId, Long userId) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request", requestId));

        if (!request.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You can only cancel your own requests");
        }

        if (request.getStatus() == RequestStatus.COMPLETED ||
            request.getStatus() == RequestStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel a completed or already cancelled request");
        }

        request.setStatus(RequestStatus.CANCELLED);
        request = serviceRequestRepository.save(request);

        // Notify mechanic if one was assigned
        if (request.getMechanic() != null) {
            notificationService.notifyRequestCancelled(request);
        }

        return ServiceRequestDto.fromEntity(request);
    }
}
