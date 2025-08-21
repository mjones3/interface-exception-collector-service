package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.resolver.ExceptionSubscriptionResolver;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

/**
 * Service for publishing GraphQL subscription events for real-time updates.
 * Bridges between Kafka events and GraphQL subscriptions.
 */
@Service("graphQLExceptionEventPublisher")
@RequiredArgsConstructor
@Slf4j
public class ExceptionEventPublisher {

        private final ExceptionSubscriptionResolver subscriptionResolver;

        /**
         * Publishes an exception created event to GraphQL subscribers.
         * 
         * @param exception   The newly created exception
         * @param triggeredBy Who or what triggered the creation
         */
        public void publishExceptionCreated(InterfaceException exception, String triggeredBy) {
                log.debug("Publishing exception created event for transaction: {}", exception.getTransactionId());

                ExceptionSubscriptionResolver.ExceptionUpdateEvent event = new ExceptionSubscriptionResolver.ExceptionUpdateEvent(
                                ExceptionSubscriptionResolver.ExceptionEventType.CREATED,
                                mapToGraphQLException(exception),
                                OffsetDateTime.now(),
                                triggeredBy);

                subscriptionResolver.publishExceptionUpdate(event);
        }

        /**
         * Publishes an exception updated event to GraphQL subscribers.
         * 
         * @param exception   The updated exception
         * @param triggeredBy Who or what triggered the update
         */
        public void publishExceptionUpdated(InterfaceException exception, String triggeredBy) {
                log.debug("Publishing exception updated event for transaction: {}", exception.getTransactionId());

                ExceptionSubscriptionResolver.ExceptionUpdateEvent event = new ExceptionSubscriptionResolver.ExceptionUpdateEvent(
                                ExceptionSubscriptionResolver.ExceptionEventType.UPDATED,
                                mapToGraphQLException(exception),
                                OffsetDateTime.now(),
                                triggeredBy);

                subscriptionResolver.publishExceptionUpdate(event);
        }

        /**
         * Publishes an exception acknowledged event to GraphQL subscribers.
         * 
         * @param exception      The acknowledged exception
         * @param acknowledgedBy Who acknowledged the exception
         */
        public void publishExceptionAcknowledged(InterfaceException exception, String acknowledgedBy) {
                log.debug("Publishing exception acknowledged event for transaction: {}", exception.getTransactionId());

                ExceptionSubscriptionResolver.ExceptionUpdateEvent event = new ExceptionSubscriptionResolver.ExceptionUpdateEvent(
                                ExceptionSubscriptionResolver.ExceptionEventType.ACKNOWLEDGED,
                                mapToGraphQLException(exception),
                                OffsetDateTime.now(),
                                acknowledgedBy);

                subscriptionResolver.publishExceptionUpdate(event);
        }

        /**
         * Publishes an exception resolved event to GraphQL subscribers.
         * 
         * @param exception  The resolved exception
         * @param resolvedBy Who resolved the exception
         */
        public void publishExceptionResolved(InterfaceException exception, String resolvedBy) {
                log.debug("Publishing exception resolved event for transaction: {}", exception.getTransactionId());

                ExceptionSubscriptionResolver.ExceptionUpdateEvent event = new ExceptionSubscriptionResolver.ExceptionUpdateEvent(
                                ExceptionSubscriptionResolver.ExceptionEventType.RESOLVED,
                                mapToGraphQLException(exception),
                                OffsetDateTime.now(),
                                resolvedBy);

                subscriptionResolver.publishExceptionUpdate(event);
        }

        /**
         * Publishes a retry initiated event to GraphQL subscribers.
         * 
         * @param exception    The exception being retried
         * @param retryAttempt The retry attempt details
         * @param initiatedBy  Who initiated the retry
         */
        public void publishRetryInitiated(InterfaceException exception, RetryAttempt retryAttempt, String initiatedBy) {
                log.debug("Publishing retry initiated event for transaction: {}", exception.getTransactionId());

                // Publish exception update
                ExceptionSubscriptionResolver.ExceptionUpdateEvent exceptionEvent = new ExceptionSubscriptionResolver.ExceptionUpdateEvent(
                                ExceptionSubscriptionResolver.ExceptionEventType.RETRY_INITIATED,
                                mapToGraphQLException(exception),
                                OffsetDateTime.now(),
                                initiatedBy);

                subscriptionResolver.publishExceptionUpdate(exceptionEvent);

                // Publish retry status update
                ExceptionSubscriptionResolver.RetryStatusEvent retryEvent = new ExceptionSubscriptionResolver.RetryStatusEvent(
                                exception.getTransactionId(),
                                mapToGraphQLRetryAttempt(retryAttempt),
                                ExceptionSubscriptionResolver.RetryEventType.INITIATED,
                                OffsetDateTime.now());

                subscriptionResolver.publishRetryStatusUpdate(retryEvent);
        }

