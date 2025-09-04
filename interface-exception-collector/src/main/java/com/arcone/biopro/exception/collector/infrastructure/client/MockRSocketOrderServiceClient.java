package com.arcone.biopro.exception.collector.infrastructure.client;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.infrastructure.config.RSocketProperties;
import com.arcone.biopro.exception.collector.infrastructure.logging.RSocketLoggingInterceptor;
import com.arcone.biopro.exception.collector.infrastructure.metrics.RSocketMetrics;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * Mock RSocket client for interacting with the mock RSocket server to retrieve order data
 * during OrderRejected event processing. This client is used for development and testing
 * scenarios to simulate Partner Order Service responses without external dependencies.
 * 
 * Enhanced with comprehensive error handling, fallback mechanisms, and connection management.
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "app.rsocket.mock-server.enabled", havingValue = "true")
public class MockRSocketOrderServiceClient extends BaseSourceServiceClient {

    private final RSocketProperties rSocketProperties;
    private final RSocketConnectionManager connectionManager;

    @Autowired(required = false)
    private RSocketMetrics rSocketMetrics;

    @Autowired(required = false)
    private RSocketLoggingInterceptor loggingInterceptor;

    public MockRSocketOrderServiceClient(RestTemplate restTemplate,
                                       RSocketRequester.Builder rSocketRequesterBuilder,
                                       RSocketProperties rSocketProperties,
                                       RSocketConnectionManager connectionManager) {
        super(restTemplate, "rsocket://localhost:7000", "mock-rsocket-server");
        this.rSocketProperties = rSocketProperties;
        this.connectionManager = connectionManager;
    }

    @PostConstruct
    public void validateConfiguration() {
        log.info("Validating Mock RSocket Order Service Client configuration...");
        
        try {
            validateRSocketConfiguration();
            log.info("Mock RSocket Order Service Client configuration validation completed successfully");
        } catch (Exception e) {
            log.error("Mock RSocket Order Service Client configuration validation failed: {}", e.getMessage(), e);
            throw new IllegalStateException("Invalid Mock RSocket configuration", e);
        }
    }

    /**
     * Validates RSocket configuration parameters.
     */
    private void validateRSocketConfiguration() {
        RSocketProperties.MockServer config = rSocketProperties.getMockServer();
        
        if (!config.isEnabled()) {
            throw new IllegalStateException("Mock RSocket server is not enabled but client is being initialized");
        }
        
        if (config.getHost() == null || config.getHost().trim().isEmpty()) {
            throw new IllegalArgumentException("Mock RSocket server host cannot be null or empty");
        }
        
        if (config.getPort() <= 0 || config.getPort() > 65535) {
            throw new IllegalArgumentException("Mock RSocket server port must be between 1 and 65535, got: " + config.getPort());
        }
        
        if (config.getTimeout() == null || config.getTimeout().isNegative() || config.getTimeout().isZero()) {
            throw new IllegalArgumentException("Mock RSocket server timeout must be positive, got: " + config.getTimeout());
        }
        
        log.debug("RSocket configuration validation passed for {}:{}", config.getHost(), config.getPort());
    }

    @Override
    public boolean supports(String interfaceType) {
        return InterfaceType.ORDER.name().equals(interfaceType);
    }

