package com.example.demo;

/** Exception thrown when a user cannot be found. */
public final class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
} 