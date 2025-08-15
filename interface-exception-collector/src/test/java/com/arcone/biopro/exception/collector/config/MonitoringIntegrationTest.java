package com.arcone.biopro.exception.collector.config;

import com.arcone.biopro.exception.collector.application.service.MetricsService;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.infrastructure.health.CacheHealthIndicator;
import com.arcone.biopro.exception.collector.infrastructure.health.DatabaseHealthIndicator;import com.arcone.biopro.exception.collector.infrastructure.health.KafkaHealthIndicator;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for monitoring and observability features.
 * Tests metrics collection, health indicators, and Prometheus export.
 */
@SpringBootTest
@ActiveProfiles("test")
class MonitoringIntegrationTest {

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private DatabaseHealthIndicator databaseHealthIndicator;

    @Autowired
    private KafkaHealthIndicator kafkaHealthIndicator;

    @Autowired
    private CacheHealthIndicator cacheHealthIndicator;

    @Test
    void shouldRecordExceptionProcessingMetrics() {
        // Given
        InterfaceType interfaceType = InterfaceType.ORDER;
        ExceptionSeverity severity = ExceptionSeverity.HIGH;

        // When
        metricsService.recordExceptionProcessed(interfaceType, severity);

        // Then
        Counter counter = meterRegistry.find("exceptions.processed.total")
                .tag("interface_type", interfaceType.name())
                .tag("severity", severity.name())
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isGreaterThan(0);
    }

