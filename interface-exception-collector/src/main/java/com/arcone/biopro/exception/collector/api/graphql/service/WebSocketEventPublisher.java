package com.arcone.biopro.exception.collector.api.graphql.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for publishing WebSocket events to GraphQL subscription clients.
 * Handles broadcasting exception updates and other real-time events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final SubscriptionFilterService subscriptionFilterService;

    // Track active subscriptions and their filters
    private final Map<String, SubscriptionContext> activeSubscriptions = new ConcurrentHashMap<>();

    /**
     * Publishes an exception update to all subscribed clients.
     * Applies filtering based on user permissions and subscription criteria.
     * 
     * @param exceptionEvent the exception event to publish
     */
    public void publishExceptionUpdate(Object exceptionEvent) {
        log.debug("Publishing exception update to WebSocket subscribers");

        try {
            // Broadcast to all subscribers on the exceptions topic
            messagingTemplate.convertAndSend("/topic/exceptions", exceptionEvent);

            // Send filtered updates to specific users if needed
            activeSubscriptions.forEach((sessionId, context) -> {
                if (shouldSendToSubscription(exceptionEvent, context)) {
                    sendToUser(context.getUsername(), exceptionEvent);
                }
            });

            log.debug("Exception update published successfully");
        } catch (Exception e) {
            log.error("Failed to publish exception update via WebSocket", e);
        }
    }

    /**
     * Publishes a retry operation result to subscribed clients.
     * 
     * @param retryEvent the retry event to publish
     */
    public void publishRetryUpdate(Object retryEvent) {
        log.debug("Publishing retry update to WebSocket subscribers");

        try {
            messagingTemplate.convertAndSend("/topic/retries", retryEvent);
            log.debug("Retry update published successfully");
        } catch (Exception e) {
            log.error("Failed to publish retry update via WebSocket", e);
        }
    }

    /**
     * Publishes system statistics updates to dashboard subscribers.
     * 
     * @param statsEvent the statistics event to publish
     */
    public void publishStatsUpdate(Object statsEvent) {
        log.debug("Publishing statistics update to WebSocket subscribers");

        try {
            messagingTemplate.convertAndSend("/topic/stats", statsEvent);
            log.debug("Statistics update published successfully");
        } catch (Exception e) {
            log.error("Failed to publish statistics update via WebSocket", e);
        }
    }

    /**
     * Registers a new subscription with its context and filters.
     * 
     * @param sessionId      the WebSocket session ID
     * @param username       the authenticated username
     * @param authentication the user's authentication context
     * @param filters        the subscription filters
     */
    public void registerSubscription(String sessionId, String username,
            Authentication authentication,
            SubscriptionFilterService.ExceptionSubscriptionFilters filters) {

        SubscriptionContext context = new SubscriptionContext(username, authentication, filters);
        activeSubscriptions.put(sessionId, context);

        log.info("Registered WebSocket subscription for user: {}, session: {}", username, sessionId);
    }

    /**
     * Unregisters a subscription when the WebSocket connection is closed.
     * 
     * @param sessionId the WebSocket session ID
     */
    public void unregisterSubscription(String sessionId) {
        SubscriptionContext removed = activeSubscriptions.remove(sessionId);
        if (removed != null) {
            log.info("Unregistered WebSocket subscription for user: {}, session: {}",
                    removed.getUsername(), sessionId);
        }
    }

    /**
     * Gets the count of active subscriptions.
     */
    public int getActiveSubscriptionCount() {
        return activeSubscriptions.size();
    }

    /**
     * Sends a message to a specific user.
     */
    private void sendToUser(String username, Object message) {
        try {
            messagingTemplate.convertAndSendToUser(username, "/queue/exceptions", message);
            log.debug("Sent personalized message to user: {}", username);
        } catch (Exception e) {
            log.error("Failed to send message to user: {}", username, e);
        }
    }

    /**
     * Determines if an event should be sent to a specific subscription.
     */
    private boolean shouldSendToSubscription(Object event, SubscriptionContext context) {
        // Check user permissions
        if (!subscriptionFilterService.canReceiveExceptionUpdate(context.getAuthentication(), event)) {
            return false;
        }

        // Check subscription filters
        return subscriptionFilterService.matchesSubscriptionFilters(event, context.getFilters());
    }

    /**
     * Context information for an active subscription.
     */
    private static class SubscriptionContext {
        private final String username;
        private final Authentication authentication;
        private final SubscriptionFilterService.ExceptionSubscriptionFilters filters;

        public SubscriptionContext(String username, Authentication authentication,
                SubscriptionFilterService.ExceptionSubscriptionFilters filters) {
            this.username = username;
            this.authentication = authentication;
            this.filters = filters;
        }

        public String getUsername() {
            return username;
        }

        public Authentication getAuthentication() {
            return authentication;
        }

        public SubscriptionFilterService.ExceptionSubscriptionFilters getFilters() {
            return filters;
        }
    }
}