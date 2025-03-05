package com.example.demo;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration class for setting up API documentation.
 * This class defines the OpenAPI specification for the User Management API,
 * including server details, contact information, and grouped API paths.
 */
@Configuration
public class OpenAPIConfig {
    
    /**
     * Configures the OpenAPI instance with server information and API details.
     * 
     * @return OpenAPI instance with configured information.
     */
    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Server URL in Development environment");

        Contact contact = new Contact();
        contact.setName("API Support");
        contact.setEmail("support@example.com");

        Info info = new Info()
                .title("User Management API")
                .version("1.0")
                .contact(contact)
                .description("This API exposes endpoints to manage users.");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }

    /**
     * Groups the user management APIs for documentation.
     * 
     * @return GroupedOpenApi instance for user management APIs.
     */
    @Bean
    public GroupedOpenApi userApis() {
        return GroupedOpenApi.builder()
                .group("User Management")
                .pathsToMatch("/api/v1/users/**")
                .build();
    }

    /**
     * Groups the authentication APIs for documentation.
     * 
     * @return GroupedOpenApi instance for authentication APIs.
     */
    @Bean
    public GroupedOpenApi authApis() {
        return GroupedOpenApi.builder()
                .group("Authentication")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    /**
     * Groups the health check APIs for documentation.
     * 
     * @return GroupedOpenApi instance for health check APIs.
     */
    @Bean
    public GroupedOpenApi healthApis() {
        return GroupedOpenApi.builder()
                .group("Health")
                .pathsToMatch("/api/health/**")
                .build();
    }
} 