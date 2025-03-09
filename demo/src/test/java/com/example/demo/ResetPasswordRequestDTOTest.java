package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.*;

class ResetPasswordRequestDTOTest {
    private Validator validator;
    private ResetPasswordRequestDTO resetPasswordRequestDTO;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        resetPasswordRequestDTO = new ResetPasswordRequestDTO("test@example.com");
    }

    @Test
    void testValidResetPasswordRequestDTO() {
        var violations = validator.validate(resetPasswordRequestDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testEmailNotBlank() {
        resetPasswordRequestDTO.setEmail("");

        var violations = validator.validate(resetPasswordRequestDTO);
        assertEquals(1, violations.size());
        assertEquals("Email is mandatory", violations.iterator().next().getMessage());
    }

    @Test
    void testEmailFormat() {
        resetPasswordRequestDTO.setEmail("invalid-email");

        var violations = validator.validate(resetPasswordRequestDTO);
        assertEquals(1, violations.size());
        assertEquals("Email should be valid", violations.iterator().next().getMessage());
    }

    @Test
    void testConstructorAndGetters() {
        String email = "test@example.com";
        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO(email);
        assertEquals(email, dto.getEmail());
    }

    @Test
    void testSetters() {
        String newEmail = "new@example.com";
        resetPasswordRequestDTO.setEmail(newEmail);
        assertEquals(newEmail, resetPasswordRequestDTO.getEmail());
    }
} 