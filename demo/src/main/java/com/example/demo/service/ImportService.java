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
        
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.Builder.create()
                     .setHeader("username", "email", "password", "active", "roles")
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build())) {

            for (CSVRecord record : csvParser) {
                try {
                    User user = new User(
                            record.get("username"),
                            record.get("email"),
                            passwordEncoder.encode(record.get("password"))
                    );
                    
                    // Set active status
                    user.setActive(Boolean.parseBoolean(record.get("active")));
                    
                    // Handle roles
                    String rolesStr = record.get("roles");
                    Set<String> roles = new HashSet<>();
                    if (rolesStr != null && !rolesStr.trim().isEmpty()) {
                        for (String role : rolesStr.split(",")) {
                            roles.add(role.trim());
                        }
                    }
                    user.setRoles(roles);
                    
                    userService.createUser(user);
                    results.add("Successfully imported user: " + user.getUsername());
                } catch (Exception e) {
                    results.add("Failed to import user at row " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }
        }
        
        return results;
    }
} 