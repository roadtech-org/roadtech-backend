package com.roadtech.dto.request;

import com.roadtech.dto.UserDto;
import com.roadtech.dto.mechanic.MechanicProfileDto;
import com.roadtech.entity.ServiceRequest;
import com.roadtech.entity.ServiceRequest.IssueType;
import com.roadtech.entity.ServiceRequest.RequestStatus;
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
public class ServiceRequestDto {

    private Long id;
    private Long userId;
    private Long mechanicId;
    private IssueType issueType;
    private String description;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
    private RequestStatus status;
    private LocalDateTime estimatedArrival;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // Populated when fetching with details
    private UserDto user;
    private MechanicProfileDto mechanic;

    public static ServiceRequestDto fromEntity(ServiceRequest request) {
        return ServiceRequestDto.builder()
                .id(request.getId())
                .userId(request.getUser().getId())
                .mechanicId(request.getMechanic() != null ? request.getMechanic().getId() : null)
                .issueType(request.getIssueType())
                .description(request.getDescription())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress())
                .status(request.getStatus())
                .estimatedArrival(request.getEstimatedArrival())
                .createdAt(request.getCreatedAt())
                .acceptedAt(request.getAcceptedAt())
                .startedAt(request.getStartedAt())
                .completedAt(request.getCompletedAt())
                .build();
    }

    public static ServiceRequestDto fromEntityWithDetails(ServiceRequest request) {
        ServiceRequestDto dto = fromEntity(request);

        if (request.getUser() != null) {
            dto.setUser(UserDto.fromEntity(request.getUser()));
        }

        if (request.getMechanic() != null) {
            dto.setMechanic(MechanicProfileDto.fromUser(request.getMechanic()));
        }

        return dto;
    }
}
