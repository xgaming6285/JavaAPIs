package com.example.demo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for resetting a user's password.
 * This class is used to transfer the email data during the password reset process.
 */
public class ResetPasswordRequestDTO {

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    private String email;

    // Default constructor
    public ResetPasswordRequestDTO() {}

    // Parameterized constructor
    public ResetPasswordRequestDTO(String email) {
        this.email = email;
    }

    // Getter and setter
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
} 