// ============= PartsProviderVerificationDto.java =============
package com.roadtech.dto.admin;

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
public class PartsProviderVerificationDto {
    private Long id;
    private Long userId;
    private String email;
    private String shopName;
    private String address;
    private String phone;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isVerified;
    private LocalDateTime createdAt;

    public static PartsProviderVerificationDto fromEntity(PartsProvider provider) {
        return PartsProviderVerificationDto.builder()
                .id(provider.getId())
                .userId(provider.getUser().getId())
                .email(provider.getUser().getEmail())
                .shopName(provider.getShopName())
                .address(provider.getAddress())
                .phone(provider.getPhone())
                .latitude(provider.getLatitude())
                .longitude(provider.getLongitude())
                .isVerified(provider.getIsVerified())
                .createdAt(provider.getCreatedAt())
                .build();
    }
}