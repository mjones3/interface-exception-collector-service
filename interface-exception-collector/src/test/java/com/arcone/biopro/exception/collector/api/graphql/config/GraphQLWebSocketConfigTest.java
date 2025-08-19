package com.arcone.biopro.exception.collector.api.graphql.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Test class for GraphQL WebSocket configuration.
 * Verifies that the WebSocket configuration loads correctly.
 */
@SpringBootTest(classes = { GraphQLWebSocketConfig.class })
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "graphql.websocket.heartbeat.interval=30",
        "graphql.websocket.max-connections=1000"
})
class GraphQLWebSocketConfigTest {

    @Test
    void contextLoads() {
        // Test that the WebSocket configuration can be loaded without errors
        assertDoesNotThrow(() -> {
            // Configuration should load successfully
        });
    }

    @Test
    void webSocketConfigurationIsValid() {
        // Test that WebSocket configuration beans can be created
        GraphQLWebSocketConfig config = new GraphQLWebSocketConfig();

        assertDoesNotThrow(() -> {
            GraphQLWebSocketConfig.WebSocketConnectionManager manager = config.webSocketConnectionManager();

            // Verify connection manager is created successfully
            assert manager != null;
            assert manager.getActiveConnectionCount() == 0;
        });
    }
}