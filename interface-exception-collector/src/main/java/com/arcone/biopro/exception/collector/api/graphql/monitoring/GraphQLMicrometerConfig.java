package com.arcone.biopro.exception.collector.api.graphql.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.Duration;

/**
 * Micrometer configuration for GraphQL metrics collection and Prometheus
 * integration. Extends existing monitoring infrastructure to include GraphQL
 * metrics.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "graphql.features.metrics-enabled", havingValue = "true", matchIfMissing = false)
public class GraphQLMicrometerConfig {

        /**
         * Customize meter registry with GraphQL-specific configurations
         * This extends the existing meter registry rather than creating a new one
         */
        @Bean
        public MeterRegistryCustomizer<MeterRegistry> graphqlMeterRegistryCustomizer(Environment environment) {
                return registry -> {
                        // Add GraphQL-specific common tags
                        registry.config()
                                        .commonTags("api_type", "graphql")

                                        // Configure meter filters for GraphQL metrics
                                        .meterFilter(MeterFilter.deny(id -> {
                                                // Exclude noisy metrics that aren't useful for GraphQL monitoring
                                                String name = id.getName();
                                                return name.startsWith("jvm.gc.pause") &&
                                                                id.getTag("cause") != null &&
                                                                id.getTag("cause").contains("Metadata");
                                        }))

                                        // Rename GraphQL metrics for better organization and dashboard compatibility
                                        .meterFilter(MeterFilter.renameTag("graphql_query_count_total", "operation",
                                                        "query_name"))
                                        .meterFilter(MeterFilter.renameTag("graphql_mutation_count_total", "operation",
                                                        "mutation_name"))
                                        .meterFilter(
                                                        MeterFilter.renameTag("graphql_subscription_count_total",
                                                                        "operation", "subscription_name"))

                                        // Configure meter filters for GraphQL metrics
                                        .meterFilter(MeterFilter.accept())

                                        // Accept GraphQL metrics and related metrics
                                        .meterFilter(MeterFilter.accept(id -> {
                                                String name = id.getName();
                                                return name.startsWith("graphql_") ||
                                                                name.startsWith("exception_") ||
                                                                name.startsWith("cache_") ||
                                                                name.startsWith("database_") ||
                                                                name.startsWith("http_server_requests");
                                        }));

                        String env = environment.getProperty("ENVIRONMENT", "local");
                        log.info("Configured GraphQL meter registry customizations for environment: {}", env);
                };
        }

        /**
         * Configure GraphQL metrics instrumentation bean
         */
        @Bean
        public GraphQLMetrics graphqlMetrics(MeterRegistry meterRegistry) {
                log.info("Initializing GraphQL metrics instrumentation");
                return new GraphQLMetrics(meterRegistry);
        }

        /**
         * Configure alerting thresholds as Micrometer gauges
         */
        @Bean
        public GraphQLAlertingThresholds graphqlAlertingThresholds(MeterRegistry meterRegistry,
                        Environment environment) {
                return new GraphQLAlertingThresholds(meterRegistry, environment);
        }

        /**
         * Alerting thresholds configuration for GraphQL metrics
         * These thresholds are exposed as gauges for Prometheus alerting rules
         */
        public static class GraphQLAlertingThresholds {

                private final MeterRegistry meterRegistry;
                private final Environment environment;

                // Default thresholds (can be overridden via environment variables)
                private final double queryResponseTimeThreshold;
                private final double mutationResponseTimeThreshold;
                private final double fieldFetchThreshold;
                private final double errorRateThreshold;
                private final double cacheMissRateThreshold;
                private final double maxConcurrentQueries;
                private final double maxSubscriptionConnections;

                public GraphQLAlertingThresholds(MeterRegistry meterRegistry, Environment environment) {
                        this.meterRegistry = meterRegistry;
                        this.environment = environment;

                        // Load thresholds from configuration
                        this.queryResponseTimeThreshold = environment.getProperty(
                                        "graphql.monitoring.alerting.thresholds.query-response-time-ms", Double.class,
                                        500.0);
                        this.mutationResponseTimeThreshold = environment.getProperty(
                                        "graphql.monitoring.alerting.thresholds.mutation-response-time-ms",
                                        Double.class, 3000.0);
                        this.fieldFetchThreshold = environment.getProperty(
                                        "graphql.monitoring.alerting.thresholds.field-fetch-time-ms", Double.class,
                                        100.0);
                        this.errorRateThreshold = environment.getProperty(
                                        "graphql.monitoring.alerting.thresholds.error-rate-percent", Double.class, 5.0);
                        this.cacheMissRateThreshold = environment.getProperty(
                                        "graphql.monitoring.alerting.thresholds.cache-miss-rate-percent", Double.class,
                                        20.0);
                        this.maxConcurrentQueries = environment.getProperty(
                                        "graphql.monitoring.alerting.thresholds.max-concurrent-queries", Double.class,
                                        100.0);
                        this.maxSubscriptionConnections = environment.getProperty(
                                        "graphql.monitoring.alerting.thresholds.max-subscription-connections",
                                        Double.class, 1000.0);

                        registerThresholdGauges();
                }

                private void registerThresholdGauges() {
                        // Register threshold values as gauges for alerting rules

                        // Response time thresholds
                        io.micrometer.core.instrument.Gauge
                                        .builder("graphql_alert_threshold_query_response_time_ms", this,
                                                        obj -> obj.queryResponseTimeThreshold)
                                        .description("Alert threshold for GraphQL query response time")
                                        .register(meterRegistry);

                        io.micrometer.core.instrument.Gauge
                                        .builder("graphql_alert_threshold_mutation_response_time_ms", this,
                                                        obj -> obj.mutationResponseTimeThreshold)
                                        .description("Alert threshold for GraphQL mutation response time")
                                        .register(meterRegistry);

                        io.micrometer.core.instrument.Gauge
                                        .builder("graphql_alert_threshold_field_fetch_time_ms", this,
                                                        obj -> obj.fieldFetchThreshold)
                                        .description("Alert threshold for GraphQL field fetch time")
                                        .register(meterRegistry);

                        // Error rate thresholds
                        io.micrometer.core.instrument.Gauge
                                        .builder("graphql_alert_threshold_error_rate_percent", this,
                                                        obj -> obj.errorRateThreshold)
                                        .description("Alert threshold for GraphQL error rate")
                                        .register(meterRegistry);

                        io.micrometer.core.instrument.Gauge
                                        .builder("graphql_alert_threshold_cache_miss_rate_percent", this,
                                                        obj -> obj.cacheMissRateThreshold)
                                        .description("Alert threshold for cache miss rate")
                                        .register(meterRegistry);

                        // Throughput thresholds
                        io.micrometer.core.instrument.Gauge
                                        .builder("graphql_alert_threshold_max_concurrent_queries", this,
                                                        obj -> obj.maxConcurrentQueries)
                                        .description("Alert threshold for maximum concurrent queries")
                                        .register(meterRegistry);

                        io.micrometer.core.instrument.Gauge
                                        .builder("graphql_alert_threshold_max_subscription_connections", this,
                                                        obj -> obj.maxSubscriptionConnections)
                                        .description("Alert threshold for maximum subscription connections")
                                        .register(meterRegistry);

                        log.info("Registered GraphQL alerting thresholds as Micrometer gauges:");
                        log.info("  Query response time: {}ms", queryResponseTimeThreshold);
                        log.info("  Mutation response time: {}ms", mutationResponseTimeThreshold);
                        log.info("  Field fetch time: {}ms", fieldFetchThreshold);
                        log.info("  Error rate: {}%", errorRateThreshold);
                        log.info("  Cache miss rate: {}%", cacheMissRateThreshold);
                        log.info("  Max concurrent queries: {}", maxConcurrentQueries);
                        log.info("  Max subscription connections: {}", maxSubscriptionConnections);
                }

                // Getter methods for threshold values
                public double getQueryResponseTimeThreshold() {
                        return queryResponseTimeThreshold;
                }

                public double getMutationResponseTimeThreshold() {
                        return mutationResponseTimeThreshold;
                }

                public double getFieldFetchThreshold() {
                        return fieldFetchThreshold;
                }

                public double getErrorRateThreshold() {
                        return errorRateThreshold;
                }

                public double getCacheMissRateThreshold() {
                        return cacheMissRateThreshold;
                }

                public double getMaxConcurrentQueries() {
                        return maxConcurrentQueries;
                }

                public double getMaxSubscriptionConnections() {
                        return maxSubscriptionConnections;
                }
        }
}