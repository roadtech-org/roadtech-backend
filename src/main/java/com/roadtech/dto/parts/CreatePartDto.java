// ============= CreatePartDto.java =============
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
public class CreatePartDto {
    @NotBlank(message = "Part name is required")
    @Size(max = 200)
    private String name;

    @NotNull(message = "Category is required")
    private Part.PartCategory category;

    @NotBlank(message = "Brand is required")
    @Size(max = 100)
    private String brand;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @Size(max = 1000)
    private String description;

    @Size(max = 500)
    private String imageUrl;
}