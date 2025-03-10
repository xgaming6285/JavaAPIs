package com.example.demo.service;

import com.example.demo.config.TestContainersConfig;
import com.example.demo.model.mongo.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@Import(TestContainersConfig.class)
class MongoLoggingServiceIntegrationTest {

    @Autowired
    private MongoLoggingService mongoLoggingService;

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Test
    void testUserActivityLogging() {
        // Given
        String userId = "test-user";
        String action = "LOGIN";
        String details = "User logged in from web interface";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        // When
        UserActivity activity = mongoLoggingService.logUserActivity(userId, action, details, ipAddress, userAgent);

        // Then
        assertNotNull(activity);
        assertNotNull(activity.getId());
        assertEquals(userId, activity.getUserId());
        assertEquals(action, activity.getAction());

        // Verify retrieval
        List<UserActivity> activities = mongoLoggingService.getUserActivities(userId);
        assertFalse(activities.isEmpty());
        assertEquals(1, activities.size());
        assertEquals(activity.getId(), activities.get(0).getId());
    }

    @Test
    void testAnalyticsEventLogging() {
        // Given
        String eventType = "PAGE_VIEW";
        String userId = "test-user";
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("page", "/home");
        metadata.put("duration", 120);

        // When
        AnalyticsData analyticsData = mongoLoggingService.logAnalyticsEvent(eventType, userId, metadata);

        // Then
        assertNotNull(analyticsData);
        assertNotNull(analyticsData.getId());
        assertEquals(eventType, analyticsData.getEventType());
        assertEquals(userId, analyticsData.getUserId());
        assertEquals(metadata, analyticsData.getMetadata());

        // Verify retrieval
        List<AnalyticsData> events = mongoLoggingService.getAnalyticsByEventType(eventType);
        assertFalse(events.isEmpty());
        assertEquals(1, events.size());
        assertEquals(analyticsData.getId(), events.get(0).getId());
    }

    @Test
    void testAuditLogging() {
        // Given
        String userId = "test-user";
        String action = "UPDATE";
        String resourceType = "USER";
        String resourceId = "123";
        Map<String, Object> changes = new HashMap<>();
        changes.put("email", "new@example.com");
        String ipAddress = "192.168.1.1";
        String status = "SUCCESS";
        String details = "Email updated successfully";

        // When
        AuditLog auditLog = mongoLoggingService.createAuditLog(userId, action, resourceType, 
            resourceId, changes, ipAddress, status, details);

        // Then
        assertNotNull(auditLog);
        assertNotNull(auditLog.getId());
        assertEquals(userId, auditLog.getUserId());
        assertEquals(action, auditLog.getAction());
        assertEquals(resourceType, auditLog.getResourceType());
        assertEquals(resourceId, auditLog.getResourceId());

        // Verify retrieval
        List<AuditLog> logs = mongoLoggingService.getAuditLogsByUser(userId);
        assertFalse(logs.isEmpty());
        assertEquals(1, logs.size());
        assertEquals(auditLog.getId(), logs.get(0).getId());
    }

    @Test
    void testSessionManagement() {
        // Given
        String userId = "test-user";
        String sessionToken = "test-session-token";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        // When
        UserSession session = mongoLoggingService.createSession(userId, sessionToken, ipAddress, userAgent);

        // Then
        assertNotNull(session);
        assertNotNull(session.getId());
        assertEquals(userId, session.getUserId());
        assertEquals(sessionToken, session.getSessionToken());
        assertTrue(session.isActive());

        // Test session retrieval
        assertTrue(mongoLoggingService.getActiveSession(sessionToken).isPresent());

        // Test session update
        Instant beforeUpdate = session.getLastAccessTime();
        mongoLoggingService.updateSessionActivity(sessionToken);
        var updatedSession = mongoLoggingService.getActiveSession(sessionToken).get();
        assertTrue(updatedSession.getLastAccessTime().isAfter(beforeUpdate));

        // Test session invalidation
        mongoLoggingService.invalidateSession(sessionToken);
        assertTrue(mongoLoggingService.getActiveSession(sessionToken).isEmpty());
    }

    @Test
    void testTimeBasedQueries() throws InterruptedException {
        // Given
        Instant start = Instant.now();
        Thread.sleep(100); // Ensure some time passes
        
        // Create some test data
        mongoLoggingService.logUserActivity("user1", "LOGIN", "details", "ip", "agent");
        mongoLoggingService.logAnalyticsEvent("VIEW", "user1", new HashMap<>());
        mongoLoggingService.createAuditLog("user1", "CREATE", "USER", "123", 
            new HashMap<>(), "ip", "SUCCESS", "details");
        
        Thread.sleep(100); // Ensure some time passes
        Instant end = Instant.now();

        // When & Then
        List<UserActivity> activities = mongoLoggingService.getActivitiesBetween(start, end);
        List<AnalyticsData> analytics = mongoLoggingService.getAnalyticsBetween(start, end);
        List<AuditLog> auditLogs = mongoLoggingService.getAuditLogsBetween(start, end);

        assertTrue(activities.size() == 1, "Expected 1 activity");
        assertTrue(analytics.size() == 1, "Expected 1 analytics event");
        assertTrue(auditLogs.size() == 1, "Expected 1 audit log");
    }
} 