package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.infrastructure.monitoring.DynatraceIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Enhanced exception processing service that integrates Dynatrace business metrics
 * with the existing exception processing workflow. This service wraps the original
 * ExceptionProcessingService to add comprehensive monitoring capabilities.
 */
@Service
@Primary
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "dynatrace.enabled", havingValue = "true", matchIfMissing = true)
public class EnhancedExceptionProcessingService {

    private final ExceptionProcessingService originalProcessingService;
    private final DynatraceIntegrationService dynatraceIntegrationService;

    /**
     * Enhanced processing of OrderRejected events with Dynatrace integration.
     */
    public InterfaceException processOrderRejectedEvent(com.arcone.biopro.exception.collector.domain.event.inbound.OrderRejectedEvent event) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Process the exception using the original service
            InterfaceException exception = originalProcessingService.processOrderRejectedEvent(event);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Record Dynatrace business metrics
            dynatraceIntegrationService.recordExceptionProcessed(exception, processingTime);
            dynatraceIntegrationService.recordBusinessImpactMetrics(exception);
            dynatraceIntegrationService.recordLifecycleEvent(exception, "ORDER_REJECTED", 
                    "Reason: " + event.getPayload().getRejectedReason());
            
            log.debug("Enhanced processing completed for OrderRejected event: {} ({}ms)", 
                    exception.getTransactionId(), processingTime);
            
