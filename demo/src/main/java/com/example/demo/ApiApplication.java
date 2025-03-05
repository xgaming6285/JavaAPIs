package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for the Spring Boot User Management API.
 * This class serves as the entry point for the application.
 */
@SpringBootApplication
@EnableCaching       // Enable Spring caching for efficient data retrieval
@EnableAsync         // Enable asynchronous processing to improve performance
public class ApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args); // Launch the application
    }
}
