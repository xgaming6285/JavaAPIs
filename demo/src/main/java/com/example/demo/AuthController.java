package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.Optional;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(UserService userService, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userService = userService; // Constructor injection for better testability
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }
    
    // Registration endpoint
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody CreateUserDTO createUserDTO) {
        logger.info("Received registration request: {}", createUserDTO); // Log the incoming request
        // Validate the CreateUserDTO
        if (createUserDTO.getUsername() == null || createUserDTO.getUsername().isEmpty()) {
            logger.error("Validation error: Username is mandatory");
            return ResponseEntity.badRequest().body(null);
        }
        if (createUserDTO.getEmail() == null || createUserDTO.getEmail().isEmpty()) {
            logger.error("Validation error: Email is mandatory");
            return ResponseEntity.badRequest().body(null);
        }
        if (createUserDTO.getPassword() == null || createUserDTO.getPassword().isEmpty()) {
            logger.error("Validation error: Password is mandatory");
            return ResponseEntity.badRequest().body(null);
        }
        User user = new User(createUserDTO.getUsername(), createUserDTO.getEmail(), passwordEncoder.encode(createUserDTO.getPassword()));
        logger.info("User object created: {}", user); // Log the created user object
        User newUser = userService.createUser(user);
        logger.info("User created in service: {}", newUser); // Log the user created in the service
        String token = UUID.randomUUID().toString(); // Generate token
        logger.info("Generated token: {}", token); // Log the generated token
        userService.saveVerificationToken(newUser.getId(), token); // Save token in cache
        emailService.sendVerificationEmail(newUser.getEmail(), token); // Send verification email
        // Save token in the database or cache for later verification
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserDTO(newUser.getId(), newUser.getUsername(), newUser.getEmail()));
    }
    
    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        boolean isValidUser = userService.verifyUserCredentials(loginRequest.getUsername(), loginRequest.getPassword());
        if (isValidUser) {
            return ResponseEntity.ok("Login successful"); // Return success response
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials"); // Return unauthorized response
        }
    }

    // New endpoint for email verification
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        Optional<User> userOpt = userService.getUserByToken(token); // Fetch user by token
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(true); // Activate the user account
            userService.updateUser(user.getId(), user); // Save the updated user
            return ResponseEntity.ok("Email verified successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token"); // Return error response
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO resetPasswordRequest) {
        String email = resetPasswordRequest.getEmail(); // Get email from DTO
        Optional<User> userOpt = userService.getUserByUsername(email); // Check if user exists
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email not found"); // Return error if email doesn't exist
        }
        String token = UUID.randomUUID().toString(); // Generate reset token
        userService.saveResetToken(userOpt.get().getId(), token); // Save token in the database
        emailService.sendResetPasswordEmail(email, token); // Send reset password email
        return ResponseEntity.ok("Password reset link sent to your email"); // Inform user
    }

    // New endpoint for updating the password
    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestParam String token, @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        // Logic to verify the token and update the password
        Optional<User> userOpt = userService.getUserByToken(token); // Fetch user by token
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Verify old password
            if (!passwordEncoder.matches(passwordUpdateDTO.getOldPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Old password is incorrect"); // Return error response
            }
            user.setPassword(passwordEncoder.encode(passwordUpdateDTO.getNewPassword())); // Encrypt new password
            userService.updateUser(user.getId(), user); // Save updated user
            return ResponseEntity.ok("Password updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token"); // Return error response
        }
    }
}
