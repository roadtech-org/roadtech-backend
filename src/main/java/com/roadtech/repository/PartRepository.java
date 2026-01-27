// ============= PartRepository.java =============
package com.roadtech.repository;

import com.roadtech.entity.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartRepository extends JpaRepository<Part, Long> {

    List<Part> findByProviderId(Long providerId);

    List<Part> findByProviderIdAndIsAvailableTrue(Long providerId);

    List<Part> findByCategory(Part.PartCategory category);

    @Query(value = """
        SELECT p.* FROM parts p
        INNER JOIN parts_providers pp ON p.provider_id = pp.id
        WHERE p.is_available = true
        AND pp.is_verified = true
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
    List<Part> findNearbyParts(
            @Param("lat") Double latitude,
            @Param("lng") Double longitude,
            @Param("radiusKm") Double radiusKm
    );

    @Query(value = """
        SELECT p.* FROM parts p
        INNER JOIN parts_providers pp ON p.provider_id = pp.id
        WHERE p.is_available = true
        AND pp.is_verified = true
        AND pp.is_open = true
        AND p.category = :category
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
    List<Part> findNearbyByCategory(
            @Param("category") Part.PartCategory category,
            @Param("lat") Double latitude,
            @Param("lng") Double longitude,
            @Param("radiusKm") Double radiusKm
    );

    @Query(value = """
        SELECT p.* FROM parts p
        INNER JOIN parts_providers pp ON p.provider_id = pp.id
        WHERE p.is_available = true
        AND pp.is_verified = true
        AND pp.is_open = true
        AND LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
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
    List<Part> searchNearbyByName(
            @Param("search") String search,
            @Param("lat") Double latitude,
            @Param("lng") Double longitude,
            @Param("radiusKm") Double radiusKm
    );

    @Query(value = """
        SELECT p.* FROM parts p
        INNER JOIN parts_providers pp ON p.provider_id = pp.id
        WHERE p.is_available = true
        AND pp.is_verified = true
        AND pp.is_open = true
        AND p.category = :category
        AND LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
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
    List<Part> searchNearbyByCategoryAndName(
            @Param("category") Part.PartCategory category,
            @Param("search") String search,
            @Param("lat") Double latitude,
            @Param("lng") Double longitude,
            @Param("radiusKm") Double radiusKm
    );
}
