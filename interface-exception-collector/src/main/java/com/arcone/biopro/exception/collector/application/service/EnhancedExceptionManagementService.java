package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.api.dto.AcknowledgeRequest;
import com.arcone.biopro.exception.collector.api.dto.AcknowledgeResponse;
import com.arcone.biopro.exception.collector.api.dto.ResolveRequest;
import com.arcone.biopro.exception.collector.api.dto.ResolveResponse;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.infrastructure.monitoring.DynatraceIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Enhanced exception management service that integrates Dynatrace business
 * metrics
 * with exception acknowledgment and resolution operations.
 */
@Service
@Primary
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "dynatrace.enabled", havingValue = "true", matchIfMissing = true)
public class EnhancedExceptionManagementService {

    private final ExceptionManagementService originalManagementService;
    private final DynatraceIntegrationService dynatraceIntegrationService;

    /**
     * Enhanced exception acknowledgment with Dynatrace integration.
     */
    public AcknowledgeResponse acknowledgeException(String transactionId, AcknowledgeRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // Acknowledge using the original service
            AcknowledgeResponse response = originalManagementService.acknowledgeException(transactionId, request);

            long processingTime = System.currentTimeMillis() - startTime;

            // Get the updated exception for metrics
            Optional<ExceptionStatus> statusOpt = originalManagementService.getExceptionStatus(transactionId);
            if (statusOpt.isPresent() && statusOpt.get() == ExceptionStatus.ACKNOWLEDGED) {
                // Create a synthetic exception object for metrics (we don't have the full
                // object from the response)
                // In a real implementation, you might want to modify the original service to
                // return the full exception
                log.info(
                        "Exception acknowledged successfully with Dynatrace tracking - Transaction: {}, User: {}, Time: {}ms",
                        transactionId, request.getAcknowledgedBy(), processingTime);

                // Record lifecycle event
                dynatraceIntegrationService.recordLifecycleEvent(null, "EXCEPTION_ACKNOWLEDGED",
                        String.format("User: %s, Notes: %s", request.getAcknowledgedBy(),
                                request.getNotes() != null ? request.getNotes() : "No notes"));
            }

            log.debug("Enhanced acknowledgment completed for exception: {} ({}ms)", transactionId, processingTime);

            return response;

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Enhanced acknowledgment failed for exception: {} after {}ms", transactionId, processingTime, e);
            throw e;
        }
    }

    /**
     * Enhanced exception resolution with Dynatrace integration.
     */
    public ResolveResponse resolveException(String transactionId, ResolveRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // Resolve using the original service
            ResolveResponse response = originalManagementService.resolveException(transactionId, request);

            long processingTime = System.currentTimeMillis() - startTime;

            // Get the updated exception for metrics
            Optional<ExceptionStatus> statusOpt = originalManagementService.getExceptionStatus(transactionId);
            if (statusOpt.isPresent() && statusOpt.get() == ExceptionStatus.RESOLVED) {
                log.info(
                        "Exception resolved successfully with Dynatrace tracking - Transaction: {}, User: {}, Method: {}, Time: {}ms",
                        transactionId, request.getResolvedBy(), request.getResolutionMethod(), processingTime);

                // Record lifecycle event
                dynatraceIntegrationService.recordLifecycleEvent(null, "EXCEPTION_RESOLVED",
                        String.format("User: %s, Method: %s, Retries: %d",
                                request.getResolvedBy(), request.getResolutionMethod(),
                                response.getTotalRetryAttempts()));
            }

            log.debug("Enhanced resolution completed for exception: {} ({}ms)", transactionId, processingTime);

            return response;

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Enhanced resolution failed for exception: {} after {}ms", transactionId, processingTime, e);
            throw e;
        }
    }

    /**
     * Enhanced capability check with Dynatrace integration.
     */
    public boolean canAcknowledge(String transactionId) {
        long startTime = System.currentTimeMillis();

        try {
            boolean canAck = originalManagementService.canAcknowledge(transactionId);

            long processingTime = System.currentTimeMillis() - startTime;

            log.debug("Acknowledgment capability check for exception: {} -> {} ({}ms)",
                    transactionId, canAck, processingTime);

            return canAck;

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Acknowledgment capability check failed for exception: {} after {}ms", transactionId,
                    processingTime, e);
            throw e;
        }
    }

    /**
     * Enhanced capability check with Dynatrace integration.
     */
    public boolean canResolve(String transactionId) {
        long startTime = System.currentTimeMillis();

        try {
            boolean canResolve = originalManagementService.canResolve(transactionId);

            long processingTime = System.currentTimeMillis() - startTime;

            log.debug("Resolution capability check for exception: {} -> {} ({}ms)",
                    transactionId, canResolve, processingTime);

            return canResolve;

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Resolution capability check failed for exception: {} after {}ms", transactionId, processingTime,
                    e);
            throw e;
        }
    }

    /**
     * Enhanced status retrieval with Dynatrace integration.
     */
    public Optional<ExceptionStatus> getExceptionStatus(String transactionId) {
        long startTime = System.currentTimeMillis();

        try {
            Optional<ExceptionStatus> status = originalManagementService.getExceptionStatus(transactionId);

            long processingTime = System.currentTimeMillis() - startTime;

            log.debug("Status retrieval for exception: {} -> {} ({}ms)",
                    transactionId, status.orElse(null), processingTime);

            return status;

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Status retrieval failed for exception: {} after {}ms", transactionId, processingTime, e);
            throw e;
        }
    }

    /**
     * Records business metrics for bulk operations.
     * This method should be called when performing bulk acknowledgments or
     * resolutions.
     */
    public void recordBulkOperation(String operationType, int count, long totalDurationMs) {
        try {
            log.info("Bulk operation completed - Type: {}, Count: {}, Total Duration: {}ms, Avg per item: {}ms",
                    operationType, count, totalDurationMs, count > 0 ? totalDurationMs / count : 0);

            // Record lifecycle event for bulk operations
            dynatraceIntegrationService.recordLifecycleEvent(null, "BULK_OPERATION",
                    String.format("Type: %s, Count: %d, Duration: %dms", operationType, count, totalDurationMs));

        } catch (Exception e) {
            log.error("Failed to record bulk operation metrics", e);
        }
    }

    /**
     * Records user activity metrics for audit and monitoring purposes.
     */
    public void recordUserActivity(String userId, String action, String transactionId, boolean success) {
        try {
            log.info("User activity - User: {}, Action: {}, Transaction: {}, Success: {}",
                    userId, action, transactionId, success);

            // Record lifecycle event for user activities
            dynatraceIntegrationService.recordLifecycleEvent(null, "USER_ACTIVITY",
                    String.format("User: %s, Action: %s, Success: %s", userId, action, success));

        } catch (Exception e) {
            log.error("Failed to record user activity metrics", e);
        }
    }
}