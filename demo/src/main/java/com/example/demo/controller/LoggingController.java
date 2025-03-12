package com.example.demo.controller;

import com.example.demo.model.mongo.*;
import com.example.demo.service.MongoLoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@Validated
public class LoggingController {

    private static final Logger logger = LoggerFactory.getLogger(LoggingController.class);
    private final MongoLoggingService mongoLoggingService;

    public LoggingController(MongoLoggingService mongoLoggingService) {
        this.mongoLoggingService = mongoLoggingService;
    }

    /**
     * Logs user activity.
     *
     * @param userId User identifier
     * @param action Action performed
     * @param details Additional details about the action
     * @param request HTTP request for IP and user agent information
     * @return ResponseEntity containing the created UserActivity
     */
    @PostMapping("/activity")
    public ResponseEntity<UserActivity> logUserActivity(
            @NotBlank @RequestParam String userId,
            @NotBlank @RequestParam String action,
            @NotBlank @RequestParam String details,
            HttpServletRequest request) {
        logger.debug("Logging user activity for user: {}, action: {}", userId, action);
        
        UserActivity activity = mongoLoggingService.logUserActivity(
            userId,
            action,
            details,
            request.getRemoteAddr(),
            request.getHeader("User-Agent")
        );
        
        logger.info("User activity logged successfully for user: {}", userId);
        return ResponseEntity.ok(activity);
    }

    /**
     * Logs analytics events.
     *
     * @param eventType Type of the analytics event
     * @param userId User identifier
     * @param metadata Additional metadata for the event
     * @return ResponseEntity containing the created AnalyticsData
     */
    @PostMapping("/analytics")
    public ResponseEntity<AnalyticsData> logAnalyticsEvent(
            @NotBlank @RequestParam String eventType,
            @NotBlank @RequestParam String userId,
            @Valid @RequestBody Map<String, Object> metadata) {
        logger.debug("Logging analytics event: {} for user: {}", eventType, userId);
        
        AnalyticsData data = mongoLoggingService.logAnalyticsEvent(eventType, userId, metadata);
        
        logger.info("Analytics event logged successfully: {}", eventType);
        return ResponseEntity.ok(data);
    }

    /**
     * Creates a new user session.
     *
     * @param userId User identifier
     * @param sessionToken Unique session token
     * @param request HTTP request for IP and user agent information
     * @return ResponseEntity containing the created UserSession
     */
    @PostMapping("/sessions")
    public ResponseEntity<UserSession> createSession(
            @NotBlank @RequestParam String userId,
            @NotBlank @RequestParam String sessionToken,
            HttpServletRequest request) {
        logger.debug("Creating session for user: {}", userId);
        
        UserSession session = mongoLoggingService.createSession(
            userId,
            sessionToken,
            request.getRemoteAddr(),
            request.getHeader("User-Agent")
        );
        
        logger.info("Session created successfully for user: {}", userId);
        return ResponseEntity.ok(session);
    }

    /**
     * Updates the last access time of a session.
     *
     * @param sessionToken Session identifier to update
     * @return ResponseEntity with no content
     */
    @PutMapping("/sessions/{sessionToken}")
    public ResponseEntity<Void> updateSession(
            @NotBlank @PathVariable String sessionToken) {
        logger.debug("Updating session: {}", sessionToken);
        mongoLoggingService.updateSessionActivity(sessionToken);
        logger.info("Session updated successfully: {}", sessionToken);
        return ResponseEntity.ok().build();
    }

    /**
     * Invalidates a user session.
     *
     * @param sessionToken Session identifier to invalidate
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/sessions/{sessionToken}")
    public ResponseEntity<Void> invalidateSession(
            @NotBlank @PathVariable String sessionToken) {
        logger.debug("Invalidating session: {}", sessionToken);
        mongoLoggingService.invalidateSession(sessionToken);
        logger.info("Session invalidated successfully: {}", sessionToken);
        return ResponseEntity.ok().build();
    }

    /**
     * Creates an audit log entry.
     *
     * @param userId User identifier
     * @param action Action performed
     * @param resourceType Type of resource being audited
     * @param resourceId Identifier of the resource
     * @param changes Changes made to the resource
     * @param request HTTP request for IP information
     * @return ResponseEntity containing the created AuditLog
     */
    @PostMapping("/audit")
    public ResponseEntity<AuditLog> createAuditLog(
            @NotBlank @RequestParam String userId,
            @NotBlank @RequestParam String action,
            @NotBlank @RequestParam String resourceType,
            @NotBlank @RequestParam String resourceId,
            @Valid @RequestBody Map<String, Object> changes,
            HttpServletRequest request) {
        logger.debug("Creating audit log for user: {}, action: {}", userId, action);
        
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
        
        logger.info("Audit log created successfully for user: {}", userId);
        return ResponseEntity.ok(log);
    }

    /**
     * Retrieves user activities for a specific user.
     *
     * @param userId User identifier
     * @return ResponseEntity containing a list of UserActivity
     */
    @GetMapping("/activity/user/{userId}")
    public ResponseEntity<List<UserActivity>> getUserActivities(
            @NotBlank @PathVariable String userId) {
        logger.debug("Retrieving activities for user: {}", userId);
        return ResponseEntity.ok(mongoLoggingService.getUserActivities(userId));
    }

    /**
     * Retrieves analytics data for a specific event type.
     *
     * @param eventType Type of analytics event
     * @return ResponseEntity containing a list of AnalyticsData
     */
    @GetMapping("/analytics/type/{eventType}")
    public ResponseEntity<List<AnalyticsData>> getAnalyticsByType(
            @NotBlank @PathVariable String eventType) {
        logger.debug("Retrieving analytics for event type: {}", eventType);
        return ResponseEntity.ok(mongoLoggingService.getAnalyticsByEventType(eventType));
    }

    /**
     * Retrieves audit logs for a specific user.
     *
     * @param userId User identifier
     * @return ResponseEntity containing a list of AuditLog
     */
    @GetMapping("/audit/user/{userId}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByUser(
            @NotBlank @PathVariable String userId) {
        logger.debug("Retrieving audit logs for user: {}", userId);
        return ResponseEntity.ok(mongoLoggingService.getAuditLogsByUser(userId));
    }
} 