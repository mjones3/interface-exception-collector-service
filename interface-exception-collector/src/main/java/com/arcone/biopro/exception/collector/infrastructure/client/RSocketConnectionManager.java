package com.arcone.biopro.exception.collector.infrastructure.client;

import com.arcone.biopro.exception.collector.infrastructure.config.RSocketProperties;
import com.arcone.biopro.exception.collector.infrastructure.logging.RSocketLoggingInterceptor;
import com.arcone.biopro.exception.collector.infrastructure.metrics.RSocketMetrics;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpClient;
import reactor.util.retry.Retry;

import jakarta.annotation.PreDestroy;
import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages RSocket connections with comprehensive error handling, fallback mechanisms,
 * and connection lifecycle management. Provides resilient connection management
 * for mock server integration with automatic reconnection and graceful degradation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RSocketConnectionManager {

    private final RSocketProperties rSocketProperties;
    private final RSocketLoggingInterceptor loggingInterceptor;
    private final RSocketMetrics rSocketMetrics;
    private final RSocketRequester.Builder rSocketRequesterBuilder;

    private final AtomicReference<RSocketRequester> activeRequester = new AtomicReference<>();
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
    private final AtomicBoolean fallbackMode = new AtomicBoolean(false);

    /**
     * Initializes RSocket connection after application startup.
     * Implements fallback mechanisms for when mock server is unavailable.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeConnection() {
        if (!rSocketProperties.getMockServer().isEnabled()) {
            log.info("Mock RSocket server is disabled, skipping connection initialization");
            return;
        }

        log.info("Initializing RSocket connection to mock server at {}:{}",
                rSocketProperties.getMockServer().getHost(),
                rSocketProperties.getMockServer().getPort());

        try {
            establishConnection();
        } catch (Exception e) {
            log.error("Failed to establish initial RSocket connection: {}", e.getMessage(), e);
            enableFallbackMode("Initial connection failed: " + e.getMessage());
        }
    }

    /**
     * Establishes RSocket connection with retry logic and comprehensive error handling.
     */
    @Retryable(
        value = {ConnectException.class, RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void establishConnection() {
        if (isShuttingDown.get()) {
            log.debug("Application is shutting down, skipping connection establishment");
            return;
        }

        try {
            RSocketProperties.MockServer mockServerConfig = rSocketProperties.getMockServer();
            
            // Validate configuration before attempting connection
            validateConnectionConfiguration(mockServerConfig);

            // Create TCP client with timeouts and keep-alive settings
            TcpClient tcpClient = TcpClient.create()
                .host(mockServerConfig.getHost())
                .port(mockServerConfig.getPort())
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 
                       (int) mockServerConfig.getConnectionTimeout().toMillis())
                .option(io.netty.channel.ChannelOption.SO_KEEPALIVE, true)
                .doOnConnected(connection -> {
                    log.info("TCP connection established to {}:{}", 
                            mockServerConfig.getHost(), mockServerConfig.getPort());
                    rSocketMetrics.recordConnectionSuccess();
                })
                .doOnDisconnected(connection -> {
                    log.warn("TCP connection disconnected from {}:{}", 
                            mockServerConfig.getHost(), mockServerConfig.getPort());
                    isConnected.set(false);
                    rSocketMetrics.recordConnectionFailure();
                    
                    // Attempt reconnection if not shutting down
                    if (!isShuttingDown.get()) {
                        scheduleReconnection();
                    }
                });

            // Create RSocket connector with keep-alive settings
            RSocketConnector connector = RSocketConnector.create()
                .keepAlive(mockServerConfig.getKeepAliveInterval(), 
                          mockServerConfig.getKeepAliveMaxLifetime())
                .reconnect(Retry.backoff(3, Duration.ofSeconds(1))
                    .maxBackoff(Duration.ofSeconds(10))
                    .doBeforeRetry(retrySignal -> {
                        log.warn("Retrying RSocket connection, attempt: {}", 
                                retrySignal.totalRetries() + 1);
                        rSocketMetrics.recordConnectionRetry();
                    }));

            // Create RSocket requester
            RSocketRequester requester = rSocketRequesterBuilder
                .rsocketConnector(connector)
                .transport(TcpClientTransport.create(tcpClient));

            // Test the connection
            testConnection(requester, mockServerConfig.getTimeout());

            // Store the active requester
            RSocketRequester previousRequester = activeRequester.getAndSet(requester);
            if (previousRequester != null && !previousRequester.isDisposed()) {
                previousRequester.dispose();
            }

            isConnected.set(true);
            fallbackMode.set(false);
            
            log.info("RSocket connection established successfully to {}:{}", 
                    mockServerConfig.getHost(), mockServerConfig.getPort());
            
            loggingInterceptor.logConnectionEvent("CONNECTED", 
                    mockServerConfig.getHost() + ":" + mockServerConfig.getPort(), 
                    null, true);

        } catch (Exception e) {
            log.error("Failed to establish RSocket connection: {}", e.getMessage(), e);
            rSocketMetrics.recordConnectionFailure();
            
            loggingInterceptor.logConnectionEvent("CONNECTION_FAILED", 
                    rSocketProperties.getMockServer().getHost() + ":" + rSocketProperties.getMockServer().getPort(), 
                    e.getMessage(), false);
            
            enableFallbackMode("Connection establishment failed: " + e.getMessage());
            throw new RuntimeException("Failed to establish RSocket connection", e);
        }
    }

    /**
     * Tests the RSocket connection by performing a simple health check.
     */
    private void testConnection(RSocketRequester requester, Duration timeout) {
        try {
            // Perform a simple connection test
            requester.route("health")
                .retrieveMono(String.class)
                .timeout(timeout)
                .onErrorReturn("UNKNOWN")  // Fallback for health check
                .block(timeout);
            
            log.debug("RSocket connection test successful");
            
        } catch (Exception e) {
            log.warn("RSocket connection test failed, but connection may still be usable: {}", e.getMessage());
            // Don't fail the connection establishment for health check failures
            // The mock server might not have a health endpoint
        }
    }

    /**
     * Validates connection configuration before attempting to connect.
     */
    private void validateConnectionConfiguration(RSocketProperties.MockServer config) {
        if (config.getHost() == null || config.getHost().trim().isEmpty()) {
            throw new IllegalArgumentException("Mock server host cannot be null or empty");
        }
        
        if (config.getPort() <= 0 || config.getPort() > 65535) {
            throw new IllegalArgumentException("Mock server port must be between 1 and 65535, got: " + config.getPort());
        }
        
        if (config.getTimeout() == null || config.getTimeout().isNegative() || config.getTimeout().isZero()) {
            throw new IllegalArgumentException("Mock server timeout must be positive, got: " + config.getTimeout());
        }
        
        if (config.getConnectionTimeout() == null || config.getConnectionTimeout().isNegative() || config.getConnectionTimeout().isZero()) {
            throw new IllegalArgumentException("Mock server connection timeout must be positive, got: " + config.getConnectionTimeout());
        }
        
        log.debug("Connection configuration validation passed");
    }

    /**
     * Schedules reconnection attempt with exponential backoff.
     */
    private void scheduleReconnection() {
        if (isShuttingDown.get() || fallbackMode.get()) {
            return;
        }

        log.info("Scheduling RSocket reconnection attempt in 5 seconds...");
        
        Mono.delay(Duration.ofSeconds(5))
            .doOnNext(tick -> {
                if (!isShuttingDown.get() && !isConnected.get()) {
                    log.info("Attempting RSocket reconnection...");
                    try {
                        establishConnection();
                    } catch (Exception e) {
                        log.warn("Reconnection attempt failed: {}", e.getMessage());
                        // Will be retried by the scheduler
                    }
                }
            })
            .subscribe();
    }

    /**
     * Enables fallback mode when RSocket connection is unavailable.
     */
    private void enableFallbackMode(String reason) {
        fallbackMode.set(true);
        isConnected.set(false);
        
        log.warn("Enabling fallback mode for RSocket operations. Reason: {}", reason);
        log.info("Order data retrieval will be skipped, but exception processing will continue");
        
        rSocketMetrics.recordFallbackModeEnabled();
        
        loggingInterceptor.logConnectionEvent("FALLBACK_ENABLED", 
                rSocketProperties.getMockServer().getHost() + ":" + rSocketProperties.getMockServer().getPort(), 
                reason, false);
    }

    /**
     * Gets the active RSocket requester with connection validation.
     *
     * @return the active requester or null if not connected
     */
    public RSocketRequester getRequester() {
        RSocketRequester requester = activeRequester.get();
        
        if (requester == null || requester.isDisposed()) {
            log.warn("RSocket requester is not available");
            return null;
        }
        
        if (!isConnected.get()) {
            log.warn("RSocket connection is not established");
            return null;
        }
        
        return requester;
    }

    /**
     * Checks if RSocket connection is available and healthy.
     *
     * @return true if connection is available
     */
    public boolean isConnectionAvailable() {
        return isConnected.get() && !fallbackMode.get() && 
               activeRequester.get() != null && !activeRequester.get().isDisposed();
    }

    /**
     * Checks if the system is running in fallback mode.
     *
     * @return true if in fallback mode
     */
    public boolean isFallbackMode() {
        return fallbackMode.get();
    }

    /**
     * Forces reconnection attempt.
     */
    public void forceReconnect() {
        log.info("Forcing RSocket reconnection...");
        
        // Dispose current connection
        RSocketRequester currentRequester = activeRequester.get();
        if (currentRequester != null && !currentRequester.isDisposed()) {
            currentRequester.dispose();
        }
        
        isConnected.set(false);
        fallbackMode.set(false);
        
        // Attempt new connection
        try {
            establishConnection();
        } catch (Exception e) {
            log.error("Forced reconnection failed: {}", e.getMessage(), e);
            enableFallbackMode("Forced reconnection failed: " + e.getMessage());
        }
    }

    /**
     * Gets connection status information.
     *
     * @return connection status details
     */
    public ConnectionStatus getConnectionStatus() {
        RSocketRequester requester = activeRequester.get();
        
        return ConnectionStatus.builder()
                .connected(isConnected.get())
                .fallbackMode(fallbackMode.get())
                .requesterAvailable(requester != null && !requester.isDisposed())
                .host(rSocketProperties.getMockServer().getHost())
                .port(rSocketProperties.getMockServer().getPort())
                .build();
    }

    /**
     * Gracefully shuts down RSocket connections.
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down RSocket connection manager...");
        
        isShuttingDown.set(true);
        isConnected.set(false);
        
        RSocketRequester requester = activeRequester.get();
        if (requester != null && !requester.isDisposed()) {
            try {
                requester.dispose();
                log.info("RSocket requester disposed successfully");
            } catch (Exception e) {
                log.warn("Error disposing RSocket requester: {}", e.getMessage());
            }
        }
        
        loggingInterceptor.logConnectionEvent("SHUTDOWN", 
                rSocketProperties.getMockServer().getHost() + ":" + rSocketProperties.getMockServer().getPort(), 
                null, true);
        
        log.info("RSocket connection manager shutdown completed");
    }

    /**
     * Connection status information.
     */
    public static class ConnectionStatus {
        private final boolean connected;
        private final boolean fallbackMode;
        private final boolean requesterAvailable;
        private final String host;
        private final int port;

        private ConnectionStatus(boolean connected, boolean fallbackMode, boolean requesterAvailable, 
                               String host, int port) {
            this.connected = connected;
            this.fallbackMode = fallbackMode;
            this.requesterAvailable = requesterAvailable;
            this.host = host;
            this.port = port;
        }

        public static ConnectionStatusBuilder builder() {
            return new ConnectionStatusBuilder();
        }

        public boolean isConnected() { return connected; }
        public boolean isFallbackMode() { return fallbackMode; }
        public boolean isRequesterAvailable() { return requesterAvailable; }
        public String getHost() { return host; }
        public int getPort() { return port; }

        @Override
        public String toString() {
            return String.format("ConnectionStatus{connected=%s, fallbackMode=%s, requesterAvailable=%s, endpoint=%s:%d}",
                    connected, fallbackMode, requesterAvailable, host, port);
        }

        public static class ConnectionStatusBuilder {
            private boolean connected;
            private boolean fallbackMode;
            private boolean requesterAvailable;
            private String host;
            private int port;

            public ConnectionStatusBuilder connected(boolean connected) {
                this.connected = connected;
                return this;
            }

            public ConnectionStatusBuilder fallbackMode(boolean fallbackMode) {
                this.fallbackMode = fallbackMode;
                return this;
            }

            public ConnectionStatusBuilder requesterAvailable(boolean requesterAvailable) {
                this.requesterAvailable = requesterAvailable;
                return this;
            }

            public ConnectionStatusBuilder host(String host) {
                this.host = host;
                return this;
            }

            public ConnectionStatusBuilder port(int port) {
                this.port = port;
                return this;
            }

            public ConnectionStatus build() {
                return new ConnectionStatus(connected, fallbackMode, requesterAvailable, host, port);
            }
        }
    }
}