package com.roadtech.dto.mechanic;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMechanicProfileDto {

    private List<String> specializations;

    @NotNull(message = "Availability status is required")
    private Boolean isAvailable;
}
