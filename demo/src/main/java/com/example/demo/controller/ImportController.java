package com.example.demo.controller;

import com.example.demo.service.ImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/import")
public class ImportController {
    private static final Logger logger = LoggerFactory.getLogger(ImportController.class);
    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/users")
    public ResponseEntity<List<String>> importUsers(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            logger.warn("Empty file uploaded for user import");
            return ResponseEntity.badRequest().body(List.of("Please select a file to upload"));
        }

        try {
            logger.info("Processing user import from file: {}", file.getOriginalFilename());
            List<String> results = importService.importUsersFromCsv(file);
            
            if (results.isEmpty()) {
                logger.info("No users imported from file: {}", file.getOriginalFilename());
                return ResponseEntity.ok(List.of("No users were imported"));
            }
            
            logger.info("Successfully processed import with {} results", results.size());
            return ResponseEntity.ok(results);
        } catch (IOException e) {
            logger.error("Failed to process import file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(List.of("Failed to process file: " + e.getMessage()));
        }
    }
} 