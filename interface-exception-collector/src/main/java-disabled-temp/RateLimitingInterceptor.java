package com.arcone.biopro.exception.collector.api.graphql.security;

import graphql.ExecutionResult;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * GraphQL rate limiting interceptor using Redis counters per user/IP.
 * Implements request throttling based on user roles and query complexity.
 */
// @Component // Disabled due to Redis dependency
@RequiredArgsConstructor
@Slf4j
public class RateLimitingInterceptor extends SimpleInstrumentation {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RateLimitingConfig rateLimitingConfig;

    @Override
    public InstrumentationContext<ExecutionResult> beginExecution(
            InstrumentationExecutionParameters parameters,
            InstrumentationState state) {

        return new InstrumentationContext<ExecutionResult>() {
            @Override
            public void onDispatched(CompletableFuture<ExecutionResult> result) {
                // Check rate limits before execution
                checkRateLimit(parameters);
            }

            @Override
            public void onCompleted(ExecutionResult result, Throwable t) {
                // Increment counters after execution
                incrementCounters(parameters);
            }
        };
    }

    /**
     * Checks if the current request exceeds rate limits.
     * Throws RateLimitExceededException if limits are exceeded.
     */
    private void checkRateLimit(InstrumentationExecutionParameters parameters) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return; // Skip rate limiting for unauthenticated requests
        }

        String userId = authentication.getName();
        String userRole = getUserRole(authentication);

        // Get rate limits based on user role
        RateLimit rateLimit = rateLimitingConfig.getRateLimitForRole(userRole);

        // Check per-minute rate limit
        String minuteKey = buildRateLimitKey(userId, "minute");
        Long minuteCount = getCurrentCount(minuteKey);

        if (minuteCount != null && minuteCount >= rateLimit.getRequestsPerMinute()) {
            log.warn("Rate limit exceeded for user {} (role: {}): {} requests in last minute",
                    userId, userRole, minuteCount);
            throw new RateLimitExceededException(
                    String.format("Rate limit exceeded: %d requests per minute allowed for role %s",
                            rateLimit.getRequestsPerMinute(), userRole));
        }

        // Check per-hour rate limit
        String hourKey = buildRateLimitKey(userId, "hour");
        Long hourCount = getCurrentCount(hourKey);

        if (hourCount != null && hourCount >= rateLimit.getRequestsPerHour()) {
            log.warn("Rate limit exceeded for user {} (role: {}): {} requests in last hour",
                    userId, userRole, hourCount);
            throw new RateLimitExceededException(
                    String.format("Rate limit exceeded: %d requests per hour allowed for role %s",
                            rateLimit.getRequestsPerHour(), userRole));
        }

        // Check query complexity limits
        checkComplexityLimits(parameters, userRole, rateLimit);
    }

    /**
     * Checks query complexity limits based on user role.
     */
    private void checkComplexityLimits(InstrumentationExecutionParameters parameters,
            String userRole, RateLimit rateLimit) {
        // This would integrate with query complexity analysis
        // For now, we'll implement a simple query length check as a proxy
        String query = parameters.getQuery();
        int queryLength = query.length();

        if (queryLength > rateLimit.getMaxQueryComplexity()) {
            log.warn("Query complexity limit exceeded for user role {}: query length {} > max {}",
                    userRole, queryLength, rateLimit.getMaxQueryComplexity());
            throw new RateLimitExceededException(
                    String.format("Query too complex for role %s: maximum complexity %d",
                            userRole, rateLimit.getMaxQueryComplexity()));
        }
    }

    /**
     * Increments rate limiting counters after successful execution.
     */
    private void incrementCounters(InstrumentationExecutionParameters parameters) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        String userId = authentication.getName();

        // Increment minute counter
        String minuteKey = buildRateLimitKey(userId, "minute");
        incrementCounter(minuteKey, Duration.ofMinutes(1));

        // Increment hour counter
        String hourKey = buildRateLimitKey(userId, "hour");
        incrementCounter(hourKey, Duration.ofHours(1));

        log.debug("Incremented rate limit counters for user: {}", userId);
    }

    /**
     * Builds a Redis key for rate limiting counters.
     */
    private String buildRateLimitKey(String userId, String timeWindow) {
        long timestamp = System.currentTimeMillis();
        long windowStart;

        if ("minute".equals(timeWindow)) {
            windowStart = timestamp - (timestamp % 60000); // Round to minute
        } else if ("hour".equals(timeWindow)) {
            windowStart = timestamp - (timestamp % 3600000); // Round to hour
        } else {
            throw new IllegalArgumentException("Invalid time window: " + timeWindow);
        }

        return String.format("rate_limit:%s:%s:%d", userId, timeWindow, windowStart);
    }

    /**
     * Gets the current count for a rate limit key.
     */
    private Long getCurrentCount(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return value != null ? Long.valueOf(value.toString()) : 0L;
        } catch (Exception e) {
            log.error("Error getting rate limit count for key {}: {}", key, e.getMessage());
            return 0L; // Fail open - don't block requests on Redis errors
        }
    }

    /**
     * Increments a counter with expiration.
     */
    private void incrementCounter(String key, Duration expiration) {
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count == 1) {
                // Set expiration only on first increment
                redisTemplate.expire(key, expiration);
            }
        } catch (Exception e) {
            log.error("Error incrementing rate limit counter for key {}: {}", key, e.getMessage());
            // Fail silently - don't block requests on Redis errors
        }
    }

    /**
     * Extracts the user role from authentication.
     */
    private String getUserRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5)) // Remove "ROLE_" prefix
                .findFirst()
                .orElse("VIEWER"); // Default role
    }

    /**
     * Rate limit configuration for different user roles.
     */
    public static class RateLimit {
        private final int requestsPerMinute;
        private final int requestsPerHour;
        private final int maxQueryComplexity;

        public RateLimit(int requestsPerMinute, int requestsPerHour, int maxQueryComplexity) {
            this.requestsPerMinute = requestsPerMinute;
            this.requestsPerHour = requestsPerHour;
            this.maxQueryComplexity = maxQueryComplexity;
        }

        public int getRequestsPerMinute() {
            return requestsPerMinute;
        }

        public int getRequestsPerHour() {
            return requestsPerHour;
        }

        public int getMaxQueryComplexity() {
            return maxQueryComplexity;
        }
    }
}