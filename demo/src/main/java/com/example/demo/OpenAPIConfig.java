package com.example.demo;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${server.url:http://localhost:8080}")
    private String serverUrl;

    /**
     * Creates and configures the main OpenAPI documentation object.
     *
     * @return configured OpenAPI instance
     */
    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server()
                .url(serverUrl)
                .description("Server URL in Development environment");

        Contact contact = new Contact()
                .name("API Support")
                .email("support@example.com");

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
     * Configures the User Management API group.
     *
     * @return GroupedOpenApi instance for user management endpoints
     */
    @Bean
    public GroupedOpenApi userApis() {
        return GroupedOpenApi.builder()
                .group("User Management")
                .pathsToMatch("/api/v1/users/**")
                .build();
    }

    /**
     * Configures the Authentication API group.
     *
     * @return GroupedOpenApi instance for authentication endpoints
     */
    @Bean
    public GroupedOpenApi authApis() {
        return GroupedOpenApi.builder()
                .group("Authentication")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    /**
     * Configures the Health API group.
     *
     * @return GroupedOpenApi instance for health check endpoints
     */
    @Bean
    public GroupedOpenApi healthApis() {
        return GroupedOpenApi.builder()
                .group("Health")
                .pathsToMatch("/api/health/**")
                .build();
    }

    /**
     * Configures the Analytics API group.
     *
     * @return GroupedOpenApi instance for analytics endpoints
     */
    @Bean
    public GroupedOpenApi analyticsApis() {
        return GroupedOpenApi.builder()
                .group("Analytics")
                .pathsToMatch("/api/v1/analytics/**")
                .build();
    }
}