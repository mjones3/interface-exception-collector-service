package com.arcone.biopro.exception.collector.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validates application configuration at startup to ensure proper setup
 * and prevent common configuration errors. Provides comprehensive validation
 * for RSocket connection parameters and error handling configuration.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigurationValidator {

    private final RSocketProperties rSocketProperties;
    private final FeatureFlagsProperties featureFlagsProperties;
    private final Environment environment;
    
    // Validation constants
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;
    private static final Duration MIN_TIMEOUT = Duration.ofMillis(100);
    private static final Duration MAX_TIMEOUT = Duration.ofMinutes(10);
    private static final Duration MIN_CONNECTION_TIMEOUT = Duration.ofSeconds(1);
    private static final Duration MAX_CONNECTION_TIMEOUT = Duration.ofMinutes(5);
    private static final Pattern HOSTNAME_PATTERN = Pattern.compile(
        "^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)*(([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?))$|^localhost$|^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$"
    );

    /**
     * Validates configuration after application startup.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        log.info("Starting comprehensive configuration validation...");
        
        List<String> validationErrors = new ArrayList<>();
        List<String> validationWarnings = new ArrayList<>();
        
        try {
            validateEnvironmentConfiguration(validationErrors, validationWarnings);
            validateRSocketConfiguration(validationErrors, validationWarnings);
            validateFeatureFlags(validationErrors, validationWarnings);
            validateResilienceConfiguration(validationErrors, validationWarnings);
            
            // Log warnings
            if (!validationWarnings.isEmpty()) {
                log.warn("Configuration validation completed with {} warnings:", validationWarnings.size());
                validationWarnings.forEach(warning -> log.warn("  - {}", warning));
            }
            
            // Fail if there are errors
            if (!validationErrors.isEmpty()) {
                log.error("Configuration validation failed with {} errors:", validationErrors.size());
                validationErrors.forEach(error -> log.error("  - {}", error));
                throw new IllegalStateException(
                    String.format("Configuration validation failed with %d errors. See logs for details.", 
                        validationErrors.size()));
            }
            
            log.info("Configuration validation completed successfully with {} warnings", validationWarnings.size());
            
        } catch (Exception e) {
            log.error("Configuration validation failed with exception: {}", e.getMessage(), e);
            throw new IllegalStateException("Configuration validation failed", e);
        }
    }

    /**
     * Validates environment-specific configuration.
     */
    private void validateEnvironmentConfiguration(List<String> errors, List<String> warnings) {
        String[] activeProfiles = environment.getActiveProfiles();
        String activeProfile = activeProfiles.length > 0 ? activeProfiles[0] : "default";
        
        log.info("Active profile: {}", activeProfile);
        
        // Validate production safety
        if (isProductionEnvironment(activeProfile)) {
            if (rSocketProperties.getMockServer().isEnabled()) {
                errors.add("Mock RSocket server is enabled in production environment. " +
                          "This is not allowed for security and reliability reasons.");
            }
            
            if (featureFlagsProperties.isDebugMode()) {
                warnings.add("Debug mode is enabled in production environment. " +
                           "Consider disabling for performance and security.");
            }
            
            if (rSocketProperties.getMockServer().isDebugLogging()) {
                warnings.add("RSocket debug logging is enabled in production environment. " +
                           "This may impact performance and expose sensitive information.");
            }
        }
        
        // Validate development environment setup
        if (isDevelopmentEnvironment(activeProfile)) {
            if (!rSocketProperties.getMockServer().isEnabled() && 
                !rSocketProperties.getPartnerOrderService().isEnabled()) {
                warnings.add("Neither mock server nor partner service is enabled in development environment. " +
                           "This may cause order data retrieval failures.");
            }
        }
        
        // Validate required environment variables
        validateRequiredEnvironmentVariables(errors, warnings, activeProfile);
    }

    /**
     * Validates RSocket configuration.
     */
    private void validateRSocketConfiguration(List<String> errors, List<String> warnings) {
        RSocketProperties.MockServer mockServer = rSocketProperties.getMockServer();
        RSocketProperties.PartnerOrderService partnerService = rSocketProperties.getPartnerOrderService();
        
        // Validate mock server configuration
        if (mockServer.isEnabled()) {
            validateMockServerConfig(mockServer, errors, warnings);
        }
        
        // Validate partner service configuration
        if (partnerService.isEnabled()) {
            validatePartnerServiceConfig(partnerService, errors, warnings);
        }
        
        // Ensure at least one service is enabled
        if (!mockServer.isEnabled() && !partnerService.isEnabled()) {
            errors.add("Neither mock RSocket server nor partner order service is enabled. " +
                      "At least one must be enabled for order data retrieval.");
        }
        
        // Warn if both are enabled
        if (mockServer.isEnabled() && partnerService.isEnabled()) {
            warnings.add("Both mock RSocket server and partner order service are enabled. " +
                        "Mock server will take priority.");
        }
        
        // Validate connection parameters consistency
        validateConnectionParametersConsistency(mockServer, partnerService, errors, warnings);
    }

    /**
     * Validates mock server configuration.
     */
    private void validateMockServerConfig(RSocketProperties.MockServer mockServer, 
                                        List<String> errors, List<String> warnings) {
        // Validate host
        if (mockServer.getHost() == null || mockServer.getHost().trim().isEmpty()) {
            errors.add("Mock RSocket server host cannot be empty");
        } else if (!isValidHostname(mockServer.getHost())) {
            errors.add("Mock RSocket server host '" + mockServer.getHost() + "' is not a valid hostname or IP address");
        }
        
        // Validate port
        if (mockServer.getPort() < MIN_PORT || mockServer.getPort() > MAX_PORT) {
            errors.add("Mock RSocket server port must be between " + MIN_PORT + " and " + MAX_PORT + 
                      ", got: " + mockServer.getPort());
        }
        
        // Validate timeout
        if (mockServer.getTimeout() == null) {
            errors.add("Mock RSocket server timeout cannot be null");
        } else if (mockServer.getTimeout().isNegative() || mockServer.getTimeout().isZero()) {
            errors.add("Mock RSocket server timeout must be positive, got: " + mockServer.getTimeout());
        } else if (mockServer.getTimeout().compareTo(MIN_TIMEOUT) < 0) {
            warnings.add("Mock RSocket server timeout is very short (" + mockServer.getTimeout() + 
                        "), minimum recommended: " + MIN_TIMEOUT);
        } else if (mockServer.getTimeout().compareTo(MAX_TIMEOUT) > 0) {
            warnings.add("Mock RSocket server timeout is very long (" + mockServer.getTimeout() + 
                        "), maximum recommended: " + MAX_TIMEOUT);
        }
        
        // Validate connection timeout
        if (mockServer.getConnectionTimeout() == null) {
            errors.add("Mock RSocket server connection timeout cannot be null");
        } else if (mockServer.getConnectionTimeout().isNegative() || mockServer.getConnectionTimeout().isZero()) {
            errors.add("Mock RSocket server connection timeout must be positive, got: " + mockServer.getConnectionTimeout());
        } else if (mockServer.getConnectionTimeout().compareTo(MIN_CONNECTION_TIMEOUT) < 0) {
            warnings.add("Mock RSocket server connection timeout is very short (" + mockServer.getConnectionTimeout() + 
                        "), minimum recommended: " + MIN_CONNECTION_TIMEOUT);
        } else if (mockServer.getConnectionTimeout().compareTo(MAX_CONNECTION_TIMEOUT) > 0) {
            warnings.add("Mock RSocket server connection timeout is very long (" + mockServer.getConnectionTimeout() + 
                        "), maximum recommended: " + MAX_CONNECTION_TIMEOUT);
        }
        
        // Validate keep-alive settings
        validateKeepAliveSettings(mockServer, errors, warnings);
        
        // Validate circuit breaker settings
        validateCircuitBreakerSettings(mockServer.getCircuitBreaker(), "Mock RSocket server", errors, warnings);
        
        // Validate retry settings
        validateRetrySettings(mockServer.getRetry(), "Mock RSocket server", errors, warnings);
        
        log.info("Mock RSocket server configuration validated: {}:{}", 
                mockServer.getHost(), mockServer.getPort());
    }

    /**
     * Validates partner service configuration.
     */
    private void validatePartnerServiceConfig(RSocketProperties.PartnerOrderService partnerService, 
                                            List<String> errors, List<String> warnings) {
        // Validate host
        if (partnerService.getHost() == null || partnerService.getHost().trim().isEmpty()) {
            errors.add("Partner order service host cannot be empty");
        } else if (!isValidHostname(partnerService.getHost())) {
            errors.add("Partner order service host '" + partnerService.getHost() + "' is not a valid hostname or IP address");
        }
        
        // Validate port
        if (partnerService.getPort() < MIN_PORT || partnerService.getPort() > MAX_PORT) {
            errors.add("Partner order service port must be between " + MIN_PORT + " and " + MAX_PORT + 
                      ", got: " + partnerService.getPort());
        }
        
        // Validate timeout
        if (partnerService.getTimeout() == null) {
            errors.add("Partner order service timeout cannot be null");
        } else if (partnerService.getTimeout().isNegative() || partnerService.getTimeout().isZero()) {
            errors.add("Partner order service timeout must be positive, got: " + partnerService.getTimeout());
        } else if (partnerService.getTimeout().compareTo(MIN_TIMEOUT) < 0) {
            warnings.add("Partner order service timeout is very short (" + partnerService.getTimeout() + 
                        "), minimum recommended: " + MIN_TIMEOUT);
        } else if (partnerService.getTimeout().compareTo(MAX_TIMEOUT) > 0) {
            warnings.add("Partner order service timeout is very long (" + partnerService.getTimeout() + 
                        "), maximum recommended: " + MAX_TIMEOUT);
        }
        
        // Validate connection timeout
        if (partnerService.getConnectionTimeout() == null) {
            errors.add("Partner order service connection timeout cannot be null");
        } else if (partnerService.getConnectionTimeout().isNegative() || partnerService.getConnectionTimeout().isZero()) {
            errors.add("Partner order service connection timeout must be positive, got: " + partnerService.getConnectionTimeout());
        } else if (partnerService.getConnectionTimeout().compareTo(MIN_CONNECTION_TIMEOUT) < 0) {
            warnings.add("Partner order service connection timeout is very short (" + partnerService.getConnectionTimeout() + 
                        "), minimum recommended: " + MIN_CONNECTION_TIMEOUT);
        } else if (partnerService.getConnectionTimeout().compareTo(MAX_CONNECTION_TIMEOUT) > 0) {
            warnings.add("Partner order service connection timeout is very long (" + partnerService.getConnectionTimeout() + 
                        "), maximum recommended: " + MAX_CONNECTION_TIMEOUT);
        }
        
        log.info("Partner order service configuration validated: {}:{}", 
                partnerService.getHost(), partnerService.getPort());
    }

    /**
     * Validates feature flags configuration.
     */
    private void validateFeatureFlags(List<String> errors, List<String> warnings) {
        log.info("Feature flags validation:");
        log.info("  Enhanced Logging: {}", featureFlagsProperties.isEnhancedLogging());
        log.info("  Debug Mode: {}", featureFlagsProperties.isDebugMode());
        log.info("  Payload Caching: {}", featureFlagsProperties.isPayloadCaching());
        log.info("  Circuit Breaker: {}", featureFlagsProperties.isCircuitBreaker());
        log.info("  Retry Mechanism: {}", featureFlagsProperties.isRetryMechanism());
        log.info("  Metrics Collection: {}", featureFlagsProperties.isMetricsCollection());
        log.info("  Audit Logging: {}", featureFlagsProperties.isAuditLogging());
        
        // Validate critical feature flags
        if (!featureFlagsProperties.isCircuitBreaker()) {
            warnings.add("Circuit breaker is disabled. This may impact system resilience.");
        }
        
        if (!featureFlagsProperties.isRetryMechanism()) {
            warnings.add("Retry mechanism is disabled. This may impact system reliability.");
        }
        
        if (!featureFlagsProperties.isMetricsCollection()) {
            warnings.add("Metrics collection is disabled. This may impact monitoring and observability.");
        }
        
        // Validate feature flag combinations
        if (featureFlagsProperties.isDebugMode() && !featureFlagsProperties.isEnhancedLogging()) {
            warnings.add("Debug mode is enabled but enhanced logging is disabled. " +
                        "Consider enabling enhanced logging for better debugging experience.");
        }
        
        if (featureFlagsProperties.isPayloadCaching() && featureFlagsProperties.isDebugMode()) {
            warnings.add("Payload caching is enabled in debug mode. " +
                        "This may mask issues during development.");
        }
    }

    /**
     * Validates resilience configuration (circuit breaker, retry, timeout).
     */
    private void validateResilienceConfiguration(List<String> errors, List<String> warnings) {
        // This method validates resilience4j configuration if needed
        // For now, we validate through the RSocket properties validation
        log.debug("Resilience configuration validation completed");
    }
    
    /**
     * Validates required environment variables for the given profile.
     */
    private void validateRequiredEnvironmentVariables(List<String> errors, List<String> warnings, String profile) {
        // Check for required environment variables based on profile
        if (isProductionEnvironment(profile)) {
            // In production, certain environment variables should be set
            if (environment.getProperty("SPRING_DATASOURCE_URL") == null) {
                warnings.add("SPRING_DATASOURCE_URL environment variable is not set in production");
            }
            
            if (environment.getProperty("SPRING_KAFKA_BOOTSTRAP_SERVERS") == null) {
                warnings.add("SPRING_KAFKA_BOOTSTRAP_SERVERS environment variable is not set in production");
            }
        }
    }
    
    /**
     * Validates connection parameters consistency between services.
     */
    private void validateConnectionParametersConsistency(RSocketProperties.MockServer mockServer,
                                                       RSocketProperties.PartnerOrderService partnerService,
                                                       List<String> errors, List<String> warnings) {
        if (mockServer.isEnabled() && partnerService.isEnabled()) {
            // Check if timeouts are consistent
            if (mockServer.getTimeout().compareTo(partnerService.getTimeout()) > 0) {
                warnings.add("Mock server timeout (" + mockServer.getTimeout() + 
                           ") is longer than partner service timeout (" + partnerService.getTimeout() + 
                           "). This may cause inconsistent behavior.");
            }
        }
    }
    
    /**
     * Validates keep-alive settings for mock server.
     */
    private void validateKeepAliveSettings(RSocketProperties.MockServer mockServer, 
                                         List<String> errors, List<String> warnings) {
        if (mockServer.getKeepAliveInterval() == null) {
            errors.add("Mock RSocket server keep-alive interval cannot be null");
            return;
        }
        
        if (mockServer.getKeepAliveMaxLifetime() == null) {
            errors.add("Mock RSocket server keep-alive max lifetime cannot be null");
            return;
        }
        
        if (mockServer.getKeepAliveInterval().isNegative() || mockServer.getKeepAliveInterval().isZero()) {
            errors.add("Mock RSocket server keep-alive interval must be positive, got: " + 
                      mockServer.getKeepAliveInterval());
        }
        
        if (mockServer.getKeepAliveMaxLifetime().isNegative() || mockServer.getKeepAliveMaxLifetime().isZero()) {
            errors.add("Mock RSocket server keep-alive max lifetime must be positive, got: " + 
                      mockServer.getKeepAliveMaxLifetime());
        }
        
        if (mockServer.getKeepAliveInterval().compareTo(mockServer.getKeepAliveMaxLifetime()) >= 0) {
            warnings.add("Mock RSocket server keep-alive interval (" + mockServer.getKeepAliveInterval() + 
                        ") should be less than max lifetime (" + mockServer.getKeepAliveMaxLifetime() + ")");
        }
    }
    
    /**
     * Validates circuit breaker settings.
     */
    private void validateCircuitBreakerSettings(RSocketProperties.CircuitBreaker circuitBreaker, 
                                              String serviceName, List<String> errors, List<String> warnings) {
        if (circuitBreaker == null) {
            errors.add(serviceName + " circuit breaker configuration cannot be null");
            return;
        }
        
        if (circuitBreaker.getFailureRateThreshold() < 1 || circuitBreaker.getFailureRateThreshold() > 100) {
            errors.add(serviceName + " circuit breaker failure rate threshold must be between 1 and 100, got: " + 
                      circuitBreaker.getFailureRateThreshold());
        }
        
        if (circuitBreaker.getWaitDurationInOpenState() == null || 
            circuitBreaker.getWaitDurationInOpenState().isNegative() || 
            circuitBreaker.getWaitDurationInOpenState().isZero()) {
            errors.add(serviceName + " circuit breaker wait duration must be positive, got: " + 
                      circuitBreaker.getWaitDurationInOpenState());
        }
        
        if (circuitBreaker.getSlidingWindowSize() < 1) {
            errors.add(serviceName + " circuit breaker sliding window size must be positive, got: " + 
                      circuitBreaker.getSlidingWindowSize());
        }
        
        if (circuitBreaker.getMinimumNumberOfCalls() < 1) {
            errors.add(serviceName + " circuit breaker minimum number of calls must be positive, got: " + 
                      circuitBreaker.getMinimumNumberOfCalls());
        }
        
        if (circuitBreaker.getPermittedCallsInHalfOpen() < 1) {
            errors.add(serviceName + " circuit breaker permitted calls in half-open must be positive, got: " + 
                      circuitBreaker.getPermittedCallsInHalfOpen());
        }
        
        // Warnings for suboptimal settings
        if (circuitBreaker.getFailureRateThreshold() > 80) {
            warnings.add(serviceName + " circuit breaker failure rate threshold is very high (" + 
                        circuitBreaker.getFailureRateThreshold() + "%). Consider lowering for better resilience.");
        }
        
        if (circuitBreaker.getMinimumNumberOfCalls() > circuitBreaker.getSlidingWindowSize()) {
            warnings.add(serviceName + " circuit breaker minimum calls (" + circuitBreaker.getMinimumNumberOfCalls() + 
                        ") is greater than sliding window size (" + circuitBreaker.getSlidingWindowSize() + ")");
        }
    }
    
    /**
     * Validates retry settings.
     */
    private void validateRetrySettings(RSocketProperties.Retry retry, String serviceName, 
                                     List<String> errors, List<String> warnings) {
        if (retry == null) {
            errors.add(serviceName + " retry configuration cannot be null");
            return;
        }
        
        if (retry.getMaxAttempts() < 1) {
            errors.add(serviceName + " retry max attempts must be positive, got: " + retry.getMaxAttempts());
        }
        
        if (retry.getWaitDuration() == null || retry.getWaitDuration().isNegative()) {
            errors.add(serviceName + " retry wait duration must be non-negative, got: " + retry.getWaitDuration());
        }
        
        if (retry.getExponentialBackoffMultiplier() < 1.0) {
            errors.add(serviceName + " retry exponential backoff multiplier must be >= 1.0, got: " + 
                      retry.getExponentialBackoffMultiplier());
        }
        
        // Warnings for suboptimal settings
        if (retry.getMaxAttempts() > 10) {
            warnings.add(serviceName + " retry max attempts is very high (" + retry.getMaxAttempts() + 
                        "). This may cause long delays during failures.");
        }
        
        if (retry.getExponentialBackoffMultiplier() > 5.0) {
            warnings.add(serviceName + " retry exponential backoff multiplier is very high (" + 
                        retry.getExponentialBackoffMultiplier() + "). This may cause very long delays.");
        }
    }
    
    /**
     * Validates if a hostname is valid.
     */
    private boolean isValidHostname(String hostname) {
        if (hostname == null || hostname.trim().isEmpty()) {
            return false;
        }
        
        // Check length
        if (hostname.length() > 253) {
            return false;
        }
        
        // Use regex pattern for validation
        return HOSTNAME_PATTERN.matcher(hostname.trim()).matches();
    }
    
    /**
     * Checks if the current environment is production.
     */
    private boolean isProductionEnvironment(String profile) {
        return "prod".equalsIgnoreCase(profile) || 
               "production".equalsIgnoreCase(profile) ||
               profile.toLowerCase().contains("prod");
    }
    
    /**
     * Checks if the current environment is development.
     */
    private boolean isDevelopmentEnvironment(String profile) {
        return "dev".equalsIgnoreCase(profile) || 
               "development".equalsIgnoreCase(profile) ||
               "local".equalsIgnoreCase(profile) ||
               profile.toLowerCase().contains("dev");
    }
}