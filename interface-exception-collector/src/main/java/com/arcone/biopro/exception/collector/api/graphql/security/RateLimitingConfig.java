package com.arcone.biopro.exception.collector.api.graphql.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for GraphQL rate limiting based on user roles.
 * Defines different rate limits for ADMIN, OPERATIONS, and VIEWER roles.
 */
@Configuration
@ConfigurationProperties(prefix = "graphql.rate-limiting")
@Slf4j
public class RateLimitingConfig {

    private Map<String, RoleRateLimit> roles = new HashMap<>();
    private boolean enabled = true;

    public RateLimitingConfig() {
        // Initialize default rate limits
        initializeDefaultRateLimits();
    }

    /**
     * Initializes default rate limits for different user roles.
     */
    private void initializeDefaultRateLimits() {
        // ADMIN role - highest limits
        roles.put("ADMIN", new RoleRateLimit(
                300, // 300 requests per minute
                10000, // 10,000 requests per hour
                5000 // High query complexity allowed
        ));

        // OPERATIONS role - moderate limits
        roles.put("OPERATIONS", new RoleRateLimit(
                120, // 120 requests per minute
                3600, // 3,600 requests per hour
                2000 // Moderate query complexity
        ));

        // VIEWER role - conservative limits
        roles.put("VIEWER", new RoleRateLimit(
                60, // 60 requests per minute
                1800, // 1,800 requests per hour
                1000 // Basic query complexity
        ));

        log.info("Initialized default rate limits for {} roles", roles.size());
    }

    /**
     * Gets the rate limit configuration for a specific role.
     */
    public RateLimitingInterceptor.RateLimit getRateLimitForRole(String role) {
        RoleRateLimit roleLimit = roles.getOrDefault(role, roles.get("VIEWER"));

        return new RateLimitingInterceptor.RateLimit(
                roleLimit.getRequestsPerMinute(),
                roleLimit.getRequestsPerHour(),
                roleLimit.getMaxQueryComplexity());
    }

    /**
     * Checks if rate limiting is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, RoleRateLimit> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, RoleRateLimit> roles) {
        this.roles = roles;
    }

    /**
     * Rate limit configuration for a specific role.
     */
    public static class RoleRateLimit {
        private int requestsPerMinute;
        private int requestsPerHour;
        private int maxQueryComplexity;

        public RoleRateLimit() {
        }

        public RoleRateLimit(int requestsPerMinute, int requestsPerHour, int maxQueryComplexity) {
            this.requestsPerMinute = requestsPerMinute;
            this.requestsPerHour = requestsPerHour;
            this.maxQueryComplexity = maxQueryComplexity;
        }

        public int getRequestsPerMinute() {
            return requestsPerMinute;
        }

        public void setRequestsPerMinute(int requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
        }

        public int getRequestsPerHour() {
            return requestsPerHour;
        }

        public void setRequestsPerHour(int requestsPerHour) {
            this.requestsPerHour = requestsPerHour;
        }

        public int getMaxQueryComplexity() {
            return maxQueryComplexity;
        }

        public void setMaxQueryComplexity(int maxQueryComplexity) {
            this.maxQueryComplexity = maxQueryComplexity;
        }
    }
}