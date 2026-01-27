// ============= ServiceRequestManagementDto.java =============
package com.roadtech.dto.admin;

import com.roadtech.entity.ServiceRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestManagementDto {
    private Long id;
    private Long userId;
    private String userEmail;
    private Long mechanicId;
    private String mechanicEmail;
    private ServiceRequest.IssueType issueType;
    private String description;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private ServiceRequest.RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public static ServiceRequestManagementDto fromEntity(ServiceRequest request) {
        return ServiceRequestManagementDto.builder()
                .id(request.getId())
                .userId(request.getUser().getId())
                .userEmail(request.getUser().getEmail())
                .mechanicId(request.getMechanic() != null ? request.getMechanic().getId() : null)
                .mechanicEmail(request.getMechanic() != null ? request.getMechanic().getEmail() : null)
                .issueType(request.getIssueType())
                .description(request.getDescription())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .completedAt(request.getCompletedAt())
                .build();
    }
}