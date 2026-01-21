package com.roadtech.dto.mechanic;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityDto {

    @NotNull(message = "Availability status is required")
    private Boolean isAvailable;
}
