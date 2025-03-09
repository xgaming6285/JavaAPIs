package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void whenHandleRuntimeException_thenReturnsBadRequest() {
        RuntimeException ex = new RuntimeException("Test runtime exception");
        
        ResponseEntity<ErrorResponse> response = handler.handleRuntimeException(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertEquals("Test runtime exception", body.getMessage());
        assertEquals(400, body.getStatus());
    }

    @Test
    void whenHandleUserNotFoundException_thenReturnsNotFound() {
        UserNotFoundException ex = new UserNotFoundException("User not found");
        
        ResponseEntity<ErrorResponse> response = handler.handleUserNotFound(ex);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertEquals("User not found", body.getMessage());
        assertEquals(404, body.getStatus());
    }

    @Test
    void whenHandleEmailServiceException_thenReturnsServiceUnavailable() {
        EmailServiceException ex = new EmailServiceException("Email service error");
        
        ResponseEntity<ErrorResponse> response = handler.handleEmailServiceException(ex);
        
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertEquals("Email service error", body.getMessage());
        assertEquals(503, body.getStatus());
    }

    @Test
    void whenHandleMethodArgumentNotValidException_thenReturnsBadRequest() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        
        FieldError fieldError = new FieldError("object", "field", "Field error message");
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));
        
        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertEquals("field: Field error message", body.getMessage());
        assertEquals(400, body.getStatus());
    }

    @Test
    void whenHandleGeneralException_thenReturnsInternalServerError() {
        Exception ex = new Exception("Unexpected error");
        
        ResponseEntity<ErrorResponse> response = handler.handleGeneralException(ex);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertEquals("An unexpected error occurred", body.getMessage());
        assertEquals(500, body.getStatus());
    }
} 