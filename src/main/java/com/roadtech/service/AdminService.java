package com.roadtech.service;

import com.roadtech.dto.admin.*;
import com.roadtech.entity.*;
import com.roadtech.entity.User.UserRole;
import com.roadtech.exception.ResourceNotFoundException;
import com.roadtech.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final MechanicProfileRepository mechanicProfileRepository;
    private final PartsProviderRepository partsProviderRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final SystemLogRepository systemLogRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {

        long totalUsers = userRepository.count();
        long totalCustomers = userRepository.countByRole(UserRole.USER);
        long totalMechanics = mechanicProfileRepository.count();
        long totalProviders = partsProviderRepository.count();

        long totalRequests = serviceRequestRepository.count();
        long pendingRequests = serviceRequestRepository.countByStatus(
                ServiceRequest.RequestStatus.PENDING
        );
        long activeRequests = serviceRequestRepository.countByStatusIn(
                Arrays.asList(
                        ServiceRequest.RequestStatus.ACCEPTED,
                        ServiceRequest.RequestStatus.IN_PROGRESS
                )
        );

        // ✅ COMPLETED TODAY
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        long completedToday = serviceRequestRepository
                .countByStatusAndCompletedAtBetween(
                        ServiceRequest.RequestStatus.COMPLETED,
                        startOfDay,
                        endOfDay
                );
        long availableMechanics = mechanicProfileRepository.countAvailableMechanics();


        return DashboardStatsDto.builder()
                .totalUsers(totalUsers)
                .totalCustomers(totalCustomers)
                .totalMechanics(totalMechanics)
                .totalProviders(totalProviders)
                .totalRequests(totalRequests)
                .pendingRequests(pendingRequests)
                .activeRequests(activeRequests)
                .completedToday(completedToday) // 👈 add this
                .availableMechanics(availableMechanics)
                .build();
    }


    @Transactional(readOnly = true)
    public Page<UserManagementDto> getAllUsers(String role, String search, Pageable pageable) {
        Page<User> users;
        
        if (role != null && search != null) {
            users = userRepository.findByRoleAndSearchTerm(User.UserRole.valueOf(role), search, pageable);
        } else if (role != null) {
            users = userRepository.findByRole(User.UserRole.valueOf(role), pageable);
        } else if (search != null) {
            users = userRepository.findBySearchTerm(search, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(UserManagementDto::fromEntity);
    }

    @Transactional
    public UserManagementDto toggleUserActive(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        
        user.setIsActive(!user.getIsActive());
        user = userRepository.save(user);

        logAction(SystemLog.LogLevel.INFO, "USER_STATUS_CHANGED", 
                "User " + user.getEmail() + " status changed to " + user.getIsActive());

        return UserManagementDto.fromEntity(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        
        userRepository.delete(user);
        logAction(SystemLog.LogLevel.INFO, "USER_DELETED", "User " + user.getEmail() + " deleted");
    }

    @Transactional(readOnly = true)
    public List<MechanicVerificationDto> getPendingMechanics() {
        List<MechanicProfile> mechanics = mechanicProfileRepository.findByIsVerifiedFalse();
        return mechanics.stream()
                .map(MechanicVerificationDto::fromEntity)
                .toList();
    }

    @Transactional
    public MechanicVerificationDto verifyMechanic(Long id, VerifyDto dto) {
        MechanicProfile profile = mechanicProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic profile", id));
        
        profile.setIsVerified(true);
        profile = mechanicProfileRepository.save(profile);

        logAction(SystemLog.LogLevel.INFO, "MECHANIC_VERIFIED", 
                "Mechanic " + profile.getUser().getEmail() + " verified. Reason: " + dto.getReason());

        return MechanicVerificationDto.fromEntity(profile);
    }

    @Transactional
    public MechanicVerificationDto rejectMechanic(Long id, RejectDto dto) {
        MechanicProfile profile = mechanicProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic profile", id));
        
        profile.setIsVerified(false);
        profile = mechanicProfileRepository.save(profile);

        logAction(SystemLog.LogLevel.WARN, "MECHANIC_REJECTED", 
                "Mechanic " + profile.getUser().getEmail() + " rejected. Reason: " + dto.getReason());

        return MechanicVerificationDto.fromEntity(profile);
    }

    @Transactional(readOnly = true)
    public List<PartsProviderVerificationDto> getPendingProviders() {
        List<PartsProvider> providers = partsProviderRepository.findByIsVerifiedFalse();
        return providers.stream()
                .map(PartsProviderVerificationDto::fromEntity)
                .toList();
    }

    @Transactional
    public PartsProviderVerificationDto verifyProvider(Long id, VerifyDto dto) {
        PartsProvider provider = partsProviderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parts provider", id));
        
        provider.setIsVerified(true);
        provider = partsProviderRepository.save(provider);

        logAction(SystemLog.LogLevel.INFO, "PROVIDER_VERIFIED", 
                "Provider " + provider.getShopName() + " verified. Reason: " + dto.getReason());

        return PartsProviderVerificationDto.fromEntity(provider);
    }

    @Transactional
    public PartsProviderVerificationDto rejectProvider(Long id, RejectDto dto) {
        PartsProvider provider = partsProviderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parts provider", id));
        
        provider.setIsVerified(false);
        provider = partsProviderRepository.save(provider);

        logAction(SystemLog.LogLevel.WARN, "PROVIDER_REJECTED", 
                "Provider " + provider.getShopName() + " rejected. Reason: " + dto.getReason());

        return PartsProviderVerificationDto.fromEntity(provider);
    }

    @Transactional(readOnly = true)
    public Page<SystemLogDto> getSystemLogs(String level, String action, Long userId, Pageable pageable) {
        Page<SystemLog> logs;

        if (level != null && action != null && userId != null) {
            logs = systemLogRepository.findByLevelAndActionAndUserId(
                    SystemLog.LogLevel.valueOf(level), action, userId, pageable);
        } else if (level != null) {
            logs = systemLogRepository.findByLevel(SystemLog.LogLevel.valueOf(level), pageable);
        } else if (action != null) {
            logs = systemLogRepository.findByAction(action, pageable);
        } else if (userId != null) {
            logs = systemLogRepository.findByUserId(userId, pageable);
        } else {
            logs = systemLogRepository.findAll(pageable);
        }

        return logs.map(SystemLogDto::fromEntity);
    }

    @Transactional
    public void deleteLog(Long id) {
        systemLogRepository.deleteById(id);
    }

    @Transactional
    public int clearOldLogs(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        return systemLogRepository.deleteByTimestampBefore(cutoffDate);
    }

    @Transactional(readOnly = true)
    public Page<ServiceRequestManagementDto> getAllRequests(String status, Pageable pageable) {
        Page<ServiceRequest> requests;
        
        if (status != null) {
            requests = serviceRequestRepository.findByStatus(
                    ServiceRequest.RequestStatus.valueOf(status), pageable);
        } else {
            requests = serviceRequestRepository.findAll(pageable);
        }

        return requests.map(ServiceRequestManagementDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRequestAnalytics(String period) {
        Map<String, Object> analytics = new HashMap<>();
        
        LocalDateTime startDate = calculateStartDate(period);
        
        long totalRequests = serviceRequestRepository.countByCreatedAtAfter(startDate);
        long completedRequests = serviceRequestRepository.countByStatusAndCreatedAtAfter(
                ServiceRequest.RequestStatus.COMPLETED, startDate);
        long cancelledRequests = serviceRequestRepository.countByStatusAndCreatedAtAfter(
                ServiceRequest.RequestStatus.CANCELLED, startDate);
        
        analytics.put("totalRequests", totalRequests);
        analytics.put("completedRequests", completedRequests);
        analytics.put("cancelledRequests", cancelledRequests);
        analytics.put("period", period != null ? period : "all");
        
        return analytics;
    }

    private LocalDateTime calculateStartDate(String period) {
        if (period == null) return LocalDateTime.MIN;
        
        return switch (period.toLowerCase()) {
            case "day" -> LocalDateTime.now().minusDays(1);
            case "week" -> LocalDateTime.now().minusWeeks(1);
            case "month" -> LocalDateTime.now().minusMonths(1);
            case "year" -> LocalDateTime.now().minusYears(1);
            default -> LocalDateTime.MIN;
        };
    }

    private void logAction(SystemLog.LogLevel level, String action, String details) {
        SystemLog log = SystemLog.builder()
                .level(level)
                .action(action)
                .details(details)
                .build();
        systemLogRepository.save(log);
    }
}