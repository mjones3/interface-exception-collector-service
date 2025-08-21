package com.arcone.biopro.exception.collector.api.graphql.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * GraphQL alerting service that monitors response times, error rates,
 * and other critical metrics to trigger alerts when thresholds are exceeded.
 * Integrates with existing monitoring infrastructure.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "graphql.monitoring.alerting.enabled", havingValue = "true", matchIfMissing = true)
public class GraphQLAlertingService implements HealthIndicator {

    private final MeterRegistry meterRegistry;
    private final Environment environment;

    // Alert state tracking
    private final AtomicBoolean queryResponseTimeAlert = new AtomicBoolean(false);
    private final AtomicBoolean mutationResponseTimeAlert = new AtomicBoolean(false);
    private final AtomicBoolean errorRateAlert = new AtomicBoolean(false);
    private final AtomicBoolean cacheMissRateAlert = new AtomicBoolean(false);
    private final AtomicBoolean throughputAlert = new AtomicBoolean(false);
    private final AtomicBoolean subscriptionConnectionAlert = new AtomicBoolean(false);

    // Metrics tracking
    private final AtomicLong lastAlertCheck = new AtomicLong(System.currentTimeMillis());
    private final AtomicLong totalAlerts = new AtomicLong(0);
    private final AtomicLong totalAlertsResolved = new AtomicLong(0);

    // Alert cooldown tracking (5 minutes default)
    private final Map<String, Long> lastAlertTimes = new HashMap<>();
    private final long alertCooldownMs;

    // Configurable thresholds
    private final double queryResponseTimeThreshold;
    private final double mutationResponseTimeThreshold;
    private final double errorRateThreshold;
    private final double cacheMissRateThreshold;
    private final long throughputThreshold;
    private final long maxSubscriptionConnections;

