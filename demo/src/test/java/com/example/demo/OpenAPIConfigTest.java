package com.example.demo;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;

import static org.junit.jupiter.api.Assertions.*;

class OpenAPIConfigTest {

    private final OpenAPIConfig openAPIConfig = new OpenAPIConfig();

    @Test
    void testMyOpenAPI() {
        OpenAPI api = openAPIConfig.myOpenAPI();
        
        assertNotNull(api);
        assertNotNull(api.getInfo());
        assertNotNull(api.getServers());
        
        // Test Info object
        Info info = api.getInfo();
        assertEquals("User Management API", info.getTitle());
        assertEquals("1.0", info.getVersion());
        assertEquals("This API exposes endpoints to manage users.", info.getDescription());
        
        // Test Contact object
        Contact contact = info.getContact();
        assertNotNull(contact);
        assertEquals("API Support", contact.getName());
        assertEquals("support@example.com", contact.getEmail());
        
        // Test Server object
        assertEquals(1, api.getServers().size());
        Server server = api.getServers().get(0);
        assertEquals("http://localhost:8080", server.getUrl());
        assertEquals("Server URL in Development environment", server.getDescription());
    }

    @Test
    void testUserApis() {
        GroupedOpenApi api = openAPIConfig.userApis();
        
        assertNotNull(api);
        assertEquals("User Management", api.getGroup());
        assertArrayEquals(new String[]{"/api/v1/users/**"}, api.getPathsToMatch().toArray());
    }

    @Test
    void testAuthApis() {
        GroupedOpenApi api = openAPIConfig.authApis();
        
        assertNotNull(api);
        assertEquals("Authentication", api.getGroup());
        assertArrayEquals(new String[]{"/api/auth/**"}, api.getPathsToMatch().toArray());
    }

    @Test
    void testHealthApis() {
        GroupedOpenApi api = openAPIConfig.healthApis();
        
        assertNotNull(api);
        assertEquals("Health", api.getGroup());
        assertArrayEquals(new String[]{"/api/health/**"}, api.getPathsToMatch().toArray());
    }

    @Test
    void testAnalyticsApis() {
        GroupedOpenApi api = openAPIConfig.analyticsApis();
        
        assertNotNull(api);
        assertEquals("Analytics", api.getGroup());
        assertArrayEquals(new String[]{"/api/v1/analytics/**"}, api.getPathsToMatch().toArray());
    }
} 