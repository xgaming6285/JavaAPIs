package com.example.demo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailServiceExceptionTest {

    @Test
    void whenCreatedWithMessage_thenMessageIsSet() {
        String errorMessage = "Test error message";
        EmailServiceException exception = new EmailServiceException(errorMessage);
        
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void whenCreatedWithMessageAndCause_thenBothAreSet() {
        String errorMessage = "Test error message";
        RuntimeException cause = new RuntimeException("Cause");
        EmailServiceException exception = new EmailServiceException(errorMessage, cause);
        
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
} 