    @Override
    @CircuitBreaker(name = "mock-rsocket-server", fallbackMethod = "fallbackGetPayload")
    @TimeLimiter(name = "mock-rsocket-server")
    @Retry(name = "mock-rsocket-server")
    public CompletableFuture<PayloadResponse> getOriginalPayload(InterfaceException exception) {
        log.info("Retrieving order data from mock RSocket server for externalId: {}, transactionId: {}", 
                exception.getExternalId(), exception.getTransactionId());

        return CompletableFuture.supplyAsync(() -> {
            Instant startTime = Instant.now();
            String correlationId = null;
            
            if (loggingInterceptor != null) {
                correlationId = loggingInterceptor.logRSocketCallStart(
                    exception.getExternalId(), "GET_ORDER_DATA", exception.getTransactionId());
            }
            
            try {
                // Check if connection is available or in fallback mode
                if (connectionManager.isFallbackMode()) {
                    log.warn("RSocket connection is in fallback mode, skipping order data retrieval for externalId: {}", 
                            exception.getExternalId());
                    return createFallbackResponse(exception, "RSocket connection is in fallback mode");
                }
                
                RSocketRequester requester = connectionManager.getRequester();
                if (requester == null) {
                    log.warn("RSocket requester not available for externalId: {}, attempting reconnection", 
                            exception.getExternalId());
                    
                    // Attempt to force reconnection
                    connectionManager.forceReconnect();
                    requester = connectionManager.getRequester();
                    
                    if (requester == null) {
                        throw new RuntimeException("RSocket connection not available after reconnection attempt");
                    }
                }

                String route = "orders." + exception.getExternalId();
                log.debug("Making RSocket call with route: {}", route);

                RSocketProperties.MockServer config = rSocketProperties.getMockServer();
                String orderData = requester
                    .route(route)
                    .retrieveMono(String.class)
                    .timeout(config.getTimeout())
                    .doOnError(error -> {
                        log.error("RSocket call failed for externalId: {}, error: {}", 
                                exception.getExternalId(), error.getMessage());
                        
                        if (error instanceof TimeoutException && rSocketMetrics != null) {
                            rSocketMetrics.recordTimeout("GET_ORDER_DATA");
                        }
                        
                        // Handle connection errors by triggering reconnection
                        if (isConnectionError(error)) {
                            log.warn("Connection error detected, triggering reconnection");
                            connectionManager.forceReconnect();
                        }
                    })
                    .doOnSuccess(data -> log.debug("Successfully retrieved order data for externalId: {}", 
                            exception.getExternalId()))
                    .block();

                Duration duration = Duration.between(startTime, Instant.now());
                boolean dataRetrieved = orderData != null;
                
                log.info("Successfully retrieved order data from mock RSocket server for externalId: {}", 
                        exception.getExternalId());

                // Record successful metrics and logging
                if (rSocketMetrics != null) {
                    rSocketMetrics.recordSuccessfulCall(duration, "GET_ORDER_DATA");
                }
                
                if (loggingInterceptor != null && correlationId != null) {
                    loggingInterceptor.logRSocketCallSuccess(correlationId, exception.getExternalId(), 
                        "GET_ORDER_DATA", duration, dataRetrieved);
                }

                return PayloadResponse.builder()
                        .transactionId(exception.getTransactionId())
                        .interfaceType(exception.getInterfaceType().name())
                        .payload(orderData)
                        .sourceService("mock-rsocket-server")
                        .retrieved(dataRetrieved)
                        .build();

            } catch (Exception e) {
                Duration duration = Duration.between(startTime, Instant.now());
                String errorType = e.getClass().getSimpleName();
                
                log.error("Failed to retrieve order data from mock RSocket server for externalId: {}, error: {}", 
                        exception.getExternalId(), e.getMessage(), e);

                // Record failure metrics and logging
                if (rSocketMetrics != null) {
                    rSocketMetrics.recordFailedCall(duration, "GET_ORDER_DATA", errorType);
                }
                
                if (loggingInterceptor != null && correlationId != null) {
                    loggingInterceptor.logRSocketCallFailure(correlationId, exception.getExternalId(), 
                        "GET_ORDER_DATA", duration, errorType, e.getMessage());
                }

                return PayloadResponse.builder()
                        .transactionId(exception.getTransactionId())
                        .interfaceType(exception.getInterfaceType().name())
                        .sourceService("mock-rsocket-server")
                        .retrieved(false)
                        .errorMessage(e.getMessage())
                        .build();
            }
        });
    }

    /**
     * Creates a fallback response when RSocket operations cannot be performed.
     */
    private PayloadResponse createFallbackResponse(InterfaceException exception, String reason) {
        if (rSocketMetrics != null) {
            rSocketMetrics.recordFallbackResponse("GET_ORDER_DATA");
        }
        
        return PayloadResponse.builder()
                .transactionId(exception.getTransactionId())
                .interfaceType(exception.getInterfaceType().name())
                .sourceService("mock-rsocket-server")
                .retrieved(false)
                .errorMessage("Fallback response: " + reason)
                .build();
    }

    /**
     * Checks if an error is related to connection issues.
     */
    private boolean isConnectionError(Throwable error) {
        return error instanceof java.net.ConnectException ||
               error instanceof java.nio.channels.ClosedChannelException ||
               error instanceof io.rsocket.exceptions.ConnectionErrorException ||
               (error.getMessage() != null && 
                (error.getMessage().contains("Connection refused") ||
                 error.getMessage().contains("Connection reset") ||
                 error.getMessage().contains("Connection closed")));
    }

