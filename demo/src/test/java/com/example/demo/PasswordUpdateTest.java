package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUpdateTest {
    private Validator validator;
    private PasswordUpdate passwordUpdate;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        passwordUpdate = new PasswordUpdate();
    }

    @Test
    void testValidPasswordUpdate() {
        passwordUpdate.setOldPassword("oldPass123");
        passwordUpdate.setNewPassword("newPass123");

        var violations = validator.validate(passwordUpdate);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testOldPasswordNotBlank() {
        passwordUpdate.setOldPassword("");
        passwordUpdate.setNewPassword("newPass123");

        var violations = validator.validate(passwordUpdate);
        assertEquals(1, violations.size());
        assertEquals("Old password is required", violations.iterator().next().getMessage());
    }

    @Test
    void testNewPasswordNotBlank() {
        passwordUpdate.setOldPassword("oldPass123");
        passwordUpdate.setNewPassword("");

        var violations = validator.validate(passwordUpdate);
        assertEquals(2, violations.size());
        assertTrue(violations.stream()
            .map(violation -> violation.getMessage())
            .anyMatch(message -> message.equals("New password is required")));
        assertTrue(violations.stream()
            .map(violation -> violation.getMessage())
            .anyMatch(message -> message.equals("New password must be at least 8 characters long")));
    }

    @Test
    void testNewPasswordMinLength() {
        passwordUpdate.setOldPassword("oldPass123");
        passwordUpdate.setNewPassword("short");

        var violations = validator.validate(passwordUpdate);
        assertEquals(1, violations.size());
        assertEquals("New password must be at least 8 characters long", violations.iterator().next().getMessage());
    }

    @Test
    void testGettersAndSetters() {
        String oldPassword = "oldPass123";
        String newPassword = "newPass123";

        passwordUpdate.setOldPassword(oldPassword);
        passwordUpdate.setNewPassword(newPassword);

        assertEquals(oldPassword, passwordUpdate.getOldPassword());
        assertEquals(newPassword, passwordUpdate.getNewPassword());
    }
} 