// Update UserRepository.java with these additional methods:
package com.roadtech.repository;

import com.roadtech.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailAndIsActiveTrue(String email);

    // Additional methods for admin panel
    Page<User> findByRole(User.UserRole role, Pageable pageable);

    @Query("""
        SELECT u FROM User u
        WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%'))
        """)
    Page<User> findBySearchTerm(@Param("search") String search, Pageable pageable);

    @Query("""
        SELECT u FROM User u
        WHERE u.role = :role
        AND (
            LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        """)
    Page<User> findByRoleAndSearchTerm(
            @Param("role") User.UserRole role,
            @Param("search") String search,
            Pageable pageable
    );
}
