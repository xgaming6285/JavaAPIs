package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserActivityServiceTest {

    @Mock
    private UserService userService;

    private UserActivityService userActivityService;
    private LocalDateTime startDate;

    @BeforeEach
    void setUp() {
        userActivityService = new UserActivityService(userService);
        startDate = LocalDateTime.now().minusDays(7);
    }

    @Test
    void testConstructorWithNullUserService() {
        assertThrows(NullPointerException.class, () -> new UserActivityService(null));
    }

    @Test
    void testGetActivityTrendsSince() {
        Map<String, Object> trends = userActivityService.getActivityTrendsSince(startDate);
        
        assertNotNull(trends);
        assertTrue(trends.containsKey("dailyActiveUsers"));
        assertTrue(trends.containsKey("peakActivityHours"));
        assertTrue(trends.containsKey("averageSessionDuration"));
    }

    @Test
    void testGetUserGrowthMetrics() {
        Map<String, Object> metrics = userActivityService.getUserGrowthMetrics(startDate);
        
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("newUsers"));
        assertTrue(metrics.containsKey("userRetentionRate"));
        assertTrue(metrics.containsKey("churnRate"));
    }

    @Test
    void testGetSecurityMetrics() {
        Map<String, Object> metrics = userActivityService.getSecurityMetrics();
        
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("failedLoginAttempts"));
        assertTrue(metrics.containsKey("suspiciousActivities"));
        assertTrue(metrics.containsKey("accountLockouts"));
    }

    @Test
    void testRecordLoginAttempt() {
        String username = "testUser";
        
        // Test failed login
        userActivityService.recordLoginAttempt(username, false);
        Map<String, Object> metrics = userActivityService.getSecurityMetrics();
        @SuppressWarnings("unchecked")
        Map<String, Integer> failedAttempts = (Map<String, Integer>) metrics.get("failedLoginAttempts");
        assertEquals(1, failedAttempts.get(username));
        
        // Test successful login clearing attempts
        userActivityService.recordLoginAttempt(username, true);
        metrics = userActivityService.getSecurityMetrics();
        @SuppressWarnings("unchecked")
        Map<String, Integer> failedAttempts2 = (Map<String, Integer>) metrics.get("failedLoginAttempts");
        assertNull(failedAttempts2.get(username));
    }

    @Test
    void testRecordUserActivity() {
        Long userId = 1L;
        String activity = "LOGIN";
        
        userActivityService.recordUserActivity(userId, activity);
        
        Map<String, Object> analysis = userActivityService.getUserBehaviorAnalysis(startDate);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> mostActiveUsers = (List<Map<String, Object>>) analysis.get("mostActiveUsers");
        
        assertTrue(mostActiveUsers.stream()
            .anyMatch(user -> user.get("userId").equals(userId)));
    }

    @Test
    void testGetUserRetentionMetrics() {
        when(userService.getAllUsers()).thenReturn(new ArrayList<>());
        
        Map<String, Object> metrics = userActivityService.getUserRetentionMetrics(startDate);
        
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("dailyRetention"));
        assertTrue(metrics.containsKey("weeklyRetention"));
        assertTrue(metrics.containsKey("monthlyRetention"));
    }

    @Test
    void testGetUserBehaviorAnalysis() {
        Map<String, Object> analysis = userActivityService.getUserBehaviorAnalysis(startDate);
        
        assertNotNull(analysis);
        assertTrue(analysis.containsKey("mostActiveUsers"));
        assertTrue(analysis.containsKey("commonUserPaths"));
        assertTrue(analysis.containsKey("featureUsage"));
    }

    @Test
    void testCalculateRetentionRate() {
        List<User> users = new ArrayList<>();
        users.add(new User("user1", "user1@example.com", "pass1"));
        when(userService.getAllUsers()).thenReturn(users);
        
        Map<String, Object> metrics = userActivityService.getUserGrowthMetrics(startDate);
        double retentionRate = (double) metrics.get("userRetentionRate");
        
        assertTrue(retentionRate >= 0 && retentionRate <= 100);
    }

    @Test
    void testCalculateChurnRate() {
        List<User> users = new ArrayList<>();
        users.add(new User("user1", "user1@example.com", "pass1"));
        when(userService.getAllUsers()).thenReturn(users);
        
        Map<String, Object> metrics = userActivityService.getUserGrowthMetrics(startDate);
        double churnRate = (double) metrics.get("churnRate");
        
        assertTrue(churnRate >= 0 && churnRate <= 100);
    }

    @Test
    void testGetFeatureUsageStats() {
        Long userId = 1L;
        userActivityService.recordUserActivity(userId, "LOGIN");
        userActivityService.recordUserActivity(userId, "VIEW_PROFILE");
        userActivityService.recordUserActivity(userId, "LOGIN");
        
        Map<String, Object> analysis = userActivityService.getUserBehaviorAnalysis(startDate);
        @SuppressWarnings("unchecked")
        Map<String, Long> featureUsage = (Map<String, Long>) analysis.get("featureUsage");
        
        assertEquals(2L, featureUsage.get("LOGIN"));
        assertEquals(1L, featureUsage.get("VIEW_PROFILE"));
    }

    @Test
    void testCleanupOldData() {
        Long userId = 1L;
        userActivityService.recordUserActivity(userId, "OLD_ACTIVITY");
        
        // Simulate old activity by directly modifying the lastLoginTimes map
        try {
            var lastLoginTimesField = UserActivityService.class.getDeclaredField("lastLoginTimes");
            lastLoginTimesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Long, LocalDateTime> lastLoginTimes = (Map<Long, LocalDateTime>) lastLoginTimesField.get(userActivityService);
            lastLoginTimes.put(userId, LocalDateTime.now().minusMonths(2));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set up test: " + e.getMessage());
        }
        
        userActivityService.cleanupOldData();
        
        Map<String, Object> analysis = userActivityService.getUserBehaviorAnalysis(startDate);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> mostActiveUsers = (List<Map<String, Object>>) analysis.get("mostActiveUsers");
        
        assertTrue(mostActiveUsers.isEmpty() || 
                  !mostActiveUsers.stream().anyMatch(user -> user.get("userId").equals(userId)));
    }
} 