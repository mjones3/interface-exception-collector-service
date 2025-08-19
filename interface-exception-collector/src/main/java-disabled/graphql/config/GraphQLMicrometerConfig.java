package com.arcone.biopro.exception.collector.api.graphql.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcMetricsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Micrometer configuration for GraphQL metrics collection and Prometheus
 * integration.
 * Configures custom metrics, filters, and registry settings for monitoring
 * GraphQL operations.
 */
@Slf4j
@Configuration
public class GraphQLMicrometerConfig {

    /**
     * Configure Prometheus meter registry with custom settings
     */
    @Bean
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        // Add common tags to all metrics
        registry.config()
                .commonTags(
                        "application", "interface-exception-collector-service",
                        "component", "graphql-api");

        log.info("Configured Prometheus meter registry for GraphQL metrics");
        return registry;
    }

    /**
     * Customize meter registry with GraphQL-specific configurations
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> graphqlMeterRegistryCustomizer(Environment environment) {
        return registry -> {
            // Add environment-specific tags
            String env = environment.getProperty("ENVIRONMENT", "local");
            String version = environment.getProperty("BUILD_VERSION", "1.0.0-SNAPSHOT");

            registry.config()
                    .commonTags(
                            "environment", env,
                            "version", version)
                    // Configure meter filters for GraphQL metrics
                    .meterFilter(MeterFilter.deny(id -> {
                        // Exclude noisy metrics that aren't useful for GraphQL monitoring
                        String name = id.getName();
                        return name.startsWith("jvm.gc.pause") &&
                                id.getTag("cause") != null &&
                                id.getTag("cause").contains("Metadata");
                    }))
                    .meterFilter(MeterFilter.denyNameStartsWith("tomcat.sessions"))
                    .meterFilter(MeterFilter.denyNameStartsWith("process.files"))

                    // Rename GraphQL metrics for better organization
                    .meterFilter(MeterFilter.renameTag("graphql.query.count", "operation", "query_name"))
                    .meterFilter(MeterFilter.renameTag("graphql.mutation.count", "operation", "mutation_name"))
                    .meterFilter(MeterFilter.renameTag("graphql.subscription.count", "operation", "subscription_name"))

                    // Configure histogram buckets for GraphQL timing metrics
                    .meterFilter(MeterFilter.maximumExpectedValue("graphql.query.duration",
                            java.time.Duration.ofSeconds(30)))
                    .meterFilter(MeterFilter.maximumExpectedValue("graphql.mutation.duration",
                            java.time.Duration.ofSeconds(10)))
                    .meterFilter(MeterFilter.maximumExpectedValue("graphql.field.fetch.duration",
                            java.time.Duration.ofMillis(500)))

                    // Configure percentiles for key GraphQL metrics
                    .meterFilter(MeterFilter.accept(id -> {
                        String name = id.getName();
                        return name.startsWith("graphql.") ||
                                name.startsWith("exception.") ||
                                name.startsWith("cache.") ||
                                name.startsWith("database.");
                    }));

            log.info("Configured GraphQL meter registry customizations for environment: {}", env);
        };
    }

    /**
     * Configure custom GraphQL metrics beans
     */
    @Bean
    public GraphQLMetrics graphqlMetrics(MeterRegistry meterRegistry) {
        return new GraphQLMetrics(meterRegistry);
    }

    /**
     * Configure WebMVC metrics filter to exclude GraphQL endpoint from standard
     * HTTP metrics
     * since we have custom GraphQL metrics
     */
    // @Bean
    // public WebMvcMetricsFilter webMvcMetricsFilter(MeterRegistry meterRegistry) {
    // return new WebMvcMetricsFilter(meterRegistry,
    // new GraphQLWebMvcTagsProvider(),
    // "http.server.requests",
    // true);
    // }

    /**
     * Custom tags provider for WebMVC metrics that handles GraphQL endpoints
     * specially
     */
    private static class GraphQLWebMvcTagsProvider
            implements io.micrometer.core.instrument.Tags {

        // This is a placeholder - in a real implementation, you'd extend
        // DefaultWebMvcTagsProvider and customize GraphQL endpoint tagging

        @Override
        public java.util.Iterator<io.micrometer.core.instrument.Tag> iterator() {
            return java.util.Collections.emptyIterator();
        }
    }

    /**
     * Configure alerting thresholds as Micrometer gauges
     */
    @Bean
    public AlertingThresholds alertingThresholds(MeterRegistry meterRegistry) {
        return new AlertingThresholds(meterRegistry);
    }

    /**
     * Alerting thresholds configuration for GraphQL metrics
     */
    public static class AlertingThresholds {

        private final MeterRegistry meterRegistry;

        // Response time thresholds (in milliseconds)
        private static final double QUERY_RESPONSE_TIME_THRESHOLD = 500.0;
        private static final double MUTATION_RESPONSE_TIME_THRESHOLD = 3000.0;
        private static final double FIELD_FETCH_THRESHOLD = 100.0;

        // Error rate thresholds (percentage)
        private static final double ERROR_RATE_THRESHOLD = 5.0;
        private static final double CACHE_MISS_RATE_THRESHOLD = 20.0;

        // Throughput thresholds
        private static final double MAX_CONCURRENT_QUERIES = 100.0;
        private static final double MAX_SUBSCRIPTION_CONNECTIONS = 1000.0;

        public AlertingThresholds(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
            registerThresholdGauges();
        }

        private void registerThresholdGauges() {
            // Register threshold values as gauges for alerting rules

            // Response time thresholds
            io.micrometer.core.instrument.Gauge.builder("graphql.alert.threshold.query_response_time_ms")
                    .description("Alert threshold for GraphQL query response time")
                    .register(meterRegistry, this, obj -> QUERY_RESPONSE_TIME_THRESHOLD);

            io.micrometer.core.instrument.Gauge.builder("graphql.alert.threshold.mutation_response_time_ms")
                    .description("Alert threshold for GraphQL mutation response time")
                    .register(meterRegistry, this, obj -> MUTATION_RESPONSE_TIME_THRESHOLD);

            io.micrometer.core.instrument.Gauge.builder("graphql.alert.threshold.field_fetch_time_ms")
                    .description("Alert threshold for GraphQL field fetch time")
                    .register(meterRegistry, this, obj -> FIELD_FETCH_THRESHOLD);

            // Error rate thresholds
            io.micrometer.core.instrument.Gauge.builder("graphql.alert.threshold.error_rate_percent")
                    .description("Alert threshold for GraphQL error rate")
                    .register(meterRegistry, this, obj -> ERROR_RATE_THRESHOLD);

            io.micrometer.core.instrument.Gauge.builder("graphql.alert.threshold.cache_miss_rate_percent")
                    .description("Alert threshold for cache miss rate")
                    .register(meterRegistry, this, obj -> CACHE_MISS_RATE_THRESHOLD);

            // Throughput thresholds
            io.micrometer.core.instrument.Gauge.builder("graphql.alert.threshold.max_concurrent_queries")
                    .description("Alert threshold for maximum concurrent queries")
                    .register(meterRegistry, this, obj -> MAX_CONCURRENT_QUERIES);

            io.micrometer.core.instrument.Gauge.builder("graphql.alert.threshold.max_subscription_connections")
                    .description("Alert threshold for maximum subscription connections")
                    .register(meterRegistry, this, obj -> MAX_SUBSCRIPTION_CONNECTIONS);

            log.info("Registered GraphQL alerting thresholds as Micrometer gauges");
        }

        // Getter methods for threshold values
        public double getQueryResponseTimeThreshold() {
            return QUERY_RESPONSE_TIME_THRESHOLD;
        }

        public double getMutationResponseTimeThreshold() {
            return MUTATION_RESPONSE_TIME_THRESHOLD;
        }

        public double getFieldFetchThreshold() {
            return FIELD_FETCH_THRESHOLD;
        }

        public double getErrorRateThreshold() {
            return ERROR_RATE_THRESHOLD;
        }

        public double getCacheMissRateThreshold() {
            return CACHE_MISS_RATE_THRESHOLD;
        }

        public double getMaxConcurrentQueries() {
            return MAX_CONCURRENT_QUERIES;
        }

        public double getMaxSubscriptionConnections() {
            return MAX_SUBSCRIPTION_CONNECTIONS;
        }
    }
}