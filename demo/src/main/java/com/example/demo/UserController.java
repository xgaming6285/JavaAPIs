package com.example.demo;

import java.util.List;
import java.util.stream.Collectors;
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
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management")
@Validated
public class UserController {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final Counter userCreationCounter;
    private final UserService userService;

    public UserController(UserService userService, MeterRegistry registry) {
        this.userService = Objects.requireNonNull(userService, "UserService must not be null");
        Objects.requireNonNull(registry, "MeterRegistry must not be null");
        this.userCreationCounter = Counter.builder("api.user.creation")
            .description("Number of users created")
            .register(registry);
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail());
    }

    @Timed(value = "api.getAllUsers.time")
    @Operation(summary = "Get all users")
    @GetMapping("")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable @Min(1) Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(convertToDTO(user)))
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @PostMapping("")
    @RateLimiter(name = "createUser")
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        if (logger.isInfoEnabled()) {
            logger.info("Creating new user with username: {}", createUserDTO.getUsername());
        }
        try {
            User user = new User(createUserDTO.getUsername(), 
                               createUserDTO.getEmail(), 
                               createUserDTO.getPassword());
            User created = userService.createUser(user);
            userCreationCounter.increment();
            return ResponseEntity.status(HttpStatus.CREATED)
                               .body(convertToDTO(created));
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error creating user: {}", e.getMessage(), e);
            }
            throw e;
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    public ResponseEntity<UserDTO> updateUser(@PathVariable @Min(1) Long id,
                                            @Valid @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(convertToDTO(updatedUser));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<Void> deleteUser(@PathVariable @Min(1) Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search users")
    public List<UserDTO> searchUsers(@RequestParam("username") @NotBlank String username) {
        return userService.searchUsersByUsername(username).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get paginated users")
    public Page<UserDTO> getUsersPaginated(Pageable pageable) {
        return userService.getUsersPaginated(pageable).map(this::convertToDTO);
    }

    @PutMapping("/{id}/password")
    @RateLimiter(name = "updatePassword")
    @Operation(summary = "Update password")
    public ResponseEntity<UserDTO> updatePassword(@PathVariable @Min(1) Long id,
                                                @Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        User updatedUser = userService.updatePassword(id, 
                passwordUpdateDTO.getOldPassword(),
                passwordUpdateDTO.getNewPassword());
        return ResponseEntity.ok(convertToDTO(updatedUser));
    }

    @GetMapping("/circuit-test/{id}")
    @Operation(summary = "Get user with circuit breaker")
    public ResponseEntity<UserDTO> getUserWithCircuitBreaker(@PathVariable @Min(1) Long id) {
        User user = userService.getUserByIdWithCircuitBreaker(id);
        return ResponseEntity.ok(convertToDTO(user));
    }

    @Operation(summary = "Get all active users")
    @GetMapping("/active")
    public ResponseEntity<List<UserDTO>> getActiveUsers() {
        List<UserDTO> users = userService.getActiveUsers().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get all inactive users")
    @GetMapping("/inactive")
    public ResponseEntity<List<UserDTO>> getInactiveUsers() {
        List<UserDTO> users = userService.getInactiveUsers().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get users by email domain")
    @GetMapping("/by-domain")
    public ResponseEntity<List<UserDTO>> getUsersByEmailDomain(
            @RequestParam @NotBlank String domain) {
        List<UserDTO> users = userService.getUsersByEmailDomain(domain).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get users by role")
    @GetMapping("/by-role")
    public ResponseEntity<List<UserDTO>> getUsersByRole(
            @RequestParam @NotBlank String role) {
        List<UserDTO> users = userService.getUsersByRole(role).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get users with minimum number of roles")
    @GetMapping("/by-min-roles")
    public ResponseEntity<List<UserDTO>> getUsersByMinimumRoles(
            @RequestParam @Min(1) int minRoles) {
        List<UserDTO> users = userService.getUsersByMinimumRoles(minRoles).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Search users by multiple criteria")
    @GetMapping("/search/advanced")
    public ResponseEntity<List<UserDTO>> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String role) {
        List<UserDTO> users = userService.searchUsers(username, email, active, role).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Update user roles")
    public ResponseEntity<UserDTO> updateUserRoles(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody UpdateRolesDTO rolesDTO) {
        User updatedUser = userService.updateUserRoles(id, rolesDTO.getRoles());
        return ResponseEntity.ok(convertToDTO(updatedUser));
    }
}
