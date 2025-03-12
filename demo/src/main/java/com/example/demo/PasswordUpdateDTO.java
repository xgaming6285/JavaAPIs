package com.example.demo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object for password update operations.
 *
 * <p>This class validates and transfers password update data between layers.
 */
public final class PasswordUpdateDTO {
    @NotBlank(message = "Old password is required")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @Pattern(regexp = "^(?!\\$2a\\$).*", message = "New password cannot start with '$2a$'")
    private String newPassword;

    public PasswordUpdateDTO() {}

    public PasswordUpdateDTO(String oldPassword, String newPassword) {
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
