package com.example.demo;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void whenCreated_thenFieldsAreSet() {
        String message = "Test error message";
        int status = 400;
        
        ErrorResponse response = new ErrorResponse(message, status);
        
        assertEquals(message, response.getMessage());
        assertEquals(status, response.getStatus());
        assertNotNull(response.getTimestamp());
        assertTrue(response.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(response.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void whenSettersUsed_thenFieldsAreUpdated() {
        ErrorResponse response = new ErrorResponse("Initial message", 400);
        
        String newMessage = "Updated message";
        int newStatus = 500;
        LocalDateTime newTimestamp = LocalDateTime.now().minusHours(1);
        
        response.setMessage(newMessage);
        response.setStatus(newStatus);
        response.setTimestamp(newTimestamp);
        
        assertEquals(newMessage, response.getMessage());
        assertEquals(newStatus, response.getStatus());
        assertEquals(newTimestamp, response.getTimestamp());
    }
} 