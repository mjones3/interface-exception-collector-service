package com.arcone.biopro.exception.collector.api.graphql.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiter for GraphQL mutation operations.
 * Implements basic rate limiting to prevent abuse without complex dependencies.
 * Uses sliding window approach with automatic cleanup.
 * 
 * Requirements: 5.3, 5.5
 */
@Component
@Slf4j
public class MutationRateLimiter {

    private final int maxRequestsPerMinute;
    private final int maxRequestsPerHour;
    private final long windowSizeMs;
    
    // In-memory storage for rate limiting counters
    private final ConcurrentHashMap<String, UserRateLimit> userLimits = new ConcurrentHashMap<>();
    
    public MutationRateLimiter(
            @Value("${app.security.rate-limit.mutations.per-minute:30}") int maxRequestsPerMinute,
            @Value("${app.security.rate-limit.mutations.per-hour:500}") int maxRequestsPerHour) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.maxRequestsPerHour = maxRequestsPerHour;
        this.windowSizeMs = 60_000; // 1 minute window
        
        log.info("Mutation rate limiter initialized: {}req/min, {}req/hour", 
                maxRequestsPerMinute, maxRequestsPerHour);
    }

    /**
     * Checks if a user can perform a mutation operation.
     * Throws RateLimitExceededException if limits are exceeded.
     * 
     * @param userId the user performing the operation
     * @param operationType the type of mutation operation
     * @throws RateLimitExceededException if rate limits are exceeded
     */
    public void checkRateLimit(String userId, String operationType) {
        if (userId == null || operationType == null) {
            return; // Skip rate limiting for null values
        }

        String key = userId + ":" + operationType;
        UserRateLimit userLimit = userLimits.computeIfAbsent(key, k -> new UserRateLimit());
        
        long now = System.currentTimeMillis();
        
        // Clean up old entries periodically
        if (now - userLimit.lastCleanup > windowSizeMs) {
            cleanupOldEntries(userLimit, now);
        }
        
        // Check minute-based rate limit
        int currentMinuteCount = userLimit.getCurrentMinuteCount(now);
        if (currentMinuteCount >= maxRequestsPerMinute) {
            long resetTime = userLimit.getNextMinuteReset(now);
            log.warn("Rate limit exceeded for user {} on operation {}: {}/{} per minute", 
                    userId, operationType, currentMinuteCount, maxRequestsPerMinute);
            throw new RateLimitExceededException(userId, operationType, 
                    currentMinuteCount, maxRequestsPerMinute, resetTime - now);
        }
        
        // Check hour-based rate limit
        int currentHourCount = userLimit.getCurrentHourCount(now);
        if (currentHourCount >= maxRequestsPerHour) {
            long resetTime = userLimit.getNextHourReset(now);
            log.warn("Hourly rate limit exceeded for user {} on operation {}: {}/{} per hour", 
                    userId, operationType, currentHourCount, maxRequestsPerHour);
            throw new RateLimitExceededException(userId, operationType, 
                    currentHourCount, maxRequestsPerHour, resetTime - now);
        }
        
        // Record the request
        userLimit.recordRequest(now);
        
        log.debug("Rate limit check passed for user {} on operation {}: minute={}/{}, hour={}/{}", 
                userId, operationType, currentMinuteCount + 1, maxRequestsPerMinute, 
                currentHourCount + 1, maxRequestsPerHour);
    }

    /**
     * Gets the current rate limit status for a user and operation.
     * 
     * @param userId the user ID
     * @param operationType the operation type
     * @return rate limit status information
     */
    public RateLimitStatus getRateLimitStatus(String userId, String operationType) {
        if (userId == null || operationType == null) {
            return new RateLimitStatus(0, maxRequestsPerMinute, 0, maxRequestsPerHour, 0, 0);
        }

        String key = userId + ":" + operationType;
        UserRateLimit userLimit = userLimits.get(key);
        
        if (userLimit == null) {
            return new RateLimitStatus(0, maxRequestsPerMinute, 0, maxRequestsPerHour, 0, 0);
        }
        
        long now = System.currentTimeMillis();
        int currentMinuteCount = userLimit.getCurrentMinuteCount(now);
        int currentHourCount = userLimit.getCurrentHourCount(now);
        long minuteReset = userLimit.getNextMinuteReset(now);
        long hourReset = userLimit.getNextHourReset(now);
        
        return new RateLimitStatus(currentMinuteCount, maxRequestsPerMinute, 
                currentHourCount, maxRequestsPerHour, minuteReset, hourReset);
    }

    /**
     * Cleans up old entries to prevent memory leaks.
     */
    private void cleanupOldEntries(UserRateLimit userLimit, long now) {
        userLimit.cleanup(now);
        userLimit.lastCleanup = now;
    }

    /**
     * Clears all rate limiting data (for testing purposes).
     */
    public void clearAll() {
        userLimits.clear();
        log.debug("All rate limiting data cleared");
    }

    /**
     * Internal class to track rate limits for a user/operation combination.
     */
    private static class UserRateLimit {
        private final ConcurrentHashMap<Long, AtomicInteger> minuteWindows = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<Long, AtomicInteger> hourWindows = new ConcurrentHashMap<>();
        private volatile long lastCleanup = System.currentTimeMillis();

        void recordRequest(long timestamp) {
            long minuteWindow = timestamp / 60_000; // 1-minute windows
            long hourWindow = timestamp / 3_600_000; // 1-hour windows
            
            minuteWindows.computeIfAbsent(minuteWindow, k -> new AtomicInteger(0)).incrementAndGet();
            hourWindows.computeIfAbsent(hourWindow, k -> new AtomicInteger(0)).incrementAndGet();
        }

        int getCurrentMinuteCount(long timestamp) {
            long currentMinute = timestamp / 60_000;
            AtomicInteger count = minuteWindows.get(currentMinute);
            return count != null ? count.get() : 0;
        }

        int getCurrentHourCount(long timestamp) {
            long currentHour = timestamp / 3_600_000;
            AtomicInteger count = hourWindows.get(currentHour);
            return count != null ? count.get() : 0;
        }

        long getNextMinuteReset(long timestamp) {
            long currentMinute = timestamp / 60_000;
            return (currentMinute + 1) * 60_000;
        }

        long getNextHourReset(long timestamp) {
            long currentHour = timestamp / 3_600_000;
            return (currentHour + 1) * 3_600_000;
        }

        void cleanup(long now) {
            long currentMinute = now / 60_000;
            long currentHour = now / 3_600_000;
            
            // Remove minute windows older than 2 minutes
            minuteWindows.entrySet().removeIf(entry -> entry.getKey() < currentMinute - 2);
            
            // Remove hour windows older than 2 hours
            hourWindows.entrySet().removeIf(entry -> entry.getKey() < currentHour - 2);
        }
    }

    /**
     * Rate limit status information.
     */
    public static class RateLimitStatus {
        private final int currentMinuteCount;
        private final int maxMinuteCount;
        private final int currentHourCount;
        private final int maxHourCount;
        private final long minuteResetTime;
        private final long hourResetTime;

        public RateLimitStatus(int currentMinuteCount, int maxMinuteCount, 
                              int currentHourCount, int maxHourCount,
                              long minuteResetTime, long hourResetTime) {
            this.currentMinuteCount = currentMinuteCount;
            this.maxMinuteCount = maxMinuteCount;
            this.currentHourCount = currentHourCount;
            this.maxHourCount = maxHourCount;
            this.minuteResetTime = minuteResetTime;
            this.hourResetTime = hourResetTime;
        }

        public int getCurrentMinuteCount() { return currentMinuteCount; }
        public int getMaxMinuteCount() { return maxMinuteCount; }
        public int getCurrentHourCount() { return currentHourCount; }
        public int getMaxHourCount() { return maxHourCount; }
        public long getMinuteResetTime() { return minuteResetTime; }
        public long getHourResetTime() { return hourResetTime; }
        
        public boolean isMinuteLimitExceeded() { return currentMinuteCount >= maxMinuteCount; }
        public boolean isHourLimitExceeded() { return currentHourCount >= maxHourCount; }
    }
}