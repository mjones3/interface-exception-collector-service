package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.resolver.ExceptionSubscriptionResolver;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

/**
 * Service that bridges domain events to GraphQL subscription events.
 * Listens for application events and publishes them to GraphQL subscribers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionEventBridge {

    private final ExceptionSubscriptionResolver subscriptionResolver;

    /**
     * Handles exception created events and publishes to GraphQL subscribers.
     */
    @EventListener
    public void handleExceptionCreated(ExceptionCreatedEvent event) {
        log.debug("Handling exception created event for transaction: {}", event.getException().getTransactionId());

        ExceptionSubscriptionResolver.ExceptionUpdateEvent subscriptionEvent = new ExceptionSubscriptionResolver.ExceptionUpdateEvent(
                ExceptionSubscriptionResolver.ExceptionEventType.CREATED,
                mapToGraphQLException(event.getException()),
                OffsetDateTime.now(),
                event.getTriggeredBy());

        subscriptionResolver.publishExceptionUpdate(subscriptionEvent);
    }

    /**
     * Handles exception updated events and publishes to GraphQL subscribers.
     */
    @EventListener
    public void handleExceptionUpdated(ExceptionUpdatedEvent event) {
        log.debug("Handling exception updated event for transaction: {}", event.getException().getTransactionId());
        
        ExceptionSubscriptionResolver.ExceptionUpdateEvent subscriptionEvent = 
            new ExceptionSubscriptionResolver.ExceptionUpdateEvent(
                ExceptionSubscriptionResolver.ExceptionEventType.UPDATED,
                mapToGraphQLException(event.getException()),
                OffsetDateTime.now(),
                event.getTriggeredBy()
            );
        
        subscriptionResolver.publishExceptionUpdate(subscriptionEvent);
    }

