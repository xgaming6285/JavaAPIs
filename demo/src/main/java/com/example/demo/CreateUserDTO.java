package com.example.demo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for creating a new user.
 * This class is used to transfer user data during registration.
 */
public class CreateUserDTO {
    
    @NotBlank(message = "Username is mandatory")
    private String username;
    
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is mandatory")
    private String password;

    // Default constructor
    public CreateUserDTO() {
    }
    
    // Parameterized constructor
    public CreateUserDTO(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    } 
}
