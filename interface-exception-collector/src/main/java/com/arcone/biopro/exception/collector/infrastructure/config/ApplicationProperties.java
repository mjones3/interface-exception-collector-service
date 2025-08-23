package com.arcone.biopro.exception.collector.infrastructure.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Map;

/**
 * Configuration properties for the Interface Exception Collector Service.
 * This class provides type-safe configuration binding and validation.
 */
@ConfigurationProperties(prefix = "app")
public record ApplicationProperties(
        @Valid @NestedConfigurationProperty Features features,

        @Valid @NestedConfigurationProperty Security security,

        @Valid @NestedConfigurationProperty SourceServices sourceServices,

        @Valid @NestedConfigurationProperty Database database,

        @Valid @NestedConfigurationProperty Kafka kafka,

        @Valid @NestedConfigurationProperty Exception exception) {

    /**
     * Feature flags configuration
     */
    public record Features(
            Boolean enhancedLogging,

            Boolean debugMode,

            Boolean payloadCaching,

            Boolean circuitBreaker,

            Boolean retryMechanism,

            Boolean hotReload,

            Boolean metricsCollection,

            Boolean auditLogging) {
        public Features {
            // Set defaults for all fields
            if (enhancedLogging == null)
                enhancedLogging = true;
            if (debugMode == null)
                debugMode = false;
            if (payloadCaching == null)
                payloadCaching = true;
            if (circuitBreaker == null)
                circuitBreaker = true;
            if (retryMechanism == null)
                retryMechanism = true;
            if (hotReload == null)
                hotReload = false;
            if (metricsCollection == null)
                metricsCollection = true;
            if (auditLogging == null)
                auditLogging = false;
        }
    }

    /**
     * Security configuration
     */
    public record Security(
            @Valid @NestedConfigurationProperty Jwt jwt,

            @Valid @NestedConfigurationProperty RateLimit rateLimit,

            @Valid @NestedConfigurationProperty Tls tls,

            @NestedConfigurationProperty Audit audit) {

        public record Jwt(
                @NotBlank @Size(min = 32, message = "JWT secret must be at least 32 characters") String secret,

                @Positive @Max(value = 86400000, message = "JWT expiration cannot exceed 24 hours") Long expiration,

                String issuer,
                String audience) {
            public Jwt {
                if (expiration == null)
                    expiration = 3600000L; // 1 hour default
            }
        }

        public record RateLimit(
                Boolean enabled,

                @Positive @Max(value = 10000, message = "Rate limit cannot exceed 10000 requests per minute") Integer requestsPerMinute,

                @Positive @Max(value = 1000, message = "Burst capacity cannot exceed 1000") Integer burstCapacity) {
            public RateLimit {
                if (enabled == null)
                    enabled = true;
                if (requestsPerMinute == null)
                    requestsPerMinute = 60;
                if (burstCapacity == null)
                    burstCapacity = 10;
            }
        }

        public record Tls(
                Boolean enabled,

                @NestedConfigurationProperty KeyStore keystore,

                @NestedConfigurationProperty KeyStore truststore) {
            public Tls {
                if (enabled == null)
                    enabled = false;
            }

            public record KeyStore(
                    String path,
                    String password) {
            }
        }

        public record Audit(
                Boolean enabled,
                Boolean logSuccessfulRequests,
                Boolean logFailedRequests) {
            public Audit {
                if (enabled == null)
                    enabled = false;
                if (logSuccessfulRequests == null)
                    logSuccessfulRequests = false;
                if (logFailedRequests == null)
                    logFailedRequests = true;
            }
        }
    }

    /**
     * External source services configuration
     */
    public record SourceServices(
            @Valid @NestedConfigurationProperty ServiceConfig order,

            @Valid @NestedConfigurationProperty ServiceConfig collection,

            @Valid @NestedConfigurationProperty ServiceConfig distribution,

            @Positive @Max(value = 60000, message = "Timeout cannot exceed 60 seconds") Integer timeout,

            @Positive @Max(value = 30000, message = "Connection timeout cannot exceed 30 seconds") Integer connectionTimeout,

            Integer readTimeout) {
        public SourceServices {
            if (timeout == null)
                timeout = 5000;
            if (connectionTimeout == null)
                connectionTimeout = 3000;
            if (readTimeout == null)
                readTimeout = timeout;
        }

        public record ServiceConfig(
                @NotBlank @Pattern(regexp = "^https?://.*", message = "Base URL must be a valid HTTP/HTTPS URL") String baseUrl,

                @NotBlank String apiKey,

                @NotBlank String authHeader) {
        }
    }

    /**
     * Database configuration
     */
    public record Database(
            @Valid @NestedConfigurationProperty Retry retry) {
        public record Retry(
                Boolean enabled,

                @Positive @Max(value = 10, message = "Max retry attempts cannot exceed 10") Integer maxAttempts,

                @Positive Long initialInterval,

                @DecimalMin(value = "1.0", message = "Multiplier must be at least 1.0") @DecimalMax(value = "10.0", message = "Multiplier cannot exceed 10.0") Double multiplier,

                @Positive Long maxInterval) {
            public Retry {
                if (enabled == null)
                    enabled = true;
                if (maxAttempts == null)
                    maxAttempts = 5;
                if (initialInterval == null)
                    initialInterval = 1000L;
                if (multiplier == null)
                    multiplier = 2.0;
                if (maxInterval == null)
                    maxInterval = 30000L;
            }
        }
    }

    /**
     * Kafka configuration
     */
    public record Kafka(
            @Valid @NestedConfigurationProperty DeadLetter deadLetter,

            @NestedConfigurationProperty Map<String, String> topics) {
        public record DeadLetter(
                Boolean enabled,

                String suffix,

                @Positive @Max(value = 10, message = "Max retries cannot exceed 10") Integer maxRetries,

                @Positive Long retryInterval) {
            public DeadLetter {
                if (enabled == null)
                    enabled = true;
                if (suffix == null)
                    suffix = ".DLT";
                if (maxRetries == null)
                    maxRetries = 5;
                if (retryInterval == null)
                    retryInterval = 1000L;
            }
        }
    }

    /**
     * Exception processing configuration
     */
    public record Exception(
            @Valid @NestedConfigurationProperty Processing processing,

            @Valid @NestedConfigurationProperty Retry retry,

            @Valid @NestedConfigurationProperty Alert alert,

            @NestedConfigurationProperty Cleanup cleanup) {
        public record Processing(
                @Positive @Max(value = 1000, message = "Batch size cannot exceed 1000") Integer batchSize,

                @Positive Long timeout,

                Integer maxConcurrent) {
            public Processing {
                if (batchSize == null)
                    batchSize = 100;
                if (timeout == null)
                    timeout = 30000L;
                if (maxConcurrent == null)
                    maxConcurrent = 10;
            }
        }

        public record Retry(
                @Positive @Max(value = 10, message = "Max retry attempts cannot exceed 10") Integer maxAttempts,

                @Positive Long backoffDelay) {
            public Retry {
                if (maxAttempts == null)
                    maxAttempts = 5;
                if (backoffDelay == null)
                    backoffDelay = 1000L;
            }
        }

        public record Alert(
                @Positive @Max(value = 100, message = "Critical threshold cannot exceed 100") Integer criticalThreshold,

                @Positive Long escalationTimeout) {
            public Alert {
                if (criticalThreshold == null)
                    criticalThreshold = 5;
                if (escalationTimeout == null)
                    escalationTimeout = 300L;
            }
        }

        public record Cleanup(
                Boolean enabled,

                @Positive @Max(value = 3650, message = "Retention days cannot exceed 10 years") Integer retentionDays,

                @Positive Integer batchSize) {
            public Cleanup {
                if (enabled == null)
                    enabled = true;
                if (retentionDays == null)
                    retentionDays = 90;
                if (batchSize == null)
                    batchSize = 1000;
            }
        }
    }
}