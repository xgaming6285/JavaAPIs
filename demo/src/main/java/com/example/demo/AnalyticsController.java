package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.micrometer.core.annotation.Timed;
import org.springframework.validation.annotation.Validated;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "User Analytics", description = "Advanced analytics endpoints for user data")
@Validated
public class AnalyticsController {

    private final UserService userService;
    private final UserActivityService userActivityService;

    public AnalyticsController(UserService userService, UserActivityService userActivityService) {
        this.userService = userService;
        this.userActivityService = userActivityService;
    }

    @Timed(value = "api.analytics.userStats")
    @Operation(summary = "Get user statistics")
    @GetMapping("/user-stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = Map.of(
            "totalUsers", userService.getTotalUsers(),
            "activeUsers", userService.getActiveUsers().size(),
            "inactiveUsers", userService.getInactiveUsers().size(),
            "usersByRole", userService.getUserCountByRole(),
            "averageRolesPerUser", userService.getAverageRolesPerUser()
        );
        return ResponseEntity.ok(stats);
    }

    @Timed(value = "api.analytics.activityTrends")
    @Operation(summary = "Get user activity trends")
    @GetMapping("/activity-trends")
    public ResponseEntity<Map<String, Object>> getActivityTrends(
            @RequestParam(defaultValue = "7") int days) {
        LocalDateTime startDate = LocalDateTime.now().minus(days, ChronoUnit.DAYS);
        Map<String, Object> trends = userActivityService.getActivityTrendsSince(startDate);
        return ResponseEntity.ok(trends);
    }

    @Timed(value = "api.analytics.roleDistribution")
    @Operation(summary = "Get role distribution analysis")
    @GetMapping("/role-distribution")
    public ResponseEntity<Map<String, Object>> getRoleDistribution() {
        Map<String, Object> distribution = Map.of(
            "roleDistribution", userService.getRoleDistribution(),
            "commonRoleCombinations", userService.getCommonRoleCombinations(),
            "averageRolesPerUser", userService.getAverageRolesPerUser()
        );
        return ResponseEntity.ok(distribution);
    }

    @Timed(value = "api.analytics.userGrowth")
    @Operation(summary = "Get user growth metrics")
    @GetMapping("/user-growth")
    public ResponseEntity<Map<String, Object>> getUserGrowth(
            @RequestParam(defaultValue = "30") int days) {
        LocalDateTime startDate = LocalDateTime.now().minus(days, ChronoUnit.DAYS);
        Map<String, Object> growth = userActivityService.getUserGrowthMetrics(startDate);
        return ResponseEntity.ok(growth);
    }

    @Timed(value = "api.analytics.securityMetrics")
    @Operation(summary = "Get security metrics")
    @GetMapping("/security-metrics")
    public ResponseEntity<Map<String, Object>> getSecurityMetrics() {
        Map<String, Object> metrics = userActivityService.getSecurityMetrics();
        return ResponseEntity.ok(metrics);
    }

    @Timed(value = "api.analytics.userRetention")
    @Operation(summary = "Get user retention metrics")
    @GetMapping("/user-retention")
    public ResponseEntity<Map<String, Object>> getUserRetention(
            @RequestParam(defaultValue = "30") int days) {
        LocalDateTime startDate = LocalDateTime.now().minus(days, ChronoUnit.DAYS);
        Map<String, Object> retention = userActivityService.getUserRetentionMetrics(startDate);
        return ResponseEntity.ok(retention);
    }

    @Timed(value = "api.analytics.userBehavior")
    @Operation(summary = "Get user behavior analysis")
    @GetMapping("/user-behavior")
    public ResponseEntity<Map<String, Object>> getUserBehavior(
            @RequestParam(defaultValue = "7") int days) {
        LocalDateTime startDate = LocalDateTime.now().minus(days, ChronoUnit.DAYS);
        Map<String, Object> behavior = userActivityService.getUserBehaviorAnalysis(startDate);
        return ResponseEntity.ok(behavior);
    }
} 