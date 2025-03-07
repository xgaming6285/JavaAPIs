package com.example.demo;

import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
@Validated
public class AuthController {
    
    private final String TOKEN_PARAM = "token";
    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "Register new user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/register")
    @RateLimiter(name = "registration")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody CreateUserDTO createUserDTO) {
        if (isInvalidUser(createUserDTO)) {
            return ResponseEntity.badRequest().build();
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

    @Operation(summary = "Request password reset")
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
            .orElseGet(() -> ResponseEntity.badRequest().body("Email not found"));
    }

    @Operation(summary = "Update password")
    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(
            @RequestParam(TOKEN_PARAM) @NotBlank String token,
            @Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        return userService.getUserByToken(token)
            .map(user -> {
                if (!passwordEncoder.matches(passwordUpdateDTO.getOldPassword(), user.getPassword())) {
                    return ResponseEntity.badRequest().body("Old password is incorrect");
                }
                
                user.setPassword(passwordEncoder.encode(passwordUpdateDTO.getNewPassword()));
                userService.updateUser(user.getId(), user);
                return ResponseEntity.ok("Password updated successfully");
            })
            .orElseGet(() -> ResponseEntity.badRequest().body("Invalid token"));
    }

    private boolean isInvalidUser(CreateUserDTO createUserDTO) {
        return createUserDTO.getUsername() == null || createUserDTO.getUsername().isEmpty() ||
               createUserDTO.getEmail() == null || createUserDTO.getEmail().isEmpty() ||
               createUserDTO.getPassword() == null || createUserDTO.getPassword().isEmpty();
    }
}