            return exception;
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Enhanced processing failed for OrderRejected event after {}ms", processingTime, e);
            throw e;
        }
    }

    /**
     * Enhanced processing of OrderCancelled events with Dynatrace integration.
     */
    public InterfaceException processOrderCancelledEvent(com.arcone.biopro.exception.collector.domain.event.inbound.OrderCancelledEvent event) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Process the exception using the original service
            InterfaceException exception = originalProcessingService.processOrderCancelledEvent(event);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Record Dynatrace business metrics
            dynatraceIntegrationService.recordExceptionProcessed(exception, processingTime);
            dynatraceIntegrationService.recordBusinessImpactMetrics(exception);
            dynatraceIntegrationService.recordLifecycleEvent(exception, "ORDER_CANCELLED", 
                    "Reason: " + event.getPayload().getCancelReason());
            
            log.debug("Enhanced processing completed for OrderCancelled event: {} ({}ms)", 
                    exception.getTransactionId(), processingTime);
            
            return exception;
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Enhanced processing failed for OrderCancelled event after {}ms", processingTime, e);
            throw e;
        }
    }

    /**
     * Enhanced processing of CollectionRejected events with Dynatrace integration.
     */
    public InterfaceException processCollectionRejectedEvent(com.arcone.biopro.exception.collector.domain.event.inbound.CollectionRejectedEvent event) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Process the exception using the original service
            InterfaceException exception = originalProcessingService.processCollectionRejectedEvent(event);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Record Dynatrace business metrics
            dynatraceIntegrationService.recordExceptionProcessed(exception, processingTime);
            dynatraceIntegrationService.recordBusinessImpactMetrics(exception);
            dynatraceIntegrationService.recordLifecycleEvent(exception, "COLLECTION_REJECTED", 
                    "Reason: " + event.getPayload().getRejectedReason());
            
            log.debug("Enhanced processing completed for CollectionRejected event: {} ({}ms)", 
                    exception.getTransactionId(), processingTime);
            
            return exception;
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Enhanced processing failed for CollectionRejected event after {}ms", processingTime, e);
            throw e;
        }
    }

    /**
     * Enhanced processing of DistributionFailed events with Dynatrace integration.
     */
    public InterfaceException processDistributionFailedEvent(com.arcone.biopro.exception.collector.domain.event.inbound.DistributionFailedEvent event) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Process the exception using the original service
            InterfaceException exception = originalProcessingService.processDistributionFailedEvent(event);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Record Dynatrace business metrics
            dynatraceIntegrationService.recordExceptionProcessed(exception, processingTime);
            dynatraceIntegrationService.recordBusinessImpactMetrics(exception);
            dynatraceIntegrationService.recordLifecycleEvent(exception, "DISTRIBUTION_FAILED", 
                    "Reason: " + event.getPayload().getFailureReason());
            
            log.debug("Enhanced processing completed for DistributionFailed event: {} ({}ms)", 
                    exception.getTransactionId(), processingTime);
            
            return exception;
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Enhanced processing failed for DistributionFailed event after {}ms", processingTime, e);
            throw e;
        }
    }

    /**
     * Enhanced processing of ValidationError events with Dynatrace integration.
     */
    public InterfaceException processValidationErrorEvent(com.arcone.biopro.exception.collector.domain.event.inbound.ValidationErrorEvent event) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Process the exception using the original service
            InterfaceException exception = originalProcessingService.processValidationErrorEvent(event);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Record Dynatrace business metrics
            dynatraceIntegrationService.recordExceptionProcessed(exception, processingTime);
            dynatraceIntegrationService.recordBusinessImpactMetrics(exception);
            dynatraceIntegrationService.recordLifecycleEvent(exception, "VALIDATION_ERROR", 
                    "Errors: " + event.getPayload().getValidationErrors().size());
            
            log.debug("Enhanced processing completed for ValidationError event: {} ({}ms)", 
                    exception.getTransactionId(), processingTime);
            
            return exception;
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Enhanced processing failed for ValidationError event after {}ms", processingTime, e);
            throw e;
        }
    }

    /**
     * Enhanced exception status update with Dynatrace integration.
     */
    public InterfaceException updateExceptionStatus(String transactionId, ExceptionStatus newStatus, String updatedBy) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Get the current exception to track status change
            InterfaceException currentException = originalProcessingService.updateExceptionStatus(transactionId, newStatus, updatedBy);
            ExceptionStatus previousStatus = currentException.getStatus();
            
            // Update using the original service
            InterfaceException updatedException = originalProcessingService.updateExceptionStatus(transactionId, newStatus, updatedBy);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Record Dynatrace business metrics for status change
            dynatraceIntegrationService.recordStatusChange(updatedException, previousStatus, newStatus);
            dynatraceIntegrationService.recordLifecycleEvent(updatedException, "STATUS_CHANGE", 
                    String.format("%s -> %s by %s", previousStatus, newStatus, updatedBy));
            
            log.debug("Enhanced status update completed for exception: {} ({} -> {}, {}ms)", 
                    transactionId, previousStatus, newStatus, processingTime);
            
            return updatedException;
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Enhanced status update failed for exception: {} after {}ms", transactionId, processingTime, e);
            throw e;
        }
    }

    /**
     * Enhanced retry count increment with Dynatrace integration.
     */
    public InterfaceException incrementRetryCount(String transactionId) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Update using the original service
            InterfaceException updatedException = originalProcessingService.incrementRetryCount(transactionId);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Record Dynatrace business metrics for retry increment
            dynatraceIntegrationService.recordLifecycleEvent(updatedException, "RETRY_COUNT_INCREMENT", 
                    "Count: " + updatedException.getRetryCount());
            
            log.debug("Enhanced retry count increment completed for exception: {} (count: {}, {}ms)", 
                    transactionId, updatedException.getRetryCount(), processingTime);
            
            return updatedException;
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Enhanced retry count increment failed for exception: {} after {}ms", transactionId, processingTime, e);
            throw e;
        }
    }

    /**
     * Records system health metrics periodically.
     * This method should be called by a scheduled task to provide ongoing health insights.
     */
    public void recordSystemHealthMetrics() {
        try {
            dynatraceIntegrationService.recordSystemHealthMetrics();
            log.debug("Recorded system health metrics to Dynatrace");
        } catch (Exception e) {
            log.error("Failed to record system health metrics", e);
        }
    }
}