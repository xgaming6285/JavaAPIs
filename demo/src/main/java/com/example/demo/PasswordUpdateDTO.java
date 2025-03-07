package com.example.demo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class PasswordUpdateDTO {
    
    @NotBlank(message = "Old password is mandatory")
    private String oldPassword;
    
    @NotBlank(message = "New password is mandatory") 
    @Pattern(regexp = "^(?!\\$2a\\$).*", message = "New password cannot start with '$2a$'")
    private String newPassword;

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
