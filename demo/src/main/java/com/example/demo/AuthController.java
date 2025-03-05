package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public AuthController(UserService userService, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userService = userService; // Constructor injection for better testability
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }
    
    // Registration endpoint
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody CreateUserDTO createUserDTO) {
        User user = new User(createUserDTO.getUsername(), createUserDTO.getEmail(), passwordEncoder.encode(createUserDTO.getPassword()));
        User newUser = userService.createUser(user);
        logger.info("User created: {}", newUser);
        String token = UUID.randomUUID().toString(); // Generate token
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
        // Logic to verify the token and activate the user account
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
        String token = UUID.randomUUID().toString(); // Generate reset token
        emailService.sendResetPasswordEmail(email, token); // Send reset password email
        // Save token in the database or cache for later verification
        return ResponseEntity.ok("Password reset link sent to your email");
    }

    // New endpoint for updating the password
    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestParam String token, @RequestBody String newPassword) {
        // Logic to verify the token and update the password
        Optional<User> userOpt = userService.getUserByToken(token); // Fetch user by token
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword)); // Encrypt new password
            userService.updateUser(user.getId(), user); // Save updated user
            return ResponseEntity.ok("Password updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token"); // Return error response
        }
    }
}
