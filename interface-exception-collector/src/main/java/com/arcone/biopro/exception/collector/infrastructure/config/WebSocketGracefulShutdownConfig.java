package com.arcone.biopro.exception.collector.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Configuration for graceful shutdown of WebSocket connections.
 * Ensures all WebSocket connections are properly closed during application shutdown
 * to support zero-downtime blue-green deployments.
 */
@Slf4j
@Component
public class WebSocketGracefulShutdownConfig implements ApplicationListener<ContextClosedEvent> {

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry userRegistry;
    private final ConcurrentHashMap<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final AtomicBoolean shutdownInitiated = new AtomicBoolean(false);

    @Value("${websocket.graceful-shutdown.timeout:30s}")
    private Duration shutdownTimeout;

    @Value("${websocket.graceful-shutdown.close-code:1001}")
    private int closeCode;

    @Value("${websocket.graceful-shutdown.close-reason:Server shutting down}")
    private String closeReason;

    @Value("${websocket.connection-limits.heartbeat-interval:30s}")
    private Duration heartbeatInterval;

    public WebSocketGracefulShutdownConfig(SimpMessagingTemplate messagingTemplate, 
                                         SimpUserRegistry userRegistry) {
        this.messagingTemplate = messagingTemplate;
        this.userRegistry = userRegistry;
    }

    /**
     * WebSocket handler decorator factory to track active sessions
     */
    @Component
    public static class SessionTrackingDecoratorFactory implements WebSocketHandlerDecoratorFactory {
        
        private final WebSocketGracefulShutdownConfig shutdownConfig;

        public SessionTrackingDecoratorFactory(WebSocketGracefulShutdownConfig shutdownConfig) {
            this.shutdownConfig = shutdownConfig;
        }

        @Override
        public org.springframework.web.socket.WebSocketHandler decorate(
                org.springframework.web.socket.WebSocketHandler handler) {
            return new WebSocketHandlerDecorator(handler) {
                @Override
                public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                    shutdownConfig.registerSession(session);
                    super.afterConnectionEstablished(session);
                }

                @Override
                public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                    shutdownConfig.unregisterSession(session);
                    super.afterConnectionClosed(session, closeStatus);
                }
            };
        }
    }

    /**
     * Register a new WebSocket session
     */
    public void registerSession(WebSocketSession session) {
        activeSessions.put(session.getId(), session);
        log.debug("Registered WebSocket session: {} (Total active: {})", 
                 session.getId(), activeSessions.size());
    }

    /**
     * Unregister a WebSocket session
     */
    public void unregisterSession(WebSocketSession session) {
        activeSessions.remove(session.getId());
        log.debug("Unregistered WebSocket session: {} (Total active: {})", 
                 session.getId(), activeSessions.size());
    }

    /**
     * Start heartbeat mechanism to keep connections alive and detect dead connections
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startHeartbeat() {
        scheduler.scheduleAtFixedRate(this::sendHeartbeat, 
                                    heartbeatInterval.toSeconds(), 
                                    heartbeatInterval.toSeconds(), 
                                    TimeUnit.SECONDS);
        log.info("WebSocket heartbeat started with interval: {}", heartbeatInterval);
    }

    /**
     * Send heartbeat to all active WebSocket connections
     */
    private void sendHeartbeat() {
        if (shutdownInitiated.get()) {
            return;
        }

        try {
            messagingTemplate.convertAndSend("/topic/heartbeat", 
                new HeartbeatMessage(System.currentTimeMillis()));
            
            log.debug("Sent heartbeat to {} active WebSocket connections", activeSessions.size());
        } catch (Exception e) {
            log.warn("Failed to send WebSocket heartbeat", e);
        }
    }

    /**
     * Handle application shutdown event
     */
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        initiateGracefulShutdown();
    }

    /**
     * Initiate graceful shutdown of all WebSocket connections
     */
    public void initiateGracefulShutdown() {
        if (!shutdownInitiated.compareAndSet(false, true)) {
            log.info("Graceful shutdown already initiated");
            return;
        }

        log.info("Initiating graceful shutdown of {} WebSocket connections", activeSessions.size());

        try {
            // Send shutdown notification to all connected clients
            sendShutdownNotification();

            // Wait a moment for clients to receive the notification
            Thread.sleep(2000);

            // Close all active sessions gracefully
            closeAllSessions();

            // Wait for sessions to close or timeout
            waitForSessionsClosure();

        } catch (Exception e) {
            log.error("Error during WebSocket graceful shutdown", e);
        } finally {
            log.info("WebSocket graceful shutdown completed");
        }
    }

    /**
     * Send shutdown notification to all connected clients
     */
    private void sendShutdownNotification() {
        try {
            ShutdownNotification notification = new ShutdownNotification(
                "Server is shutting down. Please reconnect in a few moments.",
                System.currentTimeMillis(),
                shutdownTimeout.toSeconds()
            );

            messagingTemplate.convertAndSend("/topic/shutdown", notification);
            log.info("Sent shutdown notification to all WebSocket clients");

        } catch (Exception e) {
            log.warn("Failed to send shutdown notification", e);
        }
    }

    /**
     * Close all active WebSocket sessions
     */
    private void closeAllSessions() {
        CloseStatus closeStatus = new CloseStatus(closeCode, closeReason);
        
        activeSessions.values().parallelStream().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.close(closeStatus);
                    log.debug("Closed WebSocket session: {}", session.getId());
                }
            } catch (Exception e) {
                log.warn("Failed to close WebSocket session: {}", session.getId(), e);
            }
        });
    }

    /**
     * Wait for all sessions to close or timeout
     */
    private void waitForSessionsClosure() {
        long startTime = System.currentTimeMillis();
        long timeoutMillis = shutdownTimeout.toMillis();

        while (!activeSessions.isEmpty() && 
               (System.currentTimeMillis() - startTime) < timeoutMillis) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        if (!activeSessions.isEmpty()) {
            log.warn("Timeout reached. {} WebSocket sessions still active after {}ms", 
                    activeSessions.size(), timeoutMillis);
        } else {
            log.info("All WebSocket sessions closed gracefully");
        }
    }

    /**
     * Get current active session count
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * Check if shutdown has been initiated
     */
    public boolean isShutdownInitiated() {
        return shutdownInitiated.get();
    }

    /**
     * Cleanup resources
     */
    @PreDestroy
    public void cleanup() {
        log.info("Shutting down WebSocket graceful shutdown scheduler");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Heartbeat message DTO
     */
    public static class HeartbeatMessage {
        private final long timestamp;

        public HeartbeatMessage(long timestamp) {
            this.timestamp = timestamp;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Shutdown notification DTO
     */
    public static class ShutdownNotification {
        private final String message;
        private final long timestamp;
        private final long gracePeriodSeconds;

        public ShutdownNotification(String message, long timestamp, long gracePeriodSeconds) {
            this.message = message;
            this.timestamp = timestamp;
            this.gracePeriodSeconds = gracePeriodSeconds;
        }

        public String getMessage() {
            return message;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public long getGracePeriodSeconds() {
            return gracePeriodSeconds;
        }
    }
}