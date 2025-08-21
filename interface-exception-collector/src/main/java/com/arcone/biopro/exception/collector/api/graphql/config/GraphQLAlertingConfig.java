package com.arcone.biopro.exception.collector.api.graphql.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * GraphQL alerting configuration that monitors response times, error rates,
 * and other critical metrics to trigger alerts when thresholds are exceeded.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GraphQLAlertingConfig implements HealthIndicator {

    private final MeterRegistry meterRegistry;

    // Alert state tracking
    private final AtomicBoolean queryResponseTimeAlert = new AtomicBoolean(false);
    private final AtomicBoolean mutationResponseTimeAlert = new AtomicBoolean(false);
    private final AtomicBoolean errorRateAlert = new AtomicBoolean(false);
    private final AtomicBoolean cacheMissRateAlert = new AtomicBoolean(false);
    private final AtomicBoolean throughputAlert = new AtomicBoolean(false);

    // Metrics tracking
    private final AtomicLong lastAlertCheck = new AtomicLong(System.currentTimeMillis());
    private final AtomicLong totalAlerts = new AtomicLong(0);

    // Thresholds (configurable via properties)
    private static final double QUERY_RESPONSE_TIME_THRESHOLD_MS = 500.0;
    private static final double MUTATION_RESPONSE_TIME_THRESHOLD_MS = 3000.0;
    private static final double ERROR_RATE_THRESHOLD_PERCENT = 5.0;
    private static final double CACHE_MISS_RATE_THRESHOLD_PERCENT = 20.0;
    private static final long THROUGHPUT_THRESHOLD_PER_MINUTE = 1000;

    // Alert cooldown period (5 minutes)
    private static final long ALERT_COOLDOWN_MS = 5 * 60 * 1000;
    private final Map<String, Long> lastAlertTimes = new HashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void initializeAlerting() {
        log.info("GraphQL alerting system initialized with thresholds:");
        log.info("  Query response time: {}ms", QUERY_RESPONSE_TIME_THRESHOLD_MS);
        log.info("  Mutation response time: {}ms", MUTATION_RESPONSE_TIME_THRESHOLD_MS);
        log.info("  Error rate: {}%", ERROR_RATE_THRESHOLD_PERCENT);
        log.info("  Cache miss rate: {}%", CACHE_MISS_RATE_THRESHOLD_PERCENT);
        log.info("  Throughput: {} requests/minute", THROUGHPUT_THRESHOLD_PER_MINUTE);
    }

    /**
     * Scheduled task to check metrics and trigger alerts
     * Runs every 30 seconds
     */
    @Scheduled(fixedRate = 30000)
    public void checkMetricsAndAlert() {
        try {
            lastAlertCheck.set(System.currentTimeMillis());

            // Check query response times
            checkQueryResponseTimes();

            // Check mutation response times
            checkMutationResponseTimes();

            // Check error rates
            checkErrorRates();

            // Check cache performance
            checkCachePerformance();

            // Check throughput
            checkThroughput();

        } catch (Exception e) {
            log.error("Error during metrics alerting check", e);
        }
    }

    /**
     * Check GraphQL query response times
     */
    private void checkQueryResponseTimes() {
        Timer queryTimer = meterRegistry.find("graphql.query.duration").timer();
        if (queryTimer != null) {
            double meanResponseTime = queryTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
            double p95ResponseTime = queryTimer.percentile(0.95, java.util.concurrent.TimeUnit.MILLISECONDS);

            boolean alertCondition = p95ResponseTime > QUERY_RESPONSE_TIME_THRESHOLD_MS;

            if (alertCondition && !queryResponseTimeAlert.get()) {
                triggerAlert("QUERY_RESPONSE_TIME",
                        String.format(
                                "GraphQL query P95 response time (%.2fms) exceeds threshold (%.2fms). Mean: %.2fms",
                                p95ResponseTime, QUERY_RESPONSE_TIME_THRESHOLD_MS, meanResponseTime),
                        createResponseTimeAlertDetails(meanResponseTime, p95ResponseTime,
                                QUERY_RESPONSE_TIME_THRESHOLD_MS));
                queryResponseTimeAlert.set(true);
            } else if (!alertCondition && queryResponseTimeAlert.get()) {
                resolveAlert("QUERY_RESPONSE_TIME",
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
        Timer mutationTimer = meterRegistry.find("graphql.mutation.duration").timer();
        if (mutationTimer != null) {
            double meanResponseTime = mutationTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
            double p95ResponseTime = mutationTimer.percentile(0.95, java.util.concurrent.TimeUnit.MILLISECONDS);

            boolean alertCondition = p95ResponseTime > MUTATION_RESPONSE_TIME_THRESHOLD_MS;

            if (alertCondition && !mutationResponseTimeAlert.get()) {
                triggerAlert("MUTATION_RESPONSE_TIME",
                        String.format(
                                "GraphQL mutation P95 response time (%.2fms) exceeds threshold (%.2fms). Mean: %.2fms",
                                p95ResponseTime, MUTATION_RESPONSE_TIME_THRESHOLD_MS, meanResponseTime),
                        createResponseTimeAlertDetails(meanResponseTime, p95ResponseTime,
                                MUTATION_RESPONSE_TIME_THRESHOLD_MS));
                mutationResponseTimeAlert.set(true);
            } else if (!alertCondition && mutationResponseTimeAlert.get()) {
                resolveAlert("MUTATION_RESPONSE_TIME",
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
            // Calculate error rate from counters
            double totalQueries = getCounterValue("graphql.query.count");
            double totalErrors = getCounterValue("graphql.error.count");

            if (totalQueries > 0) {
                double errorRate = (totalErrors / totalQueries) * 100;
                boolean alertCondition = errorRate > ERROR_RATE_THRESHOLD_PERCENT;

                if (alertCondition && !errorRateAlert.get()) {
                    triggerAlert("ERROR_RATE",
                            String.format("GraphQL error rate (%.2f%%) exceeds threshold (%.2f%%)",
                                    errorRate, ERROR_RATE_THRESHOLD_PERCENT),
                            createErrorRateAlertDetails(errorRate, totalQueries, totalErrors));
                    errorRateAlert.set(true);
                } else if (!alertCondition && errorRateAlert.get()) {
                    resolveAlert("ERROR_RATE",
                            String.format("GraphQL error rate recovered. Current rate: %.2f%%", errorRate));
                    errorRateAlert.set(false);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to check error rates", e);
        }
    }

    /**
     * Check cache performance
     */
    private void checkCachePerformance() {
        try {
            double cacheHits = getCounterValue("graphql.cache.access", "result", "hit");
            double cacheMisses = getCounterValue("graphql.cache.access", "result", "miss");
            double totalCacheAccess = cacheHits + cacheMisses;

            if (totalCacheAccess > 0) {
                double missRate = (cacheMisses / totalCacheAccess) * 100;
                boolean alertCondition = missRate > CACHE_MISS_RATE_THRESHOLD_PERCENT;

                if (alertCondition && !cacheMissRateAlert.get()) {
                    triggerAlert("CACHE_MISS_RATE",
                            String.format("Cache miss rate (%.2f%%) exceeds threshold (%.2f%%)",
                                    missRate, CACHE_MISS_RATE_THRESHOLD_PERCENT),
                            createCacheAlertDetails(missRate, cacheHits, cacheMisses));
                    cacheMissRateAlert.set(true);
                } else if (!alertCondition && cacheMissRateAlert.get()) {
                    resolveAlert("CACHE_MISS_RATE",
                            String.format("Cache miss rate recovered. Current rate: %.2f%%", missRate));
                    cacheMissRateAlert.set(false);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to check cache performance", e);
        }
    }

    /**
     * Check throughput metrics
     */
    private void checkThroughput() {
        try {
            // This is a simplified throughput check
            // In a real implementation, you'd calculate requests per minute
            double totalQueries = getCounterValue("graphql.query.count");

            // For demonstration, we'll use a simple threshold
            boolean alertCondition = totalQueries > THROUGHPUT_THRESHOLD_PER_MINUTE;

            if (alertCondition && !throughputAlert.get()) {
                triggerAlert("HIGH_THROUGHPUT",
                        String.format("GraphQL throughput (%.0f requests) exceeds threshold (%d)",
                                totalQueries, THROUGHPUT_THRESHOLD_PER_MINUTE),
                        createThroughputAlertDetails(totalQueries));
                throughputAlert.set(true);
            } else if (!alertCondition && throughputAlert.get()) {
                resolveAlert("HIGH_THROUGHPUT",
                        String.format("GraphQL throughput normalized. Current: %.0f requests", totalQueries));
                throughputAlert.set(false);
            }
        } catch (Exception e) {
            log.warn("Failed to check throughput", e);
        }
    }

    /**
     * Trigger an alert with cooldown logic
     */
    private void triggerAlert(String alertType, String message, Map<String, Object> details) {
        long currentTime = System.currentTimeMillis();
        Long lastAlertTime = lastAlertTimes.get(alertType);

        // Check cooldown period
        if (lastAlertTime != null && (currentTime - lastAlertTime) < ALERT_COOLDOWN_MS) {
            log.debug("Alert {} is in cooldown period, skipping", alertType);
            return;
        }

        lastAlertTimes.put(alertType, currentTime);
        totalAlerts.incrementAndGet();

        // Log the alert
        log.error("ALERT TRIGGERED [{}]: {}", alertType, message);
        log.error("Alert details: {}", details);

        // In a real implementation, you would:
        // 1. Send to alerting system (PagerDuty, Slack, etc.)
        // 2. Create incident tickets
        // 3. Send notifications to on-call engineers

        // For now, we'll just increment a metric
        meterRegistry.counter("graphql.alerts.triggered", "type", alertType).increment();
    }

    /**
     * Resolve an alert
     */
    private void resolveAlert(String alertType, String message) {
        log.info("ALERT RESOLVED [{}]: {}", alertType, message);
        meterRegistry.counter("graphql.alerts.resolved", "type", alertType).increment();
    }

    /**
     * Get counter value from meter registry
     */
    private double getCounterValue(String meterName) {
        return meterRegistry.find(meterName).counter() != null ? meterRegistry.find(meterName).counter().count() : 0.0;
    }

    /**
     * Get counter value with tags from meter registry
     */
    private double getCounterValue(String meterName, String tagKey, String tagValue) {
        return meterRegistry.find(meterName).tag(tagKey, tagValue).counter() != null
                ? meterRegistry.find(meterName).tag(tagKey, tagValue).counter().count()
                : 0.0;
    }

    /**
     * Create alert details for response time alerts
     */
    private Map<String, Object> createResponseTimeAlertDetails(double mean, double p95, double threshold) {
        Map<String, Object> details = new HashMap<>();
        details.put("mean_response_time_ms", mean);
        details.put("p95_response_time_ms", p95);
        details.put("threshold_ms", threshold);
        details.put("timestamp", Instant.now().toString());
        return details;
    }

    /**
     * Create alert details for error rate alerts
     */
    private Map<String, Object> createErrorRateAlertDetails(double errorRate, double totalQueries, double totalErrors) {
        Map<String, Object> details = new HashMap<>();
        details.put("error_rate_percent", errorRate);
        details.put("total_queries", totalQueries);
        details.put("total_errors", totalErrors);
        details.put("threshold_percent", ERROR_RATE_THRESHOLD_PERCENT);
        details.put("timestamp", Instant.now().toString());
        return details;
    }

    /**
     * Create alert details for cache alerts
     */
    private Map<String, Object> createCacheAlertDetails(double missRate, double hits, double misses) {
        Map<String, Object> details = new HashMap<>();
        details.put("miss_rate_percent", missRate);
        details.put("cache_hits", hits);
        details.put("cache_misses", misses);
        details.put("threshold_percent", CACHE_MISS_RATE_THRESHOLD_PERCENT);
        details.put("timestamp", Instant.now().toString());
        return details;
    }

    /**
     * Create alert details for throughput alerts
     */
    private Map<String, Object> createThroughputAlertDetails(double totalQueries) {
        Map<String, Object> details = new HashMap<>();
        details.put("total_queries", totalQueries);
        details.put("threshold", THROUGHPUT_THRESHOLD_PER_MINUTE);
        details.put("timestamp", Instant.now().toString());
        return details;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        details.put("last_check", Instant.ofEpochMilli(lastAlertCheck.get()).toString());
        details.put("total_alerts_triggered", totalAlerts.get());
        details.put("active_alerts", getActiveAlerts());
        details.put("alert_thresholds", getAlertThresholds());

        boolean hasActiveAlerts = queryResponseTimeAlert.get() ||
                mutationResponseTimeAlert.get() ||
                errorRateAlert.get() ||
                cacheMissRateAlert.get() ||
                throughputAlert.get();

        return hasActiveAlerts ? Health.down().withDetails(details).build() : Health.up().withDetails(details).build();
    }

    private Map<String, Boolean> getActiveAlerts() {
        Map<String, Boolean> alerts = new HashMap<>();
        alerts.put("query_response_time", queryResponseTimeAlert.get());
        alerts.put("mutation_response_time", mutationResponseTimeAlert.get());
        alerts.put("error_rate", errorRateAlert.get());
        alerts.put("cache_miss_rate", cacheMissRateAlert.get());
        alerts.put("throughput", throughputAlert.get());
        return alerts;
    }

    private Map<String, Object> getAlertThresholds() {
        Map<String, Object> thresholds = new HashMap<>();
        thresholds.put("query_response_time_ms", QUERY_RESPONSE_TIME_THRESHOLD_MS);
        thresholds.put("mutation_response_time_ms", MUTATION_RESPONSE_TIME_THRESHOLD_MS);
        thresholds.put("error_rate_percent", ERROR_RATE_THRESHOLD_PERCENT);
        thresholds.put("cache_miss_rate_percent", CACHE_MISS_RATE_THRESHOLD_PERCENT);
        thresholds.put("throughput_per_minute", THROUGHPUT_THRESHOLD_PER_MINUTE);
        return thresholds;
    }
}