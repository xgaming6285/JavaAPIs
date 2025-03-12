package com.example.demo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository interface for User entity operations. */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByUsernameContainingIgnoreCase(String username);
    Optional<User> findByToken(String token);
    Optional<User> findByEmail(String email);
    
    List<User> findByActiveTrue();
    List<User> findByActiveFalse();
    
    @Query("SELECT u FROM User u WHERE u.email LIKE %:domain%")
    List<User> findByEmailDomain(@Param("domain") String domain);
    
    List<User> findByRolesContaining(String role);
    
    @Query("SELECT u FROM User u WHERE SIZE(u.roles) > :minRoles")
    List<User> findByMinimumRoles(@Param("minRoles") int minRoles);
    
    @Query(
        "SELECT u FROM User u WHERE "
            + "(:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) AND "
            + "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND "
            + "(:active IS NULL OR u.active = :active) AND "
            + "(:role IS NULL OR :role MEMBER OF u.roles)")
    List<User> findByMultipleCriteria(
        @Param("username") String username,
        @Param("email") String email,
        @Param("active") Boolean active,
        @Param("role") String role);
}
