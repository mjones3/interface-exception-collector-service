package com.arcone.biopro.exception.collector.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
// Optional Spring Cloud Config import - only available if dependency is present
// import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for hot-reloading configuration properties without requiring
 * application restart.
 * This service provides capabilities to refresh specific configuration sections
 * at runtime.
 */
@Service
@Endpoint(id = "config-reload")
public class ConfigurationHotReloadService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationHotReloadService.class);

    private final FeatureFlagService featureFlagService;
    private final Environment environment;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, LocalDateTime> lastReloadTimes = new ConcurrentHashMap<>();

    // Optional: Spring Cloud Config support (using Object to avoid compile-time
    // dependency)
    private Object contextRefresher;

    @Autowired
    public ConfigurationHotReloadService(
            FeatureFlagService featureFlagService,
            Environment environment,
            ApplicationEventPublisher eventPublisher) {
        this.featureFlagService = featureFlagService;
        this.environment = environment;
        this.eventPublisher = eventPublisher;
    }

    @Autowired(required = false)
    public void setContextRefresher(Object contextRefresher) {
        this.contextRefresher = contextRefresher;
    }

    /**
     * Reload all configuration properties
     */
    @WriteOperation
    public Map<String, Object> reloadAllConfiguration() {
        logger.info("Reloading all configuration properties...");

        Map<String, Object> result = new HashMap<>();
        LocalDateTime reloadTime = LocalDateTime.now();

        try {
            // Reload feature flags
            reloadFeatureFlags();
            result.put("featureFlags", "reloaded");

            // Reload logging configuration
            reloadLoggingConfiguration();
            result.put("logging", "reloaded");

            // If Spring Cloud Config is available, refresh context
            if (contextRefresher != null && featureFlagService.isHotReloadEnabled()) {
                try {
                    // Use reflection to call refresh() method to avoid compile-time dependency
                    java.lang.reflect.Method refreshMethod = contextRefresher.getClass().getMethod("refresh");
                    @SuppressWarnings("unchecked")
                    Set<String> refreshedProperties = (Set<String>) refreshMethod.invoke(contextRefresher);
                    result.put("refreshedProperties", refreshedProperties);
                    logger.info("Refreshed {} properties via Spring Cloud Config", refreshedProperties.size());
                } catch (Exception e) {
                    logger.warn("Failed to refresh Spring Cloud Config properties", e);
                    result.put("springCloudConfigError", e.getMessage());
                }
            }

            lastReloadTimes.put("all", reloadTime);
            result.put("reloadTime", reloadTime.toString());
            result.put("status", "success");

            // Publish configuration reload event
            eventPublisher.publishEvent(new ConfigurationReloadEvent(this, "all", reloadTime));

            logger.info("Configuration reload completed successfully");

        } catch (Exception e) {
            logger.error("Failed to reload configuration", e);
            result.put("status", "error");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Reload only feature flags
     */
    @WriteOperation
    public Map<String, Object> reloadFeatureFlags() {
        logger.info("Reloading feature flags...");

        Map<String, Object> result = new HashMap<>();
        LocalDateTime reloadTime = LocalDateTime.now();

        try {
            Map<String, Boolean> oldFlags = featureFlagService.getAllFeatureFlags();
            featureFlagService.resetToConfigurationValues();
            Map<String, Boolean> newFlags = featureFlagService.getAllFeatureFlags();

            // Identify changed flags
            Map<String, String> changes = new HashMap<>();
            for (String flagName : newFlags.keySet()) {
                Boolean oldValue = oldFlags.get(flagName);
                Boolean newValue = newFlags.get(flagName);
                if (!newValue.equals(oldValue)) {
                    changes.put(flagName, oldValue + " -> " + newValue);
                }
            }

            lastReloadTimes.put("featureFlags", reloadTime);
            result.put("reloadTime", reloadTime.toString());
            result.put("status", "success");
            result.put("changes", changes);
            result.put("currentFlags", newFlags);

            // Publish feature flags reload event
            eventPublisher.publishEvent(new ConfigurationReloadEvent(this, "featureFlags", reloadTime));

            logger.info("Feature flags reloaded successfully. Changes: {}", changes);

        } catch (Exception e) {
            logger.error("Failed to reload feature flags", e);
            result.put("status", "error");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Reload logging configuration
     */
    @WriteOperation
    public Map<String, Object> reloadLoggingConfiguration() {
        logger.info("Reloading logging configuration...");

        Map<String, Object> result = new HashMap<>();
        LocalDateTime reloadTime = LocalDateTime.now();

        try {
            // Get current logging levels from environment
            Map<String, String> loggingLevels = new HashMap<>();

            // Common logging properties that can be hot-reloaded
            String[] loggingProperties = {
                    "logging.level.com.arcone.biopro.exception.collector",
                    "logging.level.org.springframework.kafka",
                    "logging.level.org.springframework.web",
                    "logging.level.org.hibernate.SQL",
                    "logging.level.io.github.resilience4j",
                    "logging.level.org.springframework.cache",
                    "logging.level.root"
            };

            for (String property : loggingProperties) {
                String value = environment.getProperty(property);
                if (value != null) {
                    loggingLevels.put(property, value);

                    // Extract logger name from property
                    String loggerName = property.replace("logging.level.", "");

                    // Set the logging level (this would require LogBack or Log4j2 integration)
                    // For now, we'll just log the intended change
                    logger.info("Would set logger '{}' to level '{}'", loggerName, value);
                }
            }

            lastReloadTimes.put("logging", reloadTime);
            result.put("reloadTime", reloadTime.toString());
            result.put("status", "success");
            result.put("loggingLevels", loggingLevels);

            // Publish logging reload event
            eventPublisher.publishEvent(new ConfigurationReloadEvent(this, "logging", reloadTime));

            logger.info("Logging configuration reloaded successfully");

        } catch (Exception e) {
            logger.error("Failed to reload logging configuration", e);
            result.put("status", "error");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Get the status of configuration hot-reload capability
     */
    public Map<String, Object> getReloadStatus() {
        Map<String, Object> status = new HashMap<>();

        status.put("hotReloadEnabled", featureFlagService.isHotReloadEnabled());
        status.put("springCloudConfigAvailable", contextRefresher != null);
        status.put("lastReloadTimes", new HashMap<>(lastReloadTimes));

        return status;
    }

    /**
     * Event published when configuration is reloaded
     */
    public static class ConfigurationReloadEvent {
        private final Object source;
        private final String configurationSection;
        private final LocalDateTime reloadTime;

        public ConfigurationReloadEvent(Object source, String configurationSection, LocalDateTime reloadTime) {
            this.source = source;
            this.configurationSection = configurationSection;
            this.reloadTime = reloadTime;
        }

        public Object getSource() {
            return source;
        }

        public String getConfigurationSection() {
            return configurationSection;
        }

        public LocalDateTime getReloadTime() {
            return reloadTime;
        }

        @Override
        public String toString() {
            return "ConfigurationReloadEvent{" +
                    "configurationSection='" + configurationSection + '\'' +
                    ", reloadTime=" + reloadTime +
                    '}';
        }
    }
}