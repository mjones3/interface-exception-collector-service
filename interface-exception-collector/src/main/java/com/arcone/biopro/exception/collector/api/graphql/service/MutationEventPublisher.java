package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.resolver.ExceptionSubscriptionResolver;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

/**
 * Service for publishing mutation completion events to GraphQL subscriptions.
 * Integrates mutation results with the existing GraphQL subscription system
 * to provide real-time updates for mutation operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MutationEventPublisher {

    private final ExceptionSubscriptionResolver subscriptionResolver;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DashboardSubscriptionService dashboardSubscriptionService;

    /**
     * Publishes a retry mutation completion event.
     * Broadcasts the event to all subscribers with mutation-specific filtering.
     * 
     * @param exception The exception that was retried
     * @param retryAttempt The retry attempt details
     * @param success Whether the retry was successful
     * @param initiatedBy Who initiated the retry
     */
    public void publishRetryMutationCompleted(InterfaceException exception, RetryAttempt retryAttempt, 
                                            boolean success, String initiatedBy) {
        log.info("Publishing retry mutation completion event for transaction: {} with success: {}", 
                exception.getTransactionId(), success);

        try {
            // Create mutation-specific event with enhanced metadata
            ExceptionSubscriptionResolver.MutationCompletionEvent mutationEvent = 
                new ExceptionSubscriptionResolver.MutationCompletionEvent(
                    ExceptionSubscriptionResolver.MutationType.RETRY,
                    exception.getTransactionId(),
                    success,
                    initiatedBy,
                    OffsetDateTime.now(),
                    "Retry operation " + (success ? "completed successfully" : "failed"),
                    "retry_" + exception.getTransactionId() + "_" + System.currentTimeMillis()
                );

            // Publish mutation completion event
            subscriptionResolver.publishMutationCompletion(mutationEvent);

            // Publish exception update with retry completion event type
            ExceptionSubscriptionResolver.ExceptionUpdateEvent exceptionEvent = 
                new ExceptionSubscriptionResolver.ExceptionUpdateEvent(
                    ExceptionSubscriptionResolver.ExceptionEventType.RETRY_COMPLETED,
                    mapToGraphQLException(exception),
                    OffsetDateTime.now(),
                    initiatedBy
                );

            subscriptionResolver.publishExceptionUpdate(exceptionEvent);

            // Publish retry status update
            ExceptionSubscriptionResolver.RetryEventType eventType = success 
                ? ExceptionSubscriptionResolver.RetryEventType.COMPLETED
                : ExceptionSubscriptionResolver.RetryEventType.FAILED;

            ExceptionSubscriptionResolver.RetryStatusEvent retryEvent = 
                new ExceptionSubscriptionResolver.RetryStatusEvent(
                    exception.getTransactionId(),
                    mapToGraphQLRetryAttempt(retryAttempt),
                    eventType,
                    OffsetDateTime.now()
                );

            subscriptionResolver.publishRetryStatusUpdate(retryEvent);

            // Publish application event for other listeners
            applicationEventPublisher.publishEvent(
                new SubscriptionEventBridge.RetryCompletedEvent(exception, retryAttempt, success)
            );

            // Trigger dashboard update for real-time metrics
            dashboardSubscriptionService.triggerUpdate();

            log.info("Successfully published retry mutation completion event for transaction: {}", 
                    exception.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to publish retry mutation completion event for transaction: {}", 
                    exception.getTransactionId(), e);
            // Don't rethrow - mutation success shouldn't be affected by subscription failures
        }
    }

    /**
     * Publishes an acknowledge mutation completion event.
     * 
     * @param exception The exception that was acknowledged
     * @param acknowledgedBy Who acknowledged the exception
     */
    public void publishAcknowledgeMutationCompleted(InterfaceException exception, String acknowledgedBy) {
        log.info("Publishing acknowledge mutation completion event for transaction: {}", 
                exception.getTransactionId());

        try {
            // Create mutation-specific event
            ExceptionSubscriptionResolver.MutationCompletionEvent mutationEvent = 
                new ExceptionSubscriptionResolver.MutationCompletionEvent(
                    ExceptionSubscriptionResolver.MutationType.ACKNOWLEDGE,
                    exception.getTransactionId(),
                    true, // Acknowledge operations are always successful if they complete
                    acknowledgedBy,
                    OffsetDateTime.now(),
                    "Exception acknowledged successfully",
                    "acknowledge_" + exception.getTransactionId() + "_" + System.currentTimeMillis()
                );

            // Publish mutation completion event
            subscriptionResolver.publishMutationCompletion(mutationEvent);

            // Publish exception update
            ExceptionSubscriptionResolver.ExceptionUpdateEvent exceptionEvent = 
                new ExceptionSubscriptionResolver.ExceptionUpdateEvent(
                    ExceptionSubscriptionResolver.ExceptionEventType.ACKNOWLEDGED,
                    mapToGraphQLException(exception),
                    OffsetDateTime.now(),
                    acknowledgedBy
                );

            subscriptionResolver.publishExceptionUpdate(exceptionEvent);

            // Publish application event
            applicationEventPublisher.publishEvent(
                new SubscriptionEventBridge.ExceptionAcknowledgedEvent(exception, acknowledgedBy)
            );

            // Trigger dashboard update
            dashboardSubscriptionService.triggerUpdate();

            log.info("Successfully published acknowledge mutation completion event for transaction: {}", 
                    exception.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to publish acknowledge mutation completion event for transaction: {}", 
                    exception.getTransactionId(), e);
        }
    }

    /**
     * Publishes a resolve mutation completion event.
     * 
     * @param exception The exception that was resolved
     * @param resolvedBy Who resolved the exception
     */
    public void publishResolveMutationCompleted(InterfaceException exception, String resolvedBy) {
        log.info("Publishing resolve mutation completion event for transaction: {}", 
                exception.getTransactionId());

        try {
            // Create mutation-specific event
            ExceptionSubscriptionResolver.MutationCompletionEvent mutationEvent = 
                new ExceptionSubscriptionResolver.MutationCompletionEvent(
                    ExceptionSubscriptionResolver.MutationType.RESOLVE,
                    exception.getTransactionId(),
                    true, // Resolve operations are always successful if they complete
                    resolvedBy,
                    OffsetDateTime.now(),
                    "Exception resolved successfully",
                    "resolve_" + exception.getTransactionId() + "_" + System.currentTimeMillis()
                );

            // Publish mutation completion event
            subscriptionResolver.publishMutationCompletion(mutationEvent);

            // Publish exception update
            ExceptionSubscriptionResolver.ExceptionUpdateEvent exceptionEvent = 
                new ExceptionSubscriptionResolver.ExceptionUpdateEvent(
                    ExceptionSubscriptionResolver.ExceptionEventType.RESOLVED,
                    mapToGraphQLException(exception),
                    OffsetDateTime.now(),
                    resolvedBy
                );

            subscriptionResolver.publishExceptionUpdate(exceptionEvent);

            // Publish application event
            applicationEventPublisher.publishEvent(
                new SubscriptionEventBridge.ExceptionResolvedEvent(exception, resolvedBy)
            );

            // Trigger dashboard update
            dashboardSubscriptionService.triggerUpdate();

            log.info("Successfully published resolve mutation completion event for transaction: {}", 
                    exception.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to publish resolve mutation completion event for transaction: {}", 
                    exception.getTransactionId(), e);
        }
    }

    /**
     * Publishes a cancel retry mutation completion event.
     * 
     * @param exception The exception whose retry was cancelled
     * @param retryAttempt The cancelled retry attempt
     * @param cancelledBy Who cancelled the retry
     * @param reason The reason for cancellation
     */
    public void publishCancelRetryMutationCompleted(InterfaceException exception, RetryAttempt retryAttempt, 
                                                   String cancelledBy, String reason) {
        log.info("Publishing cancel retry mutation completion event for transaction: {}", 
                exception.getTransactionId());

        try {
            // Create mutation-specific event
            ExceptionSubscriptionResolver.MutationCompletionEvent mutationEvent = 
                new ExceptionSubscriptionResolver.MutationCompletionEvent(
                    ExceptionSubscriptionResolver.MutationType.CANCEL_RETRY,
                    exception.getTransactionId(),
                    true, // Cancel operations are always successful if they complete
                    cancelledBy,
                    OffsetDateTime.now(),
                    "Retry cancelled: " + reason,
                    "cancel_" + exception.getTransactionId() + "_" + System.currentTimeMillis()
                );

            // Publish mutation completion event
            subscriptionResolver.publishMutationCompletion(mutationEvent);

            // Publish exception update
            ExceptionSubscriptionResolver.ExceptionUpdateEvent exceptionEvent = 
                new ExceptionSubscriptionResolver.ExceptionUpdateEvent(
                    ExceptionSubscriptionResolver.ExceptionEventType.CANCELLED,
                    mapToGraphQLException(exception),
                    OffsetDateTime.now(),
                    cancelledBy
                );

            subscriptionResolver.publishExceptionUpdate(exceptionEvent);

            // Publish retry status update
            ExceptionSubscriptionResolver.RetryStatusEvent retryEvent = 
                new ExceptionSubscriptionResolver.RetryStatusEvent(
                    exception.getTransactionId(),
                    mapToGraphQLRetryAttempt(retryAttempt),
                    ExceptionSubscriptionResolver.RetryEventType.CANCELLED,
                    OffsetDateTime.now()
                );

            subscriptionResolver.publishRetryStatusUpdate(retryEvent);

            // Publish application event
            applicationEventPublisher.publishEvent(
                new SubscriptionEventBridge.RetryCancelledEvent(exception, retryAttempt, cancelledBy, reason)
            );

            // Trigger dashboard update
            dashboardSubscriptionService.triggerUpdate();

            log.info("Successfully published cancel retry mutation completion event for transaction: {}", 
                    exception.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to publish cancel retry mutation completion event for transaction: {}", 
                    exception.getTransactionId(), e);
        }
    }

    /**
     * Publishes a bulk mutation completion event with summary statistics.
     * 
     * @param mutationType The type of bulk mutation
     * @param totalCount Total number of operations attempted
     * @param successCount Number of successful operations
     * @param failureCount Number of failed operations
     * @param initiatedBy Who initiated the bulk operation
     */
    public void publishBulkMutationCompleted(MutationType mutationType, int totalCount, 
                                           int successCount, int failureCount, String initiatedBy) {
        log.info("Publishing bulk {} mutation completion event: {} total, {} success, {} failures", 
                mutationType, totalCount, successCount, failureCount);

        try {
            // Create bulk mutation event
            ExceptionSubscriptionResolver.MutationType subscriptionMutationType = 
                ExceptionSubscriptionResolver.MutationType.valueOf(mutationType.name());
            
            ExceptionSubscriptionResolver.MutationCompletionEvent bulkEvent = 
                new ExceptionSubscriptionResolver.MutationCompletionEvent(
                    subscriptionMutationType,
                    "BULK_OPERATION",
                    successCount > 0, // Consider successful if any operations succeeded
                    initiatedBy,
                    OffsetDateTime.now(),
                    String.format("Bulk %s completed: %d total, %d success, %d failures", 
                            mutationType, totalCount, successCount, failureCount),
                    "bulk_" + mutationType.name().toLowerCase() + "_" + System.currentTimeMillis()
                );

            // Publish bulk mutation completion event
            subscriptionResolver.publishMutationCompletion(bulkEvent);

            // Trigger dashboard update for bulk operation metrics
            dashboardSubscriptionService.triggerUpdate();

            log.info("Successfully published bulk {} mutation completion event", mutationType);

        } catch (Exception e) {
            log.error("Failed to publish bulk {} mutation completion event", mutationType, e);
        }
    }

    /**
     * Maps domain InterfaceException to GraphQL Exception for subscriptions.
     * Includes all necessary fields for subscription updates.
     */
    private ExceptionSubscriptionResolver.Exception mapToGraphQLException(InterfaceException exception) {
        ExceptionSubscriptionResolver.Exception graphqlException = new ExceptionSubscriptionResolver.Exception();
        
        // Map all required fields for comprehensive subscription updates
        graphqlException.setId(exception.getId() != null ? exception.getId().toString() : null);
        graphqlException.setTransactionId(exception.getTransactionId());
        graphqlException.setExternalId(exception.getExternalId());
        graphqlException.setInterfaceType(exception.getInterfaceType() != null ? exception.getInterfaceType().name() : null);
        graphqlException.setExceptionReason(exception.getExceptionReason());
        graphqlException.setOperation(exception.getOperation());
        graphqlException.setStatus(exception.getStatus() != null ? exception.getStatus().name() : null);
        graphqlException.setSeverity(exception.getSeverity() != null ? exception.getSeverity().name() : null);
        graphqlException.setCategory(exception.getCategory() != null ? exception.getCategory().name() : null);
        graphqlException.setCustomerId(exception.getCustomerId());
        graphqlException.setLocationCode(exception.getLocationCode());
        graphqlException.setTimestamp(exception.getTimestamp());
        graphqlException.setProcessedAt(exception.getProcessedAt());
        graphqlException.setRetryable(exception.getRetryable());
        graphqlException.setRetryCount(exception.getRetryCount());
        graphqlException.setMaxRetries(exception.getMaxRetries());
        graphqlException.setLastRetryAt(exception.getLastRetryAt());
        graphqlException.setAcknowledgedBy(exception.getAcknowledgedBy());
        graphqlException.setAcknowledgedAt(exception.getAcknowledgedAt());
        
        return graphqlException;
    }

    /**
     * Maps domain RetryAttempt to GraphQL RetryAttempt for subscriptions.
     */
    private ExceptionSubscriptionResolver.RetryAttempt mapToGraphQLRetryAttempt(RetryAttempt retryAttempt) {
        ExceptionSubscriptionResolver.RetryAttempt graphqlRetryAttempt = new ExceptionSubscriptionResolver.RetryAttempt();
        graphqlRetryAttempt.setAttemptNumber(retryAttempt.getAttemptNumber());
        return graphqlRetryAttempt;
    }

    /**
     * Enum for mutation types to enable filtering.
     */
    public enum MutationType {
        RETRY,
        ACKNOWLEDGE,
        RESOLVE,
        CANCEL_RETRY
    }
}