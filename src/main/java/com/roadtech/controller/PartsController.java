package com.roadtech.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.roadtech.dto.parts.PartDto;
import com.roadtech.dto.parts.PartsProviderDto;
import com.roadtech.service.PartsProviderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/parts")
@RequiredArgsConstructor
@Tag(name = "Parts", description = "Public parts search endpoints")
public class PartsController {

    private final PartsProviderService partsProviderService;

    @GetMapping("/search")
    @Operation(summary = "Search parts by location and category")
    public ResponseEntity<List<PartDto>> searchParts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") Double radiusKm
    ) {
        List<PartDto> parts = partsProviderService.searchNearbyParts(
                category, search, latitude, longitude, radiusKm
        );
        return ResponseEntity.ok(parts);
    }

    @GetMapping("/providers/nearby")
    @Operation(summary = "Get nearby parts providers")
    public ResponseEntity<List<PartsProviderDto>> getNearbyProviders(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") Double radiusKm
    ) {
        List<PartsProviderDto> providers = partsProviderService.getNearbyProviders(
                latitude, longitude, radiusKm
        );
        return ResponseEntity.ok(providers);
    }
}