        /**
         * Publishes a retry completed event to GraphQL subscribers.
         * 
         * @param exception    The exception that was retried
         * @param retryAttempt The completed retry attempt
         * @param success      Whether the retry was successful
         */
        public void publishRetryCompleted(InterfaceException exception, RetryAttempt retryAttempt, boolean success) {
                log.debug("Publishing retry completed event for transaction: {} with success: {}",
                                exception.getTransactionId(), success);

                // Publish exception update
                ExceptionSubscriptionResolver.ExceptionUpdateEvent exceptionEvent = new ExceptionSubscriptionResolver.ExceptionUpdateEvent(
                                ExceptionSubscriptionResolver.ExceptionEventType.RETRY_COMPLETED,
                                mapToGraphQLException(exception),
                                OffsetDateTime.now(),
                                retryAttempt.getInitiatedBy());

                subscriptionResolver.publishExceptionUpdate(exceptionEvent);

                // Publish retry status update
                ExceptionSubscriptionResolver.RetryEventType eventType = success
                                ? ExceptionSubscriptionResolver.RetryEventType.COMPLETED
                                : ExceptionSubscriptionResolver.RetryEventType.FAILED;

                ExceptionSubscriptionResolver.RetryStatusEvent retryEvent = new ExceptionSubscriptionResolver.RetryStatusEvent(
                                exception.getTransactionId(),
                                mapToGraphQLRetryAttempt(retryAttempt),
                                eventType,
                                OffsetDateTime.now());

                subscriptionResolver.publishRetryStatusUpdate(retryEvent);
        }

        /**
         * Publishes a retry cancelled event to GraphQL subscribers.
         * 
         * @param exception    The exception whose retry was cancelled
         * @param retryAttempt The cancelled retry attempt
         * @param cancelledBy  Who cancelled the retry
         */
        public void publishRetryCancelled(InterfaceException exception, RetryAttempt retryAttempt, String cancelledBy) {
                log.debug("Publishing retry cancelled event for transaction: {}", exception.getTransactionId());

                // Publish exception update
                ExceptionSubscriptionResolver.ExceptionUpdateEvent exceptionEvent = new ExceptionSubscriptionResolver.ExceptionUpdateEvent(
                                ExceptionSubscriptionResolver.ExceptionEventType.CANCELLED,
                                mapToGraphQLException(exception),
                                OffsetDateTime.now(),
                                cancelledBy);

                subscriptionResolver.publishExceptionUpdate(exceptionEvent);

                // Publish retry status update
                ExceptionSubscriptionResolver.RetryStatusEvent retryEvent = new ExceptionSubscriptionResolver.RetryStatusEvent(
                                exception.getTransactionId(),
                                mapToGraphQLRetryAttempt(retryAttempt),
                                ExceptionSubscriptionResolver.RetryEventType.CANCELLED,
                                OffsetDateTime.now());

                subscriptionResolver.publishRetryStatusUpdate(retryEvent);
        }

        /**
         * Maps domain InterfaceException to GraphQL Exception for subscriptions.
         * This is a simplified mapping - in a real implementation, this would use
         * the actual GraphQL Exception type.
         */
        private ExceptionSubscriptionResolver.Exception mapToGraphQLException(InterfaceException exception) {
                ExceptionSubscriptionResolver.Exception graphqlException = new ExceptionSubscriptionResolver.Exception();
                graphqlException.setTransactionId(exception.getTransactionId());
                return graphqlException;
        }

        /**
         * Maps domain RetryAttempt to GraphQL RetryAttempt for subscriptions.
         * This is a simplified mapping - in a real implementation, this would use
         * the actual GraphQL RetryAttempt type.
         */
        private ExceptionSubscriptionResolver.RetryAttempt mapToGraphQLRetryAttempt(RetryAttempt retryAttempt) {
                ExceptionSubscriptionResolver.RetryAttempt graphqlRetryAttempt = new ExceptionSubscriptionResolver.RetryAttempt();
                graphqlRetryAttempt.setAttemptNumber(retryAttempt.getAttemptNumber());
                return graphqlRetryAttempt;
        }
}