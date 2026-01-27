// ============= SystemLogRepository.java =============
package com.roadtech.repository;

import com.roadtech.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {

    Page<SystemLog> findByLevel(SystemLog.LogLevel level, Pageable pageable);

    Page<SystemLog> findByAction(String action, Pageable pageable);

    Page<SystemLog> findByUserId(Long userId, Pageable pageable);

    Page<SystemLog> findByLevelAndActionAndUserId(
            SystemLog.LogLevel level, String action, Long userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM SystemLog sl WHERE sl.timestamp < :cutoffDate")
    int deleteByTimestampBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}