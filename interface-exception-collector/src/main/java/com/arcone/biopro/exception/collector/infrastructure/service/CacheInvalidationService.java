package com.arcone.biopro.exception.collector.infrastructure.service;

import com.arcone.biopro.exception.collector.domain.event.outbound.ExceptionStatusChangedEvent;
import com.arcone.biopro.exception.collector.domain.event.outbound.RetryAttemptCompletedEvent;
import com.arcone.biopro.exception.collector.domain.event.outbound.RetryAttemptStartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service responsible for invalidating validation caches when exception status changes.
 * Listens to domain events and ensures cache consistency by removing stale data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheInvalidationService {

    private final DatabaseCachingService databaseCachingService;

    /**
     * Handles exception status change events by invalidating relevant caches.
     * 
     * @param event the exception status changed event
     */
    @EventListener
    @Async
    public void handleExceptionStatusChanged(ExceptionStatusChangedEvent event) {
        String transactionId = event.getTransactionId();
        log.debug("Handling exception status change for transaction: {} (old: {}, new: {})", 
            transactionId, event.getOldStatus(), event.getNewStatus());
        
        try {
            // Invalidate all validation caches for this transaction
            databaseCachingService.invalidateValidationCache(transactionId);
            
            // Invalidate operation-specific caches
            databaseCachingService.invalidateOperationValidationCache(transactionId, "retry");
            databaseCachingService.invalidateOperationValidationCache(transactionId, "acknowledge");
            databaseCachingService.invalidateOperationValidationCache(transactionId, "resolve");
            databaseCachingService.invalidateOperationValidationCache(transactionId, "cancel");
            
            log.debug("Successfully invalidated validation caches for transaction: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to invalidate validation caches for transaction: {}", transactionId, e);
        }
    }

    /**
     * Handles retry attempt started events by invalidating retry-related caches.
     * 
     * @param event the retry attempt started event
     */
    @EventListener
    @Async
    public void handleRetryAttemptStarted(RetryAttemptStartedEvent event) {
        String transactionId = event.getTransactionId();
        log.debug("Handling retry attempt started for transaction: {}", transactionId);
        
        try {
            // Invalidate retry-specific caches since a new retry is starting
            databaseCachingService.invalidateOperationValidationCache(transactionId, "retry");
            databaseCachingService.invalidateOperationValidationCache(transactionId, "cancel");
            
            log.debug("Successfully invalidated retry-related caches for transaction: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to invalidate retry-related caches for transaction: {}", transactionId, e);
        }
    }

    /**
     * Handles retry attempt completed events by invalidating retry-related caches.
     * 
     * @param event the retry attempt completed event
     */
    @EventListener
    @Async
    public void handleRetryAttemptCompleted(RetryAttemptCompletedEvent event) {
        String transactionId = event.getTransactionId();
        log.debug("Handling retry attempt completed for transaction: {} (success: {})", 
            transactionId, event.getSuccess());
        
        try {
            // Invalidate retry-specific caches since retry status has changed
            databaseCachingService.invalidateOperationValidationCache(transactionId, "retry");
            databaseCachingService.invalidateOperationValidationCache(transactionId, "cancel");
            
            // If retry was successful, the exception status might have changed
            if (event.getSuccess()) {
                databaseCachingService.invalidateValidationCache(transactionId);
            }
            
            log.debug("Successfully invalidated retry completion caches for transaction: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to invalidate retry completion caches for transaction: {}", transactionId, e);
        }
    }

    /**
     * Manually invalidate caches for a specific transaction.
     * Useful for administrative operations or when cache inconsistency is detected.
     * 
     * @param transactionId the transaction ID to invalidate
     */
    public void manuallyInvalidateTransaction(String transactionId) {
        log.info("Manually invalidating all caches for transaction: {}", transactionId);
        
        try {
            databaseCachingService.invalidateValidationCache(transactionId);
            databaseCachingService.invalidateOperationValidationCache(transactionId, "retry");
            databaseCachingService.invalidateOperationValidationCache(transactionId, "acknowledge");
            databaseCachingService.invalidateOperationValidationCache(transactionId, "resolve");
            databaseCachingService.invalidateOperationValidationCache(transactionId, "cancel");
            
            log.info("Successfully manually invalidated all caches for transaction: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to manually invalidate caches for transaction: {}", transactionId, e);
            throw new RuntimeException("Cache invalidation failed for transaction: " + transactionId, e);
        }
    }

    /**
     * Clear all validation caches.
     * Should be used sparingly, typically during maintenance or when cache corruption is suspected.
     */
    public void clearAllCaches() {
        log.warn("Clearing all validation caches - this will impact performance temporarily");
        
        try {
            databaseCachingService.clearAllValidationCaches();
            log.warn("Successfully cleared all validation caches");
        } catch (Exception e) {
            log.error("Failed to clear all validation caches", e);
            throw new RuntimeException("Failed to clear all validation caches", e);
        }
    }
}