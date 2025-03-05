package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService; // Constructor injection for better testability
    }
    
    // Registration endpoint
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody CreateUserDTO createUserDTO) {
        User user = new User(createUserDTO.getUsername(), createUserDTO.getEmail(), createUserDTO.getPassword());
        User newUser = userService.createUser(user);
        UserDTO userDTO = new UserDTO(newUser.getId(), newUser.getUsername(), newUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }
    
    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        if (userService.verifyUserCredentials(loginRequest.getUsername(), loginRequest.getPassword())) {
            return ResponseEntity.ok("Login successful");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
}
