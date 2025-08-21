package com.arcone.biopro.exception.collector.infrastructure.config;

import com.arcone.biopro.exception.collector.api.graphql.resolver.ExceptionSubscriptionResolver;
import com.arcone.biopro.exception.collector.api.graphql.dto.SubscriptionFilters;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket configuration for GraphQL subscriptions.
 * Provides real-time updates for exception events and retry status changes.
 */
@Configuration
@ConditionalOnProperty(name = "graphql.features.enabled", havingValue = "true", matchIfMissing = true)
@EnableWebSocket
@RequiredArgsConstructor
@Slf4j
public class GraphQLWebSocketConfig implements WebSocketConfigurer {

    private final ExceptionSubscriptionResolver subscriptionResolver;
    private final ObjectMapper objectMapper;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new GraphQLSubscriptionHandler(), "/subscriptions")
                .setAllowedOrigins("*"); // Configure CORS as needed
    }

    /**
     * WebSocket handler for GraphQL subscriptions.
     * Handles subscription lifecycle and real-time event streaming.
     */
    private class GraphQLSubscriptionHandler implements WebSocketHandler {

        private final Map<String, SubscriptionSession> activeSessions = new ConcurrentHashMap<>();

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            log.info("WebSocket connection established: {}", session.getId());

            // Send connection acknowledgment
            Map<String, Object> ack = Map.of(
                    "type", "connection_ack",
                    "payload", Map.of("message", "GraphQL subscription connection established"));

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(ack)));
        }

        @Override
        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
            if (message instanceof TextMessage textMessage) {
                try {
                    Map<String, Object> payload = objectMapper.readValue(textMessage.getPayload(), Map.class);
                    String type = (String) payload.get("type");
                    String id = (String) payload.get("id");

                    log.debug("Received WebSocket message - type: {}, id: {}", type, id);

                    switch (type) {
                        case "connection_init" -> handleConnectionInit(session, payload);
                        case "start" -> handleSubscriptionStart(session, id, payload);
                        case "stop" -> handleSubscriptionStop(session, id);
                        case "connection_terminate" -> handleConnectionTerminate(session);
                        default -> log.warn("Unknown message type: {}", type);
                    }

                } catch (Exception e) {
                    log.error("Error handling WebSocket message", e);
                    sendError(session, "INTERNAL_ERROR", "Error processing subscription message");
                }
            }
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
            cleanupSession(session.getId());
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
            log.info("WebSocket connection closed: {} - {}", session.getId(), closeStatus);
            cleanupSession(session.getId());
        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }

        private void handleConnectionInit(WebSocketSession session, Map<String, Object> payload) throws IOException {
            // Handle authentication if provided in payload
            Map<String, Object> connectionParams = (Map<String, Object>) payload.get("payload");

            // Send connection acknowledgment
            Map<String, Object> ack = Map.of("type", "connection_ack");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(ack)));

            // Send keep alive
            Map<String, Object> keepAlive = Map.of("type", "ka");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(keepAlive)));
        }

        private void handleSubscriptionStart(WebSocketSession session, String id, Map<String, Object> payload) {
            try {
                Map<String, Object> subscriptionPayload = (Map<String, Object>) payload.get("payload");
                String query = (String) subscriptionPayload.get("query");
                Map<String, Object> variables = (Map<String, Object>) subscriptionPayload.get("variables");

                log.debug("Starting subscription - id: {}, query: {}", id, query);

                // Determine subscription type and start appropriate subscription
                if (query.contains("exceptionUpdated")) {
                    startExceptionUpdatesSubscription(session, id, variables);
                } else if (query.contains("retryStatusUpdated")) {
                    startRetryStatusSubscription(session, id, variables);
                } else if (query.contains("summaryUpdated")) {
                    startSummaryUpdatesSubscription(session, id, variables);
                } else {
                    sendError(session, "SUBSCRIPTION_NOT_FOUND", "Unknown subscription type");
                }

            } catch (Exception e) {
                log.error("Error starting subscription", e);
                sendError(session, "SUBSCRIPTION_ERROR", "Failed to start subscription: " + e.getMessage());
            }
        }

        private void startExceptionUpdatesSubscription(WebSocketSession session, String id,
                Map<String, Object> variables) {
            try {
                // Extract subscription filters
                SubscriptionFilters filters = null;
                if (variables != null && variables.containsKey("filters")) {
                    filters = objectMapper.convertValue(variables.get("filters"), SubscriptionFilters.class);
                }

                // Get authentication context
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                // Start subscription using the resolver
                Flux<ExceptionSubscriptionResolver.ExceptionUpdateEvent> eventFlux = subscriptionResolver
                        .exceptionUpdated(filters, auth);

                // Subscribe to the flux and send events to WebSocket
                Disposable subscription = eventFlux.subscribe(
                        event -> {
                            try {
                                Map<String, Object> message = Map.of(
                                        "id", id,
                                        "type", "data",
                                        "payload", Map.of(
                                                "data", Map.of("exceptionUpdated", event)));

                                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));

                            } catch (Exception e) {
                                log.error("Error sending subscription event", e);
                            }
                        },
                        error -> {
                            log.error("Subscription error", error);
                            sendError(session, "SUBSCRIPTION_ERROR", error.getMessage());
                        },
                        () -> {
                            log.debug("Subscription completed for id: {}", id);
                            sendComplete(session, id);
                        });

                // Store subscription session
                activeSessions.put(session.getId() + ":" + id, new SubscriptionSession(session, subscription));

                log.info("Started exception updates subscription - session: {}, id: {}", session.getId(), id);

            } catch (Exception e) {
                log.error("Failed to start exception updates subscription", e);
                sendError(session, "SUBSCRIPTION_ERROR", "Failed to start exception updates subscription");
            }
        }

        private void startRetryStatusSubscription(WebSocketSession session, String id, Map<String, Object> variables) {
            try {
                // Extract transaction ID filter
                String transactionId = null;
                if (variables != null && variables.containsKey("transactionId")) {
                    transactionId = (String) variables.get("transactionId");
                }

                // Get authentication context
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                // Start subscription using the resolver
                Flux<ExceptionSubscriptionResolver.RetryStatusEvent> eventFlux = subscriptionResolver
                        .retryStatusUpdated(transactionId, auth);

                // Subscribe to the flux and send events to WebSocket
                Disposable subscription = eventFlux.subscribe(
                        event -> {
                            try {
                                Map<String, Object> message = Map.of(
                                        "id", id,
                                        "type", "data",
                                        "payload", Map.of(
                                                "data", Map.of("retryStatusUpdated", event)));

                                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));

                            } catch (Exception e) {
                                log.error("Error sending retry status event", e);
                            }
                        },
                        error -> {
                            log.error("Retry status subscription error", error);
                            sendError(session, "SUBSCRIPTION_ERROR", error.getMessage());
                        },
                        () -> {
                            log.debug("Retry status subscription completed for id: {}", id);
                            sendComplete(session, id);
                        });

                // Store subscription session
                activeSessions.put(session.getId() + ":" + id, new SubscriptionSession(session, subscription));

                log.info("Started retry status subscription - session: {}, id: {}", session.getId(), id);

            } catch (Exception e) {
                log.error("Failed to start retry status subscription", e);
                sendError(session, "SUBSCRIPTION_ERROR", "Failed to start retry status subscription");
            }
        }

        private void startSummaryUpdatesSubscription(WebSocketSession session, String id,
                Map<String, Object> variables) {
            try {
                // For summary updates, we could implement a periodic update mechanism
                // or integrate with event-driven summary updates

                // This is a placeholder implementation that sends periodic updates
                // In a real implementation, this would be event-driven

                log.info("Summary updates subscription requested but not yet implemented - session: {}, id: {}",
                        session.getId(), id);

                // Send a placeholder response
                Map<String, Object> message = Map.of(
                        "id", id,
                        "type", "data",
                        "payload", Map.of(
                                "data", Map.of(
                                        "summaryUpdated", Map.of(
                                                "totalExceptions", 0,
                                                "message",
                                                "Summary updates subscription is available but not yet fully implemented"))));

                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));

            } catch (Exception e) {
                log.error("Failed to start summary updates subscription", e);
                sendError(session, "SUBSCRIPTION_ERROR", "Failed to start summary updates subscription");
            }
        }

        private void handleSubscriptionStop(WebSocketSession session, String id) {
            String sessionKey = session.getId() + ":" + id;
            SubscriptionSession subscriptionSession = activeSessions.remove(sessionKey);

            if (subscriptionSession != null) {
                subscriptionSession.subscription.dispose();
                log.info("Stopped subscription - session: {}, id: {}", session.getId(), id);
            }

            sendComplete(session, id);
        }

        private void handleConnectionTerminate(WebSocketSession session) throws Exception {
            cleanupSession(session.getId());
            session.close();
        }

        private void cleanupSession(String sessionId) {
            // Clean up all subscriptions for this session
            activeSessions.entrySet().removeIf(entry -> {
                if (entry.getKey().startsWith(sessionId + ":")) {
                    entry.getValue().subscription.dispose();
                    return true;
                }
                return false;
            });

            log.debug("Cleaned up subscriptions for session: {}", sessionId);
        }

        private void sendError(WebSocketSession session, String code, String message) {
            try {
                Map<String, Object> error = Map.of(
                        "type", "error",
                        "payload", Map.of(
                                "message", message,
                                "extensions", Map.of("code", code)));

                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));

            } catch (Exception e) {
                log.error("Failed to send error message", e);
            }
        }

        private void sendComplete(WebSocketSession session, String id) {
            try {
                Map<String, Object> complete = Map.of(
                        "id", id,
                        "type", "complete");

                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(complete)));

            } catch (Exception e) {
                log.error("Failed to send complete message", e);
            }
        }
    }

    /**
     * Represents an active subscription session.
     */
    private static class SubscriptionSession {
        final WebSocketSession session;
        final Disposable subscription;

        SubscriptionSession(WebSocketSession session, Disposable subscription) {
            this.session = session;
            this.subscription = subscription;
        }
    }
}