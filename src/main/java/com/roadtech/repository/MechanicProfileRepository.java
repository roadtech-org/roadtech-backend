package com.roadtech.repository;

import com.roadtech.entity.MechanicProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MechanicProfileRepository extends JpaRepository<MechanicProfile, Long> {

    Optional<MechanicProfile> findByUserId(Long userId);

    List<MechanicProfile> findByIsAvailableTrue();

    // For admin verification
    List<MechanicProfile> findByIsVerifiedFalse();

    List<MechanicProfile> findByIsVerifiedTrue();

    @Query(value = """
        SELECT mp.* FROM mechanic_profiles mp
        JOIN users u ON mp.user_id = u.id
        WHERE mp.is_available = true
        AND mp.is_verified = true
        AND u.is_active = true
        AND mp.current_latitude IS NOT NULL
        AND mp.current_longitude IS NOT NULL
        ORDER BY (
            POW(mp.current_latitude - :lat, 2) +
            POW(mp.current_longitude - :lng, 2)
        ) ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<MechanicProfile> findNearestAvailableMechanics(
            @Param("lat") BigDecimal latitude,
            @Param("lng") BigDecimal longitude,
            @Param("limit") int limit
    );

    @Query("""
        SELECT mp FROM MechanicProfile mp
        JOIN FETCH mp.user u
        WHERE mp.isAvailable = true
        AND mp.isVerified = true
        AND u.isActive = true
        """)
    List<MechanicProfile> findAllAvailableWithUser();
    
    @Query("""
    	    SELECT COUNT(mp)
    	    FROM MechanicProfile mp
    	    JOIN mp.user u
    	    WHERE mp.isAvailable = true
    	      AND mp.isVerified = true
    	      AND u.isActive = true
    	""")
    	long countAvailableMechanics();

}