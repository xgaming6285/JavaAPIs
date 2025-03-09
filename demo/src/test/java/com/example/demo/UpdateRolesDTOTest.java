package com.example.demo;

import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UpdateRolesDTOTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void testNoArgsConstructor() {
        UpdateRolesDTO dto = new UpdateRolesDTO();
        assertNotNull(dto.getRoles());
        assertTrue(dto.getRoles().isEmpty());
    }

    @Test
    void testAllArgsConstructor() {
        Set<String> roles = new HashSet<>();
        roles.add("ADMIN");
        roles.add("USER");
        
        UpdateRolesDTO dto = new UpdateRolesDTO(roles);
        
        assertEquals(2, dto.getRoles().size());
        assertTrue(dto.getRoles().contains("ADMIN"));
        assertTrue(dto.getRoles().contains("USER"));
    }

    @Test
    void testSettersAndGetters() {
        UpdateRolesDTO dto = new UpdateRolesDTO();
        
        Set<String> roles = new HashSet<>();
        roles.add("MANAGER");
        
        dto.setRoles(roles);
        
        assertEquals(1, dto.getRoles().size());
        assertTrue(dto.getRoles().contains("MANAGER"));
    }

    @Test
    void testDefensiveCopy() {
        Set<String> roles = new HashSet<>();
        roles.add("ADMIN");
        
        UpdateRolesDTO dto = new UpdateRolesDTO(roles);
        
        // Try to modify the original set
        roles.add("USER");
        
        // DTO's roles should remain unchanged
        assertEquals(1, dto.getRoles().size());
        assertTrue(dto.getRoles().contains("ADMIN"));
        assertFalse(dto.getRoles().contains("USER"));
    }

    @Test
    void testGetRolesDefensiveCopy() {
        UpdateRolesDTO dto = new UpdateRolesDTO();
        Set<String> roles = new HashSet<>();
        roles.add("ADMIN");
        dto.setRoles(roles);
        
        Set<String> returnedRoles = dto.getRoles();
        
        // Try to modify the returned set
        assertThrows(UnsupportedOperationException.class, () -> {
            returnedRoles.add("USER");
        });
    }

    @Test
    void testValidation() {
        UpdateRolesDTO dto = new UpdateRolesDTO();
        dto.setRoles(new HashSet<>());
        
        var violations = validator.validate(dto);
        assertEquals(1, violations.size());
        
        var violation = violations.iterator().next();
        assertEquals("Roles cannot be empty", violation.getMessage());
    }

    @Test
    void testNullRolesHandling() {
        UpdateRolesDTO dto = new UpdateRolesDTO();
        dto.setRoles(null);
        
        assertNotNull(dto.getRoles());
        assertTrue(dto.getRoles().isEmpty());
    }
} 