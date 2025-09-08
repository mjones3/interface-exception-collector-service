package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.resolver.ExceptionSubscriptionResolver;
import com.arcone.biopro.exception.collector.api.graphql.service.DashboardSubscriptionService;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

/**
 * Bridge service to connect retry operations with GraphQL subscriptions.
 * Publishes retry events to the subscription system for real-time updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RetryEventBridge {

    private final ExceptionSubscriptionResolver subscriptionResolver;
    private final DashboardSubscriptionService dashboardSubscriptionService;

    /**
     * Publishes a retry status event to all subscribers.
     * 
     * @param transactionId the transaction ID of the exception being retried
     * @param retryAttempt the retry attempt details
     * @param eventType the type of retry event
     */
    public void publishRetryEvent(String transactionId, RetryAttempt retryAttempt, 
                                 ExceptionSubscriptionResolver.RetryEventType eventType) {
        
        log.info("🔄 Publishing retry event: {} for transaction: {}, attempt: {}", 
                eventType, transactionId, retryAttempt.getAttemptNumber());

        try {
            // Convert domain RetryAttempt to GraphQL RetryAttempt
            ExceptionSubscriptionResolver.RetryAttempt graphqlRetryAttempt = 
                new ExceptionSubscriptionResolver.RetryAttempt();
            graphqlRetryAttempt.setAttemptNumber(retryAttempt.getAttemptNumber());

            // Create retry status event
            ExceptionSubscriptionResolver.RetryStatusEvent retryEvent = 
                new ExceptionSubscriptionResolver.RetryStatusEvent(
                    transactionId,
                    graphqlRetryAttempt,
                    eventType,
                    OffsetDateTime.now()
                );

            // Publish to subscription resolver
            subscriptionResolver.publishRetryStatusUpdate(retryEvent);
            
            // Trigger dashboard update
            dashboardSubscriptionService.triggerUpdate();
            
            log.info("✅ Successfully published retry event for transaction: {}", transactionId);
            
        } catch (Exception e) {
            log.error("❌ Failed to publish retry event for transaction: {}", transactionId, e);
        }
    }

    /**
     * Publishes a retry initiated event.
     */
    public void publishRetryInitiated(String transactionId, RetryAttempt retryAttempt) {
        publishRetryEvent(transactionId, retryAttempt, ExceptionSubscriptionResolver.RetryEventType.INITIATED);
    }

    /**
     * Publishes a retry in progress event.
     */
    public void publishRetryInProgress(String transactionId, RetryAttempt retryAttempt) {
        publishRetryEvent(transactionId, retryAttempt, ExceptionSubscriptionResolver.RetryEventType.IN_PROGRESS);
    }

    /**
     * Publishes a retry completed event.
     */
    public void publishRetryCompleted(String transactionId, RetryAttempt retryAttempt) {
        publishRetryEvent(transactionId, retryAttempt, ExceptionSubscriptionResolver.RetryEventType.COMPLETED);
    }

    /**
     * Publishes a retry failed event.
     */
    public void publishRetryFailed(String transactionId, RetryAttempt retryAttempt) {
        publishRetryEvent(transactionId, retryAttempt, ExceptionSubscriptionResolver.RetryEventType.FAILED);
    }

    /**
     * Publishes a retry cancelled event.
     */
    public void publishRetryCancelled(String transactionId, RetryAttempt retryAttempt) {
        publishRetryEvent(transactionId, retryAttempt, ExceptionSubscriptionResolver.RetryEventType.CANCELLED);
    }
}