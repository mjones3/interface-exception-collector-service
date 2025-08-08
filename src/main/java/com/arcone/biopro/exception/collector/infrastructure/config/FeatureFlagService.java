package com.arcone.biopro.exception.collector.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for managing feature flags and providing runtime feature toggle
 * capabilities.
 * This service allows for gradual rollout of features and runtime configuration
 * changes.
 */
@Service
public class FeatureFlagService {

    private static final Logger logger = LoggerFactory.getLogger(FeatureFlagService.class);

    private final ApplicationProperties applicationProperties;
    private final ConcurrentMap<String, Boolean> runtimeFlags = new ConcurrentHashMap<>();

    @Autowired
    public FeatureFlagService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    /**
     * Initialize feature flags on application startup
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeFeatureFlags() {
        logger.info("Initializing feature flags...");

        var features = applicationProperties.features();

        // Initialize runtime flags with configuration values
        runtimeFlags.put("enhanced-logging", features.enhancedLogging());
        runtimeFlags.put("debug-mode", features.debugMode());
        runtimeFlags.put("payload-caching", features.payloadCaching());
        runtimeFlags.put("circuit-breaker", features.circuitBreaker());
        runtimeFlags.put("retry-mechanism", features.retryMechanism());
        runtimeFlags.put("hot-reload", features.hotReload());
        runtimeFlags.put("metrics-collection", features.metricsCollection());
        runtimeFlags.put("audit-logging", features.auditLogging());

        logger.info("Feature flags initialized: {}", runtimeFlags);
    }

    /**
     * Check if enhanced logging is enabled
     */
    public boolean isEnhancedLoggingEnabled() {
        return isFeatureEnabled("enhanced-logging");
    }

    /**
     * Check if debug mode is enabled
     */
    public boolean isDebugModeEnabled() {
        return isFeatureEnabled("debug-mode");
    }

    /**
     * Check if payload caching is enabled
     */
    public boolean isPayloadCachingEnabled() {
        return isFeatureEnabled("payload-caching");
    }

    /**
     * Check if circuit breaker is enabled
     */
    public boolean isCircuitBreakerEnabled() {
        return isFeatureEnabled("circuit-breaker");
    }

    /**
     * Check if retry mechanism is enabled
     */
    public boolean isRetryMechanismEnabled() {
        return isFeatureEnabled("retry-mechanism");
    }

    /**
     * Check if hot reload is enabled
     */
    public boolean isHotReloadEnabled() {
        return isFeatureEnabled("hot-reload");
    }

    /**
     * Check if metrics collection is enabled
     */
    public boolean isMetricsCollectionEnabled() {
        return isFeatureEnabled("metrics-collection");
    }

    /**
     * Check if audit logging is enabled
     */
    public boolean isAuditLoggingEnabled() {
        return isFeatureEnabled("audit-logging");
    }

    /**
     * Check if a specific feature is enabled
     * 
     * @param featureName the name of the feature to check
     * @return true if the feature is enabled, false otherwise
     */
    public boolean isFeatureEnabled(String featureName) {
        Boolean enabled = runtimeFlags.get(featureName);
        if (enabled == null) {
            logger.warn("Unknown feature flag requested: {}", featureName);
            return false;
        }
        return enabled;
    }

    /**
     * Enable a feature at runtime
     * 
     * @param featureName the name of the feature to enable
     */
    public void enableFeature(String featureName) {
        Boolean previousValue = runtimeFlags.put(featureName, true);
        logger.info("Feature '{}' enabled (previous value: {})", featureName, previousValue);
    }

    /**
     * Disable a feature at runtime
     * 
     * @param featureName the name of the feature to disable
     */
    public void disableFeature(String featureName) {
        Boolean previousValue = runtimeFlags.put(featureName, false);
        logger.info("Feature '{}' disabled (previous value: {})", featureName, previousValue);
    }

    /**
     * Toggle a feature at runtime
     * 
     * @param featureName the name of the feature to toggle
     * @return the new state of the feature
     */
    public boolean toggleFeature(String featureName) {
        Boolean currentValue = runtimeFlags.get(featureName);
        if (currentValue == null) {
            logger.warn("Cannot toggle unknown feature: {}", featureName);
            return false;
        }

        boolean newValue = !currentValue;
        runtimeFlags.put(featureName, newValue);
        logger.info("Feature '{}' toggled from {} to {}", featureName, currentValue, newValue);
        return newValue;
    }

    /**
     * Get all current feature flag states
     * 
     * @return a map of feature names to their current states
     */
    public ConcurrentMap<String, Boolean> getAllFeatureFlags() {
        return new ConcurrentHashMap<>(runtimeFlags);
    }

    /**
     * Reset all feature flags to their configuration values
     */
    public void resetToConfigurationValues() {
        logger.info("Resetting feature flags to configuration values...");

        var features = applicationProperties.features();

        runtimeFlags.put("enhanced-logging", features.enhancedLogging());
        runtimeFlags.put("debug-mode", features.debugMode());
        runtimeFlags.put("payload-caching", features.payloadCaching());
        runtimeFlags.put("circuit-breaker", features.circuitBreaker());
        runtimeFlags.put("retry-mechanism", features.retryMechanism());
        runtimeFlags.put("hot-reload", features.hotReload());
        runtimeFlags.put("metrics-collection", features.metricsCollection());
        runtimeFlags.put("audit-logging", features.auditLogging());

        logger.info("Feature flags reset to configuration values: {}", runtimeFlags);
    }

    /**
     * Check if a feature should be enabled based on a percentage rollout
     * This can be used for gradual feature rollouts
     * 
     * @param featureName       the name of the feature
     * @param rolloutPercentage the percentage of requests that should have this
     *                          feature enabled (0-100)
     * @param identifier        a unique identifier for the request/user (used for
     *                          consistent rollout)
     * @return true if the feature should be enabled for this identifier
     */
    public boolean isFeatureEnabledForRollout(String featureName, int rolloutPercentage, String identifier) {
        if (!isFeatureEnabled(featureName)) {
            return false;
        }

        if (rolloutPercentage <= 0) {
            return false;
        }

        if (rolloutPercentage >= 100) {
            return true;
        }

        // Use hash of identifier to determine if feature should be enabled
        // This ensures consistent behavior for the same identifier
        int hash = Math.abs(identifier.hashCode());
        int bucket = hash % 100;

        boolean enabled = bucket < rolloutPercentage;

        if (logger.isDebugEnabled()) {
            logger.debug("Feature '{}' rollout check for identifier '{}': bucket={}, rollout={}%, enabled={}",
                    featureName, identifier, bucket, rolloutPercentage, enabled);
        }

        return enabled;
    }
}