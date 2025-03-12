package com.example.demo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request object for password update operations.
 *
 * <p>This class handles password update requests containing both old and new password values.
 */
public final class PasswordUpdate {
  @NotBlank(message = "Old password is required")
  private String oldPassword;

  @NotBlank(message = "New password is required")
  @Size(min = 8, message = "New password must be at least 8 characters")
  private String newPassword;

  public PasswordUpdate() {}

  public PasswordUpdate(String oldPassword, String newPassword) {
    this.oldPassword = oldPassword;
    this.newPassword = newPassword;
  }

  public String getOldPassword() {
    return oldPassword;
  }

  public void setOldPassword(String oldPassword) {
    this.oldPassword = oldPassword;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }
} 