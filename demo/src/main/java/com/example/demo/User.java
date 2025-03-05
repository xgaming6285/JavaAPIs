package com.example.demo;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity representing a user in the system.
 * This class maps to the "users" table in the database and contains user-related information.
 */
@Entity
@Table(name = "users")
public class User {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique identifier for the user

    private String username; // Username of the user
    private String email; // Email address of the user
    private String password; // User's password (should be hashed in production)

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> roles = new HashSet<>(); // User roles

    // Default constructor
    public User() {}

    // Parameterized constructor for initializing User with username, email, and password
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
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

    // Getter for password
    public String getPassword() {
        return password;
    }

    // Setter for password
    public void setPassword(String password) {
        this.password = password;
    }

    // Getter for roles
    public Set<String> getRoles() {
        return roles;
    }

    // Setter for roles
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    // toString method for representing the User object as a string
    @Override
    public String toString() {
        return "User{" +
        "id=" + id +
        ", username='" + username + '\'' +
        ", email='" + email + '\'' +
        ", password='" + password + '\'' +
        ", roles=" + roles +
        '}';
    }
}
