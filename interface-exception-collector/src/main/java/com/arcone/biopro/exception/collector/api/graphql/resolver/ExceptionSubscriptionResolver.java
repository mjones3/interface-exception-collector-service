package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionFilters;
import com.arcone.biopro.exception.collector.api.graphql.dto.SubscriptionFilters;
import com.arcone.biopro.exception.collector.api.graphql.service.DashboardSubscriptionService;
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
    private final DashboardSubscriptionService dashboardSubscriptionService;

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
    @SubscriptionMapping("exceptionUpdated")
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
    @SubscriptionMapping("retryStatusUpdated")
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
     * GraphQL subscription for real-time dashboard metrics.
     * Provides aggregated statistics including active exceptions, retry rates, and API performance.
     * 
     * @param authentication User authentication context
     * @return Flux of dashboard summary updates
     */
    @SubscriptionMapping("dashboardSummary")
    @PreAuthorize("hasRole('VIEWER')")
    public Flux<DashboardSubscriptionService.DashboardSummary> dashboardSummary(Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Starting dashboard summary subscription for user: {}", username);

        return dashboardSubscriptionService.getDashboardStream()
                .doOnSubscribe(subscription -> {
                    log.info("User {} subscribed to dashboard summary", username);
                })
                .doOnCancel(() -> {
                    log.info("User {} cancelled dashboard summary subscription", username);
                })
                .timeout(Duration.ofMinutes(60)) // Longer timeout for dashboard
                .onErrorResume(throwable -> {
                    log.warn("Dashboard summary subscription timeout or error for user {}: {}", username,
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
        log.info("📡 Received exception update event for publishing: {} - transaction: {}", 
                event.getEventType(), event.getException().getTransactionId());
        log.info("🔍 DEBUG: Active subscriptions count: {}", getActiveSubscriptionCount());
        log.info("🔍 DEBUG: Sink has subscribers: {}", exceptionUpdateSink.currentSubscriberCount());

        try {
            Sinks.EmitResult result = exceptionUpdateSink.tryEmitNext(event);

            if (result.isFailure()) {
                log.warn("❌ Failed to emit exception update event: {} - Reason: {}", result, result.name());
                log.warn("🔍 DEBUG: Sink state - subscribers: {}, terminated: {}", 
                        exceptionUpdateSink.currentSubscriberCount(), 
                        result == Sinks.EmitResult.FAIL_TERMINATED);
            } else {
                log.info("✅ Successfully published exception update event for transaction: {} to {} subscribers",
                        event.getException().getTransactionId(), getActiveSubscriptionCount());
                log.info("🔍 DEBUG: Sink subscribers after emit: {}", exceptionUpdateSink.currentSubscriberCount());
            }
        } catch (java.lang.Exception e) {
            log.error("❌ ERROR in publishExceptionUpdate for transaction: {}", 
                    event.getException().getTransactionId(), e);
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
        } catch (java.lang.Exception e) {
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

    // GraphQL Exception class for subscriptions - matches the GraphQL schema
    public static class Exception {
        private String id;
        private String transactionId;
        private String externalId;
        private String interfaceType;
        private String exceptionReason;
        private String operation;
        private String status;
        private String severity;
        private String category;
        private String customerId;
        private String locationCode;
        private java.time.OffsetDateTime timestamp;
        private java.time.OffsetDateTime processedAt;
        private Boolean retryable;
        private Integer retryCount;
        private Integer maxRetries;
        private java.time.OffsetDateTime lastRetryAt;
        private String acknowledgedBy;
        private java.time.OffsetDateTime acknowledgedAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public String getExternalId() { return externalId; }
        public void setExternalId(String externalId) { this.externalId = externalId; }
        
        public String getInterfaceType() { return interfaceType; }
        public void setInterfaceType(String interfaceType) { this.interfaceType = interfaceType; }
        
        public String getExceptionReason() { return exceptionReason; }
        public void setExceptionReason(String exceptionReason) { this.exceptionReason = exceptionReason; }
        
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public String getLocationCode() { return locationCode; }
        public void setLocationCode(String locationCode) { this.locationCode = locationCode; }
        
        public java.time.OffsetDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(java.time.OffsetDateTime timestamp) { this.timestamp = timestamp; }
        
        public java.time.OffsetDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(java.time.OffsetDateTime processedAt) { this.processedAt = processedAt; }
        
        public Boolean getRetryable() { return retryable; }
        public void setRetryable(Boolean retryable) { this.retryable = retryable; }
        
        public Integer getRetryCount() { return retryCount; }
        public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
        
        public Integer getMaxRetries() { return maxRetries; }
        public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }
        
        public java.time.OffsetDateTime getLastRetryAt() { return lastRetryAt; }
        public void setLastRetryAt(java.time.OffsetDateTime lastRetryAt) { this.lastRetryAt = lastRetryAt; }
        
        public String getAcknowledgedBy() { return acknowledgedBy; }
        public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }
        
        public java.time.OffsetDateTime getAcknowledgedAt() { return acknowledgedAt; }
        public void setAcknowledgedAt(java.time.OffsetDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
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