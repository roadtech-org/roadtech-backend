package com.roadtech.service;

import com.roadtech.dto.parts.*;
import com.roadtech.entity.Part;
import com.roadtech.entity.PartsProvider;
import com.roadtech.entity.User;
import com.roadtech.exception.BadRequestException;
import com.roadtech.exception.ForbiddenException;
import com.roadtech.exception.ResourceNotFoundException;
import com.roadtech.repository.PartRepository;
import com.roadtech.repository.PartsProviderRepository;
import com.roadtech.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartsProviderService {

    private final PartsProviderRepository partsProviderRepository;
    private final PartRepository partRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PartsProviderDto getProfile(Long userId) {
        PartsProvider provider = partsProviderRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parts provider profile not found"));
        return PartsProviderDto.fromEntity(provider);
    }

    @Transactional
    public PartsProviderDto updateProfile(Long userId, UpdatePartsProviderDto dto) {
        PartsProvider provider = partsProviderRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parts provider profile not found"));

        provider.setShopName(dto.getShopName());
        provider.setAddress(dto.getAddress());
        provider.setLatitude(dto.getLatitude());
        provider.setLongitude(dto.getLongitude());
        
        if (dto.getPhone() != null) {
            provider.setPhone(dto.getPhone());
        }
        if (dto.getOpeningTime() != null) {
            provider.setOpeningTime(dto.getOpeningTime());
        }
        if (dto.getClosingTime() != null) {
            provider.setClosingTime(dto.getClosingTime());
        }

        provider = partsProviderRepository.save(provider);
        return PartsProviderDto.fromEntity(provider);
    }

    @Transactional
    public PartsProviderDto toggleStatus(Long userId, ShopStatusDto dto) {
        PartsProvider provider = partsProviderRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parts provider profile not found"));

        provider.setIsOpen(dto.getIsOpen());
        provider = partsProviderRepository.save(provider);

        return PartsProviderDto.fromEntity(provider);
    }

    @Transactional(readOnly = true)
    public List<PartDto> getMyParts(Long userId) {
        PartsProvider provider = partsProviderRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parts provider profile not found"));

        return partRepository.findByProviderId(provider.getId())
                .stream()
                .map(PartDto::fromEntity)
                .toList();
    }

    @Transactional
    public PartDto addPart(Long userId, CreatePartDto dto) {
        PartsProvider provider = partsProviderRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parts provider profile not found"));

        Part part = Part.builder()
                .provider(provider)
                .name(dto.getName())
                .category(dto.getCategory())
                .brand(dto.getBrand())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .isAvailable(true)
                .build();

        part = partRepository.save(part);
        return PartDto.fromEntity(part);
    }

    @Transactional
    public PartDto updatePart(Long userId, Long partId, UpdatePartDto dto) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new ResourceNotFoundException("Part", partId));

        validateProviderOwnership(userId, part);

        if (dto.getName() != null) part.setName(dto.getName());
        if (dto.getCategory() != null) part.setCategory(dto.getCategory());
        if (dto.getBrand() != null) part.setBrand(dto.getBrand());
        if (dto.getPrice() != null) part.setPrice(dto.getPrice());
        if (dto.getStock() != null) part.setStock(dto.getStock());
        if (dto.getDescription() != null) part.setDescription(dto.getDescription());
        if (dto.getImageUrl() != null) part.setImageUrl(dto.getImageUrl());
        if (dto.getIsAvailable() != null) part.setIsAvailable(dto.getIsAvailable());

        part = partRepository.save(part);
        return PartDto.fromEntity(part);
    }

    @Transactional
    public void deletePart(Long userId, Long partId) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new ResourceNotFoundException("Part", partId));

        validateProviderOwnership(userId, part);
        partRepository.delete(part);
    }

    @Transactional
    public PartDto updateStock(Long userId, Long partId, UpdateStockDto dto) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new ResourceNotFoundException("Part", partId));

        validateProviderOwnership(userId, part);

        part.setStock(dto.getStock());
        part.setIsAvailable(dto.getStock() > 0);

        part = partRepository.save(part);
        return PartDto.fromEntity(part);
    }

    @Transactional(readOnly = true)
    public List<PartDto> searchNearbyParts(String category, String search, 
                                           Double latitude, Double longitude, Double radiusKm) {
        List<Part> parts;

        if (category != null && search != null) {
            parts = partRepository.searchNearbyByCategoryAndName(
                    Part.PartCategory.valueOf(category), search, latitude, longitude, radiusKm);
        } else if (category != null) {
            parts = partRepository.findNearbyByCategory(
                    Part.PartCategory.valueOf(category), latitude, longitude, radiusKm);
        } else if (search != null) {
            parts = partRepository.searchNearbyByName(search, latitude, longitude, radiusKm);
        } else {
            parts = partRepository.findNearbyParts(latitude, longitude, radiusKm);
        }

        return parts.stream()
                .map(PartDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PartsProviderDto> getNearbyProviders(Double latitude, Double longitude, Double radiusKm) {
        List<PartsProvider> providers = partsProviderRepository.findNearbyProviders(
                BigDecimal.valueOf(latitude), BigDecimal.valueOf(longitude), radiusKm);
        
        return providers.stream()
                .map(PartsProviderDto::fromEntity)
                .toList();
    }

    private void validateProviderOwnership(Long userId, Part part) {
        if (!part.getProvider().getUser().getId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to modify this part");
        }
    }
}