package com.arcone.biopro.exception.collector.infrastructure.monitoring;

import com.arcone.biopro.exception.collector.application.service.ExceptionManagementService;
import com.arcone.biopro.exception.collector.application.service.ExceptionProcessingService;
import com.arcone.biopro.exception.collector.application.service.RetryService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service that integrates Dynatrace business metrics with existing exception processing workflow.
 * Acts as a bridge between the core business logic and Dynatrace monitoring,
 * ensuring all business events are captured for comprehensive observability.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "dynatrace.enabled", havingValue = "true", matchIfMissing = true)
public class DynatraceIntegrationService {

    private final DynatraceBusinessMetricsService businessMetricsService;

    /**
     * Integrates with exception processing to capture business metrics.
     * This method should be called after an exception is successfully processed.
     *
     * @param exception the processed exception
     * @param processingTimeMs the time taken to process the exception in milliseconds
     */
    public void recordExceptionProcessed(InterfaceException exception, long processingTimeMs) {
        try {
            // Record the exception as received (if it's a new exception)
            if (exception.getStatus() == ExceptionStatus.NEW) {
                businessMetricsService.recordExceptionReceived(exception);
            }

            // Record the processing metrics
            businessMetricsService.recordExceptionProcessed(exception, processingTimeMs);

            log.debug("Recorded Dynatrace metrics for processed exception: {} ({}ms)", 
                    exception.getTransactionId(), processingTimeMs);

        } catch (Exception e) {
            log.error("Failed to record Dynatrace metrics for processed exception: {}", 
                    exception.getTransactionId(), e);
        }
    }

    /**
     * Integrates with exception acknowledgment to capture business metrics.
     *
     * @param exception the acknowledged exception
     */
    public void recordExceptionAcknowledged(InterfaceException exception) {
        try {
            businessMetricsService.recordExceptionAcknowledged(exception);

            log.debug("Recorded Dynatrace metrics for acknowledged exception: {}", 
                    exception.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to record Dynatrace metrics for acknowledged exception: {}", 
                    exception.getTransactionId(), e);
        }
    }

    /**
     * Integrates with exception resolution to capture business metrics.
     *
     * @param exception the resolved exception
     */
    public void recordExceptionResolved(InterfaceException exception) {
        try {
            businessMetricsService.recordExceptionResolved(exception);

            log.debug("Recorded Dynatrace metrics for resolved exception: {}", 
                    exception.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to record Dynatrace metrics for resolved exception: {}", 
                    exception.getTransactionId(), e);
        }
    }

    /**
     * Integrates with retry operations to capture business metrics.
     *
     * @param retryAttempt the retry attempt
     * @param success whether the retry was successful
     * @param durationMs the retry duration in milliseconds
     */
    public void recordRetryAttempt(RetryAttempt retryAttempt, boolean success, long durationMs) {
        try {
            businessMetricsService.recordRetryAttempt(retryAttempt, success, durationMs);

            log.debug("Recorded Dynatrace metrics for retry attempt: {} (success: {}, {}ms)", 
                    retryAttempt.getAttemptNumber(), success, durationMs);

        } catch (Exception e) {
            log.error("Failed to record Dynatrace metrics for retry attempt: {}", 
                    retryAttempt.getAttemptNumber(), e);
        }
    }

    /**
     * Records payload retrieval metrics.
     *
     * @param exception the exception for which payload was retrieved
     * @param success whether the retrieval was successful
     * @param durationMs the retrieval duration in milliseconds
     */
    public void recordPayloadRetrieval(InterfaceException exception, boolean success, long durationMs) {
        try {
            businessMetricsService.recordPayloadRetrieval(
                exception.getTransactionId(), 
                exception.getInterfaceType(), 
                success, 
                durationMs
            );

            log.debug("Recorded Dynatrace metrics for payload retrieval: {} (success: {}, {}ms)", 
                    exception.getTransactionId(), success, durationMs);

        } catch (Exception e) {
            log.error("Failed to record Dynatrace metrics for payload retrieval: {}", 
                    exception.getTransactionId(), e);
        }
    }

