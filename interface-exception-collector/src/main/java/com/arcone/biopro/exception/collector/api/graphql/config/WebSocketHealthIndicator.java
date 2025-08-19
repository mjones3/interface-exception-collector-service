package com.arcone.biopro.exception.collector.api.graphql.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Health monitoring for WebSocket connections and GraphQL subscriptions.
 * Provides basic health information about the WebSocket subsystem.
 */
@Component("webSocketHealth")
@RequiredArgsConstructor
@Slf4j
public class WebSocketHealthIndicator {

    private final GraphQLWebSocketConfig.WebSocketConnectionManager connectionManager;

    /**
     * Gets the health status of the WebSocket subsystem.
     * 
     * @return health status information
     */
    public WebSocketHealthStatus getHealthStatus() {
        try {
            int activeConnections = connectionManager.getActiveConnectionCount();

            String status = "UP";
            String message = "WebSocket broker is running";

            // Add warning if connection count is high
            if (activeConnections > 800) { // 80% of max connections (1000)
                status = "WARNING";
                message = "High number of active connections";
            }

            return new WebSocketHealthStatus(status, activeConnections, message);

        } catch (Exception e) {
            log.error("Failed to get WebSocket health status", e);
            return new WebSocketHealthStatus("DOWN", 0, "WebSocket broker is not available: " + e.getMessage());
        }
    }

    /**
     * Health status information for WebSocket connections.
     */
    public static class WebSocketHealthStatus {
        private final String status;
        private final int activeConnections;
        private final String message;

        public WebSocketHealthStatus(String status, int activeConnections, String message) {
            this.status = status;
            this.activeConnections = activeConnections;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public int getActiveConnections() {
            return activeConnections;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "WebSocketHealthStatus{" +
                    "status='" + status + '\'' +
                    ", activeConnections=" + activeConnections +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}