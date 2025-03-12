package com.example.demo.service;

import com.example.demo.model.mongo.*;
import com.example.demo.repository.mongo.*;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MongoLoggingService {
    private final UserActivityRepository userActivityRepository;
    private final AnalyticsDataRepository analyticsDataRepository;
    private final UserSessionRepository userSessionRepository;
    private final AuditLogRepository auditLogRepository;

    public MongoLoggingService(UserActivityRepository userActivityRepository,
                              AnalyticsDataRepository analyticsDataRepository,
                              UserSessionRepository userSessionRepository,
                              AuditLogRepository auditLogRepository) {
        this.userActivityRepository = userActivityRepository;
        this.analyticsDataRepository = analyticsDataRepository;
        this.userSessionRepository = userSessionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public UserActivity logUserActivity(String userId, String action, String details, String ipAddress, String userAgent) {
        UserActivity activity = new UserActivity(userId, action, details, ipAddress, userAgent);
        return userActivityRepository.save(activity);
    }

    public List<UserActivity> getUserActivities(String userId) {
        return userActivityRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public AnalyticsData logAnalyticsEvent(String eventType, String userId, Map<String, Object> metadata) {
        AnalyticsData analyticsData = new AnalyticsData(eventType, userId, metadata);
        return analyticsDataRepository.save(analyticsData);
    }

    public List<AnalyticsData> getAnalyticsByEventType(String eventType) {
        return analyticsDataRepository.findByEventType(eventType);
    }

    public UserSession createSession(String userId, String sessionToken, String ipAddress, String userAgent) {
        UserSession session = new UserSession(userId, sessionToken, ipAddress, userAgent);
        return userSessionRepository.save(session);
    }

    public Optional<UserSession> getActiveSession(String sessionToken) {
        return userSessionRepository.findBySessionTokenAndActive(sessionToken, true);
    }

    public void updateSessionActivity(String sessionToken) {
        userSessionRepository.findBySessionToken(sessionToken)
            .ifPresent(session -> {
                session.setLastAccessTime(Instant.now());
                userSessionRepository.save(session);
            });
    }

    public void invalidateSession(String sessionToken) {
        userSessionRepository.findBySessionToken(sessionToken)
            .ifPresent(session -> {
                session.setActive(false);
                userSessionRepository.save(session);
            });
    }

    public AuditLog createAuditLog(String userId, String action, String resourceType, String resourceId,
                                 Map<String, Object> changes, String ipAddress, String status, String details) {
        AuditLog auditLog = new AuditLog(userId, action, resourceType, resourceId, changes, ipAddress, status, details);
        return auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAuditLogsByUser(String userId) {
        return auditLogRepository.findByUserId(userId);
    }

    public List<AuditLog> getAuditLogsByResource(String resourceType, String resourceId) {
        return auditLogRepository.findByResourceTypeAndResourceId(resourceType, resourceId);
    }

    public List<UserActivity> getActivitiesBetween(Instant start, Instant end) {
        return userActivityRepository.findByTimestampBetween(start, end);
    }

    public List<AnalyticsData> getAnalyticsBetween(Instant start, Instant end) {
        return analyticsDataRepository.findByTimestampBetween(start, end);
    }

    public List<AuditLog> getAuditLogsBetween(Instant start, Instant end) {
        return auditLogRepository.findByTimestampBetween(start, end);
    }
} 