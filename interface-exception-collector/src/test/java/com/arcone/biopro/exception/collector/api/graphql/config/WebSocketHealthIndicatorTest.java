package com.arcone.biopro.exception.collector.api.graphql.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for WebSocket health indicator functionality.
 * Tests that WebSocket health monitoring is working correctly.
 */
class WebSocketHealthIndicatorTest {

    private WebSocketHealthIndicator healthIndicator;
    private GraphQLWebSocketConfig.WebSocketConnectionManager connectionManager;

    @BeforeEach
    void setUp() {
        // Create a test JWT service
        TestJwtService jwtService = new TestJwtService();

        // Create WebSocket configuration
        GraphQLWebSocketConfig webSocketConfig = new GraphQLWebSocketConfig(jwtService);

        // Get connection manager
        connectionManager = webSocketConfig.webSocketConnectionManager();

        // Create health indicator
        healthIndicator = new WebSocketHealthIndicator(connectionManager);
    }

    @Test
    void testHealthStatusWithNoConnections() {
        // Test health status when no connections are active
        WebSocketHealthIndicator.WebSocketHealthStatus status = healthIndicator.getHealthStatus();

        assertNotNull(status);
        assertEquals("UP", status.getStatus());
        assertEquals(0, status.getActiveConnections());
        assertEquals("WebSocket broker is running", status.getMessage());
    }

    @Test
    void testHealthStatusToString() {
        // Test the toString method of health status
        WebSocketHealthIndicator.WebSocketHealthStatus status = healthIndicator.getHealthStatus();

        String statusString = status.toString();
        assertNotNull(statusString);
        assertTrue(statusString.contains("WebSocketHealthStatus"));
        assertTrue(statusString.contains("status='UP'"));
        assertTrue(statusString.contains("activeConnections=0"));
    }

    @Test
    void testConnectionManagerIntegration() {
        // Test that the health indicator properly integrates with connection manager
        assertNotNull(connectionManager);
        assertEquals(0, connectionManager.getActiveConnectionCount());

        // Get health status
        WebSocketHealthIndicator.WebSocketHealthStatus status = healthIndicator.getHealthStatus();
        assertEquals(connectionManager.getActiveConnectionCount(), status.getActiveConnections());
    }

    @Test
    void testHealthIndicatorCreation() {
        // Test that health indicator can be created successfully
        assertNotNull(healthIndicator);

        // Test that it can provide health status
        WebSocketHealthIndicator.WebSocketHealthStatus status = healthIndicator.getHealthStatus();
        assertNotNull(status);
        assertNotNull(status.getStatus());
        assertNotNull(status.getMessage());
        assertTrue(status.getActiveConnections() >= 0);
    }

    // Test JWT service implementation
    private static class TestJwtService
            extends com.arcone.biopro.exception.collector.infrastructure.config.security.JwtService {
        public TestJwtService() {
            super("test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm-testing");
        }
    }
}