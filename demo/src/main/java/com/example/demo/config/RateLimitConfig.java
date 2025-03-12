package com.example.demo.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {
    
    /**
     * Creates a general API rate limiter.
     * Allows 100 requests per minute.
     *
     * @return the configured rate limiter
     */
    @Bean(name = "generalLimiter")
    public Bucket rateLimiter() {
        Bandwidth limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
    
    /**
     * Creates a rate limiter for authentication endpoints.
     * Allows 10 requests per minute to prevent brute force attacks.
     *
     * @return the configured authentication rate limiter
     */
    @Bean(name = "authLimiter")
    public Bucket authRateLimiter() {
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
    
    /**
     * Creates a rate limiter for user creation.
     * Allows 5 user creations per hour to prevent spam.
     *
     * @return the configured user creation rate limiter
     */
    @Bean(name = "userCreationLimiter")
    public Bucket userCreationRateLimiter() {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofHours(1)));
        return Bucket.builder().addLimit(limit).build();
    }
    
    /**
     * Creates a map of IP-based rate limiters.
     * Used to track and limit requests from specific IP addresses.
     *
     * @return the map of IP-based rate limiters
     */
    @Bean
    public Map<String, Bucket> ipRateLimiters() {
        return new ConcurrentHashMap<>();
    }
    
    /**
     * Creates a factory for IP-based rate limiters.
     * Each IP is limited to 50 requests per minute.
     *
     * @return the IP rate limiter factory
     */
    @Bean
    public IpRateLimiterFactory ipRateLimiterFactory() {
        return new IpRateLimiterFactory(50, Duration.ofMinutes(1));
    }
    
    public static class IpRateLimiterFactory {
        private final int limit;
        private final Duration duration;
        
        /**
         * Creates a new IP rate limiter factory.
         *
         * @param limit the request limit
         * @param duration the time duration for the limit
         */
        public IpRateLimiterFactory(int limit, Duration duration) {
            this.limit = limit;
            this.duration = duration;
        }
        
        /**
         * Creates a new rate limiter for an IP address.
         *
         * @return the configured rate limiter
         */
        public Bucket create() {
            Bandwidth limit = Bandwidth.classic(this.limit, Refill.greedy(this.limit, this.duration));
            return Bucket.builder().addLimit(limit).build();
        }
    }
}