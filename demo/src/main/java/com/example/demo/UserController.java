package com.example.demo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Controller for managing user-related operations
@RestController
@RequestMapping("/api/v1/users")  // API version 1
@Tag(name = "User Management", description = "User management APIs")
public class UserController {
    
    private final Logger logger = LoggerFactory.getLogger(UserController.class); // Logger for this class
    private final Counter userCreationCounter; // Counter for tracking user creation events
    
    @Autowired
    private UserService userService; // Service for user-related operations
    
    // Constructor for UserController, initializes the user creation counter
    @Autowired
    public UserController(MeterRegistry registry) {
        this.userCreationCounter = Counter.builder("api.user.creation")
            .description("Number of users created")
            .register(registry);
    }
    
    // Endpoint to get all users
    @Timed(value = "api.getAllUsers.time", description = "Time taken to return all users")
    @Operation(summary = "Get all users", description = "Retrieves a list of all users in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved users")
    @GetMapping("")
    public List<UserDTO> getAllUsers() {
        logger.debug("Fetching all users"); // Log the action
        return userService.getAllUsers() // Fetch all users from the service
                          .stream()
                          .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getEmail())) // Map to UserDTO
                          .collect(Collectors.toList()); // Collect results into a list
    }
    
    // Endpoint to get a user by ID
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "ID of the user to retrieve") @PathVariable Long id) {
        Optional<User> userOpt = userService.getUserById(id); // Fetch user by ID
        return userOpt.map(user -> ResponseEntity.ok( // Return user if found
                                    new UserDTO(user.getId(), user.getUsername(), user.getEmail())))
                      .orElse(ResponseEntity.notFound().build()); // Return 404 if not found
    }
    
    // Endpoint to create a new user
    @Operation(summary = "Create a new user", description = "Creates a new user in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("")
    public ResponseEntity<UserDTO> createUser(
            @Parameter(description = "User details for creation") @RequestBody CreateUserDTO createUserDTO) {
        logger.info("Creating new user with username: {}", createUserDTO.getUsername()); // Log user creation
        
        try {
            User user = new User(createUserDTO.getUsername(), createUserDTO.getEmail(), createUserDTO.getPassword()); // Create user object
            User created = userService.createUser(user); // Create user in the service
            UserDTO userDTO = new UserDTO(created.getId(), created.getUsername(), created.getEmail()); // Map to UserDTO
            
            userCreationCounter.increment(); // Increment user creation counter
            logger.info("Successfully created user with ID: {}", created.getId()); // Log success
            
            return ResponseEntity.status(201).body(userDTO); // Return created user
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e); // Log error
            throw e; // Rethrow exception
        }
    }
    
    // Endpoint to update an existing user's information
    @Operation(summary = "Update user", description = "Updates an existing user's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "ID of the user to update") @PathVariable Long id,
            @Parameter(description = "Updated user details") @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(id, userDetails); // Update user in the service
        UserDTO userDTO = new UserDTO(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail()); // Map to UserDTO
        return ResponseEntity.ok(userDTO); // Return updated user
    }
    
    // Endpoint to delete a user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id); // Delete user in the service
        return ResponseEntity.noContent().build(); // Return no content response
    }
    
    // Endpoint to search users by username (partial match)
    @GetMapping("/search")
    public List<UserDTO> searchUsers(@RequestParam("username") String username) {
        return userService.searchUsersByUsername(username) // Search users by username
                          .stream()
                          .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getEmail())) // Map to UserDTO
                          .collect(Collectors.toList()); // Collect results into a list
    }
    
    // Endpoint to get users with pagination and sorting
    @GetMapping("/paginated")
    public Page<UserDTO> getUsersPaginated(Pageable pageable) {
        Page<User> usersPage = userService.getUsersPaginated(pageable); // Fetch paginated users
        return usersPage.map(user -> new UserDTO(user.getId(), user.getUsername(), user.getEmail())); // Map to UserDTO
    }
    
    // Endpoint to update password
    @PutMapping("/{id}/password")
    public ResponseEntity<UserDTO> updatePassword(@PathVariable Long id,
                                                  @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        User updatedUser = userService.updatePassword(id, // Update password in the service
                                passwordUpdateDTO.getOldPassword(),
                                passwordUpdateDTO.getNewPassword());
        UserDTO userDTO = new UserDTO(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail()); // Map to UserDTO
        return ResponseEntity.ok(userDTO); // Return updated user
    }
    
    // Endpoint to get user with circuit breaker
    @GetMapping("/circuit-test/{id}")
    public ResponseEntity<UserDTO> getUserWithCircuitBreaker(@PathVariable Long id) {
        User user = userService.getUserByIdWithCircuitBreaker(id); // Fetch user with circuit breaker
        UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(), user.getEmail()); // Map to UserDTO
        return ResponseEntity.ok(userDTO); // Return user
    }
    
    // Endpoint to update user profile
    @PutMapping("/{id}/profile")
    public ResponseEntity<UserDTO> updateProfile(@PathVariable Long id, @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(id, userDetails); // Update user in the service
        UserDTO userDTO = new UserDTO(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail()); // Map to UserDTO
        return ResponseEntity.ok(userDTO); // Return updated user
    }
}