    /**
     * Records business metrics for exception status changes.
     * This method captures the business impact of status transitions.
     *
     * @param exception the exception with updated status
     * @param previousStatus the previous status
     * @param newStatus the new status
     */
    public void recordStatusChange(InterfaceException exception, ExceptionStatus previousStatus, ExceptionStatus newStatus) {
        try {
            // Record specific metrics based on status change type
            switch (newStatus) {
                case ACKNOWLEDGED:
                    recordExceptionAcknowledged(exception);
                    break;
                case RESOLVED:
                    recordExceptionResolved(exception);
                    break;
                case ESCALATED:
                    recordExceptionEscalated(exception);
                    break;
                case RETRIED_SUCCESS:
                    recordRetrySuccess(exception);
                    break;
                case RETRIED_FAILED:
                    recordRetryFailure(exception);
                    break;
            }

            log.debug("Recorded Dynatrace metrics for status change: {} -> {} for exception: {}", 
                    previousStatus, newStatus, exception.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to record Dynatrace metrics for status change: {}", 
                    exception.getTransactionId(), e);
        }
    }

    /**
     * Records metrics for exception escalation.
     */
    private void recordExceptionEscalated(InterfaceException exception) {
        // Custom metrics for escalated exceptions
        businessMetricsService.recordExceptionReceived(exception); // Re-record with escalated context
        
        log.info("Exception escalated - Transaction: {}, Interface: {}, Severity: {}", 
                exception.getTransactionId(), exception.getInterfaceType(), exception.getSeverity());
    }

    /**
     * Records metrics for successful retry operations.
     */
    private void recordRetrySuccess(InterfaceException exception) {
        // Create a synthetic retry attempt for metrics
        RetryAttempt syntheticAttempt = RetryAttempt.builder()
                .attemptNumber(exception.getRetryCount())
                .resultSuccess(true)
                .build();
        
        businessMetricsService.recordRetryAttempt(syntheticAttempt, true, 0);
        
        log.info("Retry succeeded - Transaction: {}, Attempt: {}", 
                exception.getTransactionId(), exception.getRetryCount());
    }

    /**
     * Records metrics for failed retry operations.
     */
    private void recordRetryFailure(InterfaceException exception) {
        // Create a synthetic retry attempt for metrics
        RetryAttempt syntheticAttempt = RetryAttempt.builder()
                .attemptNumber(exception.getRetryCount())
                .resultSuccess(false)
                .build();
        
        businessMetricsService.recordRetryAttempt(syntheticAttempt, false, 0);
        
        log.info("Retry failed - Transaction: {}, Attempt: {}", 
                exception.getTransactionId(), exception.getRetryCount());
    }

    /**
     * Records comprehensive business metrics for exception lifecycle events.
     * This method provides a holistic view of exception processing performance.
     *
     * @param exception the exception
     * @param eventType the type of lifecycle event
     * @param additionalContext additional context for the event
     */
    public void recordLifecycleEvent(InterfaceException exception, String eventType, String additionalContext) {
        try {
            // Record the base exception metrics
            businessMetricsService.recordExceptionReceived(exception);

            // Add custom attributes for lifecycle tracking
            log.info("Exception lifecycle event - Transaction: {}, Event: {}, Context: {}, Interface: {}, Severity: {}", 
                    exception.getTransactionId(), eventType, additionalContext, 
                    exception.getInterfaceType(), exception.getSeverity());

        } catch (Exception e) {
            log.error("Failed to record Dynatrace lifecycle event for exception: {}", 
                    exception.getTransactionId(), e);
        }
    }

