package com.example.demo;

import org.springframework.http.HttpStatus; // Importing HttpStatus for response status codes
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
@Validated
public class AuthController {
    
    private final String TOKEN_PARAM = "token";
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Operation(summary = "Register new user", description = "Creates a new user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/register")
    @RateLimiter(name = "registration")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody CreateUserDTO createUserDTO) {
        if (isInvalidUser(createUserDTO)) {
            return ResponseEntity.badRequest().body(null);
        }
        
        User user = new User(
            createUserDTO.getUsername(), 
            createUserDTO.getEmail(), 
            passwordEncoder.encode(createUserDTO.getPassword())
        );
        
        User newUser = userService.createUser(user);
        String token = UUID.randomUUID().toString();

        userService.saveVerificationToken(newUser.getId(), token);
        emailService.sendVerificationEmail(newUser.getEmail(), token);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new UserDTO(newUser.getId(), newUser.getUsername(), newUser.getEmail()));
    }
    
    @Operation(summary = "User login", description = "Authenticates a user")
    @PostMapping("/login")
    @RateLimiter(name = "login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest loginRequest) {
        boolean isValidUser = userService.verifyUserCredentials(
            loginRequest.getUsername(), 
            loginRequest.getPassword()
        );
        
        return isValidUser 
            ? ResponseEntity.ok("Login successful")
            : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    @Operation(summary = "Verify email", description = "Verifies user's email address")
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam(TOKEN_PARAM) @NotBlank String token) {
        return userService.getUserByToken(token)
            .map(user -> {
                user.setActive(true);
                userService.updateUser(user.getId(), user);
                return ResponseEntity.ok("Email verified successfully");
            })
            .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token"));
    }

    @Operation(summary = "Request password reset", description = "Sends password reset email")
    @PostMapping("/reset-password")
    @RateLimiter(name = "passwordReset")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO resetPasswordRequest) {
        return userService.getUserByEmail(resetPasswordRequest.getEmail())
            .map(user -> {
                String token = UUID.randomUUID().toString();
                userService.saveResetToken(user.getId(), token);
                emailService.sendResetPasswordEmail(user.getEmail(), token);
                return ResponseEntity.ok("Password reset link sent to your email");
            })
            .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email not found"));
    }

    @Operation(summary = "Update password", description = "Updates user's password")
    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(
            @RequestParam(TOKEN_PARAM) @NotBlank String token,
            @Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        return userService.getUserByToken(token)
            .map(user -> {
                if (!passwordEncoder.matches(passwordUpdateDTO.getOldPassword(), user.getPassword())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Old password is incorrect");
                }
                
                user.setPassword(passwordEncoder.encode(passwordUpdateDTO.getNewPassword()));
                userService.updateUser(user.getId(), user);
                return ResponseEntity.ok("Password updated successfully");
            })
            .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token"));
    }

    private boolean isInvalidUser(CreateUserDTO createUserDTO) {
        return createUserDTO.getUsername() == null || createUserDTO.getUsername().isEmpty() ||
               createUserDTO.getEmail() == null || createUserDTO.getEmail().isEmpty() ||
               createUserDTO.getPassword() == null || createUserDTO.getPassword().isEmpty();
    }
}