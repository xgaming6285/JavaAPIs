package com.example.demo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request object for password reset initiation.
 */
public class ResetPasswordRequest {
  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  private String email;

  // Default constructor
  public ResetPasswordRequest() {}

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
} 