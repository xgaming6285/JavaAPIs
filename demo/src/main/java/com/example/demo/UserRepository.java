package com.example.demo;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    Optional<User> findByToken(String token);
    Optional<User> findByEmail(String email);
    
    Page<User> findByActiveTrue(Pageable pageable);
    Page<User> findByActiveFalse(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.email LIKE %:domain%")
    Page<User> findByEmailDomain(@Param("domain") String domain, Pageable pageable);
    
    Page<User> findByRolesContaining(String role, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE SIZE(u.roles) > :minRoles")
    Page<User> findByMinimumRoles(@Param("minRoles") int minRoles, Pageable pageable);
    
    @Query(
        "SELECT u FROM User u WHERE "
            + "(:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) AND "
            + "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND "
            + "(:active IS NULL OR u.active = :active) AND "
            + "(:role IS NULL OR :role MEMBER OF u.roles)")
    Page<User> findByMultipleCriteria(
        @Param("username") String username,
        @Param("email") String email,
        @Param("active") Boolean active,
        @Param("role") String role,
        Pageable pageable);
}
