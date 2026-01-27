package com.roadtech.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.roadtech.dto.parts.*;
import com.roadtech.security.CustomUserDetails;
import com.roadtech.service.PartsProviderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/parts-provider")
@RequiredArgsConstructor
@Tag(name = "Parts Provider", description = "Parts provider endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PartsProviderController {

    private final PartsProviderService partsProviderService;

    @GetMapping("/profile")
    @Operation(summary = "Get parts provider profile")
    public ResponseEntity<PartsProviderDto> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PartsProviderDto profile = partsProviderService.getProfile(userDetails.getUserId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update parts provider profile")
    public ResponseEntity<PartsProviderDto> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdatePartsProviderDto dto
    ) {
        PartsProviderDto profile = partsProviderService.updateProfile(userDetails.getUserId(), dto);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/status")
    @Operation(summary = "Toggle shop open/close status")
    public ResponseEntity<PartsProviderDto> toggleStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ShopStatusDto dto
    ) {
        PartsProviderDto profile = partsProviderService.toggleStatus(userDetails.getUserId(), dto);
        return ResponseEntity.ok(profile);
    }

    // Parts Management
    @GetMapping("/parts")
    @Operation(summary = "Get all parts for this provider")
    public ResponseEntity<List<PartDto>> getMyParts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<PartDto> parts = partsProviderService.getMyParts(userDetails.getUserId());
        return ResponseEntity.ok(parts);
    }

    @PostMapping("/parts")
    @Operation(summary = "Add a new part")
    public ResponseEntity<PartDto> addPart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreatePartDto dto
    ) {
        PartDto part = partsProviderService.addPart(userDetails.getUserId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(part);
    }

    @PutMapping("/parts/{id}")
    @Operation(summary = "Update a part")
    public ResponseEntity<PartDto> updatePart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdatePartDto dto
    ) {
        PartDto part = partsProviderService.updatePart(userDetails.getUserId(), id, dto);
        return ResponseEntity.ok(part);
    }

    @DeleteMapping("/parts/{id}")
    @Operation(summary = "Delete a part")
    public ResponseEntity<Void> deletePart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        partsProviderService.deletePart(userDetails.getUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/parts/{id}/stock")
    @Operation(summary = "Update part stock")
    public ResponseEntity<PartDto> updateStock(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockDto dto
    ) {
        PartDto part = partsProviderService.updateStock(userDetails.getUserId(), id, dto);
        return ResponseEntity.ok(part);
    }
}

