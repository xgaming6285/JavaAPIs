package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for the application.
 * This class handles exceptions thrown by controllers and returns appropriate HTTP responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handles RuntimeExceptions thrown by the application.
     * 
     * @param ex the RuntimeException that was thrown
     * @return a ResponseEntity containing an ErrorResponse with the exception message
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new ErrorResponse(ex.getMessage()));
    }
    
    /**
     * Custom error response DTO for returning error messages.
     */
    static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}
