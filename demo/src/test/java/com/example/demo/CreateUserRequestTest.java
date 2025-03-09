package com.example.demo;

import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import static org.junit.jupiter.api.Assertions.*;

class CreateUserRequestTest {
    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        var violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenUsernameEmpty_thenViolation() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        var violations = validator.validate(request);
        assertEquals(2, violations.size());
        assertTrue(violations.stream()
            .map(v -> v.getMessage())
            .anyMatch(message -> message.equals("Username is required")));
        assertTrue(violations.stream()
            .map(v -> v.getMessage())
            .anyMatch(message -> message.contains("between 3 and 50 characters")));
    }

    @Test
    void whenUsernameTooShort_thenViolation() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("ab");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        var violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("between 3 and 50 characters"));
    }

    @Test
    void whenEmailInvalid_thenViolation() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("invalid-email");
        request.setPassword("password123");

        var violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Email should be valid", violations.iterator().next().getMessage());
    }

    @Test
    void whenPasswordTooShort_thenViolation() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("short");

        var violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Password must be at least 8 characters long", violations.iterator().next().getMessage());
    }
} 