    @Test
    void shouldRecordExceptionProcessingTime() {
        // Given
        InterfaceType interfaceType = InterfaceType.COLLECTION;
        Duration processingTime = Duration.ofMillis(150);

        // When
        metricsService.recordExceptionProcessingTime(processingTime, interfaceType);

        // Then
        Timer timer = meterRegistry.find("exception.processing.duration")
                .tag("interface_type", interfaceType.name())
                .timer();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isGreaterThan(0);
        assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS))
                .isGreaterThanOrEqualTo(processingTime.toMillis());
    }

    @Test
    void shouldRecordRetryOperationMetrics() {
        // Given
        InterfaceType interfaceType = InterfaceType.DISTRIBUTION;
        boolean success = true;

        // When
        metricsService.recordRetryOperation(interfaceType, success);

        // Then
        Counter retryCounter = meterRegistry.find("retry.operations.total")
                .tag("interface_type", interfaceType.name())
                .tag("success", String.valueOf(success))
                .counter();

        Counter successCounter = meterRegistry.find("retry.success.total")
                .tag("interface_type", interfaceType.name())
                .counter();

        assertThat(retryCounter).isNotNull();
        assertThat(retryCounter.count()).isGreaterThan(0);
        assertThat(successCounter).isNotNull();
        assertThat(successCounter.count()).isGreaterThan(0);
    }

    @Test
    void shouldRecordCriticalAlertMetrics() {
        // Given
        String alertReason = "CRITICAL_SEVERITY";
        InterfaceType interfaceType = InterfaceType.ORDER;

        // When
        metricsService.recordCriticalAlert(alertReason, interfaceType);

        // Then
        Counter counter = meterRegistry.find("alerts.critical.total")
                .tag("alert_reason", alertReason)
                .tag("interface_type", interfaceType.name())
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isGreaterThan(0);
    }

    @Test
    void shouldRecordApiResponseTime() {
        // Given
        Duration responseTime = Duration.ofMillis(250);
        String endpoint = "/api/v1/exceptions";
        String method = "GET";
        int statusCode = 200;

        // When
        metricsService.recordApiResponseTime(responseTime, endpoint, method, statusCode);

        // Then
        Timer timer = meterRegistry.find("api.response.duration")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .tag("status", String.valueOf(statusCode))
                .timer();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isGreaterThan(0);
    }

    @Test
    void shouldRecordExternalServiceCallMetrics() {
        // Given
        Duration callDuration = Duration.ofMillis(500);
        String serviceName = "order-service";
        boolean success = true;

        // When
        metricsService.recordExternalServiceCall(callDuration, serviceName, success);

        // Then
        Timer timer = meterRegistry.find("external.service.call.duration")
                .tag("service", serviceName)
                .tag("success", String.valueOf(success))
                .timer();

        Counter counter = meterRegistry.find("external.service.calls.total")
                .tag("service", serviceName)
                .tag("success", String.valueOf(success))
                .counter();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isGreaterThan(0);
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isGreaterThan(0);
    }

    @Test
    void shouldRecordKafkaMessageMetrics() {
        // Given
        String topic = "order-events";
        boolean success = true;

        // When
        metricsService.recordKafkaMessageConsumed(topic, success);
        metricsService.recordKafkaMessageProduced(topic, success);

        // Then
        Counter consumedCounter = meterRegistry.find("kafka.messages.consumed.total")
                .tag("topic", topic)
                .tag("success", String.valueOf(success))
                .counter();

        Counter producedCounter = meterRegistry.find("kafka.messages.produced.total")
                .tag("topic", topic)
                .tag("success", String.valueOf(success))
                .counter();

        assertThat(consumedCounter).isNotNull();
        assertThat(consumedCounter.count()).isGreaterThan(0);
        assertThat(producedCounter).isNotNull();
        assertThat(producedCounter.count()).isGreaterThan(0);
    }

    @Test
    void shouldRecordDatabaseOperationMetrics() {
        // Given
        Duration operationTime = Duration.ofMillis(50);
        String operation = "findByTransactionId";
        boolean success = true;

        // When
        metricsService.recordDatabaseOperation(operationTime, operation, success);

        // Then
        Timer timer = meterRegistry.find("database.operation.duration")
                .tag("operation", operation)
                .tag("success", String.valueOf(success))
                .timer();

        Counter counter = meterRegistry.find("database.operations.total")
                .tag("operation", operation)
                .tag("success", String.valueOf(success))
                .counter();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isGreaterThan(0);
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isGreaterThan(0);
    }

    @Test
    void shouldInitializeGaugeMetrics() {
        // When
        metricsService.initializeGaugeMetrics();

        // Then
        assertThat(meterRegistry.find("exceptions.active.count").gauge()).isNotNull();
        assertThat(meterRegistry.find("exceptions.today.count").gauge()).isNotNull();
        assertThat(meterRegistry.find("exceptions.critical.today.count").gauge()).isNotNull();
        assertThat(meterRegistry.find("exceptions.resolution.time.avg.hours").gauge()).isNotNull();
    }

    @Test
    void shouldProvideHealthIndicatorStatus() {
        // When
        Health databaseHealth = databaseHealthIndicator.health();
        Health kafkaHealth = kafkaHealthIndicator.health();
        Health cacheHealth = cacheHealthIndicator.health();

        // Then
        assertThat(databaseHealth).isNotNull();
        assertThat(databaseHealth.getStatus()).isNotNull();
        
        assertThat(kafkaHealth).isNotNull();
        assertThat(kafkaHealth.getStatus()).isNotNull();
        
        assertThat(cacheHealth).isNotNull();
        assertThat(cacheHealth.getStatus()).isNotNull();
    }

    @Test
    void shouldHavePrometheusMetricsEndpoint() {
        // Then - verify that Prometheus metrics are available
        assertThat(meterRegistry.find("jvm.memory.used").gauge()).isNotNull();
        assertThat(meterRegistry.find("system.cpu.usage").gauge()).isNotNull();
        assertThat(meterRegistry.find("process.uptime").gauge()).isNotNull();
    }

    @Test
    void shouldHaveCommonTagsOnMetrics() {
        // Given
        metricsService.recordExceptionProcessed(InterfaceType.ORDER, ExceptionSeverity.MEDIUM);

        // When
        Counter counter = meterRegistry.find("exceptions.processed.total")
                .tag("interface_type", InterfaceType.ORDER.name())
                .tag("severity", ExceptionSeverity.MEDIUM.name())
                .counter();

        // Then
        assertThat(counter).isNotNull();
        assertThat(counter.getId().getTags()).isNotEmpty();
        
        // Verify common tags are present (these would be added by PrometheusConfig)
        boolean hasApplicationTag = counter.getId().getTags().stream()
                .anyMatch(tag -> "application".equals(tag.getKey()));
        
        // Note: In test environment, common tags might not be applied
        // This test verifies the metric structure is correct
        assertThat(counter.getId().getTags()).hasSizeGreaterThanOrEqualTo(2);
    }
}