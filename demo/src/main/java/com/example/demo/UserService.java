package com.example.demo;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

/**
 * UserService is a service class that handles user-related operations.
 * It interacts with the UserRepository to perform CRUD operations and manage user data.
 */
@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository; // Repository for user data
    
    @Autowired
    private PasswordEncoder passwordEncoder; // Encoder for user passwords
    
    private final Map<String, User> usersByUsername = new HashMap<>(); // Cache for users by username
    private final AtomicLong idCounter = new AtomicLong(); // Counter for generating unique IDs
    private AtomicInteger failureCounter = new AtomicInteger(0); // Counter for simulating failures
    
    /**
     * Retrieves a list of all users in the system.
     * 
     * @return List of User objects
     */
    public List<User> getAllUsers() {
        return userRepository.findAll(); // Fetch all users from the repository
    }
    
    /**
     * Retrieves a user by their ID, caching the result.
     * 
     * @param id the ID of the user
     * @return Optional<User> containing the user if found
     */
    @Cacheable(value = "user", key = "#id")
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id); // Fetch user by ID
    }
    
    /**
     * Creates a new user after encrypting their password.
     * 
     * @param user the User object to be created
     * @return the created User object
     */
    public User createUser(User user) {
        // Check if the username already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists"); // Throw an exception if username is taken
        }
        user.setRoles(Set.of("ROLE_USER")); // Assign default role
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        logger.info("Hashed password for user {}: {}", user.getUsername(), hashedPassword); // Log hashed password
        user.setPassword(hashedPassword); // Encrypt password
        return userRepository.save(user); // Save user to the repository
    }
    
    /**
     * Updates an existing user's details.
     * 
     * @param id the ID of the user to update
     * @param userDetails the new details for the user
     * @return the updated User object
     */
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found")); // Fetch user or throw exception
        user.setUsername(userDetails.getUsername()); // Update username
        user.setEmail(userDetails.getEmail()); // Update email
        // Encrypt password only if it's being updated
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword())); // Encrypt new password
        }
        return userRepository.save(user); // Save updated user
    }
    
    /**
     * Deletes a user by their ID.
     * 
     * @param id the ID of the user to delete
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id); // Delete user from the repository
    }
    
    /**
     * Retrieves a user by their username.
     * 
     * @param username the username of the user
     * @return Optional<User> containing the user if found
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username); // Fetch user by username
    }

    /**
     * Searches for users whose usernames contain the specified string.
     * 
     * @param username the string to search for in usernames
     * @return List of User objects matching the search criteria
     */
    public List<User> searchUsersByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username); // Search users by username
    }
    
    /**
     * Retrieves users with pagination support.
     * 
     * @param pageable pagination information
     * @return Page<User> containing paginated user data
     */
    public Page<User> getUsersPaginated(Pageable pageable) {
        return userRepository.findAll(pageable); // Fetch paginated users
    }
    
    /**
     * Updates a user's password after verifying the old password.
     * 
     * @param id the ID of the user
     * @param oldPassword the user's old password
     * @param newPassword the new password to set
     * @return the updated User object
     */
    public User updatePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // Hash the new password
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user); // Save updated user
    }

    /**
     * Asynchronously retrieves a user by their ID.
     * 
     * @param id the ID of the user
     * @return CompletableFuture containing the user if found
     */
    @Async("asyncExecutor")
    public CompletableFuture<Optional<User>> getUserByIdAsync(Long id) {
        Optional<User> user = userRepository.findById(id); // Fetch user by ID
        return CompletableFuture.completedFuture(user); // Return user wrapped in CompletableFuture
    }

    /**
     * Retrieves a user by their ID with circuit breaker support.
     * 
     * @param id the ID of the user
     * @return the User object if found
     */
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetUserById")
    public User getUserByIdWithCircuitBreaker(Long id) {
        // Simulate failure every other request
        if (failureCounter.getAndIncrement() % 2 == 0) {
            throw new RuntimeException("Simulated service failure"); // Simulate a failure
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found")); // Fetch user or throw exception
    }
    
    /**
     * Fallback method in case of failure when retrieving a user by ID.
     * 
     * @param id the ID of the user
     * @param t the throwable that caused the fallback
     * @return a fallback User object
     */
    public User fallbackGetUserById(Long id, Throwable t) {
        return new User("fallback-user", "fallback@example.com", "fallback-password"); // Return a fallback user
    }

    /**
     * Verifies user credentials by checking the username and password.
     * 
     * @param username the username of the user
     * @param password the password of the user
     * @return true if credentials are valid, false otherwise
     */
    public boolean verifyUserCredentials(String username, String password) {
        return userRepository.findByUsername(username)
            .map(user -> passwordEncoder.matches(password, user.getPassword())) // Check if password matches
            .orElse(false); // Return false if user not found
    }
}
