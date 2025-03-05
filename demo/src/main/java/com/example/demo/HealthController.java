package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * HealthController is responsible for providing health check endpoints.
 * This controller allows clients to verify if the API is running.
 */
@RestController
@RequestMapping("/api")
public class HealthController {
    
    /**
     * Health check endpoint to verify if the API is operational.
     * 
     * @return ResponseEntity containing a message indicating the API status.
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("API is running");
    }
}
