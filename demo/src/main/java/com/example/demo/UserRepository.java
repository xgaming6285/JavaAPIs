package com.example.demo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * UserRepository is an interface that extends JpaRepository to provide CRUD operations
 * for the User entity. It includes custom query methods for finding users by username.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Finds a user by their username.
     *
     * @param username the username of the user to find
     * @return an Optional containing the found user, or empty if no user was found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Finds users whose usernames contain the specified string, ignoring case.
     *
     * @param username the string to search for in usernames
     * @return a list of users whose usernames contain the specified string
     */
    List<User> findByUsernameContainingIgnoreCase(String username);

    /**
     * Finds a user by their authentication token.
     * 
     * @param token The authentication token associated with the user
     * @return An Optional containing the User if found, or an empty Optional if no user matches the given token
     */
    Optional<User> findByToken(String token);
}
