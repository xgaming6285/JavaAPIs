package com.example.demo;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserActivityService {
    private final UserService userService;
    private final Map<String, Integer> loginAttempts = new HashMap<>();
    private final Map<Long, LocalDateTime> lastLoginTimes = new HashMap<>();
    private final Map<Long, List<String>> userActivities = new HashMap<>();

    public UserActivityService(UserService userService) {
        this.userService = Objects.requireNonNull(userService, "UserService must not be null");
    }

    @Cacheable(value = "activityTrends", key = "#startDate")
    public Map<String, Object> getActivityTrendsSince(LocalDateTime startDate) {
        Map<String, Object> trends = new HashMap<>();
        trends.put("dailyActiveUsers", calculateDailyActiveUsers(startDate));
        trends.put("peakActivityHours", calculatePeakActivityHours());
        trends.put("averageSessionDuration", calculateAverageSessionDuration());
        return trends;
    }

    public Map<String, Object> getUserGrowthMetrics(LocalDateTime startDate) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("newUsers", calculateNewUserGrowth(startDate));
        metrics.put("userRetentionRate", calculateRetentionRate(startDate));
        metrics.put("churnRate", calculateChurnRate(startDate));
        return metrics;
    }

    public Map<String, Object> getSecurityMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("failedLoginAttempts", getFailedLoginAttempts());
        metrics.put("suspiciousActivities", getSuspiciousActivities());
        metrics.put("accountLockouts", getAccountLockouts());
        return metrics;
    }

    public Map<String, Object> getUserRetentionMetrics(LocalDateTime startDate) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("dailyRetention", calculateDailyRetention(startDate));
        metrics.put("weeklyRetention", calculateWeeklyRetention(startDate));
        metrics.put("monthlyRetention", calculateMonthlyRetention(startDate));
        return metrics;
    }

    public Map<String, Object> getUserBehaviorAnalysis(LocalDateTime startDate) {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("mostActiveUsers", getMostActiveUsers());
        analysis.put("commonUserPaths", getCommonUserPaths());
        analysis.put("featureUsage", getFeatureUsageStats());
        return analysis;
    }

    public void recordLoginAttempt(String username, boolean success) {
        if (!success) {
            loginAttempts.merge(username, 1, Integer::sum);
        } else {
            loginAttempts.remove(username);
        }
    }

    public void recordUserActivity(Long userId, String activity) {
        userActivities.computeIfAbsent(userId, k -> new ArrayList<>()).add(activity);
        lastLoginTimes.put(userId, LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 0 * * *") // Run daily at midnight
    public void cleanupOldData() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        userActivities.entrySet().removeIf(entry -> 
            lastLoginTimes.get(entry.getKey()).isBefore(oneMonthAgo));
        lastLoginTimes.entrySet().removeIf(entry -> entry.getValue().isBefore(oneMonthAgo));
    }

    private Map<LocalDateTime, Long> calculateDailyActiveUsers(LocalDateTime startDate) {
        return lastLoginTimes.entrySet().stream()
            .filter(entry -> entry.getValue().isAfter(startDate))
            .collect(Collectors.groupingBy(
                entry -> entry.getValue().toLocalDate().atStartOfDay(),
                Collectors.counting()
            ));
    }

    private Map<Integer, Long> calculatePeakActivityHours() {
        return lastLoginTimes.values().stream()
            .collect(Collectors.groupingBy(
                time -> time.getHour(),
                Collectors.counting()
            ));
    }

    private double calculateAverageSessionDuration() {
        return 30.0; 
    }

    private Map<LocalDateTime, Long> calculateNewUserGrowth(LocalDateTime startDate) {
        return lastLoginTimes.entrySet().stream()
            .filter(entry -> entry.getValue().isAfter(startDate))
            .collect(Collectors.groupingBy(
                entry -> entry.getValue().toLocalDate().atStartOfDay(),
                Collectors.counting()
            ));
    }

    private double calculateRetentionRate(LocalDateTime startDate) {
        long totalUsers = userService.getAllUsers().size();
        long activeUsers = lastLoginTimes.values().stream()
            .filter(time -> time.isAfter(startDate))
            .count();
        return totalUsers > 0 ? (double) activeUsers / totalUsers * 100 : 0;
    }

    private double calculateChurnRate(LocalDateTime startDate) {
        return 100 - calculateRetentionRate(startDate);
    }

    private Map<String, Integer> getFailedLoginAttempts() {
        return new HashMap<>(loginAttempts);
    }

    private List<Map<String, Object>> getSuspiciousActivities() {
        return new ArrayList<>(); 
    }

    private int getAccountLockouts() {
        return (int) loginAttempts.values().stream()
            .filter(attempts -> attempts >= 5)
            .count();
    }

    private Map<LocalDateTime, Double> calculateDailyRetention(LocalDateTime startDate) {
        Map<LocalDateTime, Double> retention = new HashMap<>();
        LocalDateTime currentDate = LocalDateTime.now();
        
        while (currentDate.isAfter(startDate)) {
            LocalDateTime date = currentDate;
            long totalUsers = userService.getAllUsers().size();
            long retainedUsers = lastLoginTimes.values().stream()
                .filter(loginTime -> loginTime.isAfter(date))
                .count();
            
            retention.put(date, totalUsers > 0 ? (double) retainedUsers / totalUsers * 100 : 0.0);
            currentDate = currentDate.minusDays(1);
        }
        
        return retention;
    }

    private Map<LocalDateTime, Double> calculateWeeklyRetention(LocalDateTime startDate) {
        Map<LocalDateTime, Double> retention = new HashMap<>();
        LocalDateTime currentDate = LocalDateTime.now();
        
        while (currentDate.isAfter(startDate)) {
            LocalDateTime weekStart = currentDate;
            long totalUsers = userService.getAllUsers().size();
            long retainedUsers = lastLoginTimes.values().stream()
                .filter(loginTime -> loginTime.isAfter(weekStart))
                .count();
            
            retention.put(weekStart, totalUsers > 0 ? (double) retainedUsers / totalUsers * 100 : 0.0);
            currentDate = currentDate.minusWeeks(1);
        }
        
        return retention;
    }

    private Map<LocalDateTime, Double> calculateMonthlyRetention(LocalDateTime startDate) {
        Map<LocalDateTime, Double> retention = new HashMap<>();
        LocalDateTime currentDate = LocalDateTime.now();
        
        while (currentDate.isAfter(startDate)) {
            LocalDateTime monthStart = currentDate;
            long totalUsers = userService.getAllUsers().size();
            long retainedUsers = lastLoginTimes.values().stream()
                .filter(loginTime -> loginTime.isAfter(monthStart))
                .count();
            
            retention.put(monthStart, totalUsers > 0 ? (double) retainedUsers / totalUsers * 100 : 0.0);
            currentDate = currentDate.minusMonths(1);
        }
        
        return retention;
    }

    private List<Map<String, Object>> getMostActiveUsers() {
        return userActivities.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
            .limit(10)
            .map(entry -> {
                Map<String, Object> userStats = new HashMap<>();
                userStats.put("userId", entry.getKey());
                userStats.put("activityCount", entry.getValue().size());
                return userStats;
            })
            .collect(Collectors.toList());
    }

    private List<String> getCommonUserPaths() {
        return new ArrayList<>(); 
    }

    private Map<String, Long> getFeatureUsageStats() {
        return userActivities.values().stream()
            .flatMap(List::stream)
            .collect(Collectors.groupingBy(
                activity -> activity,
                Collectors.counting()
            ));
    }
} 