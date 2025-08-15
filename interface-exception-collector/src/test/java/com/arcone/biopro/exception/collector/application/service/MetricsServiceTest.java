package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Test class for MetricsService to verify metrics recording functionality.
 */
@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    private MeterRegistry meterRegistry;
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new MetricsService(meterRegistry, exceptionRepository);
    }

    @Test
    void shouldRecordExceptionProcessed() {
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
        assertThat(counter.count()).isEqualTo(1.0);
    }

@Test
    void shouldRecordExceptionProcessingTime() {
        // Given
        Duration duration = Duration.ofMillis(150);
        InterfaceType interfaceType = InterfaceType.COLLECTION;

        // When
        metricsService.recordExceptionProcessingTime(duration, interfaceType);

        // Then
        Timer timer = meterRegistry.find("exception.processing.duration")
                .tag("interface_type", interfaceType.name())
                .timer();
        
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isEqualTo(150.0);
    }

    @Test
    void shouldRecordRetryOperationSuccess() {
        // Given
        InterfaceType interfaceType = InterfaceType.DISTRIBUTION;
        boolean success = true;

        // When
        metricsService.recordRetryOperation(interfaceType, success);

        // Then
        Counter operationsCounter = meterRegistry.find("retry.operations.total")
                .tag("interface_type", interfaceType.name())
                .tag("success", "true")
                .counter();
        
        Counter successCounter = meterRegistry.find("retry.success.total")
                .tag("interface_type", interfaceType.name())
                .counter();
        
        assertThat(operationsCounter).isNotNull();
        assertThat(operationsCounter.count()).isEqualTo(1.0);
        assertThat(successCounter).isNotNull();
        assertThat(successCounter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordRetryOperationFailure() {
        // Given
        InterfaceType interfaceType = InterfaceType.ORDER;
        boolean success = false;

        // When
        metricsService.recordRetryOperation(interfaceType, success);

        // Then
        Counter operationsCounter = meterRegistry.find("retry.operations.total")
                .tag("interface_type", interfaceType.name())
                .tag("success", "false")
                .counter();
        
        Counter failureCounter = meterRegistry.find("retry.failure.total")
                .tag("interface_type", interfaceType.name())
                .counter();
        
        assertThat(operationsCounter).isNotNull();
        assertThat(operationsCounter.count()).isEqualTo(1.0);
        assertThat(failureCounter).isNotNull();
        assertThat(failureCounter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordRetryOperationTime() {
        // Given
        Duration duration = Duration.ofSeconds(2);
        InterfaceType interfaceType = InterfaceType.COLLECTION;
        boolean success = true;

        // When
        metricsService.recordRetryOperationTime(duration, interfaceType, success);

        // Then
        Timer timer = meterRegistry.find("retry.operation.duration")
                .tag("interface_type", interfaceType.name())
                .tag("success", "true")
                .timer();
        
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.totalTime(java.util.concurrent.TimeUnit.SECONDS)).isEqualTo(2.0);
    }

    @Test
    void shouldRecordCriticalAlert() {
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
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordApiResponseTime() {
        // Given
        Duration duration = Duration.ofMillis(250);
        String endpoint = "/api/v1/exceptions";
        String method = "GET";
        int statusCode = 200;

        // When
        metricsService.recordApiResponseTime(duration, endpoint, method, statusCode);

        // Then
        Timer timer = meterRegistry.find("api.response.duration")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .tag("status", "200")
                .timer();
        
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isEqualTo(250.0);
    }

    @Test
    void shouldRecordExternalServiceCall() {
        // Given
        Duration duration = Duration.ofMillis(500);
        String serviceName = "order-service";
        boolean success = true;

        // When
        metricsService.recordExternalServiceCall(duration, serviceName, success);

        // Then
        Timer timer = meterRegistry.find("external.service.call.duration")
                .tag("service", serviceName)
                .tag("success", "true")
                .timer();
        
        Counter counter = meterRegistry.find("external.service.calls.total")
                .tag("service", serviceName)
                .tag("success", "true")
                .counter();
        
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isEqualTo(500.0);
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordKafkaMessageConsumed() {
        // Given
        String topic = "order-events";
        boolean success = true;

        // When
        metricsService.recordKafkaMessageConsumed(topic, success);

        // Then
        Counter counter = meterRegistry.find("kafka.messages.consumed.total")
                .tag("topic", topic)
                .tag("success", "true")
                .counter();
        
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordKafkaMessageProduced() {
        // Given
        String topic = "exception-events";
        boolean success = true;

        // When
        metricsService.recordKafkaMessageProduced(topic, success);

        // Then
        Counter counter = meterRegistry.find("kafka.messages.produced.total")
                .tag("topic", topic)
                .tag("success", "true")
                .counter();
        
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordDatabaseOperation() {
        // Given
        Duration duration = Duration.ofMillis(50);
        String operation = "findByTransactionId";
        boolean success = true;

        // When
        metricsService.recordDatabaseOperation(duration, operation, success);

        // Then
        Timer timer = meterRegistry.find("database.operation.duration")
                .tag("operation", operation)
                .tag("success", "true")
                .timer();
        
        Counter counter = meterRegistry.find("database.operations.total")
                .tag("operation", operation)
                .tag("success", "true")
                .counter();
        
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isEqualTo(50.0);
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldGetActiveExceptionsCount() {
        // Given
        when(exceptionRepository.countByStatusIn(anyList())).thenReturn(5L);

        // When
        double count = metricsService.getActiveExceptionsCount();

        // Then
        assertThat(count).isEqualTo(5.0);
    }

    @Test
    void shouldGetTotalExceptionsToday() {
        // Given
        metricsService.recordExceptionProcessed(InterfaceType.ORDER, ExceptionSeverity.MEDIUM);
        metricsService.recordExceptionProcessed(InterfaceType.COLLECTION, ExceptionSeverity.HIGH);

        // When
        double count = metricsService.getTotalExceptionsToday();

        // Then
        assertThat(count).isEqualTo(2.0);
    }

    @Test
    void shouldGetCriticalExceptionsToday() {
        // Given
        metricsService.recordExceptionProcessed(InterfaceType.ORDER, ExceptionSeverity.CRITICAL);
        metricsService.recordExceptionProcessed(InterfaceType.COLLECTION, ExceptionSeverity.MEDIUM);

        // When
        double count = metricsService.getCriticalExceptionsToday();

        // Then
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void shouldGetAverageResolutionTimeHours() {
        // Given
        InterfaceException exception = new InterfaceException();
        exception.setProcessedAt(OffsetDateTime.now().minusHours(2));
        exception.setResolvedAt(OffsetDateTime.now());
        
        when(exceptionRepository.findByResolvedAtAfter(any(Instant.class)))
                .thenReturn(List.of(exception));

        // When
        double avgTime = metricsService.getAverageResolutionTimeHours();

        // Then
        assertThat(avgTime).isEqualTo(2.0);
    }

    @Test
    void shouldReturnZeroWhenNoResolvedExceptions() {
        // Given
        when(exceptionRepository.findByResolvedAtAfter(any(Instant.class)))
                .thenReturn(Collections.emptyList());

        // When
        double avgTime = metricsService.getAverageResolutionTimeHours();

        // Then
        assertThat(avgTime).isEqualTo(0.0);
    }

    @Test
    void shouldResetDailyCounters() {
        // Given
        metricsService.recordExceptionProcessed(InterfaceType.ORDER, ExceptionSeverity.CRITICAL);
        assertThat(metricsService.getTotalExceptionsToday()).isEqualTo(1.0);
        assertThat(metricsService.getCriticalExceptionsToday()).isEqualTo(1.0);

        // When
        metricsService.resetDailyCounters();

        // Then
        assertThat(metricsService.getTotalExceptionsToday()).isEqualTo(0.0);
        assertThat(metricsService.getCriticalExceptionsToday()).isEqualTo(0.0);
    }
}