package com.example.demo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private CreateUserRequest validUserRequest;
    private LoginRequest validLoginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        validUserRequest = new CreateUserRequest();
        validUserRequest.setUsername("testuser");
        validUserRequest.setEmail("test@example.com");
        validUserRequest.setPassword("password123");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setUsername("testuser");
        validLoginRequest.setPassword("password123");

        // Create mock user without using setId
        mockUser = new User("testuser", "test@example.com", "hashedPassword");
    }

    @Test
    void register_WithValidRequest_ShouldCreateUser() throws Exception {
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userService.createUser(any(User.class))).thenReturn(mockUser);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void register_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        CreateUserRequest invalidRequest = new CreateUserRequest();
        // Empty request

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnSuccess() throws Exception {
        when(userService.verifyUserCredentials(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Login successful"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        when(userService.verifyUserCredentials(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    void verifyEmail_WithValidToken_ShouldActivateUser() throws Exception {
        when(userService.getUserByToken(anyString())).thenReturn(Optional.of(mockUser));
        when(userService.updateUser(any(), any())).thenReturn(mockUser);

        mockMvc.perform(get("/api/auth/verify")
                .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Email verified successfully"));
    }

    @Test
    void verifyEmail_WithInvalidToken_ShouldReturnBadRequest() throws Exception {
        when(userService.getUserByToken(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/auth/verify")
                .param("token", "invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid token"));
    }

    @Test
    void resetPassword_WithValidEmail_ShouldSendResetLink() throws Exception {
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setEmail("test@example.com");

        when(userService.getUserByEmail(anyString())).thenReturn(Optional.of(mockUser));

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset link sent to your email"));
    }

    @Test
    void resetPassword_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setEmail("nonexistent@example.com");

        when(userService.getUserByEmail(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email not found"));
    }

    @Test
    void updatePassword_WithValidTokenAndPassword_ShouldUpdatePassword() throws Exception {
        PasswordUpdate passwordUpdate = new PasswordUpdate();
        passwordUpdate.setOldPassword("oldPassword");
        passwordUpdate.setNewPassword("newPassword");

        when(userService.getUserByToken(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("newHashedPassword");
        when(userService.updateUser(any(), any())).thenReturn(mockUser);

        mockMvc.perform(post("/api/auth/update-password")
                .param("token", "valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordUpdate)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password updated successfully"));
    }

    @Test
    void updatePassword_WithInvalidToken_ShouldReturnBadRequest() throws Exception {
        PasswordUpdate passwordUpdate = new PasswordUpdate();
        passwordUpdate.setOldPassword("oldPassword");
        passwordUpdate.setNewPassword("newPassword");

        when(userService.getUserByToken(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/update-password")
                .param("token", "invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid token"));
    }
} 