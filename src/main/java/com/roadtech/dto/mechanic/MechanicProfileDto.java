package com.roadtech.dto.mechanic;

import com.roadtech.entity.MechanicProfile;
import com.roadtech.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MechanicProfileDto {

    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private List<String> specializations;
    private Boolean isAvailable;
    private Boolean isVerified;
    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
    private BigDecimal rating;
    private Integer totalJobs;
    private LocalDateTime locationUpdatedAt;

    public static MechanicProfileDto fromEntity(MechanicProfile profile) {
        User user = profile.getUser();
        return MechanicProfileDto.builder()
                .id(profile.getId())
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .specializations(profile.getSpecializations())
                .isAvailable(profile.getIsAvailable())
                .isVerified(profile.getIsVerified())
                .currentLatitude(profile.getCurrentLatitude())
                .currentLongitude(profile.getCurrentLongitude())
                .rating(profile.getRating())
                .totalJobs(profile.getTotalJobs())
                .locationUpdatedAt(profile.getLocationUpdatedAt())
                .build();
    }

    public static MechanicProfileDto fromUser(User user) {
        MechanicProfile profile = user.getMechanicProfile();
        if (profile == null) {
            return MechanicProfileDto.builder()
                    .userId(user.getId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .build();
        }
        return fromEntity(profile);
    }
}
