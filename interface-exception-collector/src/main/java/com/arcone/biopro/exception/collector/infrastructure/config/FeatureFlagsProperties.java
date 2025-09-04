package com.arcone.biopro.exception.collector.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for feature flags.
 * Centralizes all feature flag management for the application.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.features")
public class FeatureFlagsProperties {

    private boolean enhancedLogging = true;
    private boolean debugMode = false;
    private boolean payloadCaching = true;
    private boolean circuitBreaker = true;
    private boolean retryMechanism = true;
    private boolean hotReload = false;
    private boolean metricsCollection = true;
    private boolean auditLogging = true;

    /**
     * Checks if mock RSocket server should be used based on environment and feature flags.
     * This method can be used for additional business logic around feature flag decisions.
     *
     * @param mockServerEnabled the mock server enabled property
     * @param environment the current environment
     * @return true if mock server should be used
     */
    public boolean shouldUseMockRSocketServer(boolean mockServerEnabled, String environment) {
        // In production, never use mock server regardless of flag
        if ("prod".equalsIgnoreCase(environment) || "production".equalsIgnoreCase(environment)) {
            return false;
        }
        
        // In development and test environments, respect the flag
        return mockServerEnabled;
    }

    /**
     * Checks if debug logging should be enabled based on environment and feature flags.
     *
     * @param environment the current environment
     * @return true if debug logging should be enabled
     */
    public boolean shouldEnableDebugLogging(String environment) {
        return debugMode || "dev".equalsIgnoreCase(environment);
    }

    /**
     * Checks if circuit breaker should be enabled for the given service.
     *
     * @param serviceName the service name
     * @return true if circuit breaker should be enabled
     */
    public boolean shouldEnableCircuitBreaker(String serviceName) {
        return circuitBreaker;
    }

    /**
     * Checks if retry mechanism should be enabled for the given service.
     *
     * @param serviceName the service name
     * @return true if retry mechanism should be enabled
     */
    public boolean shouldEnableRetryMechanism(String serviceName) {
        return retryMechanism;
    }
}