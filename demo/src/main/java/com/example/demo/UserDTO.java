package com.example.demo;

/**
 * UserDTO is a Data Transfer Object (DTO) that encapsulates user data.
 * It is used to transfer user information between different layers of the application.
 */
public class UserDTO {
    private Long id; // Unique identifier for the user
    private String username; // Username of the user
    private String email; // Email address of the user
    
    // Default constructor
    public UserDTO() {}

    // Parameterized constructor for initializing UserDTO with id, username, and email
    public UserDTO(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // Getter for id
    public Long getId() {
        return id;
    }

    // Setter for id
    public void setId(Long id) {
        this.id = id;
    }

    // Getter for username
    public String getUsername() {
        return username;
    }

    // Setter for username
    public void setUsername(String username) {
        this.username = username;
    }

    // Getter for email
    public String getEmail() {
        return email;
    }

    // Setter for email
    public void setEmail(String email) {
        this.email = email;
    }
}
