// ============= UpdatePartDto.java =============
package com.roadtech.dto.parts;

import com.roadtech.entity.Part;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePartDto {
    @Size(max = 200)
    private String name;

    private Part.PartCategory category;

    @Size(max = 100)
    private String brand;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @Size(max = 1000)
    private String description;

    @Size(max = 500)
    private String imageUrl;

    private Boolean isAvailable;
}