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
    public UserController(MeterRegistry registry) {
        this.userCreationCounter = Counter.builder("api.user.creation")
            .description("Number of users created")
            .register(registry);
    }
    
    // Endpoint to get all users
    /**
     * Retrieves a list of all users in the system.
     * This method fetches all users from the user service, maps them to UserDTO objects,
     * and returns them as a list. It is timed for performance monitoring.
     * 
     * @return A List of UserDTO objects representing all users in the system
     */
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
    /**
     * Creates a new user in the system.
     * 
     * This method handles the creation of a new user based on the provided CreateUserDTO.
     * It logs the creation process, creates a User object, persists it using the userService,
     * and returns a UserDTO representation of the created user. If successful, it returns
     * a 201 (Created) status code. In case of any errors during the process, it logs the
     * error and re-throws the exception.
     * 
     * @param createUserDTO The DTO containing user details for creation (username, email, password)
     * @return ResponseEntity<UserDTO> A ResponseEntity containing the created user's DTO and HTTP status 201 if successful
     * @throws Exception If an error occurs during user creation
     */
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
    /**
     * Updates an existing user's information
     * 
     * @param id ID of the user to update
     * @param userDetails Updated user details
     * @return ResponseEntity containing the updated UserDTO
     * @throws ResourceNotFoundException if the user is not found
     */
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
    /**
     * Deletes a user with the specified ID.
     * 
     * @param id The unique identifier of the user to be deleted
     * @return ResponseEntity with no content, indicating successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id); // Delete user in the service
        return ResponseEntity.noContent().build(); // Return no content response
    }
    
    // Endpoint to search users by username (partial match)
    /**
     * Searches for users based on the provided username and returns a list of UserDTO objects.
     * 
     * @param username The username to search for
     * @return A list of UserDTO objects containing matching users' id, username, and email
     */
    @GetMapping("/search")
    public List<UserDTO> searchUsers(@RequestParam("username") String username) {
        return userService.searchUsersByUsername(username) // Search users by username
                          .stream()
                          .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getEmail())) // Map to UserDTO
                          .collect(Collectors.toList()); // Collect results into a list
    }
    
    // Endpoint to get users with pagination and sorting
    /**
     * Retrieves a paginated list of user DTOs.
     * 
     * This method fetches a page of users from the userService and maps them to UserDTO objects.
     * The pagination is handled by the Pageable parameter, allowing for flexible page size and sorting.
     * 
     * @param pageable The pagination information, including page number, size, and sorting details
     * @return A Page object containing UserDTO objects representing the requested page of users
     */
    @GetMapping("/paginated")
    public Page<UserDTO> getUsersPaginated(Pageable pageable) {
        Page<User> usersPage = userService.getUsersPaginated(pageable); // Fetch paginated users
        return usersPage.map(user -> new UserDTO(user.getId(), user.getUsername(), user.getEmail())); // Map to UserDTO
    }
    
    // Endpoint to update password
    /**
     * Updates the password for a user with the given ID.
     * 
     * @param id The ID of the user whose password is to be updated
     * @param passwordUpdateDTO DTO containing the old and new passwords
     * @return ResponseEntity<UserDTO> containing the updated user information
     * @throws IllegalArgumentException if the old password is incorrect or the new password is invalid
     * @throws EntityNotFoundException if no user is found with the given ID
     */
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
    /**
     * Retrieves a user by ID using a circuit breaker pattern and returns it as a UserDTO.
     * 
     * @param id The unique identifier of the user to retrieve
     * @return ResponseEntity containing the UserDTO if found, or an appropriate error response
     */
    @GetMapping("/circuit-test/{id}")
    public ResponseEntity<UserDTO> getUserWithCircuitBreaker(@PathVariable Long id) {
        User user = userService.getUserByIdWithCircuitBreaker(id); // Fetch user with circuit breaker
        UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(), user.getEmail()); // Map to UserDTO
        return ResponseEntity.ok(userDTO); // Return user
    }
    
    // Endpoint to update user profile
    /**
     * Updates the user profile for the given user ID.
     * 
     * @param id The ID of the user to update
     * @param userDetails The updated user information
     * @return ResponseEntity containing the updated UserDTO
     */
    @PutMapping("/{id}/profile")
    public ResponseEntity<UserDTO> updateProfile(@PathVariable Long id, @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(id, userDetails); // Update user in the service
        UserDTO userDTO = new UserDTO(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail()); // Map to UserDTO
        return ResponseEntity.ok(userDTO); // Return updated user
    }
}
