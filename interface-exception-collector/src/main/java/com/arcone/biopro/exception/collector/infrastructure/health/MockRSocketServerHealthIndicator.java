package com.arcone.biopro.exception.collector.infrastructure.health;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.infrastructure.client.MockRSocketOrderServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Health check component for Mock RSocket Server connectivity.
 * Provides health status information for monitoring and container orchestration.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rsocket.mock-server.enabled", havingValue = "true")
public class MockRSocketServerHealthIndicator {

    private final MockRSocketOrderServiceClient mockRSocketClient;
    
    private static final String HEALTH_CHECK_EXTERNAL_ID = "HEALTH-CHECK";
    private static final Duration HEALTH_CHECK_TIMEOUT = Duration.ofSeconds(2);

    /**
     * Performs a health check on the mock RSocket server.
     * @return Map containing health status and details
     */
    public Map<String, Object> checkHealth() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        try {
            // Attempt a simple health check call to verify connectivity
            boolean isHealthy = performHealthCheck();
            
            if (isHealthy) {
                healthStatus.put("status", "UP");
                healthStatus.put("mock-server", "Available");
                healthStatus.put("service", "mock-rsocket-server");
                healthStatus.put("timeout", HEALTH_CHECK_TIMEOUT.toString());
            } else {
                healthStatus.put("status", "DOWN");
                healthStatus.put("mock-server", "Unavailable - No response");
                healthStatus.put("service", "mock-rsocket-server");
            }
            
        } catch (Exception e) {
            log.warn("Mock RSocket server health check failed: {}", e.getMessage());
            healthStatus.put("status", "DOWN");
            healthStatus.put("mock-server", "Unavailable");
            healthStatus.put("service", "mock-rsocket-server");
            healthStatus.put("error", e.getMessage());
            healthStatus.put("error-type", e.getClass().getSimpleName());
        }
        
        return healthStatus;
    }

    /**
     * Simple boolean health check.
     * @return true if the mock server is healthy, false otherwise
     */
    public boolean isHealthy() {
        try {
            return performHealthCheck();
        } catch (Exception e) {
            log.debug("Health check failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean performHealthCheck() {
        try {
            // Create a minimal InterfaceException for health check
            var healthCheckException = createHealthCheckException();
            
            // Attempt to get payload with timeout
            CompletableFuture<Boolean> healthCheckFuture = mockRSocketClient
                .getOriginalPayload(healthCheckException)
                .thenApply(response -> response != null)
                .exceptionally(throwable -> {
                    log.debug("Health check call failed: {}", throwable.getMessage());
                    return false;
                });
            
            return healthCheckFuture.get(HEALTH_CHECK_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            
        } catch (Exception e) {
            log.debug("Health check execution failed: {}", e.getMessage());
            return false;
        }
    }

    private InterfaceException createHealthCheckException() {
        return InterfaceException.builder()
            .externalId(HEALTH_CHECK_EXTERNAL_ID)
            .transactionId("health-check-" + System.currentTimeMillis())
            .interfaceType(InterfaceType.ORDER)
            .build();
    }
}