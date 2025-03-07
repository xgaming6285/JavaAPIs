package com.example.demo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    
    private AtomicInteger failureCounter = new AtomicInteger(0); // Counter for simulating failures
    
    private final Map<Long, User> userCache = new ConcurrentHashMap<>(); // Cache for users
    private final Map<Long, String> verificationTokenCache = new ConcurrentHashMap<>(); // Cache for verification tokens
    
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
        logger.info("User object before saving: {}", user); // Log the user object
        logger.info("Creating user with username: {}", user.getUsername()); // Log user creation attempt
        user.setRoles(Set.of("ROLE_USER")); // Assign default role
        
        // Check if the password is already hashed
        if (!user.getPassword().startsWith("$2a$")) { // Assuming BCrypt hashes start with "$2a$"
            String hashedPassword = passwordEncoder.encode(user.getPassword());
            logger.info("Hashed password for user {}: {}", user.getUsername(), hashedPassword); // Log hashed password
            user.setPassword(hashedPassword); // Encrypt password
        } else {
            logger.info("Password for user {} is already hashed.", user.getUsername());
        }
        
        try {
            User savedUser = userRepository.save(user); // Save user in the repository
            logger.info("User saved successfully: {}", savedUser); // Log the saved user
            return savedUser; // Return the saved user
        } catch (Exception e) {
            logger.error("Error saving user: {}", e.getMessage(), e); // Log the exception
            throw new RuntimeException("Error saving user"); // Rethrow the exception
        }
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
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setUsername(userDetails.getUsername()); // Update username
        user.setEmail(userDetails.getEmail()); // Update email
        // Encrypt password only if it's being updated and not already hashed
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty() && !userDetails.getPassword().startsWith("$2a$")) {
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
        // Check the cache for the user
        return userCache.values().stream()
            .filter(user -> user.getUsername().equals(username))
            .findFirst(); // Fetch user by username from cache
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
        logger.debug("Updating password for user ID: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify old password
        logger.debug("Stored password hash for user {}: {}", user.getUsername(), user.getPassword());
        logger.debug("Old password being checked: {}", oldPassword);
        
        // Check if the old password matches the stored password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            logger.error("Old password is incorrect for user: {}", user.getUsername());
            throw new RuntimeException("Old password is incorrect");
        }
        
        // Hash the new password
        user.setPassword(passwordEncoder.encode(newPassword));
        logger.info("New password hashed for user: {}", user.getUsername());
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

    /**
     * Retrieves a User object associated with the given token.
     * 
     * This method first attempts to find the user by querying the database using the provided token.
     * If not found, it then checks a cache of verification tokens, matching the token to a user ID
     * and retrieving the corresponding user from the database.
     * 
     * @param token The authentication token used to identify the user
     * @return An Optional containing the User if found, or an empty Optional if no user is associated with the token
     */
    public Optional<User> getUserByToken(String token) {
        return userRepository.findByToken(token) // Fetch user by token from the database
            .or(() -> {
                // Check the cache for the token
                return verificationTokenCache.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(token))
                    .map(entry -> userRepository.findById(entry.getKey()).orElse(null))
                    .findFirst();
            });
    }

    /**
     * Saves a reset token for a given user.
     * 
     * This method retrieves a user by their ID, sets the provided reset token
     * for the user, and then saves the updated user information.
     * 
     * @param userId The ID of the user for whom to save the reset token
     * @param token The reset token to be saved for the user
     * @throws RuntimeException if the user with the given ID is not found
     */
    public void saveResetToken(Long userId, String token) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setToken(token); // Set the reset token
        userRepository.save(user); // Save the user with the token
    }

    /**
     * Saves a verification token for a specific user in the cache.
     * 
     * @param userId The unique identifier of the user
     * @param token The verification token to be saved
     */
    public void saveVerificationToken(Long userId, String token) {
        verificationTokenCache.put(userId, token); // Store token in cache
    }

    /**
     * Finds a user by their email.
     * 
     * @param email the email of the user to find
     * @return Optional<User> containing the user if found
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email); // Fetch user by email from the repository
    }
}
