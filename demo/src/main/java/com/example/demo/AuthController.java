package com.example.demo;

import org.springframework.http.HttpStatus; // Importing HttpStatus for response status codes
import org.springframework.http.ResponseEntity; // Importing ResponseEntity for HTTP responses
import org.springframework.security.crypto.password.PasswordEncoder; // Importing PasswordEncoder for password encryption
import org.springframework.web.bind.annotation.*; // Importing Spring Web annotations

import java.util.UUID; // Importing UUID for generating unique tokens

import jakarta.validation.Valid; // Importing Valid for validating request bodies
import org.slf4j.Logger; // Importing Logger for logging
import org.slf4j.LoggerFactory; // Importing LoggerFactory for creating Logger instances

@RestController // Indicates that this class is a REST controller
@RequestMapping("/api/auth") // Mapping requests to /api/auth
public class AuthController {
    
    private final UserService userService; // Service for user-related operations
    private final EmailService emailService; // Service for sending emails
    private final PasswordEncoder passwordEncoder; // Encoder for password hashing
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class); // Logger instance for logging

    // Constructor for dependency injection
    public AuthController(UserService userService, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userService = userService; // Assigning userService
        this.emailService = emailService; // Assigning emailService
        this.passwordEncoder = passwordEncoder; // Assigning passwordEncoder
    }
    
    @PostMapping("/register") // Mapping POST requests to /register
    public ResponseEntity<UserDTO> register(@Valid @RequestBody CreateUserDTO createUserDTO) {
        logger.info("Received registration request: {}", createUserDTO); // Logging registration request
        
        if (isInvalidUser(createUserDTO)) { // Checking if the user data is invalid
            return ResponseEntity.badRequest().body(null); // Returning bad request if invalid
        }
        
        // Creating a new User object with encoded password
        User user = new User(createUserDTO.getUsername(), createUserDTO.getEmail(), passwordEncoder.encode(createUserDTO.getPassword()));
        logger.info("User object created: {}", user); // Logging created User object
        
        User newUser = userService.createUser(user); // Creating user in the service
        logger.info("User created in service: {}", newUser); // Logging new user creation
        
        String token = UUID.randomUUID().toString(); // Generating a unique token
        logger.info("Generated token: {}", token); // Logging generated token
        
        userService.saveVerificationToken(newUser.getId(), token); // Saving verification token
        emailService.sendVerificationEmail(newUser.getEmail(), token); // Sending verification email
        
        // Returning created user details in response
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserDTO(newUser.getId(), newUser.getUsername(), newUser.getEmail()));
    }
    
    @PostMapping("/login") // Mapping POST requests to /login
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest loginRequest) {
        // Verifying user credentials
        boolean isValidUser = userService.verifyUserCredentials(loginRequest.getUsername(), loginRequest.getPassword());
        // Returning success or unauthorized response based on verification
        return isValidUser ? ResponseEntity.ok("Login successful") : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    @GetMapping("/verify") // Mapping GET requests to /verify
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        // Verifying the email using the provided token
        return userService.getUserByToken(token)
            .map(user -> {
                user.setActive(true); // Activating the user
                userService.updateUser(user.getId(), user); // Updating user status
                return ResponseEntity.ok("Email verified successfully"); // Returning success response
            })
            .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token")); // Returning error if token is invalid
    }

    @PostMapping("/reset-password") // Mapping POST requests to /reset-password
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO resetPasswordRequest) {
        String email = resetPasswordRequest.getEmail(); // Extracting email from request
        // Finding user by email
        return userService.getUserByEmail(email)
            .map(user -> {
                String token = UUID.randomUUID().toString(); // Generating reset token
                userService.saveResetToken(user.getId(), token); // Saving reset token
                emailService.sendResetPasswordEmail(email, token); // Sending reset password email
                return ResponseEntity.ok("Password reset link sent to your email"); // Returning success response
            })
            .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email not found")); // Returning error if email not found
    }

    @PostMapping("/update-password") // Mapping POST requests to /update-password
    public ResponseEntity<String> updatePassword(@RequestParam String token, @Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        logger.debug("Received password update request for token: {}", token); // Logging password update request
        
        // Finding user by token
        return userService.getUserByToken(token)
            .map(user -> {
                logger.debug("Stored password hash for user {}: {}", user.getUsername(), user.getPassword()); // Log stored password hash
                logger.debug("Old password being checked: {}", passwordUpdateDTO.getOldPassword()); // Log old password being checked
                
                // Compare the old password with the stored password
                if (!passwordEncoder.matches(passwordUpdateDTO.getOldPassword(), user.getPassword())) {
                    logger.error("Old password is incorrect for user: {}", user.getUsername()); // Logging error for incorrect old password
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Old password is incorrect"); // Returning error response
                }
                
                user.setPassword(passwordEncoder.encode(passwordUpdateDTO.getNewPassword())); // Updating password
                userService.updateUser(user.getId(), user); // Saving updated user
                logger.info("Password updated successfully for user: {}", user.getUsername()); // Logging successful password update
                return ResponseEntity.ok("Password updated successfully"); // Returning success response
            })
            .orElseGet(() -> {
                logger.error("Invalid token provided for password update"); // Logging error for invalid token
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token"); // Returning error response
            });
    }

    // Method to validate user data during registration
    private boolean isInvalidUser(CreateUserDTO createUserDTO) {
        if (createUserDTO.getUsername() == null || createUserDTO.getUsername().isEmpty()) {
            logger.error("Validation error: Username is mandatory"); // Logging error for missing username
            return true; // Returning true for invalid user
        }
        if (createUserDTO.getEmail() == null || createUserDTO.getEmail().isEmpty()) {
            logger.error("Validation error: Email is mandatory"); // Logging error for missing email
            return true; // Returning true for invalid user
        }
        if (createUserDTO.getPassword() == null || createUserDTO.getPassword().isEmpty()) {
            logger.error("Validation error: Password is mandatory"); // Logging error for missing password
            return true; // Returning true for invalid user
        }
        return false; // Returning false if user data is valid
    }
}