// ============= DashboardStatsDto.java =============
package com.roadtech.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private Long totalUsers;
    private Long totalMechanics;
    private Long totalPartsProviders;
    private Long totalRequests;
    private Long pendingRequests;
    private Long activeRequests;
}