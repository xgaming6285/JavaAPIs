package com.example.demo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management")
@Validated
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private static final String USER_NOT_FOUND_MSG = "User not found with id: %d";
    
    private final Counter userCreationCounter;
    private final UserService userService;

    /**
     * Creates a new UserController.
     *
     * @param userService the user service to use
     * @param registry the meter registry for metrics
     * @throws NullPointerException if either parameter is null
     */
    public UserController(UserService userService, MeterRegistry registry) {
        this.userService = Objects.requireNonNull(userService, "UserService must not be null");
        Objects.requireNonNull(registry, "MeterRegistry must not be null");
        this.userCreationCounter = Counter.builder("api.user.creation")
            .description("Number of users created")
            .register(registry);
    }

    /**
     * Converts a User entity to a UserDTO.
     *
     * @param user the user entity to convert
     * @return the user DTO
     */
    private UserDTO convertToDTO(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail());
    }

    /**
     * Gets all users with pagination.
     *
     * @param pageable pagination information
     * @return page of all users
     */
    @Timed(value = "api.getAllUsers.time")
    @Operation(summary = "Get all users with pagination")
    @GetMapping("")
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
        try {
            logger.debug("Fetching all users - page: {}, size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
            Page<UserDTO> users = userService.getAllUsers(pageable)
                    .map(this::convertToDTO);
            return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(15, TimeUnit.MINUTES))
                .body(users);
        } catch (Exception e) {
            logger.error("Error fetching all users: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets a user by ID.
     *
     * @param id the user ID
     * @return the user with the given ID
     * @throws UserNotFoundException if no user is found with the given ID
     */
    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable @Min(1) Long id) {
        logger.debug("Fetching user with ID: {}", id);
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                    .eTag(String.valueOf(user.getVersion()))
                    .body(convertToDTO(user)))
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", id);
                    return new UserNotFoundException(String.format(USER_NOT_FOUND_MSG, id));
                });
    }

    /**
     * Creates a new user.
     *
     * @param createUserRequest the user creation request
     * @return the created user
     */
    @PostMapping("")
    @RateLimiter(name = "createUser")
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        if (logger.isInfoEnabled()) {
            logger.info("Creating new user with username: {}", createUserRequest.getUsername());
        }
        try {
            User user = new User(createUserRequest.getUsername(), 
                               createUserRequest.getEmail(), 
                               createUserRequest.getPassword());
            User created = userService.createUser(user);
            userCreationCounter.increment();
            logger.info("User created successfully with ID: {}", created.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                               .body(convertToDTO(created));
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Updates an existing user.
     *
     * @param id the user ID
     * @param userDetails the updated user details
     * @return the updated user
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    public ResponseEntity<UserDTO> updateUser(@PathVariable @Min(1) Long id,
                                            @Valid @RequestBody User userDetails) {
        try {
            logger.debug("Updating user with ID: {}", id);
            User updatedUser = userService.updateUser(id, userDetails);
            logger.info("User updated successfully with ID: {}", id);
            return ResponseEntity.ok(convertToDTO(updatedUser));
        } catch (UserNotFoundException e) {
            logger.warn("Failed to update user: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating user with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Deletes a user.
     *
     * @param id the user ID
     * @return no content response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<Void> deleteUser(@PathVariable @Min(1) Long id) {
        try {
            logger.debug("Deleting user with ID: {}", id);
            // Check if user exists before deletion
            if (userService.getUserById(id).isEmpty()) {
                logger.warn("Cannot delete user: User not found with ID: {}", id);
                throw new UserNotFoundException(String.format(USER_NOT_FOUND_MSG, id));
            }
            userService.deleteUser(id);
            logger.info("User deleted successfully with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            logger.warn("Failed to delete user: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting user with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Searches for users by username with pagination.
     *
     * @param username the username to search for
     * @param pageable pagination information
     * @return page of matching users
     */
    @GetMapping("/search")
    @Operation(summary = "Search users by username with pagination")
    public ResponseEntity<Page<UserDTO>> searchUsers(
            @RequestParam @NotBlank String username,
            Pageable pageable) {
        try {
            logger.debug("Searching users by username: {} - page: {}, size: {}", 
                username, pageable.getPageNumber(), pageable.getPageSize());
            Page<UserDTO> users = userService.searchUsersByUsername(username, pageable)
                    .map(this::convertToDTO);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error searching users by username: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Advanced search for users with pagination.
     *
     * @param username optional username to search for
     * @param email optional email to search for
     * @param active optional active status to search for
     * @param role optional role to search for
     * @param pageable pagination information
     * @return page of matching users
     */
    @GetMapping("/advanced-search")
    @Operation(summary = "Advanced search for users with pagination")
    public ResponseEntity<Page<UserDTO>> advancedSearch(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String role,
            Pageable pageable) {
        try {
            logger.debug("Advanced search - username: {}, email: {}, active: {}, role: {} - page: {}, size: {}", 
                username, email, active, role, pageable.getPageNumber(), pageable.getPageSize());
            Page<UserDTO> users = userService.searchUsers(username, email, active, role, pageable)
                    .map(this::convertToDTO);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error performing advanced search: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets paginated users.
     *
     * @param pageable pagination information
     * @return page of users
     */
    @GetMapping("/paginated")
    @Operation(summary = "Get paginated users")
    public ResponseEntity<Page<UserDTO>> getUsersPaginated(Pageable pageable) {
        try {
            logger.debug("Fetching paginated users: page {}, size {}", 
                pageable.getPageNumber(), pageable.getPageSize());
            Page<UserDTO> users = userService.getUsersPaginated(pageable).map(this::convertToDTO);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching paginated users: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Updates a user's password.
     *
     * @param id the user ID
     * @param passwordUpdate the password update request
     * @return the updated user
     */
    @PutMapping("/{id}/password")
    @RateLimiter(name = "updatePassword")
    @Operation(summary = "Update password")
    public ResponseEntity<UserDTO> updatePassword(@PathVariable @Min(1) Long id,
                                                @Valid @RequestBody PasswordUpdate passwordUpdate) {
        try {
            logger.debug("Updating password for user with ID: {}", id);
            User updatedUser = userService.updatePassword(id, 
                    passwordUpdate.getOldPassword(),
                    passwordUpdate.getNewPassword());
            logger.info("Password updated successfully for user with ID: {}", id);
            return ResponseEntity.ok(convertToDTO(updatedUser));
        } catch (UserNotFoundException e) {
            logger.warn("Failed to update password: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid password update attempt for user ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating password for user ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets a user with circuit breaker protection.
     *
     * @param id the user ID
     * @return the user with the given ID
     */
    @GetMapping("/circuit-test/{id}")
    @Operation(summary = "Get user with circuit breaker")
    public ResponseEntity<UserDTO> getUserWithCircuitBreaker(@PathVariable @Min(1) Long id) {
        try {
            logger.debug("Fetching user with circuit breaker, ID: {}", id);
            User user = userService.getUserByIdWithCircuitBreaker(id);
            return ResponseEntity.ok(convertToDTO(user));
        } catch (Exception e) {
            logger.error("Error in circuit breaker test for user ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets all active users with pagination.
     *
     * @param pageable pagination information
     * @return page of active users
     */
    @Operation(summary = "Get all active users with pagination")
    @GetMapping("/active")
    public ResponseEntity<Page<UserDTO>> getActiveUsers(Pageable pageable) {
        try {
            logger.debug("Fetching active users - page: {}, size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
            Page<UserDTO> users = userService.getActiveUsers(pageable)
                    .map(this::convertToDTO);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching active users: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets all inactive users with pagination.
     *
     * @param pageable pagination information
     * @return page of inactive users
     */
    @Operation(summary = "Get all inactive users with pagination")
    @GetMapping("/inactive")
    public ResponseEntity<Page<UserDTO>> getInactiveUsers(Pageable pageable) {
        try {
            logger.debug("Fetching inactive users - page: {}, size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
            Page<UserDTO> users = userService.getInactiveUsers(pageable)
                    .map(this::convertToDTO);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching inactive users: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets users by email domain with pagination.
     *
     * @param domain the email domain
     * @param pageable pagination information
     * @return page of users with the given email domain
     */
    @Operation(summary = "Get users by email domain with pagination")
    @GetMapping("/by-domain")
    public ResponseEntity<Page<UserDTO>> getUsersByEmailDomain(
            @RequestParam @NotBlank String domain,
            Pageable pageable) {
        try {
            logger.debug("Fetching users with email domain: {} - page: {}, size: {}", 
                domain, pageable.getPageNumber(), pageable.getPageSize());
            Page<UserDTO> users = userService.getUsersByEmailDomain(domain, pageable)
                    .map(this::convertToDTO);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching users by email domain: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets users by role with pagination.
     *
     * @param role the role
     * @param pageable pagination information
     * @return page of users with the given role
     */
    @Operation(summary = "Get users by role with pagination")
    @GetMapping("/by-role")
    public ResponseEntity<Page<UserDTO>> getUsersByRole(
            @RequestParam @NotBlank String role,
            Pageable pageable) {
        try {
            logger.debug("Fetching users with role: {} - page: {}, size: {}", 
                role, pageable.getPageNumber(), pageable.getPageSize());
            Page<UserDTO> users = userService.getUsersByRole(role, pageable)
                    .map(this::convertToDTO);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching users by role: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets users with a minimum number of roles with pagination.
     *
     * @param minRoles the minimum number of roles
     * @param pageable pagination information
     * @return page of users with at least the given number of roles
     */
    @Operation(summary = "Get users with minimum number of roles with pagination")
    @GetMapping("/by-min-roles")
    public ResponseEntity<Page<UserDTO>> getUsersByMinimumRoles(
            @RequestParam @Min(1) int minRoles,
            Pageable pageable) {
        try {
            logger.debug("Fetching users with minimum {} roles - page: {}, size: {}", 
                minRoles, pageable.getPageNumber(), pageable.getPageSize());
            Page<UserDTO> users = userService.getUsersByMinimumRoles(minRoles, pageable)
                    .map(this::convertToDTO);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching users by minimum roles: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Searches for users by multiple criteria with pagination.
     *
     * @param username the username to search for (optional)
     * @param email the email to search for (optional)
     * @param active the active status to search for (optional)
     * @param role the role to search for (optional)
     * @param pageable pagination information
     * @return page of matching users
     */
    @Operation(summary = "Search users by multiple criteria with pagination")
    @GetMapping("/search/advanced")
    public ResponseEntity<Page<UserDTO>> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String role,
            Pageable pageable) {
        try {
            logger.debug("Advanced search - username: {}, email: {}, active: {}, role: {} - page: {}, size: {}", 
                username, email, active, role, pageable.getPageNumber(), pageable.getPageSize());
            Page<UserDTO> users = userService.searchUsers(username, email, active, role, pageable)
                    .map(this::convertToDTO);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error in advanced user search: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Updates roles for a user.
     *
     * @param id the user ID
     * @param rolesDTO the roles to update
     * @return the updated user
     */
    @PutMapping("/{id}/roles")
    @Operation(summary = "Update user roles")
    public ResponseEntity<UserDTO> updateUserRoles(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody UpdateRolesDTO rolesDTO) {
        try {
            logger.debug("Updating roles for user with ID: {}", id);
            User updatedUser = userService.updateUserRoles(id, rolesDTO.getRoles());
            logger.info("Roles updated successfully for user with ID: {}", id);
            return ResponseEntity.ok(convertToDTO(updatedUser));
        } catch (UserNotFoundException e) {
            logger.warn("Failed to update roles: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating roles for user ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets a user summary by ID.
     *
     * @param id the user ID
     * @return the user summary with the given ID
     * @throws UserNotFoundException if no user is found with the given ID
     */
    @Operation(summary = "Get user summary by ID")
    @GetMapping("/{id}/summary")
    public ResponseEntity<UserSummaryDTO> getUserSummaryById(@PathVariable @Min(1) Long id) {
        logger.debug("Fetching user summary with ID: {}", id);
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                    .eTag(String.valueOf(user.getVersion()))
                    .body(UserSummaryDTO.fromUser(user)))
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", id);
                    return new UserNotFoundException(String.format(USER_NOT_FOUND_MSG, id));
                });
    }

    /**
     * Gets all user summaries with pagination.
     *
     * @param pageable pagination information
     * @return page of all user summaries
     */
    @Timed(value = "api.getAllUserSummaries.time")
    @Operation(summary = "Get all user summaries with pagination")
    @GetMapping("/summaries")
    public ResponseEntity<Page<UserSummaryDTO>> getAllUserSummaries(Pageable pageable) {
        try {
            logger.debug("Fetching all user summaries - page: {}, size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
            Page<UserSummaryDTO> users = userService.getAllUsers(pageable)
                    .map(UserSummaryDTO::fromUser);
            return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(15, TimeUnit.MINUTES))
                .body(users);
        } catch (Exception e) {
            logger.error("Error fetching all user summaries: {}", e.getMessage(), e);
            throw e;
        }
    }
}
