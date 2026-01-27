// Update ServiceRequestRepository.java with these additional methods:
package com.roadtech.repository;

import com.roadtech.entity.ServiceRequest;
import com.roadtech.entity.ServiceRequest.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    List<ServiceRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ServiceRequest> findByMechanicIdOrderByCreatedAtDesc(Long mechanicId);

    @Query("""
        SELECT sr FROM ServiceRequest sr
        WHERE sr.user.id = :userId
        AND sr.status IN :statuses
        ORDER BY sr.createdAt DESC
        """)
    List<ServiceRequest> findByUserIdAndStatusIn(
            @Param("userId") Long userId,
            @Param("statuses") List<RequestStatus> statuses
    );

    @Query("""
        SELECT sr FROM ServiceRequest sr
        WHERE sr.mechanic.id = :mechanicId
        AND sr.status IN :statuses
        ORDER BY sr.createdAt DESC
        """)
    List<ServiceRequest> findByMechanicIdAndStatusIn(
            @Param("mechanicId") Long mechanicId,
            @Param("statuses") List<RequestStatus> statuses
    );

    @Query("""
        SELECT sr FROM ServiceRequest sr
        LEFT JOIN FETCH sr.mechanic
        WHERE sr.user.id = :userId
        AND sr.status NOT IN ('COMPLETED', 'CANCELLED')
        ORDER BY sr.createdAt DESC
        """)
    Optional<ServiceRequest> findActiveRequestByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT sr FROM ServiceRequest sr
        LEFT JOIN FETCH sr.user
        WHERE sr.mechanic.id = :mechanicId
        AND sr.status IN ('ACCEPTED', 'IN_PROGRESS')
        ORDER BY sr.createdAt DESC
        """)
    List<ServiceRequest> findActiveRequestsByMechanicId(@Param("mechanicId") Long mechanicId);

    @Query("""
        SELECT sr FROM ServiceRequest sr
        LEFT JOIN FETCH sr.user
        WHERE sr.status = 'PENDING'
        AND sr.mechanic IS NULL
        ORDER BY sr.createdAt ASC
        """)
    List<ServiceRequest> findPendingRequestsWithoutMechanic();

    @Query("""
        SELECT sr FROM ServiceRequest sr
        LEFT JOIN FETCH sr.user
        LEFT JOIN FETCH sr.mechanic m
        LEFT JOIN FETCH m.mechanicProfile
        WHERE sr.id = :id
        """)
    Optional<ServiceRequest> findByIdWithDetails(@Param("id") Long id);

    long countByMechanicIdAndStatus(Long mechanicId, RequestStatus status);

    // Additional methods for admin analytics
    Page<ServiceRequest> findByStatus(RequestStatus status, Pageable pageable);

    long countByStatus(RequestStatus status);

    long countByStatusIn(List<RequestStatus> statuses);

    long countByCreatedAtAfter(LocalDateTime date);

    long countByStatusAndCreatedAtAfter(RequestStatus status, LocalDateTime date);
}