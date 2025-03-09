package com.example.demo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserNotFoundExceptionTest {

    @Test
    void testExceptionMessage() {
        String errorMessage = "User not found with id: 1";
        UserNotFoundException exception = new UserNotFoundException(errorMessage);
        
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testExceptionInheritance() {
        UserNotFoundException exception = new UserNotFoundException("test");
        
        assertTrue(exception instanceof RuntimeException);
    }
} 