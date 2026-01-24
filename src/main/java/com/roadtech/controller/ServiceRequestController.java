package com.roadtech.controller;

import com.roadtech.dto.request.CreateServiceRequestDto;
import com.roadtech.dto.request.ServiceRequestDto;
import com.roadtech.security.CustomUserDetails;
import com.roadtech.service.ServiceRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller responsible for handling all
 * user-facing service request APIs.
 */
@RestController // Marks this class as a REST controller (returns JSON responses)
@RequestMapping("/service-requests") // Base URL for all endpoints in this controller
@RequiredArgsConstructor // Lombok: generates constructor for final fields
@Tag(name = "Service Requests", description = "User service request endpoints") // Swagger grouping
@SecurityRequirement(name = "bearerAuth") // Indicates JWT/Bearer authentication is required
public class ServiceRequestController {

    // Service layer dependency to handle business logic
    private final ServiceRequestService serviceRequestService;

    /**
     * Creates a new service request for the authenticated user.
     */
    @PostMapping
    @Operation(summary = "Create a new service request")
    public ResponseEntity<ServiceRequestDto> createRequest(
            // Injects currently authenticated user's details from Spring Security
            @AuthenticationPrincipal CustomUserDetails userDetails,

            // Validates request body using Jakarta Bean Validation annotations
            @Valid @RequestBody CreateServiceRequestDto dto
    ) {
        // Calls service layer to create a service request
        ServiceRequestDto response =
                serviceRequestService.createRequest(userDetails.getUserId(), dto);

        // Returns HTTP 201 Created with the created request data
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Fetches all service requests created by the authenticated user.
     */
    @GetMapping
    @Operation(summary = "Get all user's service requests")
    public ResponseEntity<List<ServiceRequestDto>> getUserRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // Retrieves all requests belonging to the user
        List<ServiceRequestDto> requests =
                serviceRequestService.getUserRequests(userDetails.getUserId());

        // Returns HTTP 200 OK with the list of requests
        return ResponseEntity.ok(requests);
    }

    /**
     * Fetches the currently active service request of the user (if any).
     */
    @GetMapping("/active")
    @Operation(summary = "Get user's active service request")
    public ResponseEntity<ServiceRequestDto> getActiveRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // Fetches active service request for the user
        ServiceRequestDto request =
                serviceRequestService.getActiveRequest(userDetails.getUserId());

        // Returns HTTP 200 OK with active request details
        return ResponseEntity.ok(request);
    }

    /**
     * Fetches a specific service request by its ID,
     * ensuring it belongs to the authenticated user.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get service request by ID")
    public ResponseEntity<ServiceRequestDto> getRequestById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id // Extracts request ID from URL
    ) {
        // Retrieves the service request after ownership validation
        ServiceRequestDto request =
                serviceRequestService.getRequestById(id, userDetails.getUserId());

        // Returns HTTP 200 OK with request details
        return ResponseEntity.ok(request);
    }

    /**
     * Cancels a service request owned by the authenticated user.
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a service request")
    public ResponseEntity<ServiceRequestDto> cancelRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        // Cancels the request after verifying user ownership
        ServiceRequestDto request =
                serviceRequestService.cancelRequest(id, userDetails.getUserId());

        // Returns updated request state
        return ResponseEntity.ok(request);
    }
}
