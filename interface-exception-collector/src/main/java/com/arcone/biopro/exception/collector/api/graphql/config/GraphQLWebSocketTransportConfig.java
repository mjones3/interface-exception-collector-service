package com.arcone.biopro.exception.collector.api.graphql.config;

import com.arcone.biopro.exception.collector.infrastructure.config.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration for GraphQL WebSocket transport.
 * Enables GraphQL subscriptions over WebSocket using the graphql-ws protocol.
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
@Slf4j
public class GraphQLWebSocketTransportConfig implements WebSocketConfigurer {

    private final JwtService jwtService;

    
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        log.info("🔌 Registering GraphQL WebSocket handler at /graphql");
        
        registry.addHandler(new GraphQLWebSocketHandler(), "/graphql")
                .setHandshakeHandler(new GraphQLHandshakeHandler())
                .setAllowedOrigins("*"); // Configure as needed for security
        
        log.info("✅ GraphQL WebSocket transport configured at ws://localhost:8080/graphql");
    }

    /**
     * Custom WebSocket handler for GraphQL subscriptions using graphql-ws protocol
     */
    private class GraphQLWebSocketHandler extends TextWebSocketHandler {

        @Override
        public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
            String sessionId = session.getId();
            Principal user = session.getPrincipal();
            
            activeSessions.put(sessionId, session);
            
            log.info("🔗 GraphQL WebSocket connection established - Session: {}, User: {}", 
                    sessionId, user != null ? user.getName() : "anonymous");
            
            super.afterConnectionEstablished(session);
        }

        @Override
        protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
            String sessionId = session.getId();
            String payload = message.getPayload();
            
            log.debug("📨 Received GraphQL WebSocket message - Session: {}, Payload: {}", sessionId, payload);
            
            try {
                // Here we would integrate with Spring GraphQL's WebSocket message handler
                // For now, let's create a basic response to test connectivity
                
                // Parse the message to understand the protocol
                if (payload.contains("\"type\":\"connection_init\"")) {
                    // Send connection_ack
                    String ackMessage = "{\"type\":\"connection_ack\"}";
                    session.sendMessage(new TextMessage(ackMessage));
                    log.info("✅ Sent connection_ack to session: {}", sessionId);
                    
                } else if (payload.contains("\"type\":\"start\"")) {
                    // Handle subscription start
                    log.info("📡 Subscription started for session: {}", sessionId);
                    
                    // Send a test message to verify the connection works
                    String testData = "{\n" +
                        "  \"id\": \"test-subscription\",\n" +
                        "  \"type\": \"next\",\n" +
                        "  \"payload\": {\n" +
                        "    \"data\": {\n" +
                        "      \"exceptionUpdated\": {\n" +
                        "        \"eventType\": \"CREATED\",\n" +
                        "        \"exception\": {\n" +
                        "          \"transactionId\": \"test-connection-" + System.currentTimeMillis() + "\",\n" +
                        "          \"status\": \"NEW\",\n" +
                        "          \"severity\": \"MEDIUM\",\n" +
                        "          \"exceptionReason\": \"WebSocket connection test\"\n" +
                        "        },\n" +
                        "        \"timestamp\": \"" + java.time.OffsetDateTime.now() + "\",\n" +
                        "        \"triggeredBy\": \"system\"\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}";
                    
                    // Send test data after a short delay
                    new Thread(() -> {
                        try {
                            Thread.sleep(1000);
                            if (session.isOpen()) {
                                session.sendMessage(new TextMessage(testData));
                                log.info("🔔 Sent test subscription data to session: {}", sessionId);
                            }
                        } catch (Exception e) {
                            log.error("Error sending test data to session: {}", sessionId, e);
                        }
                    }).start();
                    
                } else if (payload.contains("\"type\":\"stop\"")) {
                    log.info("🛑 Subscription stopped for session: {}", sessionId);
                    
                } else {
                    log.debug("📨 Unknown message type for session: {}", sessionId);
                }
                
            } catch (Exception e) {
                log.error("❌ Error handling GraphQL WebSocket message for session: {}", sessionId, e);
                
                String errorMessage = "{\n" +
                    "  \"type\": \"error\",\n" +
                    "  \"payload\": {\n" +
                    "    \"message\": \"Internal server error\"\n" +
                    "  }\n" +
                    "}";
                
                try {
                    session.sendMessage(new TextMessage(errorMessage));
                } catch (IOException ioException) {
                    log.error("Failed to send error message to session: {}", sessionId, ioException);
                }
            }
        }

        @Override
        public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
            String sessionId = session.getId();
            activeSessions.remove(sessionId);
            
            log.info("🔌 GraphQL WebSocket connection closed - Session: {}, Status: {}", sessionId, status);
            super.afterConnectionClosed(session, status);
        }

        @Override
        public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) throws Exception {
            String sessionId = session.getId();
            log.error("❌ GraphQL WebSocket transport error - Session: {}", sessionId, exception);
            
            activeSessions.remove(sessionId);
            super.handleTransportError(session, exception);
        }
    }

    /**
     * Custom handshake handler for JWT authentication
     */
    private class GraphQLHandshakeHandler extends DefaultHandshakeHandler {

        @Override
        protected Principal determineUser(
                @NonNull org.springframework.http.server.ServerHttpRequest request,
                @NonNull org.springframework.web.socket.WebSocketHandler wsHandler,
                @NonNull Map<String, Object> attributes) {

            String token = extractJwtToken(request);
            if (token != null) {
                try {
                    var claims = jwtService.validateToken(token);
                    
                    if (!jwtService.isTokenExpired(claims)) {
                        String username = jwtService.extractUsername(claims);
                        Collection<GrantedAuthority> authorities = jwtService.extractAuthorities(claims);
                        
                        if (username != null) {
                            log.info("🔐 GraphQL WebSocket authentication successful for user: {}", username);
                            return new UsernamePasswordAuthenticationToken(username, null, authorities);
                        }
                    }
                } catch (Exception e) {
                    log.warn("🔐 GraphQL WebSocket authentication failed: {}", e.getMessage());
                }
            }
            
            // Allow anonymous connections for now (can be restricted later)
            log.debug("🔐 GraphQL WebSocket allowing anonymous connection");
            return new UsernamePasswordAuthenticationToken("anonymous", null, List.of());
        }

        private String extractJwtToken(org.springframework.http.server.ServerHttpRequest request) {
            // Try Authorization header
            List<String> authHeaders = request.getHeaders().get("Authorization");
            if (authHeaders != null && !authHeaders.isEmpty()) {
                String authHeader = authHeaders.get(0);
                if (authHeader.startsWith("Bearer ")) {
                    return authHeader.substring(7);
                }
            }

            // Try query parameter
            URI uri = request.getURI();
            String query = uri.getQuery();
            if (query != null && query.contains("token=")) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("token=")) {
                        return param.substring(6);
                    }
                }
            }

            return null;
        }
    }

    /**
     * Bean to expose active WebSocket sessions for monitoring
     */
    @Bean
    public GraphQLWebSocketSessionManager graphQLWebSocketSessionManager() {
        return new GraphQLWebSocketSessionManager();
    }

    /**
     * Manager for GraphQL WebSocket sessions
     */
    public class GraphQLWebSocketSessionManager {
        
        public int getActiveSessionCount() {
            return activeSessions.size();
        }
        
        public Map<String, WebSocketSession> getActiveSessions() {
            return new ConcurrentHashMap<>(activeSessions);
        }
        
        /**
         * Broadcast a message to all active sessions
         */
        public void broadcastToAll(String message) {
            log.info("📡 Broadcasting message to {} active sessions", activeSessions.size());
            
            activeSessions.values().forEach(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(message));
                    }
                } catch (Exception e) {
                    log.error("Failed to broadcast to session: {}", session.getId(), e);
                }
            });
        }
    }
}