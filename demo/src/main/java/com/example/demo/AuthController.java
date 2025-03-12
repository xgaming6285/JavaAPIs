package com.example.demo;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
@Validated
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final String TOKEN_PARAM = "token";
    private static final String EMAIL_VERIFIED_MSG = "Email verified successfully";
    private static final String INVALID_TOKEN_MSG = "Invalid token";
    private static final String PASSWORD_UPDATED_MSG = "Password updated successfully";
    private static final String INVALID_PASSWORD_MSG = "Old password is incorrect";
    
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
        logger.debug("Processing registration request for username: {}", createUserRequest.getUsername());
        
        if (isInvalidUser(createUserRequest)) {
            logger.warn("Invalid registration request: missing required fields");
            return ResponseEntity.badRequest().build();
        }
        
        if (userService.getUserByUsername(createUserRequest.getUsername()).isPresent()) {
            logger.warn("Registration failed: username already exists: {}", createUserRequest.getUsername());
            return ResponseEntity.badRequest().build();
        }
        
        if (userService.getUserByEmail(createUserRequest.getEmail()).isPresent()) {
            logger.warn("Registration failed: email already exists: {}", createUserRequest.getEmail());
            return ResponseEntity.badRequest().build();
        }
        
        try {
            User user = new User(
                createUserRequest.getUsername(),
                createUserRequest.getEmail(),
                passwordEncoder.encode(createUserRequest.getPassword())
            );
            
            User newUser = userService.createUser(user);
            String token = UUID.randomUUID().toString();
            
            userService.saveVerificationToken(newUser.getId(), token);
            emailService.sendVerificationEmail(newUser.getEmail(), token);
            
            logger.info("User registered successfully: {}", newUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserDTO(newUser.getId(), newUser.getUsername(), newUser.getEmail()));
        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage(), e);
            throw e;
        }
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
        logger.debug("Processing login request for username: {}", loginRequest.getUsername());
        
        try {
            boolean isValidUser = userService.verifyUserCredentials(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            );
            
            if (isValidUser) {
                logger.info("User logged in successfully: {}", loginRequest.getUsername());
                return ResponseEntity.ok("Login successful");
            } else {
                logger.warn("Failed login attempt for username: {}", loginRequest.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }
        } catch (Exception e) {
            logger.error("Error during login: {}", e.getMessage(), e);
            throw e;
        }
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
        logger.debug("Processing email verification with token: {}", token);
        
        try {
            Optional<User> userOpt = userService.getUserByToken(token);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setActive(true);
                userService.updateUser(user.getId(), user);
                logger.info("Email verified successfully for user: {}", user.getUsername());
                return ResponseEntity.ok(EMAIL_VERIFIED_MSG);
            } else {
                logger.warn("Email verification failed: invalid token");
                return ResponseEntity.badRequest().body(INVALID_TOKEN_MSG);
            }
        } catch (Exception e) {
            logger.error("Error during email verification: {}", e.getMessage(), e);
            throw e;
        }
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
        logger.debug("Processing password reset request for email: {}", resetPasswordRequest.getEmail());
        
        try {
            Optional<User> userOpt = userService.getUserByEmail(resetPasswordRequest.getEmail());
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String token = UUID.randomUUID().toString();
                userService.saveResetToken(user.getId(), token);
                emailService.sendResetPasswordEmail(user.getEmail(), token);
                logger.info("Password reset link sent to: {}", user.getEmail());
                return ResponseEntity.ok("Password reset link sent to your email");
            } else {
                logger.warn("Password reset failed: email not found: {}", resetPasswordRequest.getEmail());
                return ResponseEntity.badRequest().body("Email not found");
            }
        } catch (EmailServiceException e) {
            logger.error("Email service error during password reset: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Unable to send password reset email at this time");
        } catch (Exception e) {
            logger.error("Error during password reset: {}", e.getMessage(), e);
            throw e;
        }
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
        logger.debug("Processing password update with token");
        
        try {
            Optional<User> userOpt = userService.getUserByToken(token);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                if (!passwordEncoder.matches(passwordUpdate.getOldPassword(), user.getPassword())) {
                    logger.warn("Password update failed: incorrect old password for user: {}", user.getUsername());
                    return ResponseEntity.badRequest().body(INVALID_PASSWORD_MSG);
                }
                
                user.setPassword(passwordEncoder.encode(passwordUpdate.getNewPassword()));
                user.setToken(null); // Invalidate the token after use
                userService.updateUser(user.getId(), user);
                logger.info("Password updated successfully for user: {}", user.getUsername());
                return ResponseEntity.ok(PASSWORD_UPDATED_MSG);
            } else {
                logger.warn("Password update failed: invalid token");
                return ResponseEntity.badRequest().body(INVALID_TOKEN_MSG);
            }
        } catch (Exception e) {
            logger.error("Error during password update: {}", e.getMessage(), e);
            throw e;
        }
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