    public GraphQLAlertingService(MeterRegistry meterRegistry, Environment environment) {
        this.meterRegistry = meterRegistry;
        this.environment = environment;

        // Load configuration
        this.alertCooldownMs = environment.getProperty(
                "graphql.monitoring.alerting.cooldown-minutes", Long.class, 5L) * 60 * 1000;
        this.queryResponseTimeThreshold = environment.getProperty(
                "graphql.monitoring.alerting.thresholds.query-response-time-ms", Double.class, 500.0);
        this.mutationResponseTimeThreshold = environment.getProperty(
                "graphql.monitoring.alerting.thresholds.mutation-response-time-ms", Double.class, 3000.0);
        this.errorRateThreshold = environment.getProperty(
                "graphql.monitoring.alerting.thresholds.error-rate-percent", Double.class, 5.0);
        this.cacheMissRateThreshold = environment.getProperty(
                "graphql.monitoring.alerting.thresholds.cache-miss-rate-percent", Double.class, 20.0);
        this.throughputThreshold = environment.getProperty(
                "graphql.monitoring.alerting.thresholds.throughput-per-minute", Long.class, 1000L);
        this.maxSubscriptionConnections = environment.getProperty(
                "graphql.websocket.max-connections", Long.class, 1000L);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeAlerting() {
        log.info("GraphQL alerting system initialized with thresholds:");
        log.info("  Query response time: {}ms", queryResponseTimeThreshold);
        log.info("  Mutation response time: {}ms", mutationResponseTimeThreshold);
        log.info("  Error rate: {}%", errorRateThreshold);
        log.info("  Cache miss rate: {}%", cacheMissRateThreshold);
        log.info("  Throughput: {} requests/minute", throughputThreshold);
        log.info("  Max subscription connections: {}", maxSubscriptionConnections);
        log.info("  Alert cooldown: {} minutes", alertCooldownMs / 60000);
    }

    /**
     * Scheduled task to check metrics and trigger alerts
     * Runs every 30 seconds as configured
     */
    @Scheduled(fixedRateString = "${graphql.monitoring.alerting.check-interval-seconds:30}000")
    public void checkMetricsAndAlert() {
        try {
            lastAlertCheck.set(System.currentTimeMillis());

            // Check all alert conditions
            checkQueryResponseTimes();
            checkMutationResponseTimes();
            checkErrorRates();
            checkCachePerformance();
            checkThroughput();
            checkSubscriptionConnections();

            log.debug("GraphQL alerting check completed at {}", Instant.now());

        } catch (Exception e) {
            log.error("Error during GraphQL metrics alerting check", e);
        }
    }

    /**
     * Check GraphQL query response times
     */
    private void checkQueryResponseTimes() {
        Timer queryTimer = meterRegistry.find("graphql_query_duration_seconds").timer();
        if (queryTimer != null && queryTimer.count() > 0) {
            double p95ResponseTime = queryTimer.percentile(0.95, TimeUnit.MILLISECONDS);
            double meanResponseTime = queryTimer.mean(TimeUnit.MILLISECONDS);

            boolean alertCondition = p95ResponseTime > queryResponseTimeThreshold;

            if (alertCondition && !queryResponseTimeAlert.get()) {
                triggerAlert("GRAPHQL_QUERY_RESPONSE_TIME",
                        String.format(
                                "GraphQL query P95 response time (%.2fms) exceeds threshold (%.2fms). Mean: %.2fms",
                                p95ResponseTime, queryResponseTimeThreshold, meanResponseTime),
                        "high",
                        createResponseTimeAlertDetails("query", meanResponseTime, p95ResponseTime,
                                queryResponseTimeThreshold));
                queryResponseTimeAlert.set(true);
            } else if (!alertCondition && queryResponseTimeAlert.get()) {
                resolveAlert("GRAPHQL_QUERY_RESPONSE_TIME",
                        String.format("GraphQL query response time recovered. P95: %.2fms, Mean: %.2fms",
                                p95ResponseTime, meanResponseTime));
                queryResponseTimeAlert.set(false);
            }
        }
    }

    /**
     * Check GraphQL mutation response times
     */
    private void checkMutationResponseTimes() {
        Timer mutationTimer = meterRegistry.find("graphql_mutation_duration_seconds").timer();
        if (mutationTimer != null && mutationTimer.count() > 0) {
            double p95ResponseTime = mutationTimer.percentile(0.95, TimeUnit.MILLISECONDS);
            double meanResponseTime = mutationTimer.mean(TimeUnit.MILLISECONDS);

            boolean alertCondition = p95ResponseTime > mutationResponseTimeThreshold;

            if (alertCondition && !mutationResponseTimeAlert.get()) {
                triggerAlert("GRAPHQL_MUTATION_RESPONSE_TIME",
                        String.format(
                                "GraphQL mutation P95 response time (%.2fms) exceeds threshold (%.2fms). Mean: %.2fms",
                                p95ResponseTime, mutationResponseTimeThreshold, meanResponseTime),
                        "high",
                        createResponseTimeAlertDetails("mutation", meanResponseTime, p95ResponseTime,
                                mutationResponseTimeThreshold));
                mutationResponseTimeAlert.set(true);
            } else if (!alertCondition && mutationResponseTimeAlert.get()) {
                resolveAlert("GRAPHQL_MUTATION_RESPONSE_TIME",
                        String.format("GraphQL mutation response time recovered. P95: %.2fms, Mean: %.2fms",
                                p95ResponseTime, meanResponseTime));
                mutationResponseTimeAlert.set(false);
            }
        }
    }

    /**
     * Check GraphQL error rates
     */
    private void checkErrorRates() {
        try {
            double totalQueries = getCounterValue("graphql_query_count_total");
            double totalMutations = getCounterValue("graphql_mutation_count_total");
            double totalOperations = totalQueries + totalMutations;
            double totalErrors = getCounterValue("graphql_error_count_total");

            if (totalOperations > 10) { // Only check if we have sufficient data
                double errorRate = (totalErrors / totalOperations) * 100;
                boolean alertCondition = errorRate > errorRateThreshold;

                if (alertCondition && !errorRateAlert.get()) {
                    triggerAlert("GRAPHQL_ERROR_RATE",
                            String.format("GraphQL error rate (%.2f%%) exceeds threshold (%.2f%%)",
                                    errorRate, errorRateThreshold),
                            "critical",
                            createErrorRateAlertDetails(errorRate, totalOperations, totalErrors));
                    errorRateAlert.set(true);
                } else if (!alertCondition && errorRateAlert.get()) {
                    resolveAlert("GRAPHQL_ERROR_RATE",
                            String.format("GraphQL error rate recovered. Current rate: %.2f%%", errorRate));
                    errorRateAlert.set(false);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to check GraphQL error rates", e);
        }
    }

    /**
     * Check cache performance
     */
    private void checkCachePerformance() {
        try {
            double cacheHits = getCounterValue("graphql_cache_access_total", "result", "hit");
            double cacheMisses = getCounterValue("graphql_cache_access_total", "result", "miss");
            double totalCacheAccess = cacheHits + cacheMisses;

            if (totalCacheAccess > 10) { // Only check if we have sufficient data
                double missRate = (cacheMisses / totalCacheAccess) * 100;
                boolean alertCondition = missRate > cacheMissRateThreshold;

                if (alertCondition && !cacheMissRateAlert.get()) {
                    triggerAlert("GRAPHQL_CACHE_MISS_RATE",
                            String.format("GraphQL cache miss rate (%.2f%%) exceeds threshold (%.2f%%)",
                                    missRate, cacheMissRateThreshold),
                            "medium",
                            createCacheAlertDetails(missRate, cacheHits, cacheMisses));
                    cacheMissRateAlert.set(true);
                } else if (!alertCondition && cacheMissRateAlert.get()) {
                    resolveAlert("GRAPHQL_CACHE_MISS_RATE",
                            String.format("GraphQL cache miss rate recovered. Current rate: %.2f%%", missRate));
                    cacheMissRateAlert.set(false);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to check GraphQL cache performance", e);
        }
    }

    /**
     * Check throughput metrics
     */
    private void checkThroughput() {
        try {
            // Calculate requests per minute based on recent activity
            double totalQueries = getCounterValue("graphql_query_count_total");
            double totalMutations = getCounterValue("graphql_mutation_count_total");
            double totalOperations = totalQueries + totalMutations;

            // This is a simplified throughput check
            // In production, you'd calculate actual rate over time window
            boolean alertCondition = totalOperations > throughputThreshold;

            if (alertCondition && !throughputAlert.get()) {
                triggerAlert("GRAPHQL_HIGH_THROUGHPUT",
                        String.format("GraphQL throughput (%.0f operations) exceeds threshold (%d)",
                                totalOperations, throughputThreshold),
                        "medium",
                        createThroughputAlertDetails(totalOperations));
                throughputAlert.set(true);
            } else if (!alertCondition && throughputAlert.get()) {
                resolveAlert("GRAPHQL_HIGH_THROUGHPUT",
                        String.format("GraphQL throughput normalized. Current: %.0f operations", totalOperations));
                throughputAlert.set(false);
            }
        } catch (Exception e) {
            log.warn("Failed to check GraphQL throughput", e);
        }
    }

    /**
     * Check subscription connection limits
     */
    private void checkSubscriptionConnections() {
        try {
            double activeConnections = getGaugeValue("graphql_subscription_connections_active");
            boolean alertCondition = activeConnections >= maxSubscriptionConnections * 0.9; // Alert at 90%

            if (alertCondition && !subscriptionConnectionAlert.get()) {
                triggerAlert("GRAPHQL_SUBSCRIPTION_CONNECTIONS",
                        String.format("GraphQL subscription connections (%.0f) approaching limit (%d)",
                                activeConnections, maxSubscriptionConnections),
                        "high",
                        createSubscriptionAlertDetails(activeConnections, maxSubscriptionConnections));
                subscriptionConnectionAlert.set(true);
            } else if (!alertCondition && subscriptionConnectionAlert.get()) {
                resolveAlert("GRAPHQL_SUBSCRIPTION_CONNECTIONS",
                        String.format("GraphQL subscription connections normalized. Current: %.0f", activeConnections));
                subscriptionConnectionAlert.set(false);
            }
        } catch (Exception e) {
            log.warn("Failed to check GraphQL subscription connections", e);
        }
    }

    /**
     * Trigger an alert with cooldown logic
     */
    private void triggerAlert(String alertType, String message, String severity, Map<String, Object> details) {
        long currentTime = System.currentTimeMillis();
        Long lastAlertTime = lastAlertTimes.get(alertType);

        // Check cooldown period
        if (lastAlertTime != null && (currentTime - lastAlertTime) < alertCooldownMs) {
            log.debug("Alert {} is in cooldown period, skipping", alertType);
            return;
        }

        lastAlertTimes.put(alertType, currentTime);
        totalAlerts.incrementAndGet();

        // Create structured alert log
        Map<String, Object> alertEvent = new HashMap<>();
        alertEvent.put("event", "GRAPHQL_ALERT_TRIGGERED");
        alertEvent.put("alert_type", alertType);
        alertEvent.put("severity", severity);
        alertEvent.put("message", message);
        alertEvent.put("timestamp", Instant.now().toString());
        alertEvent.put("service", "interface-exception-collector-service");
        alertEvent.put("environment", environment.getProperty("ENVIRONMENT", "local"));
        alertEvent.putAll(details);

        // Log the alert at appropriate level
        switch (severity.toLowerCase()) {
            case "critical":
                log.error("CRITICAL ALERT [{}]: {} - Details: {}", alertType, message, alertEvent);
                break;
            case "high":
                log.error("HIGH ALERT [{}]: {} - Details: {}", alertType, message, alertEvent);
                break;
            case "medium":
                log.warn("MEDIUM ALERT [{}]: {} - Details: {}", alertType, message, alertEvent);
                break;
            default:
                log.info("ALERT [{}]: {} - Details: {}", alertType, message, alertEvent);
        }

        // Record alert metric
        meterRegistry.counter("graphql_alerts_triggered_total",
                "type", alertType, "severity", severity).increment();

        // In a real implementation, you would:
        // 1. Send to alerting system (PagerDuty, Slack, etc.)
        // 2. Create incident tickets
        // 3. Send notifications to on-call engineers
        // 4. Integrate with existing alerting infrastructure
    }

    /**
     * Resolve an alert
     */
    private void resolveAlert(String alertType, String message) {
        totalAlertsResolved.incrementAndGet();

        Map<String, Object> resolveEvent = new HashMap<>();
        resolveEvent.put("event", "GRAPHQL_ALERT_RESOLVED");
        resolveEvent.put("alert_type", alertType);
        resolveEvent.put("message", message);
        resolveEvent.put("timestamp", Instant.now().toString());
        resolveEvent.put("service", "interface-exception-collector-service");

        log.info("ALERT RESOLVED [{}]: {} - Details: {}", alertType, message, resolveEvent);
        meterRegistry.counter("graphql_alerts_resolved_total", "type", alertType).increment();
    }

    /**
     * Get counter value from meter registry
     */
    private double getCounterValue(String meterName) {
        Counter counter = meterRegistry.find(meterName).counter();
        return counter != null ? counter.count() : 0.0;
    }

    /**
     * Get counter value with tags from meter registry
     */
    private double getCounterValue(String meterName, String tagKey, String tagValue) {
        Counter counter = meterRegistry.find(meterName).tag(tagKey, tagValue).counter();
        return counter != null ? counter.count() : 0.0;
    }

    /**
     * Get gauge value from meter registry
     */
    private double getGaugeValue(String meterName) {
        return meterRegistry.find(meterName).gauge() != null ? meterRegistry.find(meterName).gauge().value() : 0.0;
    }

    // Alert detail creation methods
    private Map<String, Object> createResponseTimeAlertDetails(String operationType, double mean,
            double p95, double threshold) {
        Map<String, Object> details = new HashMap<>();
        details.put("operation_type", operationType);
        details.put("mean_response_time_ms", mean);
        details.put("p95_response_time_ms", p95);
        details.put("threshold_ms", threshold);
        return details;
    }

    private Map<String, Object> createErrorRateAlertDetails(double errorRate, double totalOps, double totalErrors) {
        Map<String, Object> details = new HashMap<>();
        details.put("error_rate_percent", errorRate);
        details.put("total_operations", totalOps);
        details.put("total_errors", totalErrors);
        details.put("threshold_percent", errorRateThreshold);
        return details;
    }

    private Map<String, Object> createCacheAlertDetails(double missRate, double hits, double misses) {
        Map<String, Object> details = new HashMap<>();
        details.put("miss_rate_percent", missRate);
        details.put("cache_hits", hits);
        details.put("cache_misses", misses);
        details.put("threshold_percent", cacheMissRateThreshold);
        return details;
    }

    private Map<String, Object> createThroughputAlertDetails(double totalOps) {
        Map<String, Object> details = new HashMap<>();
        details.put("total_operations", totalOps);
        details.put("threshold", throughputThreshold);
        return details;
    }

    private Map<String, Object> createSubscriptionAlertDetails(double active, long max) {
        Map<String, Object> details = new HashMap<>();
        details.put("active_connections", active);
        details.put("max_connections", max);
        details.put("utilization_percent", (active / max) * 100);
        return details;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        details.put("last_check", Instant.ofEpochMilli(lastAlertCheck.get()).toString());
        details.put("total_alerts_triggered", totalAlerts.get());
        details.put("total_alerts_resolved", totalAlertsResolved.get());
        details.put("active_alerts", getActiveAlerts());
        details.put("alert_thresholds", getAlertThresholds());
        details.put("cooldown_minutes", alertCooldownMs / 60000);

        boolean hasActiveAlerts = queryResponseTimeAlert.get() ||
                mutationResponseTimeAlert.get() ||
                errorRateAlert.get() ||
                cacheMissRateAlert.get() ||
                throughputAlert.get() ||
                subscriptionConnectionAlert.get();

        return hasActiveAlerts ? Health.down().withDetails(details).build() : Health.up().withDetails(details).build();
    }

    private Map<String, Boolean> getActiveAlerts() {
        Map<String, Boolean> alerts = new HashMap<>();
        alerts.put("query_response_time", queryResponseTimeAlert.get());
        alerts.put("mutation_response_time", mutationResponseTimeAlert.get());
        alerts.put("error_rate", errorRateAlert.get());
        alerts.put("cache_miss_rate", cacheMissRateAlert.get());
        alerts.put("throughput", throughputAlert.get());
        alerts.put("subscription_connections", subscriptionConnectionAlert.get());
        return alerts;
    }

    private Map<String, Object> getAlertThresholds() {
        Map<String, Object> thresholds = new HashMap<>();
        thresholds.put("query_response_time_ms", queryResponseTimeThreshold);
        thresholds.put("mutation_response_time_ms", mutationResponseTimeThreshold);
        thresholds.put("error_rate_percent", errorRateThreshold);
        thresholds.put("cache_miss_rate_percent", cacheMissRateThreshold);
        thresholds.put("throughput_per_minute", throughputThreshold);
        thresholds.put("max_subscription_connections", maxSubscriptionConnections);
        return thresholds;
    }
}