package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.*;

class ResetPasswordRequestTest {
    private Validator validator;
    private ResetPasswordRequest resetPasswordRequest;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        resetPasswordRequest = new ResetPasswordRequest();
    }

    @Test
    void testValidResetPasswordRequest() {
        resetPasswordRequest.setEmail("test@example.com");

        var violations = validator.validate(resetPasswordRequest);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testEmailNotBlank() {
        resetPasswordRequest.setEmail("");

        var violations = validator.validate(resetPasswordRequest);
        assertEquals(1, violations.size());
        assertEquals("Email is required", violations.iterator().next().getMessage());
    }

    @Test
    void testEmailFormat() {
        resetPasswordRequest.setEmail("invalid-email");

        var violations = validator.validate(resetPasswordRequest);
        assertEquals(1, violations.size());
        assertEquals("Email should be valid", violations.iterator().next().getMessage());
    }

    @Test
    void testGettersAndSetters() {
        String email = "test@example.com";
        resetPasswordRequest.setEmail(email);
        assertEquals(email, resetPasswordRequest.getEmail());
    }
} 