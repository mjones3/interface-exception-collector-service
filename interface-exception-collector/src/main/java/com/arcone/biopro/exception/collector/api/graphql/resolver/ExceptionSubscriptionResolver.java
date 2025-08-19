package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionFilters;
import com.arcone.biopro.exception.collector.api.graphql.dto.SubscriptionFilters;
import com.arcone.biopro.exception.collector.api.graphql.service.GraphQLSecurityService;
import com.arcone.biopro.exception.collector.api.graphql.service.SubscriptionFilterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * GraphQL subscription resolver for real-time exception updates.
 * Provides WebSocket-based subscriptions for exception events with filtering
 * and security.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ExceptionSubscriptionResolver {

    private final SubscriptionFilterService subscriptionFilterService;
    private final GraphQLSecurityService securityService;

    // Sinks for broadcasting events to subscribers
    private final Sinks.Many<ExceptionUpdateEvent> exceptionUpdateSink = Sinks.many().multicast()
            .onBackpressureBuffer();

    private final Sinks.Many<RetryStatusEvent> retryStatusSink = Sinks.many().multicast().onBackpressureBuffer();

    // Track active subscriptions for metrics
    private final ConcurrentMap<String, SubscriptionMetrics> activeSubscriptions = new ConcurrentHashMap<>();

    /**
     * GraphQL subscription for real-time exception updates.
     * Filters events based on user permissions and subscription criteria.
     * 
     * @param filters        Optional filters to apply to the subscription
     * @param authentication User authentication context
     * @return Flux of exception update events
     */
    @SubscriptionMapping
    @PreAuthorize("hasRole('VIEWER')")
    public Flux<ExceptionUpdateEvent> exceptionUpdated(
            @Argument("filters") SubscriptionFilters filters,
            Authentication authentication) {

        String username = authentication.getName();
        String subscriptionId = generateSubscriptionId(username);

        log.info("Starting exception subscription for user: {} with filters: {}", username, filters);

        // Track subscription metrics
        activeSubscriptions.put(subscriptionId, new SubscriptionMetrics(username, OffsetDateTime.now()));

        return exceptionUpdateSink.asFlux()
                .filter(event -> {
                    // Apply security filtering
                    if (!securityService.canViewException(authentication, event.getException())) {
                        log.debug("Filtering out exception update for user {} due to security restrictions", username);
                        return false;
                    }

                    // Apply subscription filters
                    if (filters != null && !subscriptionFilterService.matchesExceptionFilters(event,
                            filters.toExceptionFilters())) {
                        log.debug("Filtering out exception update for user {} due to subscription filters", username);
                        return false;
                    }

                    return true;
                })
                .doOnSubscribe(subscription -> {
                    log.info("User {} subscribed to exception updates", username);
                })
                .doOnCancel(() -> {
                    log.info("User {} cancelled exception subscription", username);
                    activeSubscriptions.remove(subscriptionId);
                })
                .doOnError(error -> {
                    log.error("Error in exception subscription for user {}: {}", username, error.getMessage(), error);
                    activeSubscriptions.remove(subscriptionId);
                })
                .timeout(Duration.ofMinutes(30)) // Prevent zombie subscriptions
                .onErrorResume(throwable -> {
                    log.warn("Exception subscription timeout or error for user {}: {}", username,
                            throwable.getMessage());
                    return Flux.empty();
                });
    }

    /**
     * GraphQL subscription for retry status updates.
     * Optionally filters by specific transaction ID.
     * 
     * @param transactionId  Optional transaction ID to filter by
     * @param authentication User authentication context
     * @return Flux of retry status events
     */
    @SubscriptionMapping
    @PreAuthorize("hasRole('OPERATIONS')")
    public Flux<RetryStatusEvent> retryStatusUpdated(
            @Argument("transactionId") String transactionId,
            Authentication authentication) {

        String username = authentication.getName();
        log.info("Starting retry status subscription for user: {} with transaction ID: {}", username, transactionId);

        return retryStatusSink.asFlux()
                .filter(event -> {
                    // Filter by transaction ID if specified
                    if (transactionId != null && !transactionId.equals(event.getTransactionId())) {
                        return false;
                    }

                    // Apply security filtering
                    return securityService.canViewRetryStatus(authentication, event);
                })
                .doOnSubscribe(subscription -> {
                    log.info("User {} subscribed to retry status updates", username);
                })
                .doOnCancel(() -> {
                    log.info("User {} cancelled retry status subscription", username);
                })
                .timeout(Duration.ofMinutes(15)) // Shorter timeout for retry subscriptions
                .onErrorResume(throwable -> {
                    log.warn("Retry status subscription timeout or error for user {}: {}", username,
                            throwable.getMessage());
                    return Flux.empty();
                });
    }

    /**
     * Publishes an exception update event to all subscribers.
     * Called by Kafka consumers when exception events are received.
     * 
     * @param event The exception update event to publish
     */
    public void publishExceptionUpdate(ExceptionUpdateEvent event) {
        log.debug("Publishing exception update event: {}", event.getEventType());

        try {
            Sinks.EmitResult result = exceptionUpdateSink.tryEmitNext(event);

            if (result.isFailure()) {
                log.warn("Failed to emit exception update event: {}", result);
            } else {
                log.debug("Successfully published exception update event for transaction: {}",
                        event.getException().getTransactionId());
            }
        } catch (Exception e) {
            log.error("Error publishing exception update event", e);
        }
    }

    /**
     * Publishes a retry status event to all subscribers.
     * Called by retry services when retry operations complete.
     * 
     * @param event The retry status event to publish
     */
    public void publishRetryStatusUpdate(RetryStatusEvent event) {
        log.debug("Publishing retry status event: {} for transaction: {}",
                event.getEventType(), event.getTransactionId());

        try {
            Sinks.EmitResult result = retryStatusSink.tryEmitNext(event);

            if (result.isFailure()) {
                log.warn("Failed to emit retry status event: {}", result);
            } else {
                log.debug("Successfully published retry status event for transaction: {}",
                        event.getTransactionId());
            }
        } catch (Exception e) {
            log.error("Error publishing retry status event", e);
        }
    }

    /**
     * Gets the count of active exception subscriptions.
     * Used for monitoring and metrics.
     */
    public int getActiveSubscriptionCount() {
        return activeSubscriptions.size();
    }

    /**
     * Generates a unique subscription ID for tracking.
     */
    private String generateSubscriptionId(String username) {
        return username + "_" + System.currentTimeMillis();
    }

    /**
     * Metrics for tracking subscription activity.
     */
    private static class SubscriptionMetrics {
        private final String username;
        private final OffsetDateTime startTime;

        public SubscriptionMetrics(String username, OffsetDateTime startTime) {
            this.username = username;
            this.startTime = startTime;
        }

        public String getUsername() {
            return username;
        }

        public OffsetDateTime getStartTime() {
            return startTime;
        }
    }

    /**
     * Exception update event for GraphQL subscriptions.
     */
    public static class ExceptionUpdateEvent {
        private final ExceptionEventType eventType;
        private final Exception exception;
        private final OffsetDateTime timestamp;
        private final String triggeredBy;

        public ExceptionUpdateEvent(ExceptionEventType eventType, Exception exception,
                OffsetDateTime timestamp, String triggeredBy) {
            this.eventType = eventType;
            this.exception = exception;
            this.timestamp = timestamp;
            this.triggeredBy = triggeredBy;
        }

        public ExceptionEventType getEventType() {
            return eventType;
        }

        public Exception getException() {
            return exception;
        }

        public OffsetDateTime getTimestamp() {
            return timestamp;
        }

        public String getTriggeredBy() {
            return triggeredBy;
        }
    }

    /**
     * Retry status event for GraphQL subscriptions.
     */
    public static class RetryStatusEvent {
        private final String transactionId;
        private final RetryAttempt retryAttempt;
        private final RetryEventType eventType;
        private final OffsetDateTime timestamp;

        public RetryStatusEvent(String transactionId, RetryAttempt retryAttempt,
                RetryEventType eventType, OffsetDateTime timestamp) {
            this.transactionId = transactionId;
            this.retryAttempt = retryAttempt;
            this.eventType = eventType;
            this.timestamp = timestamp;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public RetryAttempt getRetryAttempt() {
            return retryAttempt;
        }

        public RetryEventType getEventType() {
            return eventType;
        }

        public OffsetDateTime getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Exception event types for subscriptions.
     */
    public enum ExceptionEventType {
        CREATED,
        UPDATED,
        ACKNOWLEDGED,
        RETRY_INITIATED,
        RETRY_COMPLETED,
        RESOLVED,
        CANCELLED
    }

    /**
     * Retry event types for subscriptions.
     */
    public enum RetryEventType {
        INITIATED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    // Placeholder classes - these should reference actual domain entities
    public static class Exception {
        private String transactionId;

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }
    }

    public static class RetryAttempt {
        private int attemptNumber;

        public int getAttemptNumber() {
            return attemptNumber;
        }

        public void setAttemptNumber(int attemptNumber) {
            this.attemptNumber = attemptNumber;
        }
    }
}