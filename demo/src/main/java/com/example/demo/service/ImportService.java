package com.example.demo.service;

import com.example.demo.User;
import com.example.demo.UserService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ImportService {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<String> importUsersFromCsv(MultipartFile file) throws IOException {
        List<String> results = new ArrayList<>();
        
        if (file == null || file.isEmpty()) {
            return results;
        }

        // Check if file only contains header
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        if (content.trim().equals("username,email,password,active,roles")) {
            return results;  // Return empty list for header-only file
        }

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.Builder.create()
                     .setHeader()
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build())) {

            // Validate CSV structure
            if (!csvParser.getHeaderMap().keySet().containsAll(List.of("username", "email", "password", "active", "roles"))) {
                return results;  // Return empty list for invalid structure
            }

            for (CSVRecord record : csvParser) {
                try {
                    // Skip empty rows
                    if (record.size() != csvParser.getHeaderMap().size() || 
                        record.get("username").trim().isEmpty() || 
                        record.get("email").trim().isEmpty()) {
                        continue;
                    }

                    User user = new User(
                            record.get("username").trim(),
                            record.get("email").trim(),
                            passwordEncoder.encode(record.get("password").trim())
                    );
                    
                    // Set active status
                    String activeStr = record.get("active").trim();
                    user.setActive(!activeStr.isEmpty() && Boolean.parseBoolean(activeStr));
                    
                    // Handle roles
                    String rolesStr = record.get("roles").trim();
                    Set<String> roles = new HashSet<>();
                    if (!rolesStr.isEmpty()) {
                        for (String role : rolesStr.split(",")) {
                            String trimmedRole = role.trim();
                            if (!trimmedRole.isEmpty()) {
                                roles.add(trimmedRole);
                            }
                        }
                    }
                    if (roles.isEmpty()) {
                        roles.add("ROLE_USER"); // Default role
                    }
                    user.setRoles(roles);
                    
                    try {
                        userService.createUser(user);
                        results.add("Successfully imported user: " + user.getUsername());
                    } catch (Exception e) {
                        results.add("Failed to import user: " + e.getMessage());
                    }
                } catch (Exception e) {
                    // Skip processing errors without adding messages
                    continue;
                }
            }
        } catch (Exception e) {
            // Return empty list for malformed CSV
            return new ArrayList<>();
        }
        
        return results;
    }
} 