    /**
     * Fallback method when circuit breaker is open or RSocket calls fail.
     * Provides comprehensive error handling and graceful degradation.
     */
    public CompletableFuture<PayloadResponse> fallbackGetPayload(InterfaceException exception, Exception ex) {
        log.warn("Fallback triggered for mock RSocket server payload retrieval, externalId: {}, transactionId: {}, error: {}",
                exception.getExternalId(), exception.getTransactionId(), ex.getMessage());

        // Record circuit breaker event
        if (rSocketMetrics != null) {
            rSocketMetrics.recordCircuitBreakerEvent("FALLBACK_TRIGGERED", "GET_ORDER_DATA");
        }
        
        if (loggingInterceptor != null) {
            loggingInterceptor.logCircuitBreakerEvent(exception.getExternalId(), "GET_ORDER_DATA", 
                "FALLBACK_TRIGGERED", "OPEN");
        }

        // Determine fallback strategy based on error type
        String fallbackReason = determineFallbackReason(ex);
        
        return CompletableFuture.completedFuture(
                PayloadResponse.builder()
                        .transactionId(exception.getTransactionId())
                        .interfaceType(exception.getInterfaceType().name())
                        .sourceService("mock-rsocket-server")
                        .retrieved(false)
                        .errorMessage(fallbackReason)
                        .build());
    }

    /**
     * Determines the appropriate fallback reason based on the exception type.
     */
    private String determineFallbackReason(Exception ex) {
        if (ex instanceof TimeoutException) {
            return "Mock RSocket server timeout - request took too long to complete: " + ex.getMessage();
        } else if (isConnectionError(ex)) {
            return "Mock RSocket server connection error - service may be unavailable: " + ex.getMessage();
        } else if (ex.getMessage() != null && ex.getMessage().contains("CircuitBreaker")) {
            return "Mock RSocket server circuit breaker is open - too many recent failures";
        } else {
            return "Mock RSocket server unavailable - service failure: " + ex.getMessage();
        }
    }

    @Override
    protected String buildPayloadEndpoint(InterfaceException exception) {
        // This method is used by the base class for REST calls, but we override getOriginalPayload
        // to use RSocket instead. Return a placeholder endpoint for compatibility.
        return "/orders/" + exception.getExternalId();
    }

    @Override
    protected String buildRetryEndpoint(InterfaceException exception) {
        // For retry operations, we might still use REST endpoints or implement RSocket retry routes
        return "/orders/" + exception.getExternalId() + "/retry";
    }

    /**
     * Gets connection status information for monitoring and health checks.
     */
    public RSocketConnectionManager.ConnectionStatus getConnectionStatus() {
        return connectionManager.getConnectionStatus();
    }

    /**
     * Checks if the RSocket connection is available and healthy.
     */
    public boolean isRSocketConnectionAvailable() {
        return connectionManager.isConnectionAvailable();
    }

    /**
     * Checks if the client is running in fallback mode.
     */
    public boolean isFallbackMode() {
        return connectionManager.isFallbackMode();
    }

    /**
     * Forces a reconnection attempt for testing or recovery purposes.
     */
    public void forceReconnect() {
        log.info("Forcing RSocket reconnection for mock server client");
        connectionManager.forceReconnect();
    }

    /**
     * Validates that the mock server configuration is appropriate for the current environment.
     */
    public void validateEnvironmentConfiguration() {
        String activeProfile = System.getProperty("spring.profiles.active", "default");
        
        if (isProductionEnvironment(activeProfile) && rSocketProperties.getMockServer().isEnabled()) {
            throw new IllegalStateException(
                "Mock RSocket server cannot be enabled in production environment. " +
                "Current profile: " + activeProfile);
        }
        
        log.debug("Environment configuration validation passed for profile: {}", activeProfile);
    }

    /**
     * Checks if the current environment is production.
     */
    private boolean isProductionEnvironment(String profile) {
        return profile != null && (
            "prod".equalsIgnoreCase(profile) || 
            "production".equalsIgnoreCase(profile) ||
            profile.toLowerCase().contains("prod")
        );
    }
}