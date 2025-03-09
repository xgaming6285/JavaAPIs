package com.example.demo;

import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void testConstructor() {
        User user = new User("testUser", "test@example.com", "password123");
        
        assertEquals("testUser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertNull(user.getId());
        assertFalse(user.isActive());
        assertTrue(user.getRoles().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        User user = new User();
        
        user.setUsername("newUser");
        user.setEmail("new@example.com");
        user.setPassword("newPass");
        user.setToken("token123");
        user.setActive(true);
        
        assertEquals("newUser", user.getUsername());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("newPass", user.getPassword());
        assertEquals("token123", user.getToken());
        assertTrue(user.isActive());
    }

    @Test
    void testRolesManagement() {
        User user = new User();
        
        // Test adding roles
        user.addRole("ADMIN");
        user.addRole("USER");
        
        Set<String> roles = user.getRoles();
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ADMIN"));
        assertTrue(roles.contains("USER"));
        
        // Test removing role
        user.removeRole("USER");
        roles = user.getRoles();
        assertEquals(1, roles.size());
        assertTrue(roles.contains("ADMIN"));
        
        // Test setting roles
        Set<String> newRoles = new HashSet<>();
        newRoles.add("MANAGER");
        user.setRoles(newRoles);
        
        roles = user.getRoles();
        assertEquals(1, roles.size());
        assertTrue(roles.contains("MANAGER"));
    }

    @Test
    void testValidation() {
        User user = new User();
        
        var violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        
        // Test username validation
        user.setUsername("");
        violations = validator.validate(user);
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
        
        // Test email validation
        user.setEmail("invalid-email");
        violations = validator.validate(user);
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testEqualsAndHashCode() {
        User user1 = new User("user1", "user1@example.com", "pass1");
        User user2 = new User("user1", "user1@example.com", "pass1");
        User user3 = new User("user2", "user2@example.com", "pass2");
        
        // Test reflexivity
        assertEquals(user1, user1);
        
        // Test null ID case - users should be equal if they have same data and both IDs are null
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        
        // Test hashCode
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }

    @Test
    void testToString() {
        User user = new User("testUser", "test@example.com", "pass");
        user.setActive(true);
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        user.setRoles(roles);
        
        String toString = user.toString();
        
        assertTrue(toString.contains("testUser"));
        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("true")); // active status
        assertTrue(toString.contains("USER")); // role
        assertFalse(toString.contains("pass")); // password should not be in toString
    }

    @Test
    void testBuilder() {
        Set<String> roles = new HashSet<>();
        roles.add("ADMIN");
        
        User user = User.builder()
            .username("builderUser")
            .email("builder@example.com")
            .password("builderPass")
            .token("token123")
            .active(true)
            .roles(roles)
            .build();
        
        assertEquals("builderUser", user.getUsername());
        assertEquals("builder@example.com", user.getEmail());
        assertEquals("builderPass", user.getPassword());
        assertEquals("token123", user.getToken());
        assertTrue(user.isActive());
        assertTrue(user.getRoles().contains("ADMIN"));
    }

    @Test
    void testBuilderWithNullValues() {
        User user = User.builder()
            .username(null)
            .email(null)
            .password(null)
            .token(null)
            .roles(null)
            .build();
        
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getToken());
        assertNotNull(user.getRoles());
        assertTrue(user.getRoles().isEmpty());
    }
} 