    /**
     * Records business impact metrics based on exception characteristics.
     * Provides insights into the business impact of different types of exceptions.
     *
     * @param exception the exception to analyze
     */
    public void recordBusinessImpactMetrics(InterfaceException exception) {
        try {
            // Calculate business impact score based on various factors
            int impactScore = calculateBusinessImpactScore(exception);

            // Record custom metrics for business impact
            log.info("Business impact analysis - Transaction: {}, Impact Score: {}, Customer: {}, Interface: {}, Severity: {}", 
                    exception.getTransactionId(), impactScore, exception.getCustomerId(), 
                    exception.getInterfaceType(), exception.getSeverity());

            // Record the exception with impact context
            businessMetricsService.recordExceptionReceived(exception);

        } catch (Exception e) {
            log.error("Failed to record business impact metrics for exception: {}", 
                    exception.getTransactionId(), e);
        }
    }

    /**
     * Calculates a business impact score for an exception.
     * Higher scores indicate greater business impact.
     */
    private int calculateBusinessImpactScore(InterfaceException exception) {
        int score = 0;

        // Severity impact
        switch (exception.getSeverity()) {
            case CRITICAL:
                score += 40;
                break;
            case HIGH:
                score += 30;
                break;
            case MEDIUM:
                score += 20;
                break;
            case LOW:
                score += 10;
                break;
        }

        // Interface type impact
        switch (exception.getInterfaceType()) {
            case ORDER:
                score += 25; // Orders have high business impact
                break;
            case COLLECTION:
                score += 20; // Collections are critical for operations
                break;
            case DISTRIBUTION:
                score += 15; // Distribution affects delivery
                break;
            default:
                score += 10;
        }

        // Customer impact (if customer ID is present, it affects a specific customer)
        if (exception.getCustomerId() != null && !exception.getCustomerId().isEmpty()) {
            score += 15;
        }

        // Retry impact (multiple retries indicate persistent issues)
        if (exception.getRetryCount() > 3) {
            score += 20;
        } else if (exception.getRetryCount() > 1) {
            score += 10;
        }

        // Age impact (older unresolved exceptions have higher impact)
        if (exception.getCreatedAt() != null) {
            long ageHours = java.time.Duration.between(exception.getCreatedAt(), java.time.OffsetDateTime.now()).toHours();
            if (ageHours > 24) {
                score += 15;
            } else if (ageHours > 4) {
                score += 10;
            }
        }

        return Math.min(score, 100); // Cap at 100
    }

    /**
     * Records performance metrics for GraphQL operations.
     *
     * @param operationType the type of GraphQL operation (query, mutation, subscription)
     * @param operationName the name of the operation
     * @param durationMs the operation duration in milliseconds
     * @param success whether the operation was successful
     * @param resultSize the size of the result (number of items)
     */
    public void recordGraphQLOperation(String operationType, String operationName, long durationMs, boolean success, int resultSize) {
        try {
            log.debug("GraphQL operation metrics - Type: {}, Name: {}, Duration: {}ms, Success: {}, Result Size: {}", 
                    operationType, operationName, durationMs, success, resultSize);

            // The actual metrics recording is handled by the instrumentation aspect
            // This method provides a programmatic way to record additional context

        } catch (Exception e) {
            log.error("Failed to record GraphQL operation metrics: {} - {}", operationType, operationName, e);
        }
    }

    /**
     * Records system health metrics based on exception processing performance.
     */
    public void recordSystemHealthMetrics() {
        try {
            DynatraceBusinessMetricsService.BusinessMetricsSummary summary = 
                    businessMetricsService.getBusinessMetricsSummary();

            // Log system health indicators
            log.info("System health metrics - Pending: {}, Failed: {}, Resolved: {}, Critical: {}, Retry Success Rate: {}%", 
                    summary.getPendingExceptions(), summary.getFailedExceptions(), summary.getResolvedExceptions(),
                    summary.getCriticalExceptions(), String.format("%.2f", summary.getRetrySuccessRate()));

        } catch (Exception e) {
            log.error("Failed to record system health metrics", e);
        }
    }
}