/**
     * Handles exception acknowledged events and publishes to GraphQL subscribers.
     */
    @EventListener
    public void handleExceptionAcknowledged(ExceptionAcknowledgedEvent event) {
        log.debug("Handling exception acknowledged event for transaction: {}", event.getException().getTransactionId());
        
        ExceptionSubscriptionResolver.ExceptionUpdateEvent subscriptionEvent = 
            new ExceptionSubscriptionResolver.ExceptionUpdateEvent(
                ExceptionSubscriptionResolver.ExceptionEventType.ACKNOWLEDGED,
                mapToGraphQLException(event.getException()),
                OffsetDateTime.now(),
                event.getAcknowledgedBy()
            );
        
        subscriptionResolver.publishExceptionUpdate(subscriptionEvent);
    }

    /**
     * Handles exception resolved events and publishes to GraphQL subscribers.
     */
    @EventListener
    public void handleExceptionResolved(ExceptionResolvedEvent event) {
        log.debug("Handling exception resolved event for transaction: {}", event.getException().getTransactionId());
        
        ExceptionSubscriptionResolver.ExceptionUpdateEvent subscriptionEvent = 
            new ExceptionSubscriptionResolver.ExceptionUpdateEvent(
                ExceptionSubscriptionResolver.ExceptionEventType.RESOLVED,
                mapToGraphQLException(event.getException()),
                OffsetDateTime.now(),
                event.getResolvedBy()
            );
        
        subscriptionResolver.publishExceptionUpdate(subscriptionEvent);
    }

    /**
     * Handles retry initiated events and publishes to GraphQL subscribers.
     */
    @EventListener
    public void handleRetryInitiated(RetryInitiatedEvent event) {
        log.debug("Handling retry initiated event for transaction: {}", event.getException().getTransactionId());
        
        // Publish exception update
        ExceptionSubscriptionResolver.ExceptionUpdateEvent exceptionEvent = 
            new ExceptionSubscriptionResolver.ExceptionUpdateEvent(
                ExceptionSubscriptionResolver.ExceptionEventType.RETRY_INITIATED,
                mapToGraphQLException(event.getException()),
                OffsetDateTime.now(),
                event.getInitiatedBy()
            );
        
        subscriptionResolver.publishExceptionUpdate(exceptionEvent);

        // Publish retry status update
        ExceptionSubscriptionResolver.RetryStatusEvent retryEvent = 
            new ExceptionSubscriptionResolver.RetryStatusEvent(
                event.getException().getTransactionId(),
                mapToGraphQLRetryAttempt(event.getRetryAttempt()),
                ExceptionSubscriptionResolver.RetryEventType.INITIATED,
                OffsetDateTime.now()
            );
        
        subscriptionResolver.publishRetryStatusUpdate(retryEvent);
    }

    /**
     * Handles retry completed events and publishes to GraphQL subscribers.
     */
    @EventListener
    public void handleRetryCompleted(RetryCompletedEvent event) {
        log.debug("Handling retry completed event for transaction: {} with success: {}", 
            event.getException().getTransactionId(), event.isSuccess());
        
        // Publish exception update
        ExceptionSubscriptionResolver.ExceptionUpdateEvent exceptionEvent = 
            new ExceptionSubscriptionResolver.ExceptionUpdateEvent(
                ExceptionSubscriptionResolver.ExceptionEventType.RETRY_COMPLETED,
                mapToGraphQLException(event.getException()),
                OffsetDateTime.now(),
                event.getRetryAttempt().getInitiatedBy()
            );
        
        subscriptionResolver.publishExceptionUpdate(exceptionEvent);

        // Publish retry status update
        ExceptionSubscriptionResolver.RetryEventType eventType = event.isSuccess() 
            ? ExceptionSubscriptionResolver.RetryEventType.COMPLETED
            : ExceptionSubscriptionResolver.RetryEventType.FAILED;

        ExceptionSubscriptionResolver.RetryStatusEvent retryEvent = 
            new ExceptionSubscriptionResolver.RetryStatusEvent(
                event.getException().getTransactionId(),
                mapToGraphQLRetryAttempt(event.getRetryAttempt()),
                eventType,
                OffsetDateTime.now()
            );
        
        subscriptionResolver.publishRetryStatusUpdate(retryEvent);
    }

    /**
     * Handles retry cancelled events and publishes to GraphQL subscribers.
     */
    @EventListener
    public void handleRetryCancelled(RetryCancelledEvent event) {
        log.debug("Handling retry cancelled event for transaction: {}", event.getException().getTransactionId());
        
        // Publish exception update
        ExceptionSubscriptionResolver.ExceptionUpdateEvent exceptionEvent = 
            new ExceptionSubscriptionResolver.ExceptionUpdateEvent(
                ExceptionSubscriptionResolver.ExceptionEventType.CANCELLED,
                mapToGraphQLException(event.getException()),
                OffsetDateTime.now(),
                event.getCancelledBy()
            );
        
        subscriptionResolver.publishExceptionUpdate(exceptionEvent);

        // Publish retry status update
        ExceptionSubscriptionResolver.RetryStatusEvent retryEvent = 
            new ExceptionSubscriptionResolver.RetryStatusEvent(
                event.getException().getTransactionId(),
                mapToGraphQLRetryAttempt(event.getRetryAttempt()),
                ExceptionSubscriptionResolver.RetryEventType.CANCELLED,
                OffsetDateTime.now()
            );
        
        subscriptionResolver.publishRetryStatusUpdate(retryEvent);
    }

    /**
     * Maps domain InterfaceException to GraphQL Exception for subscriptions.
     */
    private ExceptionSubscriptionResolver.Exception mapToGraphQLException(InterfaceException exception) {
        ExceptionSubscriptionResolver.Exception graphqlException = new ExceptionSubscriptionResolver.Exception();
        graphqlException.setTransactionId(exception.getTransactionId());
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

    // Event classes for application events
    public static class ExceptionCreatedEvent {
        private final InterfaceException exception;
        private final String triggeredBy;

        public ExceptionCreatedEvent(InterfaceException exception, String triggeredBy) {
            this.exception = exception;
            this.triggeredBy = triggeredBy;
        }

        public InterfaceException getException() { return exception; }
        public String getTriggeredBy() { return triggeredBy; }
    }

    public static class ExceptionUpdatedEvent {
        private final InterfaceException exception;
        private final String triggeredBy;

        public ExceptionUpdatedEvent(InterfaceException exception, String triggeredBy) {
            this.exception = exception;
            this.triggeredBy = triggeredBy;
        }

        public InterfaceException getException() { return exception; }
        public String getTriggeredBy() { return triggeredBy; }
    }

    public static class ExceptionAcknowledgedEvent {
        private final InterfaceException exception;
        private final String acknowledgedBy;

        public ExceptionAcknowledgedEvent(InterfaceException exception, String acknowledgedBy) {
            this.exception = exception;
            this.acknowledgedBy = acknowledgedBy;
        }

        public InterfaceException getException() { return exception; }
        public String getAcknowledgedBy() { return acknowledgedBy; }
    }

    public static class ExceptionResolvedEvent {
        private final InterfaceException exception;
        private final String resolvedBy;

        public ExceptionResolvedEvent(InterfaceException exception, String resolvedBy) {
            this.exception = exception;
            this.resolvedBy = resolvedBy;
        }

        public InterfaceException getException() { return exception; }
        public String getResolvedBy() { return resolvedBy; }
    }

    public static class RetryInitiatedEvent {
        private final InterfaceException exception;
        private final RetryAttempt retryAttempt;
        private final String initiatedBy;

        public RetryInitiatedEvent(InterfaceException exception, RetryAttempt retryAttempt, String initiatedBy) {
            this.exception = exception;
            this.retryAttempt = retryAttempt;
            this.initiatedBy = initiatedBy;
        }

        public InterfaceException getException() { return exception; }
        public RetryAttempt getRetryAttempt() { return retryAttempt; }
        public String getInitiatedBy() { return initiatedBy; }
    }

    public static class RetryCompletedEvent {
        private final InterfaceException exception;
        private final RetryAttempt retryAttempt;
        private final boolean success;

        public RetryCompletedEvent(InterfaceException exception, RetryAttempt retryAttempt, boolean success) {
            this.exception = exception;
            this.retryAttempt = retryAttempt;
            this.success = success;
        }

        public InterfaceException getException() { return exception; }
        public RetryAttempt getRetryAttempt() { return retryAttempt; }
        public boolean isSuccess() { return success; }
    }

    public static class RetryCancelledEvent {
        private final InterfaceException exception;
        private final RetryAttempt retryAttempt;
        private final String cancelledBy;
        private final String reason;

        public RetryCancelledEvent(InterfaceException exception, RetryAttempt retryAttempt, String cancelledBy, String reason) {
            this.exception = exception;
            this.retryAttempt = retryAttempt;
            this.cancelledBy = cancelledBy;
            this.reason = reason;
        }

        public InterfaceException getException() { return exception; }
        public RetryAttempt getRetryAttempt() { return retryAttempt; }
        public String getCancelledBy() { return cancelledBy; }
        public String getReason() { return reason; }
    }
}