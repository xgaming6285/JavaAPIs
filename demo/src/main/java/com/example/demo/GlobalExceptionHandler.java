package com.example.demo;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler for centralizing exception handling across the application.
 * Provides consistent error responses for different types of exceptions.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handles general runtime exceptions.
     *
     * @param ex The runtime exception to handle
     * @return ResponseEntity containing error details
     */
    @ExceptionHandler(RuntimeException.class)
    @ApiResponse(responseCode = "400", description = "Bad Request", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        if (logger.isErrorEnabled()) {
            logger.error("Runtime exception occurred: {}", ex.getMessage(), ex);
        }
        return createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handles user not found exceptions.
     *
     * @param ex The user not found exception to handle
     * @return ResponseEntity containing error details
     */
    @ExceptionHandler(UserNotFoundException.class)
    @ApiResponse(responseCode = "404", description = "User Not Found", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        if (logger.isWarnEnabled()) {
            logger.warn("User not found: {}", ex.getMessage());
        }
        return createErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handles email service exceptions.
     *
     * @param ex The email service exception to handle
     * @return ResponseEntity containing error details
     */
    @ExceptionHandler(EmailServiceException.class)
    @ApiResponse(responseCode = "503", description = "Email Service Error", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleEmailServiceException(EmailServiceException ex) {
        if (logger.isErrorEnabled()) {
            logger.error("Email service error: {}", ex.getMessage(), ex);
        }
        return createErrorResponse(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    /**
     * Handles validation exceptions for method arguments.
     *
     * @param ex The validation exception to handle
     * @return ResponseEntity containing error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ApiResponse(responseCode = "400", description = "Validation Error", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("Validation failed");
            
        if (logger.isWarnEnabled()) {
            logger.warn("Validation error: {}", errorMessage);
        }
        return createErrorResponse(errorMessage, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles constraint violation exceptions.
     *
     * @param ex The constraint violation exception
     * @param request The web request
     * @return ResponseEntity containing error details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ApiResponse(responseCode = "400", description = "Constraint Violation", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            WebRequest request) {
        
        String errorMessage = ex.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .collect(Collectors.joining(", "));
            
        if (logger.isWarnEnabled()) {
            logger.warn("Constraint violation: {}", errorMessage);
        }
        
        return createErrorResponse(errorMessage, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles illegal argument exceptions.
     *
     * @param ex The illegal argument exception
     * @return ResponseEntity containing error details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ApiResponse(responseCode = "400", description = "Invalid Argument", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        if (logger.isWarnEnabled()) {
            logger.warn("Invalid argument: {}", ex.getMessage());
        }
        return createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all unhandled exceptions.
     *
     * @param ex The exception to handle
     * @return ResponseEntity containing error details
     */
    @ExceptionHandler(Exception.class)
    @ApiResponse(responseCode = "500", description = "Internal Server Error", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        if (logger.isErrorEnabled()) {
            logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        }
        return createErrorResponse("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Creates a standardized error response.
     *
     * @param message The error message
     * @param status The HTTP status code
     * @return ResponseEntity containing error details
     */
    private ResponseEntity<ErrorResponse> createErrorResponse(String message, HttpStatus status) {
        ErrorResponse errorResponse = new ErrorResponse(message, status.value());
        errorResponse.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(status).body(errorResponse);
    }
}
