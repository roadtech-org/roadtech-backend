// ============= SystemLogDto.java =============
package com.roadtech.dto.admin;

import com.roadtech.entity.SystemLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLogDto {
    private Long id;
    private SystemLog.LogLevel level;
    private String action;
    private Long userId;
    private String userEmail;
    private String details;
    private String ipAddress;
    private LocalDateTime timestamp;

    public static SystemLogDto fromEntity(SystemLog log) {
        return SystemLogDto.builder()
                .id(log.getId())
                .level(log.getLevel())
                .action(log.getAction())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .userEmail(log.getUser() != null ? log.getUser().getEmail() : null)
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .timestamp(log.getTimestamp())
                .build();
    }
}