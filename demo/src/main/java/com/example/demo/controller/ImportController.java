package com.example.demo.controller;

import com.example.demo.service.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ImportService importService;

    @PostMapping("/users")
    public ResponseEntity<List<String>> importUsers(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(List.of("Please select a file to upload"));
        }

        try {
            List<String> results = importService.importUsersFromCsv(file);
            return ResponseEntity.ok(results);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(List.of("Failed to process file: " + e.getMessage()));
        }
    }
} 