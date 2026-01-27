// ============= PartsProviderDto.java =============
package com.roadtech.dto.parts;

import com.roadtech.entity.PartsProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartsProviderDto {
    private Long id;
    private Long userId;
    private String shopName;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isVerified;
    private Boolean isOpen;
    private BigDecimal rating;
    private Integer totalOrders;
    private String phone;
    private String openingTime;
    private String closingTime;
    private LocalDateTime createdAt;

    public static PartsProviderDto fromEntity(PartsProvider provider) {
        return PartsProviderDto.builder()
                .id(provider.getId())
                .userId(provider.getUser().getId())
                .shopName(provider.getShopName())
                .address(provider.getAddress())
                .latitude(provider.getLatitude())
                .longitude(provider.getLongitude())
                .isVerified(provider.getIsVerified())
                .isOpen(provider.getIsOpen())
                .rating(provider.getRating())
                .totalOrders(provider.getTotalOrders())
                .phone(provider.getPhone())
                .openingTime(provider.getOpeningTime())
                .closingTime(provider.getClosingTime())
                .createdAt(provider.getCreatedAt())
                .build();
    }
}