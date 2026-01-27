// ============= UserManagementDto.java =============
package com.roadtech.dto.admin;

import java.time.LocalDateTime;

import com.roadtech.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementDto {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private User.UserRole role;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static UserManagementDto fromEntity(User user) {
        return UserManagementDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}