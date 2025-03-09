package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Spring Boot application class.
 * Configures and launches the application with caching and asynchronous processing enabled.
 * 
 * {@link EnableCaching} enables Spring's caching support
 * {@link EnableAsync} enables asynchronous method execution support
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class ApiApplication {
  /**
   * Main method that starts the Spring Boot application.
   *
   * @param args Command line arguments passed to the application
   */
  public static void main(String[] args) {
    SpringApplication.run(ApiApplication.class, args);
  }
}
