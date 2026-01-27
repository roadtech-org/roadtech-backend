// ============= MechanicVerificationDto.java =============
package com.roadtech.dto.admin;

import java.time.LocalDateTime;
import java.util.List;

import com.roadtech.entity.MechanicProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MechanicVerificationDto {
    private Long id;
    private Long userId;
    private String email;
    private String fullName;
    private String phone;
    private List<String> specializations;
    private Boolean isVerified;
    private LocalDateTime createdAt;

    public static MechanicVerificationDto fromEntity(MechanicProfile profile) {
        return MechanicVerificationDto.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .email(profile.getUser().getEmail())
                .fullName(profile.getUser().getFullName())
                .phone(profile.getUser().getPhone())
                .specializations(profile.getSpecializations())
                .isVerified(profile.getIsVerified())
                .createdAt(profile.getCreatedAt())
                .build();
    }
}