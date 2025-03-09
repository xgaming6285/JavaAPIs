package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUpdateDTOTest {
    private Validator validator;
    private PasswordUpdateDTO passwordUpdateDTO;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        passwordUpdateDTO = new PasswordUpdateDTO();
    }

    @Test
    void testValidPasswordUpdateDTO() {
        passwordUpdateDTO.setOldPassword("oldPass123");
        passwordUpdateDTO.setNewPassword("newPass123");

        var violations = validator.validate(passwordUpdateDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testOldPasswordNotBlank() {
        passwordUpdateDTO.setOldPassword("");
        passwordUpdateDTO.setNewPassword("newPass123");

        var violations = validator.validate(passwordUpdateDTO);
        assertEquals(1, violations.size());
        assertEquals("Old password is mandatory", violations.iterator().next().getMessage());
    }

    @Test
    void testNewPasswordNotBlank() {
        passwordUpdateDTO.setOldPassword("oldPass123");
        passwordUpdateDTO.setNewPassword("");

        var violations = validator.validate(passwordUpdateDTO);
        assertEquals(1, violations.size());
        assertEquals("New password is mandatory", violations.iterator().next().getMessage());
    }

    @Test
    void testNewPasswordPattern() {
        passwordUpdateDTO.setOldPassword("oldPass123");
        passwordUpdateDTO.setNewPassword("$2a$10$invalid");

        var violations = validator.validate(passwordUpdateDTO);
        assertEquals(1, violations.size());
        assertEquals("New password cannot start with '$2a$'", violations.iterator().next().getMessage());
    }

    @Test
    void testGettersAndSetters() {
        String oldPassword = "oldPass123";
        String newPassword = "newPass123";

        passwordUpdateDTO.setOldPassword(oldPassword);
        passwordUpdateDTO.setNewPassword(newPassword);

        assertEquals(oldPassword, passwordUpdateDTO.getOldPassword());
        assertEquals(newPassword, passwordUpdateDTO.getNewPassword());
    }
} 