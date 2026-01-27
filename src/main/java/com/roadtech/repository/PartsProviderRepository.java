// ============= PartsProviderRepository.java =============
package com.roadtech.repository;

import com.roadtech.entity.PartsProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PartsProviderRepository extends JpaRepository<PartsProvider, Long> {

    Optional<PartsProvider> findByUserId(Long userId);

    List<PartsProvider> findByIsVerifiedFalse();

    List<PartsProvider> findByIsVerifiedTrue();

    @Query(value = """
        SELECT pp.* FROM parts_providers pp
        WHERE pp.is_verified = true
        AND pp.is_open = true
        AND (
            6371 * acos(
                cos(radians(:lat)) * cos(radians(pp.latitude)) *
                cos(radians(pp.longitude) - radians(:lng)) +
                sin(radians(:lat)) * sin(radians(pp.latitude))
            )
        ) <= :radiusKm
        ORDER BY (
            POW(pp.latitude - :lat, 2) +
            POW(pp.longitude - :lng, 2)
        ) ASC
        """, nativeQuery = true)
    List<PartsProvider> findNearbyProviders(
            @Param("lat") BigDecimal latitude,
            @Param("lng") BigDecimal longitude,
            @Param("radiusKm") Double radiusKm
    );
}