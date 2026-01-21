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

@RestController
@RequestMapping("/service-requests")
@RequiredArgsConstructor
@Tag(name = "Service Requests", description = "User service request endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    @PostMapping
    @Operation(summary = "Create a new service request")
    public ResponseEntity<ServiceRequestDto> createRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateServiceRequestDto dto
    ) {
        ServiceRequestDto response = serviceRequestService.createRequest(userDetails.getUserId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all user's service requests")
    public ResponseEntity<List<ServiceRequestDto>> getUserRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ServiceRequestDto> requests = serviceRequestService.getUserRequests(userDetails.getUserId());
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/active")
    @Operation(summary = "Get user's active service request")
    public ResponseEntity<ServiceRequestDto> getActiveRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ServiceRequestDto request = serviceRequestService.getActiveRequest(userDetails.getUserId());
        return ResponseEntity.ok(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service request by ID")
    public ResponseEntity<ServiceRequestDto> getRequestById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        ServiceRequestDto request = serviceRequestService.getRequestById(id, userDetails.getUserId());
        return ResponseEntity.ok(request);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a service request")
    public ResponseEntity<ServiceRequestDto> cancelRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        ServiceRequestDto request = serviceRequestService.cancelRequest(id, userDetails.getUserId());
        return ResponseEntity.ok(request);
    }
}
