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
        private final com.arcone.biopro.exception.collector.api.graphql.config.GraphQLWebSocketTransportConfig.GraphQLWebSocketSessionManager webSocketSessionManager;

        /**
         * Publishes an exception created event to GraphQL subscribers.
         * 
         * @param exception   The newly created exception
         * @param triggeredBy Who or what triggered the creation
         */
        public void publishExceptionCreated(InterfaceException exception, String triggeredBy) {
                log.info("🔔 Publishing GraphQL exception created event for transaction: {}", exception.getTransactionId());
                log.info("🔍 DEBUG: Exception details - ID: {}, Status: {}, Severity: {}", 
                        exception.getId(), exception.getStatus(), exception.getSeverity());

                try {
                        ExceptionSubscriptionResolver.ExceptionUpdateEvent event = new ExceptionSubscriptionResolver.ExceptionUpdateEvent(
                                        ExceptionSubscriptionResolver.ExceptionEventType.CREATED,
                                        mapToGraphQLException(exception),
                                        OffsetDateTime.now(),
                                        triggeredBy);

                        log.info("📡 Calling subscriptionResolver.publishExceptionUpdate() for transaction: {}", exception.getTransactionId());
                        subscriptionResolver.publishExceptionUpdate(event);
                        log.info("✅ Successfully called subscriptionResolver.publishExceptionUpdate() for transaction: {}", exception.getTransactionId());
                        
                        // Also broadcast via WebSocket transport
                        String webSocketMessage = createWebSocketMessage("CREATED", exception, triggeredBy);
                        log.info("🌐 Broadcasting WebSocket message for transaction: {}", exception.getTransactionId());
                        webSocketSessionManager.broadcastToAll(webSocketMessage);
                        log.info("📡 Broadcasted exception created event via WebSocket to {} sessions", 
                                webSocketSessionManager.getActiveSessionCount());
                } catch (Exception e) {
                        log.error("❌ ERROR in publishExceptionCreated for transaction: {}", exception.getTransactionId(), e);
                        throw e;
                }
        }

        /**
         * Publishes an exception updated event to GraphQL subscribers.
         * 
         * @param exception   The updated exception
         * @param triggeredBy Who or what triggered the update
         */
        public void publishExceptionUpdated(InterfaceException exception, String triggeredBy) {
                log.info("🔔 Publishing GraphQL exception updated event for transaction: {}", exception.getTransactionId());

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
         * Maps all required fields for the GraphQL subscription.
         */
        private ExceptionSubscriptionResolver.Exception mapToGraphQLException(InterfaceException exception) {
                ExceptionSubscriptionResolver.Exception graphqlException = new ExceptionSubscriptionResolver.Exception();
                
                // Map all required fields
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
         * Creates a WebSocket message in GraphQL subscription format
         */
        private String createWebSocketMessage(String eventType, InterfaceException exception, String triggeredBy) {
                return String.format("""
                        {
                          "type": "next",
                          "payload": {
                            "data": {
                              "exceptionUpdated": {
                                "eventType": "%s",
                                "exception": {
                                  "transactionId": "%s",
                                  "status": "%s",
                                  "severity": "%s",
                                  "exceptionReason": "%s",
                                  "interfaceType": "%s",
                                  "retryCount": %d
                                },
                                "timestamp": "%s",
                                "triggeredBy": "%s"
                              }
                            }
                          }
                        }""",
                        eventType,
                        exception.getTransactionId(),
                        exception.getStatus() != null ? exception.getStatus().name() : "UNKNOWN",
                        exception.getSeverity() != null ? exception.getSeverity().name() : "UNKNOWN",
                        exception.getExceptionReason() != null ? exception.getExceptionReason().replace("\"", "\\\"") : "",
                        exception.getInterfaceType() != null ? exception.getInterfaceType().name() : "UNKNOWN",
                        exception.getRetryCount() != null ? exception.getRetryCount() : 0,
                        OffsetDateTime.now().toString(),
                        triggeredBy != null ? triggeredBy : "system"
                );
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