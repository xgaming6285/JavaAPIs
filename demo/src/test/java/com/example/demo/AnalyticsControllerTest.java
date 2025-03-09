package com.example.demo;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserActivityService userActivityService;

    private Map<String, Object> mockStats;
    private Map<String, Object> mockTrends;

    @BeforeEach
    void setUp() {
        mockStats = new HashMap<>();
        mockStats.put("totalUsers", 100L);
        mockStats.put("activeUsers", 75L);
        mockStats.put("inactiveUsers", 25L);

        mockTrends = new HashMap<>();
        mockTrends.put("dailyActiveUsers", 50L);
        mockTrends.put("weeklyActiveUsers", 80L);
    }

    @Test
    void getUserStats_ShouldReturnStats() throws Exception {
        when(userService.getTotalUsers()).thenReturn(100L);
        when(userService.getActiveUsers()).thenReturn(java.util.Collections.nCopies(75, new User()));
        when(userService.getInactiveUsers()).thenReturn(java.util.Collections.nCopies(25, new User()));
        when(userService.getUserCountByRole()).thenReturn(Map.of("ADMIN", 10L, "USER", 90L));
        when(userService.getAverageRolesPerUser()).thenReturn(1.5);

        mockMvc.perform(get("/api/v1/analytics/user-stats"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.totalUsers").value(100))
               .andExpect(jsonPath("$.activeUsers").value(75))
               .andExpect(jsonPath("$.inactiveUsers").value(25));
    }

    @Test
    void getActivityTrends_WithValidDays_ShouldReturnTrends() throws Exception {
        when(userActivityService.getActivityTrendsSince(any(LocalDateTime.class)))
            .thenReturn(mockTrends);

        mockMvc.perform(get("/api/v1/analytics/activity-trends")
               .param("days", "7"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.dailyActiveUsers").value(50))
               .andExpect(jsonPath("$.weeklyActiveUsers").value(80));
    }

    @Test
    void getActivityTrends_WithInvalidDays_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/activity-trends")
               .param("days", "366"))
               .andExpect(status().isBadRequest());
    }

    @Test
    void getRoleDistribution_ShouldReturnDistribution() throws Exception {
        Map<String, Object> distribution = new HashMap<>();
        distribution.put("roleDistribution", Map.of("ADMIN", 20L, "USER", 80L));
        distribution.put("commonRoleCombinations", Arrays.asList(
            Map.of("roles", "ADMIN+USER", "count", 10L)
        ));
        distribution.put("averageRolesPerUser", 1.2);

        when(userService.getRoleDistribution()).thenReturn(Map.of("ADMIN", 20L, "USER", 80L));
        when(userService.getCommonRoleCombinations()).thenReturn(Arrays.asList(
            Map.of("roles", "ADMIN+USER", "count", 10L)
        ));
        when(userService.getAverageRolesPerUser()).thenReturn(1.2);

        mockMvc.perform(get("/api/v1/analytics/role-distribution"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.roleDistribution.ADMIN").value(20))
               .andExpect(jsonPath("$.roleDistribution.USER").value(80));
    }

    @Test
    void getUserGrowth_WithValidDays_ShouldReturnGrowthMetrics() throws Exception {
        Map<String, Object> growthMetrics = new HashMap<>();
        growthMetrics.put("totalGrowth", 25);
        growthMetrics.put("monthlyGrowthRate", 5.5);

        when(userActivityService.getUserGrowthMetrics(any(LocalDateTime.class)))
            .thenReturn(growthMetrics);

        mockMvc.perform(get("/api/v1/analytics/user-growth")
               .param("days", "30"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.totalGrowth").value(25))
               .andExpect(jsonPath("$.monthlyGrowthRate").value(5.5));
    }

    @Test
    void getSecurityMetrics_ShouldReturnMetrics() throws Exception {
        Map<String, Object> securityMetrics = new HashMap<>();
        securityMetrics.put("failedLogins", 50);
        securityMetrics.put("passwordResets", 10);

        when(userActivityService.getSecurityMetrics()).thenReturn(securityMetrics);

        mockMvc.perform(get("/api/v1/analytics/security-metrics"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.failedLogins").value(50))
               .andExpect(jsonPath("$.passwordResets").value(10));
    }

    @Test
    void getUserRetention_WithValidDays_ShouldReturnRetentionMetrics() throws Exception {
        Map<String, Object> retentionMetrics = new HashMap<>();
        retentionMetrics.put("retentionRate", 85.5);
        retentionMetrics.put("churnRate", 14.5);

        when(userActivityService.getUserRetentionMetrics(any(LocalDateTime.class)))
            .thenReturn(retentionMetrics);

        mockMvc.perform(get("/api/v1/analytics/user-retention")
               .param("days", "30"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.retentionRate").value(85.5))
               .andExpect(jsonPath("$.churnRate").value(14.5));
    }

    @Test
    void getUserBehavior_WithValidDays_ShouldReturnBehaviorAnalysis() throws Exception {
        Map<String, Object> behaviorAnalysis = new HashMap<>();
        behaviorAnalysis.put("averageSessionDuration", 45);
        behaviorAnalysis.put("mostActiveHours", "14:00-16:00");

        when(userActivityService.getUserBehaviorAnalysis(any(LocalDateTime.class)))
            .thenReturn(behaviorAnalysis);

        mockMvc.perform(get("/api/v1/analytics/user-behavior")
               .param("days", "7"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.averageSessionDuration").value(45))
               .andExpect(jsonPath("$.mostActiveHours").value("14:00-16:00"));
    }
} 