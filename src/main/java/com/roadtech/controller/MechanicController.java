package com.roadtech.controller;

import com.roadtech.dto.mechanic.AvailabilityDto;
import com.roadtech.dto.mechanic.LocationUpdateDto;
import com.roadtech.dto.mechanic.MechanicProfileDto;
import com.roadtech.dto.mechanic.UpdateMechanicProfileDto;
import com.roadtech.dto.request.ServiceRequestDto;
import com.roadtech.security.CustomUserDetails;
import com.roadtech.service.MechanicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mechanic")
@RequiredArgsConstructor
@Tag(name = "Mechanic", description = "Mechanic-specific endpoints")
@SecurityRequirement(name = "bearerAuth")
public class MechanicController {

    private final MechanicService mechanicService;

    @GetMapping("/profile")
    @Operation(summary = "Get mechanic profile")
    public ResponseEntity<MechanicProfileDto> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MechanicProfileDto profile = mechanicService.getProfile(userDetails.getUserId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update mechanic profile")
    public ResponseEntity<MechanicProfileDto> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateMechanicProfileDto dto
    ) {
        MechanicProfileDto profile = mechanicService.updateProfile(userDetails.getUserId(), dto);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/availability")
    @Operation(summary = "Toggle mechanic availability")
    public ResponseEntity<MechanicProfileDto> toggleAvailability(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AvailabilityDto dto
    ) {
        MechanicProfileDto profile = mechanicService.toggleAvailability(userDetails.getUserId(), dto);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/location")
    @Operation(summary = "Update mechanic location")
    public ResponseEntity<Void> updateLocation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LocationUpdateDto dto
    ) {
        mechanicService.updateLocation(userDetails.getUserId(), dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/requests/pending")
    @Operation(summary = "Get pending requests without assigned mechanic")
    public ResponseEntity<List<ServiceRequestDto>> getPendingRequests() {
        List<ServiceRequestDto> requests = mechanicService.getPendingRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/requests")
    @Operation(summary = "Get mechanic's assigned requests")
    public ResponseEntity<List<ServiceRequestDto>> getAssignedRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ServiceRequestDto> requests = mechanicService.getAssignedRequests(userDetails.getUserId());
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/requests/active")
    @Operation(summary = "Get mechanic's active requests")
    public ResponseEntity<List<ServiceRequestDto>> getActiveRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ServiceRequestDto> requests = mechanicService.getActiveRequests(userDetails.getUserId());
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/requests/{id}/accept")
    @Operation(summary = "Accept a service request")
    public ResponseEntity<ServiceRequestDto> acceptRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        ServiceRequestDto request = mechanicService.acceptRequest(id, userDetails.getUserId());
        return ResponseEntity.ok(request);
    }

    @PutMapping("/requests/{id}/reject")
    @Operation(summary = "Reject a service request")
    public ResponseEntity<ServiceRequestDto> rejectRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        ServiceRequestDto request = mechanicService.rejectRequest(id, userDetails.getUserId());
        return ResponseEntity.ok(request);
    }

    @PutMapping("/requests/{id}/start")
    @Operation(summary = "Start service for a request")
    public ResponseEntity<ServiceRequestDto> startService(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        ServiceRequestDto request = mechanicService.startService(id, userDetails.getUserId());
        return ResponseEntity.ok(request);
    }

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
