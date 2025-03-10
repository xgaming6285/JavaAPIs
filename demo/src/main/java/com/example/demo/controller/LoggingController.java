package com.example.demo.controller;

import com.example.demo.model.mongo.*;
import com.example.demo.service.MongoLoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class LoggingController {

    @Autowired
    private MongoLoggingService mongoLoggingService;

    @PostMapping("/activity")
    public ResponseEntity<UserActivity> logUserActivity(
            @RequestParam String userId,
            @RequestParam String action,
            @RequestParam String details,
            HttpServletRequest request) {
        
        UserActivity activity = mongoLoggingService.logUserActivity(
            userId,
            action,
            details,
            request.getRemoteAddr(),
            request.getHeader("User-Agent")
        );
        return ResponseEntity.ok(activity);
    }

    @PostMapping("/analytics")
    public ResponseEntity<AnalyticsData> logAnalyticsEvent(
            @RequestParam String eventType,
            @RequestParam String userId,
            @RequestBody Map<String, Object> metadata) {
        
        AnalyticsData data = mongoLoggingService.logAnalyticsEvent(eventType, userId, metadata);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/sessions")
    public ResponseEntity<UserSession> createSession(
            @RequestParam String userId,
            @RequestParam String sessionToken,
            HttpServletRequest request) {
        
        UserSession session = mongoLoggingService.createSession(
            userId,
            sessionToken,
            request.getRemoteAddr(),
            request.getHeader("User-Agent")
        );
        return ResponseEntity.ok(session);
    }

    @PutMapping("/sessions/{sessionToken}")
    public ResponseEntity<Void> updateSession(@PathVariable String sessionToken) {
        mongoLoggingService.updateSessionActivity(sessionToken);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/sessions/{sessionToken}")
    public ResponseEntity<Void> invalidateSession(@PathVariable String sessionToken) {
        mongoLoggingService.invalidateSession(sessionToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/audit")
    public ResponseEntity<AuditLog> createAuditLog(
            @RequestParam String userId,
            @RequestParam String action,
            @RequestParam String resourceType,
            @RequestParam String resourceId,
            @RequestBody Map<String, Object> changes,
            HttpServletRequest request) {
        
        AuditLog log = mongoLoggingService.createAuditLog(
            userId,
            action,
            resourceType,
            resourceId,
            changes,
            request.getRemoteAddr(),
            "SUCCESS",
            "Action performed successfully"
        );
        return ResponseEntity.ok(log);
    }

    @GetMapping("/activity/user/{userId}")
    public ResponseEntity<List<UserActivity>> getUserActivities(@PathVariable String userId) {
        return ResponseEntity.ok(mongoLoggingService.getUserActivities(userId));
    }

    @GetMapping("/analytics/type/{eventType}")
    public ResponseEntity<List<AnalyticsData>> getAnalyticsByType(@PathVariable String eventType) {
        return ResponseEntity.ok(mongoLoggingService.getAnalyticsByEventType(eventType));
    }

    @GetMapping("/audit/user/{userId}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(mongoLoggingService.getAuditLogsByUser(userId));
    }
} 