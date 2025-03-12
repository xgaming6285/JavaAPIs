package com.example.demo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Objects;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String USER_NOT_FOUND = "User not found";
    private static final String USER_CACHE = "user";
    private static final String USERS_CACHE = "users";
    private static final String ROLE_USER = "ROLE_USER";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AtomicInteger failureCounter;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository must not be null");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "PasswordEncoder must not be null");
        this.failureCounter = new AtomicInteger(0);
    }

    /**
     * Gets all users with pagination.
     *
     * @param pageable pagination information
     * @return page of all users
     */
    @Cacheable(value = USERS_CACHE, key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<User> getAllUsers(Pageable pageable) {
        logger.debug("Fetching all users - page: {}, size: {}", 
            pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable);
    }

    /**
     * Gets all users.
     *
     * @return list of all users
     * @deprecated Use {@link #getAllUsers(Pageable)} instead
     */
    @Deprecated(since = "2.0.0")
    @Cacheable(value = USERS_CACHE, key = "'all'")
    public List<User> getAllUsers() {
        logger.debug("Fetching all users");
        return userRepository.findAll();
    }

    /**
     * Gets a user by ID.
     *
     * @param id the user ID
     * @return optional containing the user if found
     */
    @Cacheable(value = USER_CACHE, key = "#id")
    public Optional<User> getUserById(Long id) {
        logger.debug("Fetching user by ID: {}", id);
        return userRepository.findById(id);
    }

    /**
     * Creates a new user.
     *
     * @param user the user to create
     * @return the created user
     */
    @Caching(
        put = { @CachePut(value = USER_CACHE, key = "#result.id") },
        evict = { @CacheEvict(value = USERS_CACHE, allEntries = true) }
    )
    public User createUser(User user) {
        if (logger.isInfoEnabled()) {
            logger.info("Creating user with username: {}", user.getUsername());
        }    
        if (user.getRoles().isEmpty()) {
            user.setRoles(Set.of(ROLE_USER));
        }
        if (!isPasswordHashed(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        try {
            User savedUser = userRepository.save(user);
            logger.debug("User created successfully: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user", e);
        }
    }

    /**
     * Updates an existing user.
     *
     * @param id the user ID
     * @param userDetails the updated user details
     * @return the updated user
     */
    @Caching(
        put = { @CachePut(value = USER_CACHE, key = "#id") },
        evict = { @CacheEvict(value = USERS_CACHE, allEntries = true) }
    )
    public User updateUser(Long id, User userDetails) {
        logger.debug("Updating user with ID: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("User not found for update: {}", id);
                return new UserNotFoundException(USER_NOT_FOUND);
            });
            
        updateUserFields(user, userDetails);
        User updatedUser = userRepository.save(user);
        logger.debug("User updated successfully: {}", id);
        return updatedUser;
    }

    /**
     * Deletes a user.
     *
     * @param id the user ID
     */
    @Caching(
        evict = {
            @CacheEvict(value = USER_CACHE, key = "#id"),
            @CacheEvict(value = USERS_CACHE, allEntries = true)
        }
    )
    public void deleteUser(Long id) {
        logger.debug("Deleting user with ID: {}", id);
        userRepository.deleteById(id);
        logger.debug("User deleted successfully: {}", id);
    }

    /**
     * Gets a user by username.
     *
     * @param username the username
     * @return optional containing the user if found
     */
    @Cacheable(value = USER_CACHE, key = "'username_' + #username", condition = "#username != null")
    public Optional<User> getUserByUsername(String username) {
        logger.debug("Fetching user by username: {}", username);
        if (username == null) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username);
    }

    /**
     * Searches for users by username.
     *
     * @param username the username to search for
     * @param pageable pagination information
     * @return page of matching users
     */
    public Page<User> searchUsersByUsername(String username, Pageable pageable) {
        logger.debug("Searching users by username: {} - page: {}, size: {}", 
            username, pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findByUsernameContainingIgnoreCase(username, pageable);
    }

    /**
     * Gets paginated users.
     *
     * @param pageable pagination information
     * @return page of users
     */
    public Page<User> getUsersPaginated(Pageable pageable) {
        logger.debug("Fetching paginated users: page {}, size {}", 
            pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable);
    }

    /**
     * Updates a user's password.
     *
     * @param id the user ID
     * @param oldPassword the old password
     * @param newPassword the new password
     * @return the updated user
     */
    @CachePut(value = USER_CACHE, key = "#id")
    public User updatePassword(Long id, String oldPassword, String newPassword) {
        logger.debug("Updating password for user with ID: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("User not found for password update: {}", id);
                return new UserNotFoundException(USER_NOT_FOUND);
            });
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            logger.warn("Invalid old password for user: {}", id);
            throw new IllegalArgumentException("Invalid old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        User updatedUser = userRepository.save(user);
        logger.debug("Password updated successfully for user: {}", id);
        return updatedUser;
    }

    /**
     * Gets a user by ID asynchronously.
     *
     * @param id the user ID
     * @return future containing optional with the user if found
     */
    @Async("asyncExecutor")
    public CompletableFuture<Optional<User>> getUserByIdAsync(Long id) {
        logger.debug("Fetching user asynchronously by ID: {}", id);
        return CompletableFuture.completedFuture(getUserById(id));
    }

    /**
     * Gets a user by ID with circuit breaker protection.
     *
     * @param id the user ID
     * @return the user
     */
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetUserById")
    public User getUserByIdWithCircuitBreaker(Long id) {
        logger.debug("Fetching user with circuit breaker, ID: {}", id);
        if (failureCounter.getAndIncrement() % 2 == 0) {
            logger.warn("Simulated service failure for circuit breaker test");
            throw new RuntimeException("Simulated service failure");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
    }

    /**
     * Verifies user credentials.
     *
     * @param username the username
     * @param password the password
     * @return true if credentials are valid
     */
    public boolean verifyUserCredentials(String username, String password) {
        logger.debug("Verifying credentials for username: {}", username);
        return getUserByUsername(username)
            .map(user -> passwordEncoder.matches(password, user.getPassword()))
            .orElse(false);
    }

    /**
     * Gets a user by token.
     *
     * @param token the token
     * @return optional containing the user if found
     */
    public Optional<User> getUserByToken(String token) {
        logger.debug("Fetching user by token");
        return userRepository.findByToken(token);
    }

    /**
     * Saves a reset token for a user.
     *
     * @param userId the user ID
     * @param token the token
     */
    @CachePut(value = USER_CACHE, key = "#userId")
    public void saveResetToken(Long userId, String token) {
        logger.debug("Saving reset token for user: {}", userId);
        userRepository.findById(userId).ifPresent(user -> {
            user.setToken(token);
            userRepository.save(user);
        });
    }

    /**
     * Saves a verification token for a user.
     *
     * @param userId the user ID
     * @param token the token
     */
    @CachePut(value = USER_CACHE, key = "#userId")
    public void saveVerificationToken(Long userId, String token) {
        logger.debug("Saving verification token for user: {}", userId);
        userRepository.findById(userId).ifPresent(user -> {
            user.setToken(token);
            userRepository.save(user);
        });
    }

    /**
     * Gets a user by email.
     *
     * @param email the email
     * @return optional containing the user if found
     */
    @Cacheable(value = USER_CACHE, key = "'email_' + #email", condition = "#email != null")
    public Optional<User> getUserByEmail(String email) {
        logger.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    /**
     * Checks if a password is hashed.
     *
     * @param password the password
     * @return true if the password is hashed
     */
    private boolean isPasswordHashed(String password) {
        return password != null && password.length() >= 60 && password.startsWith("$2a$");
    }

    /**
     * Updates user fields from user details.
     *
     * @param user the user to update
     * @param userDetails the user details
     */
    private void updateUserFields(User user, User userDetails) {
        if (userDetails.getUsername() != null) {
            user.setUsername(userDetails.getUsername());
        }
        if (userDetails.getEmail() != null) {
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getPassword() != null && !isPasswordHashed(userDetails.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        if (!userDetails.getRoles().isEmpty()) {
            user.setRoles(userDetails.getRoles());
        }
        user.setActive(userDetails.isActive());
    }

    /**
     * Gets all active users.
     *
     * @param pageable pagination information
     * @return page of active users
     */
    @Cacheable(value = USERS_CACHE, key = "'active_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<User> getActiveUsers(Pageable pageable) {
        logger.debug("Fetching active users - page: {}, size: {}", 
            pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findByActiveTrue(pageable);
    }

    /**
     * Gets all inactive users.
     *
     * @param pageable pagination information
     * @return page of inactive users
     */
    @Cacheable(value = USERS_CACHE, key = "'inactive_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<User> getInactiveUsers(Pageable pageable) {
        logger.debug("Fetching inactive users - page: {}, size: {}", 
            pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findByActiveFalse(pageable);
    }

    /**
     * Gets users by email domain.
     *
     * @param domain the email domain
     * @param pageable pagination information
     * @return page of users with the given email domain
     */
    public Page<User> getUsersByEmailDomain(String domain, Pageable pageable) {
        logger.debug("Fetching users by email domain: {} - page: {}, size: {}", 
            domain, pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findByEmailDomain(domain, pageable);
    }

    /**
     * Gets users by role.
     *
     * @param role the role
     * @param pageable pagination information
     * @return page of users with the given role
     */
    @Cacheable(value = USERS_CACHE, key = "'role_' + #role + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<User> getUsersByRole(String role, Pageable pageable) {
        logger.debug("Fetching users by role: {} - page: {}, size: {}", 
            role, pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findByRolesContaining(role, pageable);
    }

    /**
     * Gets users with a minimum number of roles.
     *
     * @param minRoles the minimum number of roles
     * @param pageable pagination information
     * @return page of users with at least the given number of roles
     */
    public Page<User> getUsersByMinimumRoles(int minRoles, Pageable pageable) {
        logger.debug("Fetching users with minimum {} roles - page: {}, size: {}", 
            minRoles, pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findByMinimumRoles(minRoles, pageable);
    }

    /**
     * Searches for users by multiple criteria.
     *
     * @param username the username to search for (optional)
     * @param email the email to search for (optional)
     * @param active the active status to search for (optional)
     * @param role the role to search for (optional)
     * @param pageable pagination information
     * @return page of matching users
     */
    public Page<User> searchUsers(String username, String email, Boolean active, String role, Pageable pageable) {
        logger.debug("Advanced search - username: {}, email: {}, active: {}, role: {} - page: {}, size: {}", 
            username, email, active, role, pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findByMultipleCriteria(username, email, active, role, pageable);
    }

    /**
     * Updates roles for a user.
     *
     * @param id the user ID
     * @param roles the roles to update
     * @return the updated user
     */
    @Caching(
        put = { @CachePut(value = USER_CACHE, key = "#id") },
        evict = { 
            @CacheEvict(value = USERS_CACHE, key = "'all'"),
            @CacheEvict(value = USERS_CACHE, key = "'active'", condition = "#result.active"),
            @CacheEvict(value = USERS_CACHE, key = "'inactive'", condition = "!#result.active")
        }
    )
    public User updateUserRoles(Long id, Set<String> roles) {
        logger.debug("Updating roles for user with ID: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("User not found for role update: {}", id);
                return new UserNotFoundException("User not found");
            });
        
        user.getRoles().clear();  
        user.setRoles(new HashSet<>(roles));  
        
        try {
            User updatedUser = userRepository.save(user);
            logger.debug("Roles updated successfully for user: {}", id);
            return updatedUser;
        } catch (Exception e) {
            logger.error("Error updating user roles: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update user roles", e);
        }
    }

    /**
     * Gets the total number of users.
     *
     * @return the total number of users
     */
    @Cacheable(value = USERS_CACHE, key = "'count'")
    public long getTotalUsers() {
        logger.debug("Counting total users");
        return userRepository.count();
    }

    /**
     * Gets the count of users by role.
     *
     * @return map of role to count
     */
    @Cacheable(value = USERS_CACHE, key = "'countByRole'")
    public Map<String, Long> getUserCountByRole() {
        logger.debug("Counting users by role");
        return getAllUsers().stream()
            .flatMap(user -> user.getRoles().stream())
            .collect(Collectors.groupingBy(
                role -> role,
                Collectors.counting()
            ));
    }

    /**
     * Gets the average number of roles per user.
     *
     * @return the average number of roles per user
     */
    @Cacheable(value = USERS_CACHE, key = "'avgRoles'")
    public double getAverageRolesPerUser() {
        logger.debug("Calculating average roles per user");
        List<User> users = getAllUsers();
        if (users.isEmpty()) {
            return 0.0;
        }
        return users.stream()
            .mapToInt(user -> user.getRoles().size())
            .average()
            .orElse(0.0);
    }

    /**
     * Gets the role distribution.
     *
     * @return map of role to percentage
     */
    @Cacheable(value = USERS_CACHE, key = "'roleDistribution'")
    public Map<String, Long> getRoleDistribution() {
        logger.debug("Calculating role distribution");
        List<User> users = getAllUsers();
        long totalUsers = users.size();
        if (totalUsers == 0) {
            return Map.of();
        }
        
        return users.stream()
            .flatMap(user -> user.getRoles().stream())
            .collect(Collectors.groupingBy(
                role -> role,
                Collectors.collectingAndThen(
                    Collectors.counting(),
                    count -> (count * 100) / totalUsers
                )
            ));
    }

    /**
     * Gets the common role combinations.
     *
     * @return list of role combinations
     */
    @Cacheable(value = USERS_CACHE, key = "'roleCombinations'")
    public List<Map<String, Object>> getCommonRoleCombinations() {
        logger.debug("Finding common role combinations");
        return getAllUsers().stream()
            .map(User::getRoles)
            .collect(Collectors.groupingBy(
                roles -> new HashSet<>(roles),
                Collectors.counting()
            ))
            .entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(5)
            .map(entry -> {
                Map<String, Object> combination = new HashMap<>();
                combination.put("roles", entry.getKey());
                combination.put("count", entry.getValue());
                return combination;
            })
            .collect(Collectors.toList());
    }

    /**
     * Fallback method for circuit breaker.
     *
     * @param id the user ID
     * @param ex the exception
     * @return fallback user
     */
    public User fallbackGetUserById(Long id, Exception ex) {
        logger.warn("Circuit breaker fallback for user {}: {}", id, ex.getMessage());
        return new User("fallback-user", "fallback@example.com", "fallback-password");
    }
}
