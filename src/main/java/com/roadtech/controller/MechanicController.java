package com.roadtech.controller;

// DTOs used for mechanic-related request/response payloads
import com.roadtech.dto.mechanic.AvailabilityDto;
import com.roadtech.dto.mechanic.LocationUpdateDto;
import com.roadtech.dto.mechanic.MechanicProfileDto;
import com.roadtech.dto.mechanic.UpdateMechanicProfileDto;
import com.roadtech.dto.request.ServiceRequestDto;

// Custom security principal holding authenticated user details
import com.roadtech.security.CustomUserDetails;

// Service layer handling mechanic business logic
import com.roadtech.service.MechanicService;

// Swagger / OpenAPI annotations for API documentation
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

// Validation and Spring annotations
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing all mechanic-specific APIs.
 * Handles profile management, availability, location updates,
 * and service request lifecycle for mechanics.
 */
@RestController
@RequestMapping("/mechanic") // Base URL for all mechanic APIs
@RequiredArgsConstructor     // Lombok generates constructor for final fields
@Tag(name = "Mechanic", description = "Mechanic-specific endpoints")
@SecurityRequirement(name = "bearerAuth") // All endpoints require JWT authentication
public class MechanicController {

    // Service layer dependency injected via constructor
    private final MechanicService mechanicService;

    /**
     * Fetch the logged-in mechanic's profile details.
     */
    @GetMapping("/profile")
    @Operation(summary = "Get mechanic profile")
    public ResponseEntity<MechanicProfileDto> getProfile(
            // Injects authenticated user's details from Spring Security context
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MechanicProfileDto profile = mechanicService.getProfile(userDetails.getUserId());
        return ResponseEntity.ok(profile);
    }

    /**
     * Update the logged-in mechanic's profile information.
     */
    @PutMapping("/profile")
    @Operation(summary = "Update mechanic profile")
    public ResponseEntity<MechanicProfileDto> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            // Validates request body fields using Jakarta Validation
            @Valid @RequestBody UpdateMechanicProfileDto dto
    ) {
        MechanicProfileDto profile = mechanicService.updateProfile(userDetails.getUserId(), dto);
        return ResponseEntity.ok(profile);
    }

    /**
     * Toggle mechanic availability (online/offline).
     */
    @PutMapping("/availability")
    @Operation(summary = "Toggle mechanic availability")
    public ResponseEntity<MechanicProfileDto> toggleAvailability(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AvailabilityDto dto
    ) {
        MechanicProfileDto profile = mechanicService.toggleAvailability(userDetails.getUserId(), dto);
        return ResponseEntity.ok(profile);
    }

    /**
     * Update real-time location of the mechanic.
     * Typically used for tracking and request assignment.
     */
    @PutMapping("/location")
    @Operation(summary = "Update mechanic location")
    public ResponseEntity<Void> updateLocation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LocationUpdateDto dto
    ) {
        mechanicService.updateLocation(userDetails.getUserId(), dto);
        return ResponseEntity.ok().build();
    }

    /**
     * Fetch all pending service requests
     * that do not yet have an assigned mechanic.
     */
    @GetMapping("/requests/pending")
    @Operation(summary = "Get pending requests without assigned mechanic")
    public ResponseEntity<List<ServiceRequestDto>> getPendingRequests() {
        List<ServiceRequestDto> requests = mechanicService.getPendingRequests();
        return ResponseEntity.ok(requests);
    }

    /**
     * Fetch all service requests assigned to the logged-in mechanic.
     */
    @GetMapping("/requests")
    @Operation(summary = "Get mechanic's assigned requests")
    public ResponseEntity<List<ServiceRequestDto>> getAssignedRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ServiceRequestDto> requests = mechanicService.getAssignedRequests(userDetails.getUserId());
        return ResponseEntity.ok(requests);
    }

    /**
     * Fetch only active (ongoing) service requests for the mechanic.
     */
    @GetMapping("/requests/active")
    @Operation(summary = "Get mechanic's active requests")
    public ResponseEntity<List<ServiceRequestDto>> getActiveRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ServiceRequestDto> requests = mechanicService.getActiveRequests(userDetails.getUserId());
        return ResponseEntity.ok(requests);
    }

    /**
     * Accept a pending service request.
     */
    @PutMapping("/requests/{id}/accept")
    @Operation(summary = "Accept a service request")
    public ResponseEntity<ServiceRequestDto> acceptRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            // ID of the service request to accept
            @PathVariable Long id
    ) {
        ServiceRequestDto request = mechanicService.acceptRequest(id, userDetails.getUserId());
        return ResponseEntity.ok(request);
    }

    /**
     * Reject a service request assigned to the mechanic.
     */
    @PutMapping("/requests/{id}/reject")
    @Operation(summary = "Reject a service request")
    public ResponseEntity<ServiceRequestDto> rejectRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        ServiceRequestDto request = mechanicService.rejectRequest(id, userDetails.getUserId());
        return ResponseEntity.ok(request);
    }

    /**
     * Mark a service request as started.
     */
    @PutMapping("/requests/{id}/start")
    @Operation(summary = "Start service for a request")
    public ResponseEntity<ServiceRequestDto> startService(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        ServiceRequestDto request = mechanicService.startService(id, userDetails.getUserId());
        return ResponseEntity.ok(request);
    }

    /**
     * Mark a service request as completed.
     */
    @PutMapping("/requests/{id}/complete")
    @Operation(summary = "Complete service for a request")
    public ResponseEntity<ServiceRequestDto> completeService(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        ServiceRequestDto request = mechanicService.completeService(id, userDetails.getUserId());
        return ResponseEntity.ok(request);
    }
}
