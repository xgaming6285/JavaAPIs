package com.example.demo.service;

import com.example.demo.User;
import com.example.demo.UserService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class ImportService {
    private static final Logger logger = LoggerFactory.getLogger(ImportService.class);
    private static final List<String> REQUIRED_HEADERS = Arrays.asList("username", "email", "password", "active", "roles");
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public ImportService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<String> importUsersFromCsv(MultipartFile file) throws IOException {
        List<String> results = new ArrayList<>();
        
        if (file == null || file.isEmpty()) {
            logger.warn("Empty file provided for import");
            return results;
        }

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        if (content.trim().equals("username,email,password,active,roles")) {
            logger.info("File contains only headers, no data to import");
            return results;
        }

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.Builder.create()
                     .setHeader()
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build())) {

            if (!csvParser.getHeaderMap().keySet().containsAll(REQUIRED_HEADERS)) {
                logger.error("Invalid CSV structure. Required headers: {}", REQUIRED_HEADERS);
                return results;
            }

            for (CSVRecord record : csvParser) {
                try {
                    if (isEmptyRecord(record, csvParser.getHeaderMap().size())) {
                        continue;
                    }

                    User user = createUserFromRecord(record);
                    
                    try {
                        userService.createUser(user);
                        results.add("Successfully imported user: " + user.getUsername());
                        logger.info("Successfully imported user: {}", user.getUsername());
                    } catch (Exception e) {
                        String errorMessage = "Failed to import user: " + e.getMessage();
                        results.add(errorMessage);
                        logger.error(errorMessage, e);
                    }
                } catch (Exception e) {
                    logger.warn("Skipping record due to processing error: {}", e.getMessage());
                    continue;
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing CSV file: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
        
        return results;
    }
    
    private boolean isEmptyRecord(CSVRecord record, int expectedSize) {
        return record.size() != expectedSize || 
               record.get("username").trim().isEmpty() || 
               record.get("email").trim().isEmpty();
    }
    
    private User createUserFromRecord(CSVRecord record) {
        User user = new User(
                record.get("username").trim(),
                record.get("email").trim(),
                passwordEncoder.encode(record.get("password").trim())
        );
        
        String activeStr = record.get("active").trim();
        user.setActive(!activeStr.isEmpty() && Boolean.parseBoolean(activeStr));
        
        String rolesStr = record.get("roles").trim();
        Set<String> roles = parseRoles(rolesStr);
        if (roles.isEmpty()) {
            roles.add("ROLE_USER");
        }
        user.setRoles(roles);
        
        return user;
    }
    
    private Set<String> parseRoles(String rolesStr) {
        if (rolesStr.isEmpty()) {
            return new HashSet<>();
        }
        
        return Arrays.stream(rolesStr.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .collect(Collectors.toSet());
    }
} 