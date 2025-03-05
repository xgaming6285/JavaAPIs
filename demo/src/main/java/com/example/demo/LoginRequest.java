package com.example.demo;

/**
 * LoginRequest is a Data Transfer Object (DTO) used for encapsulating the login credentials.
 * It contains the username and password fields along with their respective getters and setters.
 */
public class LoginRequest {
    private String username; // The username of the user attempting to log in
    private String password; // The password of the user attempting to log in
    
    // Default constructor
    public LoginRequest() {
    }

    // Parameterized constructor for initializing LoginRequest with username and password
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getter for username
    public String getUsername() {
        return username;
    }

    // Setter for username
    public void setUsername(String username) {
        this.username = username;
    }

    // Getter for password
    public String getPassword() {
         return password;
    }

    // Setter for password
    public void setPassword(String password) {
         this.password = password;
    }
}
