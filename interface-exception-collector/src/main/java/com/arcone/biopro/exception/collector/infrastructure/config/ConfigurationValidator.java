package com.arcone.biopro.exception.collector.infrastructure.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Configuration validator that performs startup checks to ensure all required
 * configuration properties are properly set and valid.
 */
@Configuration
public class ConfigurationValidator {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationValidator.class);

    private final ApplicationProperties applicationProperties;
    private final Environment environment;
    private final Validator validator;

    @Autowired
    public ConfigurationValidator(ApplicationProperties applicationProperties, Environment environment,
            Validator validator) {
        this.applicationProperties = applicationProperties;
        this.environment = environment;
        this.validator = validator;
    }

    /**
     * Validates configuration on application startup
     */
    @PostConstruct
    public void validateConfiguration() {
        logger.info("Starting configuration validation...");

        List<String> validationErrors = new ArrayList<>();

        // Validate ApplicationProperties using Bean Validation
        var violations = validator.validate(applicationProperties);
        for (ConstraintViolation<ApplicationProperties> violation : violations) {
            validationErrors.add(
                    "Configuration validation error: " + violation.getPropertyPath() + " " + violation.getMessage());
        }

        // Validate database configuration
        validateDatabaseConfiguration(validationErrors);

        // Validate Kafka configuration
        validateKafkaConfiguration(validationErrors);

        // Redis validation disabled - using simple cache

        // Validate external services configuration
        validateExternalServicesConfiguration(validationErrors);

        // Validate security configuration
        validateSecurityConfiguration(validationErrors);

        // Validate feature flags
        validateFeatureFlags(validationErrors);

        // Log validation results
        if (validationErrors.isEmpty()) {
            logger.info("Configuration validation completed successfully");
            logConfigurationSummary();
        } else {
            logger.error("Configuration validation failed with {} errors:", validationErrors.size());
            validationErrors.forEach(error -> logger.error("  - {}", error));
            throw new IllegalStateException("Configuration validation failed. Please check the errors above.");
        }
    }

    private void validateDatabaseConfiguration(List<String> errors) {
        String dbUrl = environment.getProperty("spring.datasource.url");
        String dbUsername = environment.getProperty("spring.datasource.username");
        String dbPassword = environment.getProperty("spring.datasource.password");

        if (!StringUtils.hasText(dbUrl)) {
            errors.add("Database URL is required (spring.datasource.url)");
        } else {
            try {
                URI uri = new URI(dbUrl.replace("jdbc:", ""));
                if (uri.getHost() == null || uri.getPort() == -1) {
                    errors.add("Database URL format is invalid: " + dbUrl);
                }
            } catch (URISyntaxException e) {
                errors.add("Database URL format is invalid: " + dbUrl);
            }
        }

        if (!StringUtils.hasText(dbUsername)) {
            errors.add("Database username is required (spring.datasource.username)");
        }

        if (!StringUtils.hasText(dbPassword)) {
            errors.add("Database password is required (spring.datasource.password)");
        }

        // Validate connection pool settings
        Integer maxPoolSize = environment.getProperty("spring.datasource.hikari.maximum-pool-size", Integer.class);
        if (maxPoolSize != null && maxPoolSize <= 0) {
            errors.add("Database connection pool max size must be positive");
        }
    }

    private void validateKafkaConfiguration(List<String> errors) {
        String bootstrapServers = environment.getProperty("spring.kafka.bootstrap-servers");
        if (!StringUtils.hasText(bootstrapServers)) {
            errors.add("Kafka bootstrap servers are required (spring.kafka.bootstrap-servers)");
        }

        String consumerGroup = environment.getProperty("spring.kafka.consumer.group-id");
        if (!StringUtils.hasText(consumerGroup)) {
            errors.add("Kafka consumer group ID is required (spring.kafka.consumer.group-id)");
        }

        // Validate SSL configuration if security protocol is SSL
        String securityProtocol = environment.getProperty("spring.kafka.consumer.properties.security.protocol");
        if ("SSL".equals(securityProtocol)) {
            validateKafkaSSLConfiguration(errors);
        }
    }

    private void validateKafkaSSLConfiguration(List<String> errors) {
        String truststore = environment.getProperty("spring.kafka.consumer.properties.ssl.truststore.location");
        String truststorePassword = environment.getProperty("spring.kafka.consumer.properties.ssl.truststore.password");
        String keystore = environment.getProperty("spring.kafka.consumer.properties.ssl.keystore.location");
        String keystorePassword = environment.getProperty("spring.kafka.consumer.properties.ssl.keystore.password");

        if (!StringUtils.hasText(truststore)) {
            errors.add("Kafka SSL truststore location is required when using SSL");
        }
        if (!StringUtils.hasText(truststorePassword)) {
            errors.add("Kafka SSL truststore password is required when using SSL");
        }
        if (!StringUtils.hasText(keystore)) {
            errors.add("Kafka SSL keystore location is required when using SSL");
        }
        if (!StringUtils.hasText(keystorePassword)) {
            errors.add("Kafka SSL keystore password is required when using SSL");
        }
    }

    // Redis validation removed - using simple in-memory cache

    private void validateExternalServicesConfiguration(List<String> errors) {
        validateServiceConfig("order", applicationProperties.sourceServices().order(), errors);
        validateServiceConfig("collection", applicationProperties.sourceServices().collection(), errors);
        validateServiceConfig("distribution", applicationProperties.sourceServices().distribution(), errors);

        if (applicationProperties.sourceServices().timeout() <= 0) {
            errors.add("External service timeout must be positive");
        }

        if (applicationProperties.sourceServices().connectionTimeout() <= 0) {
            errors.add("External service connection timeout must be positive");
        }
    }

    private void validateServiceConfig(String serviceName, ApplicationProperties.SourceServices.ServiceConfig config,
            List<String> errors) {
        if (config == null) {
            errors.add(serviceName + " service configuration is missing");
            return;
        }

        if (!StringUtils.hasText(config.baseUrl())) {
            errors.add(serviceName + " service base URL is required");
        } else {
            try {
                URI uri = new URI(config.baseUrl());
                if (!"http".equals(uri.getScheme()) && !"https".equals(uri.getScheme())) {
                    errors.add(serviceName + " service base URL must use HTTP or HTTPS protocol");
                }
            } catch (URISyntaxException e) {
                errors.add(serviceName + " service base URL format is invalid: " + config.baseUrl());
            }
        }

        if (!StringUtils.hasText(config.apiKey())) {
            errors.add(serviceName + " service API key is required");
        }

        if (!StringUtils.hasText(config.authHeader())) {
            errors.add(serviceName + " service auth header is required");
        }
    }

    private void validateSecurityConfiguration(List<String> errors) {
        if (applicationProperties.security() == null) {
            errors.add("Security configuration is missing");
            return;
        }

        // Validate JWT configuration
        var jwt = applicationProperties.security().jwt();
        if (jwt == null) {
            errors.add("JWT configuration is missing");
        } else {
            if (!StringUtils.hasText(jwt.secret()) || jwt.secret().length() < 32) {
                errors.add("JWT secret must be at least 32 characters long");
            }
            if (jwt.expiration() <= 0) {
                errors.add("JWT expiration must be positive");
            }
        }

        // Validate TLS configuration if enabled
        var tls = applicationProperties.security().tls();
        if (tls != null && tls.enabled()) {
            if (tls.keystore() == null || !StringUtils.hasText(tls.keystore().path())) {
                errors.add("TLS keystore path is required when TLS is enabled");
            }
            if (tls.keystore() == null || !StringUtils.hasText(tls.keystore().password())) {
                errors.add("TLS keystore password is required when TLS is enabled");
            }
        }
    }

    private void validateFeatureFlags(List<String> errors) {
        if (applicationProperties.features() == null) {
            errors.add("Feature flags configuration is missing");
            return;
        }

        // Log feature flag status
        var features = applicationProperties.features();
        logger.info("Feature flags configuration:");
        logger.info("  Enhanced Logging: {}", features.enhancedLogging());
        logger.info("  Debug Mode: {}", features.debugMode());
        logger.info("  Payload Caching: {}", features.payloadCaching());
        logger.info("  Circuit Breaker: {}", features.circuitBreaker());
        logger.info("  Retry Mechanism: {}", features.retryMechanism());
        logger.info("  Hot Reload: {}", features.hotReload());
        logger.info("  Metrics Collection: {}", features.metricsCollection());
        logger.info("  Audit Logging: {}", features.auditLogging());
    }

    private void logConfigurationSummary() {
        String activeProfile = String.join(",", environment.getActiveProfiles());
        if (activeProfile.isEmpty()) {
            activeProfile = "default";
        }

        logger.info("Configuration Summary:");
        logger.info("  Active Profile: {}", activeProfile);
        logger.info("  Application Name: {}", environment.getProperty("spring.application.name"));
        logger.info("  Server Port: {}", environment.getProperty("server.port"));
        logger.info("  Database URL: {}", maskSensitiveUrl(environment.getProperty("spring.datasource.url")));
        logger.info("  Kafka Bootstrap Servers: {}", environment.getProperty("spring.kafka.bootstrap-servers"));
        logger.info("  Cache Type: Simple (Redis disabled)");
        logger.info("  TLS Enabled: {}", applicationProperties.security().tls().enabled());
        logger.info("  Rate Limiting Enabled: {}", applicationProperties.security().rateLimit().enabled());

        // Log external service URLs (masked)
        logger.info("  External Services:");
        logger.info("    Order Service: {}",
                maskSensitiveUrl(applicationProperties.sourceServices().order().baseUrl()));
        logger.info("    Collection Service: {}",
                maskSensitiveUrl(applicationProperties.sourceServices().collection().baseUrl()));
        logger.info("    Distribution Service: {}",
                maskSensitiveUrl(applicationProperties.sourceServices().distribution().baseUrl()));
    }

    private String maskSensitiveUrl(String url) {
        if (url == null)
            return null;

        try {
            URI uri = new URI(url);
            String userInfo = uri.getUserInfo();
            if (userInfo != null) {
                // Mask password in URL
                String maskedUserInfo = userInfo.contains(":")
                        ? userInfo.substring(0, userInfo.indexOf(":") + 1) + "***"
                        : userInfo;
                return url.replace(userInfo, maskedUserInfo);
            }
            return url;
        } catch (URISyntaxException e) {
            return url;
        }
    }
}