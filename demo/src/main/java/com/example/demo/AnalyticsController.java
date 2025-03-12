package com.example.demo;

import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

/**
 * Controller handling analytics endpoints for user data.
 * Provides various metrics and insights about user behavior, activity, and system usage.
 */
@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "User Analytics", description = "Advanced analytics endpoints for user data")
@Validated
public class AnalyticsController {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);
    private static final int MAX_DAYS = 365;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final UserService userService;
    private final UserActivityService userActivityService;
    private final Bucket rateLimiter;

    /**
     * Constructs an AnalyticsController with required services.
     *
     * @param userService Service for user-related operations
     * @param userActivityService Service for user activity tracking
     * @param rateLimiter Service for rate limiting
     */
    public AnalyticsController(UserService userService, UserActivityService userActivityService, Bucket rateLimiter) {
        this.userService = Objects.requireNonNull(userService, "UserService must not be null");
        this.userActivityService = Objects.requireNonNull(userActivityService, "UserActivityService must not be null");
        this.rateLimiter = Objects.requireNonNull(rateLimiter, "RateLimiter must not be null");
    }

    /**
     * Retrieves comprehensive user statistics.
     *
     * @return ResponseEntity containing user statistics
     */
    @Timed(value = "api.analytics.userStats", description = "Time taken to fetch user statistics")
    @Operation(
        summary = "Get user statistics",
        description = "Retrieves comprehensive user statistics including total, active, and inactive users"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user statistics"),
        @ApiResponse(responseCode = "500", description = "Internal server error while fetching statistics")
    })
    @GetMapping("/user-stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    @Cacheable(value = "userStats", key = "'stats'", condition = "#result != null")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        ConsumptionProbe probe = rateLimiter.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Fetching user statistics");
            }
            Map<String, Object> stats = Map.of(
                "totalUsers", userService.getTotalUsers(),
                "activeUsers", userService.getActiveUsers().size(),
                "inactiveUsers", userService.getInactiveUsers().size(),
                "usersByRole", userService.getUserCountByRole(),
                "averageRolesPerUser", userService.getAverageRolesPerUser()
            );
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully retrieved user statistics");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Content-Type-Options", "nosniff");
            headers.add("X-Frame-Options", "DENY");
            headers.add("X-XSS-Protection", "1; mode=block");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(stats);
        } catch (Exception e) {
            logger.error("Error fetching user statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An internal error occurred"));
        }
    }

    /**
     * Retrieves user activity trends for a specified time period.
     *
     * @param days Number of days to analyze
     * @return ResponseEntity containing activity trends
     */
    @Timed(value = "api.analytics.activityTrends")
    @Operation(
        summary = "Get user activity trends",
        description = "Retrieves user activity trends for the specified number of days"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved activity trends"),
        @ApiResponse(responseCode = "400", description = "Invalid days parameter"),
        @ApiResponse(responseCode = "500", description = "Internal server error while fetching trends")
    })
    @GetMapping("/activity-trends")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public ResponseEntity<Map<String, Object>> getActivityTrends(
            @Parameter(description = "Number of days to analyze", example = "30")
            @RequestParam(defaultValue = "30") @Min(1) @Max(MAX_DAYS) int days,
            @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        
        ConsumptionProbe probe = rateLimiter.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        try {
            if (days > MAX_DAYS) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Days parameter exceeds maximum allowed value"));
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Fetching activity trends for {} days", days);
            }
            LocalDateTime startDate = LocalDateTime.now().minus(days, ChronoUnit.DAYS);
            Map<String, Object> trends = userActivityService.getActivityTrendsSince(startDate);
            return ResponseEntity.ok(trends);
        } catch (Exception e) {
            logger.error("Error fetching activity trends: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves analysis of role distribution among users.
     *
     * @return ResponseEntity containing role distribution data
     */
    @Timed(value = "api.analytics.roleDistribution")
    @Operation(summary = "Get role distribution analysis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved role distribution"),
        @ApiResponse(responseCode = "500", description = "Internal server error while fetching distribution")
    })
    @GetMapping("/role-distribution")
    @Cacheable(value = "roleDistribution", key = "'distribution'", condition = "#result != null")
    public ResponseEntity<Map<String, Object>> getRoleDistribution() {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Fetching role distribution");
            }
            Map<String, Object> distribution = Map.of(
                "roleDistribution", userService.getRoleDistribution(),
                "commonRoleCombinations", userService.getCommonRoleCombinations(),
                "averageRolesPerUser", userService.getAverageRolesPerUser()
            );
            return ResponseEntity.ok(distribution);
        } catch (Exception e) {
            logger.error("Error fetching role distribution: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves user growth metrics over time.
     *
     * @param days Number of days to analyze
     * @return ResponseEntity containing growth metrics
     */
    @Timed(value = "api.analytics.userGrowth")
    @Operation(summary = "Get user growth metrics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved growth metrics"),
        @ApiResponse(responseCode = "400", description = "Invalid days parameter"),
        @ApiResponse(responseCode = "500", description = "Internal server error while fetching metrics")
    })
    @GetMapping("/user-growth")
    @Cacheable(value = "userGrowth", key = "#days", condition = "#result != null")
    public ResponseEntity<Map<String, Object>> getUserGrowth(
            @Parameter(description = "Number of days to analyze", example = "30")
            @RequestParam(defaultValue = "30") @Min(1) @Max(MAX_DAYS) int days) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Fetching user growth metrics for {} days", days);
            }
            LocalDateTime startDate = LocalDateTime.now().minus(days, ChronoUnit.DAYS);
            Map<String, Object> growth = userActivityService.getUserGrowthMetrics(startDate);
            return ResponseEntity.ok(growth);
        } catch (Exception e) {
            logger.error("Error fetching user growth metrics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves security-related metrics.
     *
     * @return ResponseEntity containing security metrics
     */
    @Timed(value = "api.analytics.securityMetrics")
    @Operation(summary = "Get security metrics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved security metrics"),
        @ApiResponse(responseCode = "500", description = "Internal server error while fetching metrics")
    })
    @GetMapping("/security-metrics")
    @Cacheable(value = "securityMetrics", key = "'metrics'", condition = "#result != null")
    public ResponseEntity<Map<String, Object>> getSecurityMetrics() {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Fetching security metrics");
            }
            Map<String, Object> metrics = userActivityService.getSecurityMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            logger.error("Error fetching security metrics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves user retention metrics.
     *
     * @param days Number of days to analyze
     * @return ResponseEntity containing retention metrics
     */
    @Timed(value = "api.analytics.userRetention")
    @Operation(summary = "Get user retention metrics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved retention metrics"),
        @ApiResponse(responseCode = "400", description = "Invalid days parameter"),
        @ApiResponse(responseCode = "500", description = "Internal server error while fetching metrics")
    })
    @GetMapping("/user-retention")
    @Cacheable(value = "userRetention", key = "#days", condition = "#result != null")
    public ResponseEntity<Map<String, Object>> getUserRetention(
            @Parameter(description = "Number of days to analyze", example = "30")
            @RequestParam(defaultValue = "30") @Min(1) @Max(MAX_DAYS) int days) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Fetching user retention metrics for {} days", days);
            }
            LocalDateTime startDate = LocalDateTime.now().minus(days, ChronoUnit.DAYS);
            Map<String, Object> retention = userActivityService.getUserRetentionMetrics(startDate);
            return ResponseEntity.ok(retention);
        } catch (Exception e) {
            logger.error("Error fetching user retention metrics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves user behavior analysis.
     *
     * @param days Number of days to analyze
     * @return ResponseEntity containing behavior analysis
     */
    @Timed(value = "api.analytics.userBehavior")
    @Operation(summary = "Get user behavior analysis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved behavior analysis"),
        @ApiResponse(responseCode = "400", description = "Invalid days parameter"),
        @ApiResponse(responseCode = "500", description = "Internal server error while fetching analysis")
    })
    @GetMapping("/user-behavior")
    @Cacheable(value = "userBehavior", key = "#days", condition = "#result != null")
    public ResponseEntity<Map<String, Object>> getUserBehavior(
            @Parameter(description = "Number of days to analyze", example = "30")
            @RequestParam(defaultValue = "30") @Min(1) @Max(MAX_DAYS) int days) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Fetching user behavior analysis for {} days", days);
            }
            LocalDateTime startDate = LocalDateTime.now().minus(days, ChronoUnit.DAYS);
            Map<String, Object> behavior = userActivityService.getUserBehaviorAnalysis(startDate);
            return ResponseEntity.ok(behavior);
        } catch (Exception e) {
            logger.error("Error fetching user behavior analysis: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 