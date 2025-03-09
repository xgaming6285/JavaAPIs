package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateUserDTOTest {

    private Validator validator;
    private CreateUserDTO validUserDTO;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        validUserDTO = new CreateUserDTO("testuser", "test@example.com", "password123");
    }

    @Test
    void whenAllFieldsValid_ShouldHaveNoViolations() {
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(validUserDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenUsernameEmpty_ShouldHaveViolation() {
        validUserDTO.setUsername("");
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(validUserDTO);
        
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Username is mandatory", violations.iterator().next().getMessage());
    }

    @Test
    void whenUsernameNull_ShouldHaveViolation() {
        validUserDTO.setUsername(null);
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(validUserDTO);
        
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Username is mandatory", violations.iterator().next().getMessage());
    }

    @Test
    void whenEmailInvalid_ShouldHaveViolation() {
        validUserDTO.setEmail("invalid-email");
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(validUserDTO);
        
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Email should be valid", violations.iterator().next().getMessage());
    }

    @Test
    void whenEmailNull_ShouldHaveViolation() {
        validUserDTO.setEmail(null);
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(validUserDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(violation -> violation.getMessage().equals("Email should be valid") ||
                     violation.getMessage().equals("Email is mandatory")));
    }

    @Test
    void whenPasswordStartsWithInvalidPrefix_ShouldHaveViolation() {
        validUserDTO.setPassword("$2a$10$invalid");
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(validUserDTO);
        
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Password cannot start with '$2a$'", violations.iterator().next().getMessage());
    }

    @Test
    void whenPasswordEmpty_ShouldHaveViolation() {
        validUserDTO.setPassword("");
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(validUserDTO);
        
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Password is mandatory", violations.iterator().next().getMessage());
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        CreateUserDTO userDTO = new CreateUserDTO("initial", "initial@test.com", "initial123");
        
        userDTO.setUsername("newuser");
        assertEquals("newuser", userDTO.getUsername());
        
        userDTO.setEmail("new@example.com");
        assertEquals("new@example.com", userDTO.getEmail());
        
        userDTO.setPassword("newpassword");
        assertEquals("newpassword", userDTO.getPassword());
    }
} 