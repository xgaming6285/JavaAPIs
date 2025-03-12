package com.example.demo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for password reset requests.
 *
 * <p>This class validates and transfers password reset request data between layers.
 */
public final class ResetPasswordRequestDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    public ResetPasswordRequestDTO() {}

    public ResetPasswordRequestDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}