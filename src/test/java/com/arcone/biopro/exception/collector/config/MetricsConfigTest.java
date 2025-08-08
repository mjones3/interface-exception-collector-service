package com.arcone.biopro.exception.collector.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for MetricsConfig to verify metrics beans are properly configured.
 */
@ExtendWith(MockitoExtension.class)
class MetricsConfigTest {

    private MeterRegistry meterRegistry;
    private MetricsConfig metricsConfig;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsConfig = new MetricsConfig(meterRegistry);
    }

    @Test
    void shouldCreateExceptionsProcessedCounter() {
        // When
        Counter counter = metricsConfig.exceptionsProcessedCounter();

        // Then
        assertThat(counter).isNotNull();
        assertThat(counter.getId().getName()).isEqualTo("exceptions.processed.total");
        assertThat(counter.getId().getTag("service")).isEqualTo("interface-exception-collector");
    }

    @Test
    void shouldCreateExceptionsBySeverityCounter() {
        // When
        Counter counter = metricsConfig.exceptionsBySeverityCounter();

        // Then
        assertThat(counter).isNotNull();
        assertThat(counter.getId().getName()).isEqualTo("exceptions.by.severity.total");
        assertThat(counter.getId().getTag("service")).isEqualTo("interface-exception-collector");
    }

    @Test
    void shouldCreateRetryOperationsCounter() {
        // When
        Counter counter = metricsConfig.retryOperationsCounter();

        // Then
        assertThat(counter).isNotNull();
        assertThat(counter.getId().getName()).isEqualTo("retry.operations.total");
        assertThat(counter.getId().getTag("service")).isEqualTo("interface-exception-collector");
    }

    @Test
    void shouldCreateRetrySuccessCounter() {
        // When
        Counter counter = metricsConfig.retrySuccessCounter();

        // Then
        assertThat(counter).isNotNull();
        assertThat(counter.getId().getName()).isEqualTo("retry.success.total");
        assertThat(counter.getId().getTag("service")).isEqualTo("interface-exception-collector");
    }

    @Test
    void shouldCreateRetryFailureCounter() {
        // When
        Counter counter = metricsConfig.retryFailureCounter();

        // Then
        assertThat(counter).isNotNull();
        assertThat(counter.getId().getName()).isEqualTo("retry.failure.total");
        assertThat(counter.getId().getTag("service")).isEqualTo("interface-exception-collector");
    }

    @Test
    void shouldCreateCriticalAlertsCounter() {
        // When
        Counter counter = metricsConfig.criticalAlertsCounter();

        // Then
        assertThat(counter).isNotNull();
        assertThat(counter.getId().getName()).isEqualTo("alerts.critical.total");
        assertThat(counter.getId().getTag("service")).isEqualTo("interface-exception-collector");
    }

    @Test
    void shouldCreateExceptionProcessingTimer() {
        // When
        Timer timer = metricsConfig.exceptionProcessingTimer();

        // Then
        assertThat(timer).isNotNull();
        assertThat(timer.getId().getName()).isEqualTo("exception.processing.duration");
        assertThat(timer.getId().getTag("service")).isEqualTo("interface-exception-collector");
    }

    @Test
    void shouldCreateRetryOperationTimer() {
        // When
        Timer timer = metricsConfig.retryOperationTimer();

        // Then
        assertThat(timer).isNotNull();
        assertThat(timer.getId().getName()).isEqualTo("retry.operation.duration");
        assertThat(timer.getId().getTag("service")).isEqualTo("interface-exception-collector");
    }

    @Test
    void shouldCreateApiResponseTimer() {
        // When
        Timer timer = metricsConfig.apiResponseTimer();

        // Then
        assertThat(timer).isNotNull();
        assertThat(timer.getId().getName()).isEqualTo("api.response.duration");
        assertThat(timer.getId().getTag("service")).isEqualTo("interface-exception-collector");
    }

    @Test
    void shouldCreateExternalServiceCallTimer() {
        // When
        Timer timer = metricsConfig.externalServiceCallTimer();

        // Then
        assertThat(timer).isNotNull();
        assertThat(timer.getId().getName()).isEqualTo("external.service.call.duration");
        assertThat(timer.getId().getTag("service")).isEqualTo("interface-exception-collector");
    }

    @Test
    void shouldCreateKafkaMessagesConsumedCounter() {
        // When
        Counter counter = metricsConfig.kafkaMessagesConsumedCounter();

        // Then
        assertThat(counter).isNotNull();
        assertThat(counter.getId().getName()).isEqualTo("kafka.messages.consumed.total");
        assertThat(counter.getId().getTag("service")).isEqualTo("interface-exception-collector");
    }

    @Test
    void shouldCreateKafkaMessagesProducedCounter() {
        // When
        Counter counter = metricsConfig.kafkaMessagesProducedCounter();

        // Then
        assertThat(counter).isNotNull();
        assertThat(counter.getId().getName()).isEqualTo("kafka.messages.produced.total");
        assertThat(counter.getId().getTag("service")).isEqualTo("interface-exception-collector");
    }

    @Test
    void shouldCreateDatabaseOperationsCounter() {
        // When
        Counter counter = metricsConfig.databaseOperationsCounter();

        // Then
        assertThat(counter).isNotNull();
        assertThat(counter.getId().getName()).isEqualTo("database.operations.total");
        assertThat(counter.getId().getTag("service")).isEqualTo("interface-exception-collector");
    }

    @Test
    void shouldCreateDatabaseOperationTimer() {
        // When
        Timer timer = metricsConfig.databaseOperationTimer();

        // Then
        assertThat(timer).isNotNull();
        assertThat(timer.getId().getName()).isEqualTo("database.operation.duration");
        assertThat(timer.getId().getTag("service")).isEqualTo("interface-exception-collector");
    }
}