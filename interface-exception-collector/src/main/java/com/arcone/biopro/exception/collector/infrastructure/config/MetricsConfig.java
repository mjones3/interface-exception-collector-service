package com.arcone.biopro.exception.collector.infrastructure.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for custom metrics and monitoring.
 * Provides business KPI metrics as per requirements US-016, US-017.
 */
@Configuration
@RequiredArgsConstructor
public class MetricsConfig {

    private final MeterRegistry meterRegistry;

    /**
     * Counter for total exceptions processed by interface type.
     */
    @Bean
    public Counter exceptionsProcessedCounter() {
        return Counter.builder("exceptions.processed.total")
                .description("Total number of exceptions processed")
                .tag("service", "interface-exception-collector")
                .register(meterRegistry);
    }

    /**
     * Counter for exceptions by severity level.
     */
    @Bean
    public Counter exceptionsBySeverityCounter() {
        return Counter.builder("exceptions.by.severity.total")
                .description("Total number of exceptions by severity level")
                .tag("service", "interface-exception-collector")
                .register(meterRegistry);
    }

    /**
     * Counter for retry operations.
     */
    @Bean
    public Counter retryOperationsCounter() {
        return Counter.builder("retry.operations.total")
                .description("Total number of retry operations")
                .tag("service", "interface-exception-collector")
                .register(meterRegistry);
    }

    /**
     * Counter for successful retry operations.
     */
    @Bean
    public Counter retrySuccessCounter() {
        return Counter.builder("retry.success.total")
                .description("Total number of successful retry operations")
                .tag("service", "interface-exception-collector")
                .register(meterRegistry);
    }

    /**
     * Counter for failed retry operations.
     */
    @Bean
    public Counter retryFailureCounter() {
        return Counter.builder("retry.failure.total")
                .description("Total number of failed retry operations")
                .tag("service", "interface-exception-collector")
                .register(meterRegistry);
    }

    /**
     * Counter for critical alerts generated.
     */
    @Bean
    public Counter criticalAlertsCounter() {
        return Counter.builder("alerts.critical.total")
                .description("Total number of critical alerts generated")
                .tag("service", "interface-exception-collector")
                .register(meterRegistry);
    }

    /**
     * Timer for exception processing duration.
     */
    @Bean
    public Timer exceptionProcessingTimer() {
        return Timer.builder("exception.processing.duration")
                .description("Time taken to process exception events")
                .tag("service", "interface-exception-collector")
                .register(meterRegistry);
    }

    /**
     * Timer for retry operation duration.
     */
    @Bean
    public Timer retryOperationTimer() {
        return Timer.builder("retry.operation.duration")
                .description("Time taken to complete retry operations")
                .tag("service", "interface-exception-collector")
                .register(meterRegistry);
    }

    /**
     * Timer for API response times.
     */
    @Bean
    public Timer apiResponseTimer() {
        return Timer.builder("api.response.duration")
                .description("API endpoint response times")
                .tag("service", "interface-exception-collector")
                .register(meterRegistry);
    }

    /**
     * Timer for external service call duration.
     */
    @Bean
    public Timer externalServiceCallTimer() {
        return Timer.builder("external.service.call.duration")
                .description("Time taken for external service calls")
                .tag("service", "interface-exception-collector")
                .register(meterRegistry);
    }

    /**
     * Counter for Kafka message consumption.
     */
    @Bean
    public Counter kafkaMessagesConsumedCounter() {
        return Counter.builder("kafka.messages.consumed.total")
                .description("Total number of Kafka messages consumed")
                .tag("service", "interface-exception-collector")
                .register(meterRegistry);
    }

    /**
     * Counter for Kafka message production.
     */
    @Bean
    public Counter kafkaMessagesProducedCounter() {
        return Counter.builder("kafka.messages.produced.total")
                .description("Total number of Kafka messages produced")
                .tag("service", "interface-exception-collector")
                .register(meterRegistry);
    }

    /**
     * Counter for database operations.
     */
    @Bean
    public Counter databaseOperationsCounter() {
        return Counter.builder("database.operations.total")
                .description("Total number of database operations")
                .tag("service", "interface-exception-collector")
                .register(meterRegistry);
    }

    /**
     * Timer for database operation duration.
     */
    @Bean
    public Timer databaseOperationTimer() {
        return Timer.builder("database.operation.duration")
                .description("Time taken for database operations")
                .tag("service", "interface-exception-collector")
                .register(meterRegistry);
    }
}