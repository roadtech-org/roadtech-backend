package com.roadtech.dto.parts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorWithPartsDto {
    private Long id;
    private String shopName;
    private String address;
    private String phone;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Double distance; // in km
    private BigDecimal rating;
    private Boolean isOpen;
    private String openingTime;
    private String closingTime;
    private List<PartDto> availableParts;
}