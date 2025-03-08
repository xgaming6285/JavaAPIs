package com.example.demo;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling authentication-related endpoints including user registration,
 * login, email verification, and password management.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
@Validated
public class AuthController {
    
    private static final String TOKEN_PARAM = "token";
    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a new AuthController with required services.
     *
     * @param userService User management service
     * @param emailService Email sending service
     * @param passwordEncoder Password encryption service
     */
    public AuthController(UserService userService, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userService = Objects.requireNonNull(userService, "UserService must not be null");
        this.emailService = Objects.requireNonNull(emailService, "EmailService must not be null");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "PasswordEncoder must not be null");
    }

    /**
     * Registers a new user in the system.
     *
     * @param createUserRequest User creation request data
     * @return ResponseEntity containing the created user data
     */
    @Operation(summary = "Register new user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/register")
    @RateLimiter(name = "registration")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody CreateUserRequest createUserRequest) {
        if (isInvalidUser(createUserRequest)) {
            return ResponseEntity.badRequest().build();
        }
        
        User user = new User(
            createUserRequest.getUsername(),
            createUserRequest.getEmail(),
            passwordEncoder.encode(createUserRequest.getPassword())
        );
        
        User newUser = userService.createUser(user);
        String token = UUID.randomUUID().toString();
        
        userService.saveVerificationToken(newUser.getId(), token);
        emailService.sendVerificationEmail(newUser.getEmail(), token);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new UserDTO(newUser.getId(), newUser.getUsername(), newUser.getEmail()));
    }

    /**
     * Authenticates a user.
     *
     * @param loginRequest Login credentials
     * @return ResponseEntity with login status
     */
    @Operation(summary = "User login") 
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

    /**
     * Verifies user's email address using a token.
     *
     * @param token Verification token
     * @return ResponseEntity with verification status
     */
    @Operation(summary = "Verify email")
    @GetMapping("/verify") 
    public ResponseEntity<String> verifyEmail(@RequestParam(TOKEN_PARAM) @NotBlank String token) {
        return userService.getUserByToken(token)
            .map(user -> {
                user.setActive(true);
                userService.updateUser(user.getId(), user);
                return ResponseEntity.ok("Email verified successfully");
            })
            .orElseGet(() -> ResponseEntity.badRequest().body("Invalid token"));
    }

    /**
     * Initiates password reset process.
     *
     * @param resetPasswordRequest Password reset request data
     * @return ResponseEntity with reset status
     */
    @Operation(summary = "Request password reset")
    @PostMapping("/reset-password")
    @RateLimiter(name = "passwordReset")
    public ResponseEntity<String> resetPassword(
        @Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        return userService.getUserByEmail(resetPasswordRequest.getEmail())
            .map(user -> {
                String token = UUID.randomUUID().toString();
                userService.saveResetToken(user.getId(), token);
                emailService.sendResetPasswordEmail(user.getEmail(), token);
                return ResponseEntity.ok("Password reset link sent to your email");
            })
            .orElseGet(() -> ResponseEntity.badRequest().body("Email not found"));
    }

    /**
     * Updates user's password after verification.
     *
     * @param token Password reset token
     * @param passwordUpdate Password update data
     * @return ResponseEntity with update status
     */
    @Operation(summary = "Update password")
    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(
        @RequestParam(TOKEN_PARAM) @NotBlank String token,
        @Valid @RequestBody PasswordUpdate passwordUpdate) {
        return userService.getUserByToken(token)
            .map(user -> {
                if (!passwordEncoder.matches(passwordUpdate.getOldPassword(), user.getPassword())) {
                    return ResponseEntity.badRequest().body("Old password is incorrect");
                }
                
                user.setPassword(passwordEncoder.encode(passwordUpdate.getNewPassword()));
                userService.updateUser(user.getId(), user);
                return ResponseEntity.ok("Password updated successfully");
            })
            .orElseGet(() -> ResponseEntity.badRequest().body("Invalid token"));
    }

    private boolean isInvalidUser(CreateUserRequest createUserRequest) {
        return createUserRequest.getUsername() == null 
            || createUserRequest.getUsername().isEmpty()
            || createUserRequest.getEmail() == null 
            || createUserRequest.getEmail().isEmpty()
            || createUserRequest.getPassword() == null 
            || createUserRequest.getPassword().isEmpty();
    }
}