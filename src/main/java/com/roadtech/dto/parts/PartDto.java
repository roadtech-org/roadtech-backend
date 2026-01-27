// ============= PartDto.java =============
package com.roadtech.dto.parts;

import com.roadtech.entity.Part;
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
public class PartDto {
    private Long id;
    private Long providerId;
    private String shopName;
    private String name;
    private Part.PartCategory category;
    private String brand;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private String imageUrl;
    private Boolean isAvailable;
    private LocalDateTime createdAt;

    public static PartDto fromEntity(Part part) {
        return PartDto.builder()
                .id(part.getId())
                .providerId(part.getProvider().getId())
                .shopName(part.getProvider().getShopName())
                .name(part.getName())
                .category(part.getCategory())
                .brand(part.getBrand())
                .price(part.getPrice())
                .stock(part.getStock())
                .description(part.getDescription())
                .imageUrl(part.getImageUrl())
                .isAvailable(part.getIsAvailable())
                .createdAt(part.getCreatedAt())
                .build();
    }
}