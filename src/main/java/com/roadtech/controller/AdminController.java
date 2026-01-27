package com.roadtech.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.roadtech.dto.admin.*;
import com.roadtech.service.AdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    // Dashboard Statistics
    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        DashboardStatsDto stats = adminService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    // User Management
    @GetMapping("/users")
    @Operation(summary = "Get all users with filters")
    public ResponseEntity<Page<UserManagementDto>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        Page<UserManagementDto> users = adminService.getAllUsers(role, search, pageable);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/toggle-active")
    @Operation(summary = "Toggle user active status")
    public ResponseEntity<UserManagementDto> toggleUserActive(@PathVariable Long id) {
        UserManagementDto user = adminService.toggleUserActive(id);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Mechanic Verification
    @GetMapping("/mechanics/pending")
    @Operation(summary = "Get pending mechanic verifications")
    public ResponseEntity<List<MechanicVerificationDto>> getPendingMechanics() {
        List<MechanicVerificationDto> mechanics = adminService.getPendingMechanics();
        return ResponseEntity.ok(mechanics);
    }

    @PutMapping("/mechanics/{id}/verify")
    @Operation(summary = "Verify a mechanic")
    public ResponseEntity<MechanicVerificationDto> verifyMechanic(
            @PathVariable Long id,
            @Valid @RequestBody VerifyDto dto
    ) {
        MechanicVerificationDto mechanic = adminService.verifyMechanic(id, dto);
        return ResponseEntity.ok(mechanic);
    }

    @PutMapping("/mechanics/{id}/reject")
    @Operation(summary = "Reject a mechanic verification")
    public ResponseEntity<MechanicVerificationDto> rejectMechanic(
            @PathVariable Long id,
            @Valid @RequestBody RejectDto dto
    ) {
        MechanicVerificationDto mechanic = adminService.rejectMechanic(id, dto);
        return ResponseEntity.ok(mechanic);
    }

    // Parts Provider Verification
    @GetMapping("/parts-providers/pending")
    @Operation(summary = "Get pending parts provider verifications")
    public ResponseEntity<List<PartsProviderVerificationDto>> getPendingProviders() {
        List<PartsProviderVerificationDto> providers = adminService.getPendingProviders();
        return ResponseEntity.ok(providers);
    }

    @PutMapping("/parts-providers/{id}/verify")
    @Operation(summary = "Verify a parts provider")
    public ResponseEntity<PartsProviderVerificationDto> verifyProvider(
            @PathVariable Long id,
            @Valid @RequestBody VerifyDto dto
    ) {
        PartsProviderVerificationDto provider = adminService.verifyProvider(id, dto);
        return ResponseEntity.ok(provider);
    }

    @PutMapping("/parts-providers/{id}/reject")
    @Operation(summary = "Reject a parts provider verification")
    public ResponseEntity<PartsProviderVerificationDto> rejectProvider(
            @PathVariable Long id,
            @Valid @RequestBody RejectDto dto
    ) {
        PartsProviderVerificationDto provider = adminService.rejectProvider(id, dto);
        return ResponseEntity.ok(provider);
    }

    // System Logs
    @GetMapping("/logs")
    @Operation(summary = "Get system logs")
    public ResponseEntity<Page<SystemLogDto>> getSystemLogs(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long userId,
            Pageable pageable
    ) {
        Page<SystemLogDto> logs = adminService.getSystemLogs(level, action, userId, pageable);
        return ResponseEntity.ok(logs);
    }

    @DeleteMapping("/logs/{id}")
    @Operation(summary = "Delete a log entry")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        adminService.deleteLog(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/logs/clear")
    @Operation(summary = "Clear old logs")
    public ResponseEntity<Map<String, Object>> clearOldLogs(
            @RequestParam(defaultValue = "30") int daysOld
    ) {
        int deleted = adminService.clearOldLogs(daysOld);
        return ResponseEntity.ok(Map.of("deleted", deleted));
    }

    // Service Requests Management
    @GetMapping("/requests")
    @Operation(summary = "Get all service requests")
    public ResponseEntity<Page<ServiceRequestManagementDto>> getAllRequests(
            @RequestParam(required = false) String status,
            Pageable pageable
    ) {
        Page<ServiceRequestManagementDto> requests = adminService.getAllRequests(status, pageable);
        return ResponseEntity.ok(requests);
    }

    // Analytics
    @GetMapping("/analytics/requests")
    @Operation(summary = "Get request analytics")
    public ResponseEntity<Map<String, Object>> getRequestAnalytics(
            @RequestParam(required = false) String period
    ) {
        Map<String, Object> analytics = adminService.getRequestAnalytics(period);
        return ResponseEntity.ok(analytics);
    }
}