package com.arcone.biopro.exception.collector.api.graphql.monitoring;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * GraphQL-specific health indicator that checks GraphQL functionality
 * and exposes health metrics for monitoring dashboards.
 */
@Slf4j
@Component("graphqlHealthIndicator")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "graphql.features.health-checks-enabled", havingValue = "true", matchIfMissing = false)
public class GraphQLHealthIndicator implements HealthIndicator {

    private final MeterRegistry meterRegistry;
    private final DataSource dataSource;
    private final GraphQLMetrics graphqlMetrics;

    // Health check response time gauges
    private final Gauge databaseResponseTimeGauge;
    private final Gauge cacheResponseTimeGauge;

    public GraphQLHealthIndicator(MeterRegistry meterRegistry, DataSource dataSource, GraphQLMetrics graphqlMetrics) {
        this.meterRegistry = meterRegistry;
        this.dataSource = dataSource;
        this.graphqlMetrics = graphqlMetrics;

        // Initialize health check response time gauges
        this.databaseResponseTimeGauge = Gauge
                .builder("graphql_health_database_response_time_ms", this,
                        obj -> (double) obj.checkDatabaseResponseTime())
                .description("Database response time for GraphQL health check")
                .register(meterRegistry);

        this.cacheResponseTimeGauge = Gauge
                .builder("graphql_health_cache_response_time_ms", this, obj -> (double) obj.checkCacheResponseTime())
                .description("Cache response time for GraphQL health check")
                .register(meterRegistry);
    }

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        boolean isHealthy = true;

        try {
            // Check database connectivity
            long dbResponseTime = checkDatabaseResponseTime();
            details.put("database", createDatabaseHealthDetails(dbResponseTime));

            // Check cache connectivity (if enabled)
            long cacheResponseTime = checkCacheResponseTime();
            details.put("cache", createCacheHealthDetails(cacheResponseTime));

            // Check GraphQL metrics
            details.put("metrics", createMetricsHealthDetails());

            // Check subscription connections
            details.put("subscriptions", createSubscriptionHealthDetails());

            // Overall GraphQL system status
            details.put("graphql_system", createSystemHealthDetails());

            // Check if any critical thresholds are exceeded
            isHealthy = evaluateOverallHealth(dbResponseTime, cacheResponseTime);

        } catch (Exception e) {
            log.error("GraphQL health check failed", e);
            details.put("error", e.getMessage());
            details.put("timestamp", Instant.now().toString());
            isHealthy = false;
        }

