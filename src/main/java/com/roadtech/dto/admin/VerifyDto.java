// ============= VerifyDto.java =============
package com.roadtech.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyDto {
    @NotBlank(message = "Reason is required")
    private String reason;
}
