package com.example.demo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    @Test
    void testNoArgsConstructor() {
        LoginRequest loginRequest = new LoginRequest();
        assertNull(loginRequest.getUsername());
        assertNull(loginRequest.getPassword());
    }

    @Test
    void testAllArgsConstructor() {
        String username = "testUser";
        String password = "testPass";
        LoginRequest loginRequest = new LoginRequest(username, password);
        
        assertEquals(username, loginRequest.getUsername());
        assertEquals(password, loginRequest.getPassword());
    }

    @Test
    void testSettersAndGetters() {
        LoginRequest loginRequest = new LoginRequest();
        
        String username = "newUser";
        String password = "newPass";
        
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);
        
        assertEquals(username, loginRequest.getUsername());
        assertEquals(password, loginRequest.getPassword());
    }

    @Test
    void testSettersWithNull() {
        LoginRequest loginRequest = new LoginRequest("user", "pass");
        
        loginRequest.setUsername(null);
        loginRequest.setPassword(null);
        
        assertNull(loginRequest.getUsername());
        assertNull(loginRequest.getPassword());
    }
} 