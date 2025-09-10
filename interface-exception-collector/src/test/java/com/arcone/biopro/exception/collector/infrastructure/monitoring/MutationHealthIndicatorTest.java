package com.arcone.biopro.exception.collector.infrastructure.monitoring;

import com.arcone.biopro.exception.collector.application.service.ExceptionManagementService;
import com.arcone.biopro.exception.collector.application.service.RetryService;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
// Health import removed - using stub
// Status import removed - using stub

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

/**
 * Unit tests for MutationHealthIndicator.
 * Tests health check logic for mutation services and components.
 */
@ExtendWith(MockitoExtension.class)
class MutationHealthIndicatorTest {

    @Mock
    private RetryService retryService;

    @Mock
    private ExceptionManagementService exceptionManagementService;

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    @Mock
    private MutationMetrics mutationMetrics;

    private MutationHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new MutationHealthIndicator(
                retryService,
                exceptionManagementService,
                exceptionRepository,
                mutationMetrics
        );
    }

    @Test
    void shouldReturnUpWhenAllServicesHealthy() {
        // Given
        when(exceptionRepository.count()).thenReturn(100L);
        when(mutationMetrics.getSuccessRate("retry")).thenReturn(0.98);
        when(mutationMetrics.getSuccessRate("acknowledge")).thenReturn(0.97);
        when(mutationMetrics.getSuccessRate("resolve")).thenReturn(0.99);
        when(mutationMetrics.getSuccessRate("cancel_retry")).thenReturn(0.96);
        when(mutationMetrics.getTotalOperations("retry")).thenReturn(50L);
        when(mutationMetrics.getTotalOperations("acknowledge")).thenReturn(30L);
        when(mutationMetrics.getTotalOperations("resolve")).thenReturn(20L);
        when(mutationMetrics.getTotalOperations("cancel_retry")).thenReturn(10L);

        // When
        Object health = healthIndicator.toString();

        // Then
        assertThat(health.getStatus()).isEqualTo("UP");
        assertThat(health.getDetails()).containsKey("database_status");
        assertThat(health.getDetails().get("database_status")).isEqualTo("operational");
        assertThat(health.getDetails()).containsKey("retry_service_status");
        assertThat(health.getDetails().get("retry_service_status")).isEqualTo("operational");
        assertThat(health.getDetails()).containsKey("exception_management_status");
        assertThat(health.getDetails().get("exception_management_status")).isEqualTo("operational");
        assertThat(health.getDetails()).containsKey("total_exceptions");
        assertThat(health.getDetails().get("total_exceptions")).isEqualTo(100L);
        assertThat(health.getDetails()).containsKey("health_check_duration_ms");
    }

    @Test
    void shouldReturnDownWhenDatabaseUnhealthy() {
        // Given
        when(exceptionRepository.count()).thenThrow(new RuntimeException("Database connection failed"));

        // When
        Object health = healthIndicator.toString();

        // Then
        assertThat(health.getStatus()).isEqualTo("DOWN");
        assertThat(health.getDetails()).containsKey("database_status");
        assertThat(health.getDetails().get("database_status")).isEqualTo("degraded");
        assertThat(health.getDetails()).containsKey("database_error");
        assertThat(health.getDetails().get("database_error")).isEqualTo("Database connection failed");
    }

    @Test
    void shouldReturnDownWhenRetrySuccessRateLow() {
        // Given
        when(exceptionRepository.count()).thenReturn(100L);
        when(mutationMetrics.getSuccessRate("retry")).thenReturn(0.90); // Below 95% threshold
        when(mutationMetrics.getTotalOperations("retry")).thenReturn(50L); // Above minimum threshold
        when(mutationMetrics.getSuccessRate("acknowledge")).thenReturn(0.97);
        when(mutationMetrics.getSuccessRate("resolve")).thenReturn(0.99);
        when(mutationMetrics.getSuccessRate("cancel_retry")).thenReturn(0.96);
        when(mutationMetrics.getTotalOperations("acknowledge")).thenReturn(30L);
        when(mutationMetrics.getTotalOperations("resolve")).thenReturn(20L);
        when(mutationMetrics.getTotalOperations("cancel_retry")).thenReturn(10L);

        // When
        Object health = healthIndicator.toString();

        // Then
        assertThat(health.getStatus()).isEqualTo("DOWN");
        assertThat(health.getDetails()).containsKey("retry_service_status");
        assertThat(health.getDetails().get("retry_service_status")).isEqualTo("operational");
        assertThat(health.getDetails()).containsKey("retry_service_warning");
        assertThat(health.getDetails().get("retry_service_warning")).isEqualTo("Success rate below threshold");
        assertThat(health.getDetails()).containsKey("retry_success_rate");
        assertThat(health.getDetails().get("retry_success_rate")).isEqualTo(0.90);
    }

    @Test
    void shouldReturnDownWhenAcknowledgeSuccessRateLow() {
        // Given
        when(exceptionRepository.count()).thenReturn(100L);
        when(mutationMetrics.getSuccessRate("retry")).thenReturn(0.98);
        when(mutationMetrics.getSuccessRate("acknowledge")).thenReturn(0.90); // Below 95% threshold
        when(mutationMetrics.getSuccessRate("resolve")).thenReturn(0.99);
        when(mutationMetrics.getSuccessRate("cancel_retry")).thenReturn(0.96);
        when(mutationMetrics.getTotalOperations("retry")).thenReturn(50L);
        when(mutationMetrics.getTotalOperations("acknowledge")).thenReturn(30L); // Above minimum threshold
        when(mutationMetrics.getTotalOperations("resolve")).thenReturn(20L);
        when(mutationMetrics.getTotalOperations("cancel_retry")).thenReturn(10L);

        // When
        Object health = healthIndicator.toString();

        // Then
        assertThat(health.getStatus()).isEqualTo("DOWN");
        assertThat(health.getDetails()).containsKey("exception_management_status");
        assertThat(health.getDetails().get("exception_management_status")).isEqualTo("operational");
        assertThat(health.getDetails()).containsKey("exception_management_warning");
        assertThat(health.getDetails().get("exception_management_warning")).isEqualTo("One or more operations have low success rate");
        assertThat(health.getDetails()).containsKey("acknowledge_success_rate");
        assertThat(health.getDetails().get("acknowledge_success_rate")).isEqualTo(0.90);
    }

    @Test
    void shouldIgnoreLowSuccessRateForLowOperationCount() {
        // Given - Low success rate but also low operation count (below threshold)
        when(exceptionRepository.count()).thenReturn(100L);
        when(mutationMetrics.getSuccessRate("retry")).thenReturn(0.50); // Very low success rate
        when(mutationMetrics.getSuccessRate("acknowledge")).thenReturn(0.97);
        when(mutationMetrics.getSuccessRate("resolve")).thenReturn(0.99);
        when(mutationMetrics.getSuccessRate("cancel_retry")).thenReturn(0.96);
        when(mutationMetrics.getTotalOperations("retry")).thenReturn(5L); // Below minimum threshold of 10
        when(mutationMetrics.getTotalOperations("acknowledge")).thenReturn(30L);
        when(mutationMetrics.getTotalOperations("resolve")).thenReturn(20L);
        when(mutationMetrics.getTotalOperations("cancel_retry")).thenReturn(10L);

        // When
        Object health = healthIndicator.toString();

        // Then - Should still be UP because operation count is too low to be significant
        assertThat(health.getStatus()).isEqualTo("UP");
        assertThat(health.getDetails()).containsKey("retry_service_status");
        assertThat(health.getDetails().get("retry_service_status")).isEqualTo("operational");
        assertThat(health.getDetails()).doesNotContainKey("retry_service_warning");
    }

    @Test
    void shouldReturnDownWhenRetryServiceNull() {
        // Given
        MutationHealthIndicator healthIndicatorWithNullService = new MutationHealthIndicator(
                null, // null retry service
                exceptionManagementService,
                exceptionRepository,
                mutationMetrics
        );
        when(exceptionRepository.count()).thenReturn(100L);

        // When
        Object health = healthIndicatorWithNullService.toString();

        // Then
        assertThat(health.getStatus()).isEqualTo("DOWN");
        assertThat(health.getDetails()).containsKey("retry_service_status");
        assertThat(health.getDetails().get("retry_service_status")).isEqualTo("unavailable");
    }

    @Test
    void shouldReturnDownWhenExceptionManagementServiceNull() {
        // Given
        MutationHealthIndicator healthIndicatorWithNullService = new MutationHealthIndicator(
                retryService,
                null, // null exception management service
                exceptionRepository,
                mutationMetrics
        );
        when(exceptionRepository.count()).thenReturn(100L);
        when(mutationMetrics.getSuccessRate("retry")).thenReturn(0.98);
        when(mutationMetrics.getTotalOperations("retry")).thenReturn(50L);

        // When
        Object health = healthIndicatorWithNullService.toString();

        // Then
        assertThat(health.getStatus()).isEqualTo("DOWN");
        assertThat(health.getDetails()).containsKey("exception_management_status");
        assertThat(health.getDetails().get("exception_management_status")).isEqualTo("unavailable");
    }

    @Test
    void shouldReturnDownWhenMetricsCheckFails() {
        // Given
        when(exceptionRepository.count()).thenReturn(100L);
        when(mutationMetrics.getSuccessRate("retry")).thenThrow(new RuntimeException("Metrics error"));

        // When
        Object health = healthIndicator.toString();

        // Then
        assertThat(health.getStatus()).isEqualTo("DOWN");
        assertThat(health.getDetails()).containsKey("metrics_status");
        assertThat(health.getDetails().get("metrics_status")).isEqualTo("degraded");
        assertThat(health.getDetails()).containsKey("metrics_error");
        assertThat(health.getDetails().get("metrics_error")).isEqualTo("Metrics error");
    }

    @Test
    void shouldReturnDownWhenHealthCheckThrowsException() {
        // Given
        when(exceptionRepository.count()).thenThrow(new RuntimeException("Unexpected error"));

        // When
        Object health = healthIndicator.toString();

        // Then
        assertThat(health.getStatus()).isEqualTo("DOWN");
        assertThat(health.getDetails()).containsKey("error");
        assertThat(health.getDetails().get("error")).isEqualTo("Unexpected error");
        assertThat(health.getDetails()).containsKey("error_type");
        assertThat(health.getDetails().get("error_type")).isEqualTo("RuntimeException");
    }

    @Test
    void shouldIncludeHealthCheckDuration() {
        // Given
        when(exceptionRepository.count()).thenReturn(100L);
        when(mutationMetrics.getSuccessRate("retry")).thenReturn(0.98);
        when(mutationMetrics.getSuccessRate("acknowledge")).thenReturn(0.97);
        when(mutationMetrics.getSuccessRate("resolve")).thenReturn(0.99);
        when(mutationMetrics.getSuccessRate("cancel_retry")).thenReturn(0.96);
        when(mutationMetrics.getTotalOperations("retry")).thenReturn(50L);
        when(mutationMetrics.getTotalOperations("acknowledge")).thenReturn(30L);
        when(mutationMetrics.getTotalOperations("resolve")).thenReturn(20L);
        when(mutationMetrics.getTotalOperations("cancel_retry")).thenReturn(10L);

        // When
        Object health = healthIndicator.toString();

        // Then
        assertThat(health.getStatus()).isEqualTo("UP");
        assertThat(health.getDetails()).containsKey("health_check_duration_ms");
        assertThat(health.getDetails().get("health_check_duration_ms")).isInstanceOf(Long.class);
        
        Long duration = (Long) health.getDetails().get("health_check_duration_ms");
        assertThat(duration).isGreaterThanOrEqualTo(0L);
    }

    @Test
    void shouldIncludeMutationMetricsInHealthDetails() {
        // Given
        when(exceptionRepository.count()).thenReturn(100L);
        when(mutationMetrics.getSuccessRate("retry")).thenReturn(0.98);
        when(mutationMetrics.getSuccessRate("acknowledge")).thenReturn(0.97);
        when(mutationMetrics.getSuccessRate("resolve")).thenReturn(0.99);
        when(mutationMetrics.getSuccessRate("cancel_retry")).thenReturn(0.96);
        when(mutationMetrics.getTotalOperations("retry")).thenReturn(50L);
        when(mutationMetrics.getTotalOperations("acknowledge")).thenReturn(30L);
        when(mutationMetrics.getTotalOperations("resolve")).thenReturn(20L);
        when(mutationMetrics.getTotalOperations("cancel_retry")).thenReturn(10L);
        when(mutationMetrics.getSuccessfulOperations("retry")).thenReturn(49L);
        when(mutationMetrics.getSuccessfulOperations("acknowledge")).thenReturn(29L);
        when(mutationMetrics.getSuccessfulOperations("resolve")).thenReturn(20L);
        when(mutationMetrics.getSuccessfulOperations("cancel_retry")).thenReturn(10L);

        // When
        Object health = healthIndicator.toString();

        // Then
        assertThat(health.getStatus()).isEqualTo("UP");
        assertThat(health.getDetails()).containsKey("mutation_metrics");
        
        @SuppressWarnings("unchecked")
        var metricsData = (java.util.Map<String, Object>) health.getDetails().get("mutation_metrics");
        assertThat(metricsData).containsKeys("retry", "acknowledge", "resolve", "cancel_retry");
        
        @SuppressWarnings("unchecked")
        var retryMetrics = (java.util.Map<String, Object>) metricsData.get("retry");
        assertThat(retryMetrics).containsEntry("total_operations", 50L);
        assertThat(retryMetrics).containsEntry("successful_operations", 49L);
        assertThat(retryMetrics).containsEntry("success_rate", 0.98);
    }
}
