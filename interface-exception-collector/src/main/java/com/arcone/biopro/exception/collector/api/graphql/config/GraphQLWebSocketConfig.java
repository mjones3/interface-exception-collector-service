package com.arcone.biopro.exception.collector.api.graphql.config;

import com.arcone.biopro.exception.collector.infrastructure.config.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket configuration for GraphQL subscriptions with STOMP message broker.
 * Provides real-time subscription capabilities with JWT authentication and
 * connection management.
 */
@Configuration("graphQLSubscriptionConfig")
@EnableWebSocketMessageBroker
@ConditionalOnProperty(name = "graphql.features.subscription-enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class GraphQLWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;

    @Value("${graphql.websocket.heartbeat.interval:30}")
    private int heartbeatInterval;

    @Value("${graphql.websocket.max-connections:1000}")
    private int maxConnections;

    private final Map<String, Long> activeConnections = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(2);

    /**
     * Configures the message broker for WebSocket communication.
     * Sets up topic-based messaging and application destination prefixes.
     */
    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        log.info("Configuring WebSocket message broker");

        // Enable simple broker for topic-based messaging
        config.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[] { heartbeatInterval * 1000, heartbeatInterval * 1000 })
                .setTaskScheduler(taskScheduler());

        // Set application destination prefix for client messages
        config.setApplicationDestinationPrefixes("/app");

        // Set user destination prefix for user-specific messages
        config.setUserDestinationPrefix("/user");

        log.info("Message broker configured with heartbeat interval: {}s", heartbeatInterval);
    }

    /**
     * Registers STOMP endpoints for WebSocket connections.
     * Configures the /subscriptions endpoint with JWT authentication and SockJS
     * fallback.
     */
    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        log.info("Registering STOMP endpoints");

        registry.addEndpoint("/subscriptions")
                .setHandshakeHandler(new JwtHandshakeHandler())
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setHeartbeatTime(heartbeatInterval * 1000);

        log.info("STOMP endpoint registered at /subscriptions with SockJS support");
    }

    /**
     * Configures client inbound channel with authentication and connection
     * management.
     */
    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        registration.interceptors(new WebSocketAuthenticationInterceptor());
        log.info("Client inbound channel configured with authentication interceptor");
    }

    /**
     * Connection manager bean for tracking active WebSocket connections.
     */
    @Bean("graphqlWebSocketConnectionManager")
    public WebSocketConnectionManager webSocketConnectionManager() {
        return new WebSocketConnectionManager();
    }

    /**
     * Custom handshake handler for JWT authentication during WebSocket connection
     * establishment.
     */
    private class JwtHandshakeHandler extends DefaultHandshakeHandler {

        @Override
        protected Principal determineUser(
                @NonNull org.springframework.http.server.ServerHttpRequest request,
                @NonNull org.springframework.web.socket.WebSocketHandler wsHandler,
                @NonNull Map<String, Object> attributes) {

            // Extract JWT token from query parameters or headers
            String token = extractJwtToken(request);
            if (token != null) {
                try {
                    // Validate JWT using the actual JWT service
                    Claims claims = jwtService.validateToken(token);

                    if (!jwtService.isTokenExpired(claims)) {
                        String username = jwtService.extractUsername(claims);
                        Collection<GrantedAuthority> authorities = jwtService.extractAuthorities(claims);

                        if (username != null) {
                            log.info("WebSocket handshake successful for user: {} with authorities: {}",
                                    username, authorities);
                            return new JwtPrincipal(username, authorities);
                        }
                    } else {
                        log.warn("JWT token expired during WebSocket handshake");
                    }
                } catch (JwtService.InvalidJwtTokenException e) {
                    log.warn("JWT validation failed during WebSocket handshake: {} - {}",
                            e.getErrorType(), e.getMessage());
                } catch (Exception e) {
                    log.warn("Unexpected error during WebSocket JWT validation: {}", e.getMessage());
                }
            } else {
                log.debug("No JWT token found in WebSocket handshake request");
            }

            return null; // Reject connection if no valid JWT
        }

        private String extractJwtToken(org.springframework.http.server.ServerHttpRequest request) {
            // Try to get token from Authorization header
            List<String> authHeaders = request.getHeaders().get("Authorization");
            if (authHeaders != null && !authHeaders.isEmpty()) {
                String authHeader = authHeaders.get(0);
                if (authHeader.startsWith("Bearer ")) {
                    return authHeader.substring(7);
                }
            }

            // Try to get token from query parameter (for WebSocket clients that can't set
            // headers)
            String query = request.getURI().getQuery();
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
     * Custom principal for JWT-authenticated WebSocket connections.
     */
    private static class JwtPrincipal implements Principal {
        private final String name;
        private final Collection<GrantedAuthority> authorities;

        public JwtPrincipal(String name, Collection<GrantedAuthority> authorities) {
            this.name = name;
            this.authorities = authorities;
        }

        @Override
        public String getName() {
            return name;
        }

        public Collection<GrantedAuthority> getAuthorities() {
            return authorities;
        }
    }

    /**
     * WebSocket authentication interceptor for validating JWT tokens in STOMP
     * messages.
     */
    private class WebSocketAuthenticationInterceptor implements ChannelInterceptor {

        @Override
        public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                handleConnect(accessor);
            } else if (accessor != null && StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                handleDisconnect(accessor);
            }

            return message;
        }

        private void handleConnect(StompHeaderAccessor accessor) {
            String sessionId = accessor.getSessionId();
            Principal user = accessor.getUser();

            if (user != null) {
                // Check connection limits
                if (activeConnections.size() >= maxConnections) {
                    log.warn("Maximum WebSocket connections reached: {}", maxConnections);
                    throw new RuntimeException("Maximum connections exceeded");
                }

                // Track connection
                activeConnections.put(sessionId, System.currentTimeMillis());

                // Set up authentication context
                if (user instanceof JwtPrincipal jwtPrincipal) {
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            jwtPrincipal.getName(), null, jwtPrincipal.getAuthorities());
                    accessor.setUser(auth);
                }

                log.info("WebSocket connection established for user: {}, session: {}",
                        user.getName(), sessionId);
            } else {
                log.warn("WebSocket connection rejected - no valid authentication");
                throw new RuntimeException("Authentication required");
            }
        }

        private void handleDisconnect(StompHeaderAccessor accessor) {
            String sessionId = accessor.getSessionId();
            Principal user = accessor.getUser();

            activeConnections.remove(sessionId);

            if (user != null) {
                log.info("WebSocket connection closed for user: {}, session: {}",
                        user.getName(), sessionId);
            }
        }
    }

    /**
     * Connection manager for monitoring and managing WebSocket connections.
     */
    public class WebSocketConnectionManager {

        public WebSocketConnectionManager() {
            // Start heartbeat monitoring
            startHeartbeatMonitoring();
        }

        /**
         * Gets the number of active WebSocket connections.
         */
        public int getActiveConnectionCount() {
            return activeConnections.size();
        }

        /**
         * Gets active connection details.
         */
        public Map<String, Long> getActiveConnections() {
            return new ConcurrentHashMap<>(activeConnections);
        }

        /**
         * Starts heartbeat monitoring to detect stale connections.
         */
        private void startHeartbeatMonitoring() {
            if (heartbeatInterval > 0) {
                heartbeatExecutor.scheduleAtFixedRate(() -> {
                    long now = System.currentTimeMillis();
                    long staleThreshold = heartbeatInterval * 3 * 1000L; // 3x heartbeat interval

                    activeConnections.entrySet().removeIf(entry -> {
                        boolean isStale = (now - entry.getValue()) > staleThreshold;
                        if (isStale) {
                            log.debug("Removing stale WebSocket connection: {}", entry.getKey());
                        }
                        return isStale;
                    });

                    log.debug("Active WebSocket connections: {}", activeConnections.size());
                }, heartbeatInterval, heartbeatInterval, TimeUnit.SECONDS);
            } else {
                log.debug("Heartbeat monitoring disabled (interval: {})", heartbeatInterval);
            }
        }

        /**
         * Gracefully shuts down the connection manager.
         */
        public void shutdown() {
            heartbeatExecutor.shutdown();
            try {
                if (!heartbeatExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    heartbeatExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                heartbeatExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Task scheduler bean for WebSocket heartbeat functionality.
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("websocket-heartbeat-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(10);
        return scheduler;
    }
}