        return isHealthy ? Health.up().withDetails(details).build() : Health.down().withDetails(details).build();
    }

    /**
     * Check database response time
     */
    private long checkDatabaseResponseTime() {
        long startTime = System.currentTimeMillis();
        try (Connection connection = dataSource.getConnection()) {
            // Simple connectivity test
            connection.isValid(1000); // 1 second timeout
            return System.currentTimeMillis() - startTime;
        } catch (SQLException e) {
            log.warn("Database health check failed", e);
            return -1; // Indicates failure
        }
    }

    /**
     * Check cache response time (simplified for now since Redis is disabled)
     */
    private long checkCacheResponseTime() {
        // Since Redis is disabled in the current configuration,
        // we'll return a nominal value for the simple cache
        long startTime = System.currentTimeMillis();
        try {
            // Simple cache operation simulation
            Thread.sleep(1); // Simulate cache operation
            return System.currentTimeMillis() - startTime;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        }
    }

    /**
     * Create database health details
     */
    private Map<String, Object> createDatabaseHealthDetails(long responseTime) {
        Map<String, Object> dbHealth = new HashMap<>();
        dbHealth.put("status", responseTime >= 0 ? "UP" : "DOWN");
        dbHealth.put("responseTimeMs", responseTime);
        dbHealth.put("threshold", getDbHealthThreshold());
        dbHealth.put("healthy", responseTime >= 0 && responseTime < getDbHealthThreshold());
        return dbHealth;
    }

    /**
     * Create cache health details
     */
    private Map<String, Object> createCacheHealthDetails(long responseTime) {
        Map<String, Object> cacheHealth = new HashMap<>();
        cacheHealth.put("status", responseTime >= 0 ? "UP" : "DOWN");
        cacheHealth.put("responseTimeMs", responseTime);
        cacheHealth.put("threshold", getCacheHealthThreshold());
        cacheHealth.put("healthy", responseTime >= 0 && responseTime < getCacheHealthThreshold());
        cacheHealth.put("type", "simple"); // Since Redis is disabled
        return cacheHealth;
    }

    /**
     * Create metrics health details
     */
    private Map<String, Object> createMetricsHealthDetails() {
        Map<String, Object> metricsHealth = new HashMap<>();
        try {
            int meterCount = meterRegistry.getMeters().size();
            long graphqlMeterCount = meterRegistry.getMeters().stream()
                    .mapToLong(meter -> meter.getId().getName().startsWith("graphql_") ? 1 : 0)
                    .sum();

            metricsHealth.put("status", "UP");
            metricsHealth.put("totalMeters", meterCount);
            metricsHealth.put("graphqlMeters", graphqlMeterCount);
            metricsHealth.put("healthy", graphqlMeterCount > 0);
        } catch (Exception e) {
            metricsHealth.put("status", "DOWN");
            metricsHealth.put("error", e.getMessage());
            metricsHealth.put("healthy", false);
        }
        return metricsHealth;
    }

    /**
     * Create subscription health details
     */
    private Map<String, Object> createSubscriptionHealthDetails() {
        Map<String, Object> subscriptionHealth = new HashMap<>();
        try {
            long activeConnections = graphqlMetrics.getActiveSubscriptionCount();
            long maxConnections = getMaxSubscriptionConnections();

            subscriptionHealth.put("status", "UP");
            subscriptionHealth.put("activeConnections", activeConnections);
            subscriptionHealth.put("maxConnections", maxConnections);
            subscriptionHealth.put("utilizationPercent",
                    maxConnections > 0 ? (activeConnections * 100.0 / maxConnections) : 0);
            subscriptionHealth.put("healthy", activeConnections < maxConnections);
        } catch (Exception e) {
            subscriptionHealth.put("status", "DOWN");
            subscriptionHealth.put("error", e.getMessage());
            subscriptionHealth.put("healthy", false);
        }
        return subscriptionHealth;
    }

    /**
     * Create system health details
     */
    private Map<String, Object> createSystemHealthDetails() {
        Map<String, Object> systemHealth = new HashMap<>();
        systemHealth.put("status", "UP");
        systemHealth.put("timestamp", Instant.now().toString());
        systemHealth.put("version", getClass().getPackage().getImplementationVersion());
        systemHealth.put("graphqlEnabled", true);
        return systemHealth;
    }

    /**
     * Evaluate overall health based on individual checks
     */
    private boolean evaluateOverallHealth(long dbResponseTime, long cacheResponseTime) {
        // Database must be healthy
        if (dbResponseTime < 0 || dbResponseTime > getDbHealthThreshold()) {
            return false;
        }

        // Cache should be healthy (but not critical since it's simple cache)
        if (cacheResponseTime < 0) {
            log.warn("Cache health check failed, but continuing since it's not critical");
        }

        // Check subscription connections
        long activeConnections = graphqlMetrics.getActiveSubscriptionCount();
        if (activeConnections >= getMaxSubscriptionConnections()) {
            log.warn("Subscription connections at maximum capacity: {}", activeConnections);
            return false;
        }

        return true;
    }

    /**
     * Get database health threshold from configuration
     */
    private long getDbHealthThreshold() {
        return Long.parseLong(
                System.getProperty("graphql.monitoring.health-checks.database-timeout-ms", "1000"));
    }

    /**
     * Get cache health threshold from configuration
     */
    private long getCacheHealthThreshold() {
        return Long.parseLong(
                System.getProperty("graphql.monitoring.health-checks.cache-timeout-ms", "100"));
    }

    /**
     * Get maximum subscription connections from configuration
     */
    private long getMaxSubscriptionConnections() {
        return Long.parseLong(
                System.getProperty("graphql.websocket.max-connections", "1000"));
    }
}