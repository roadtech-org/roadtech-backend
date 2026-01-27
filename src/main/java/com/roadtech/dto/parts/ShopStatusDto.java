// ============= ShopStatusDto.java =============
package com.roadtech.dto.parts;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopStatusDto {
    @NotNull(message = "Shop status is required")
    private Boolean isOpen;
}
