package com.example.demo.service;

import com.example.demo.User;
import com.example.demo.UserService;
import com.example.demo.config.TestContainersConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestContainersConfig.class)
class ImportServiceIntegrationTest {

    @Autowired
    private ImportService importService;

    @MockBean
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testImportValidUsersFromCsv(@TempDir Path tempDir) throws IOException {
        // Given
        String csvContent = "username,email,password,active,roles\n" +
                          "john_doe,john@example.com,password123,true,ROLE_USER\n" +
                          "jane_doe,jane@example.com,password456,true,\"ROLE_USER,ROLE_ADMIN\"";
        
        Path csvFile = tempDir.resolve("users.csv");
        Files.writeString(csvFile, csvContent);
        
        MockMultipartFile file = new MockMultipartFile(
            "users.csv",
            "users.csv",
            "text/csv",
            Files.readAllBytes(csvFile)
        );

        when(userService.createUser(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // When
        List<String> results = importService.importUsersFromCsv(file);

        // Then
        assertEquals(2, results.size());
        assertTrue(results.get(0).contains("Successfully imported user: john_doe"));
        assertTrue(results.get(1).contains("Successfully imported user: jane_doe"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(2)).createUser(userCaptor.capture());

        List<User> capturedUsers = userCaptor.getAllValues();
        
        // Verify first user
        User user1 = capturedUsers.get(0);
        assertEquals("john_doe", user1.getUsername());
        assertEquals("john@example.com", user1.getEmail());
        assertTrue(passwordEncoder.matches("password123", user1.getPassword()));
        assertTrue(user1.isActive());
        assertEquals(Set.of("ROLE_USER"), user1.getRoles());

        // Verify second user
        User user2 = capturedUsers.get(1);
        assertEquals("jane_doe", user2.getUsername());
        assertEquals("jane@example.com", user2.getEmail());
        assertTrue(passwordEncoder.matches("password456", user2.getPassword()));
        assertTrue(user2.isActive());
        assertEquals(Set.of("ROLE_USER", "ROLE_ADMIN"), user2.getRoles());
    }

    @Test
    void testImportInvalidUserFromCsv(@TempDir Path tempDir) throws IOException {
        // Given
        String csvContent = "username,email,password,active,roles\n" +
                          "john_doe,invalid_email,password123,true,ROLE_USER\n" +
                          "jane_doe,jane@example.com,password456,true,ROLE_USER";
        
        Path csvFile = tempDir.resolve("users.csv");
        Files.writeString(csvFile, csvContent);
        
        MockMultipartFile file = new MockMultipartFile(
            "users.csv",
            "users.csv",
            "text/csv",
            Files.readAllBytes(csvFile)
        );

        when(userService.createUser(any(User.class)))
            .thenThrow(new IllegalArgumentException("Invalid email format"))
            .thenAnswer(i -> i.getArgument(0));

        // When
        List<String> results = importService.importUsersFromCsv(file);

        // Then
        assertEquals(2, results.size());
        assertTrue(results.get(0).contains("Failed to import user"));
        assertTrue(results.get(1).contains("Successfully imported user: jane_doe"));

        verify(userService, times(2)).createUser(any(User.class));
    }

    @Test
    void testImportEmptyCsv() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "users.csv",
            "users.csv",
            "text/csv",
            "username,email,password,active,roles\n".getBytes()
        );

        // When
        List<String> results = importService.importUsersFromCsv(file);

        // Then
        assertTrue(results.isEmpty());
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void testImportMalformedCsv() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "users.csv",
            "users.csv",
            "text/csv",
            "malformed,csv,content".getBytes()
        );

        // When
        List<String> results = importService.importUsersFromCsv(file);

        // Then
        assertTrue(results.isEmpty());
        verify(userService, never()).createUser(any(User.class));
    }
} 