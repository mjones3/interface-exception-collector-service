package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Service for calculating real-time dashboard metrics.
 * Provides aggregated statistics for exceptions, retries, and API performance.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardMetricsService {

    private final InterfaceExceptionRepository exceptionRepository;
    private final RetryAttemptRepository retryAttemptRepository;

    /**
     * Calculate comprehensive dashboard metrics.
     */
    public DashboardMetrics calculateMetrics() {
        log.debug("Calculating dashboard metrics");

        try {
            // Get today's date range
            OffsetDateTime startOfDay = LocalDate.now().atStartOfDay().atOffset(ZoneOffset.UTC);
            OffsetDateTime endOfDay = startOfDay.plusDays(1);

            // Active exceptions (NEW, ACKNOWLEDGED)
            long activeExceptions = exceptionRepository.countByStatusIn(
                java.util.List.of(ExceptionStatus.NEW.name(), ExceptionStatus.ACKNOWLEDGED.name())
            );

            // Today's exceptions
            long todayExceptions = exceptionRepository.countByTimestampBetween(startOfDay, endOfDay);

            // Retry statistics
            long failedRetries = retryAttemptRepository.countByInitiatedAtBetweenAndResultSuccess(
                startOfDay, endOfDay, false
            );

            long successfulRetries = retryAttemptRepository.countByInitiatedAtBetweenAndResultSuccess(
                startOfDay, endOfDay, true
            );

            long totalRetries = failedRetries + successfulRetries;

            // Calculate retry success rate
            double retrySuccessRate = totalRetries > 0 ? 
                (double) successfulRetries / totalRetries * 100.0 : 0.0;

            // API success rate (exceptions vs successful operations)
            // Assuming successful operations = total operations - exceptions
            long totalApiCalls = todayExceptions + (successfulRetries * 2); // Rough estimate
            double apiSuccessRate = totalApiCalls > 0 ? 
                (double) (totalApiCalls - todayExceptions) / totalApiCalls * 100.0 : 100.0;

            return DashboardMetrics.builder()
                .activeExceptions(activeExceptions)
                .todayExceptions(todayExceptions)
                .failedRetries(failedRetries)
                .successfulRetries(successfulRetries)
                .totalRetries(totalRetries)
                .retrySuccessRate(retrySuccessRate)
                .apiSuccessRate(apiSuccessRate)
                .totalApiCallsToday(totalApiCalls)
                .lastUpdated(OffsetDateTime.now())
                .build();

        } catch (Exception e) {
            log.error("Error calculating dashboard metrics", e);
            
            // Return empty metrics on error
            return DashboardMetrics.builder()
                .activeExceptions(0L)
                .todayExceptions(0L)
                .failedRetries(0L)
                .successfulRetries(0L)
                .totalRetries(0L)
                .retrySuccessRate(0.0)
                .apiSuccessRate(0.0)
                .totalApiCallsToday(0L)
                .lastUpdated(OffsetDateTime.now())
                .build();
        }
    }

    /**
     * Dashboard metrics data class.
     */
    @lombok.Builder
    @lombok.Data
    public static class DashboardMetrics {
        private final long activeExceptions;
        private final long todayExceptions;
        private final long failedRetries;
        private final long successfulRetries;
        private final long totalRetries;
        private final double retrySuccessRate;
        private final double apiSuccessRate;
        private final long totalApiCallsToday;
        private final OffsetDateTime lastUpdated;
    }
}
