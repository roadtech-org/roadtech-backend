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

@Service // Marks this class as a Spring service component
@RequiredArgsConstructor // Lombok: generates constructor for final fields
@Slf4j // Lombok: enables logging using 'log'
public class ServiceRequestService {

    // Repository for CRUD operations on ServiceRequest entity
    private final ServiceRequestRepository serviceRequestRepository;

    // Repository to fetch user details
    private final UserRepository userRepository;

    // Service responsible for sending notifications
    private final NotificationService notificationService;

    /**
     * Creates a new service request for a user
     */
    @Transactional // Ensures atomic DB operation
    public ServiceRequestDto createRequest(Long userId, CreateServiceRequestDto dto) {

        // Fetch user or throw exception if not found
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Check if user already has an active service request
        serviceRequestRepository.findActiveRequestByUserId(userId)
                .ifPresent(r -> {
                    throw new BadRequestException("You already have an active service request");
                });

        // Build a new ServiceRequest entity from input DTO
        ServiceRequest request = ServiceRequest.builder()
                .user(user)
                .issueType(dto.getIssueType())
                .description(dto.getDescription())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .address(dto.getAddress())
                .status(RequestStatus.PENDING) // Initial status
                .build();

        // Persist the service request in the database
        request = serviceRequestRepository.save(request);

        // Notify all available mechanics about the new request
        notificationService.notifyNewRequest(request);

        // Convert entity to DTO and return
        return ServiceRequestDto.fromEntity(request);
    }

    /**
     * Fetch a service request by ID with access validation
     */
    @Transactional(readOnly = true) // Read-only transaction for performance
    public ServiceRequestDto getRequestById(Long requestId, Long userId) {

        // Fetch request with related user/mechanic details
        ServiceRequest request = serviceRequestRepository.findByIdWithDetails(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request", requestId));

        // Allow access only if requester is the user or assigned mechanic
        if (!request.getUser().getId().equals(userId) &&
            (request.getMechanic() == null || !request.getMechanic().getId().equals(userId))) {
            throw new ForbiddenException("You don't have access to this request");
        }

        // Return detailed DTO
        return ServiceRequestDto.fromEntityWithDetails(request);
    }

    /**
     * Fetch all service requests created by a user
     */
    @Transactional(readOnly = true)
    public List<ServiceRequestDto> getUserRequests(Long userId) {

        // Fetch user's requests ordered by creation time (latest first)
        return serviceRequestRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ServiceRequestDto::fromEntity) // Convert entity to DTO
                .toList();
    }

    /**
     * Fetch currently active service request of a user
     */
    @Transactional(readOnly = true)
    public ServiceRequestDto getActiveRequest(Long userId) {

        // Return active request if present, else null
        return serviceRequestRepository.findActiveRequestByUserId(userId)
                .map(ServiceRequestDto::fromEntityWithDetails)
                .orElse(null);
    }

    /**
     * Cancel a service request created by the user
     */
    @Transactional
    public ServiceRequestDto cancelRequest(Long requestId, Long userId) {

        // Fetch service request or throw if not found
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request", requestId));

        // Ensure only the request owner can cancel
        if (!request.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You can only cancel your own requests");
        }

        // Prevent cancelling completed or already cancelled requests
        if (request.getStatus() == RequestStatus.COMPLETED ||
            request.getStatus() == RequestStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel a completed or already cancelled request");
        }

        // Update request status to CANCELLED
        request.setStatus(RequestStatus.CANCELLED);
        request = serviceRequestRepository.save(request);

        // Notify assigned mechanic about cancellation, if any
        if (request.getMechanic() != null) {
            notificationService.notifyRequestCancelled(request);
        }

        // Return updated request DTO
        return ServiceRequestDto.fromEntity(request);
    }
}
