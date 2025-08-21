package com.arcone.biopro.exception.collector.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import jakarta.validation.Validator;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationValidatorTest {

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private Environment environment;

    @Mock
    private jakarta.validation.Validator validator;

    @InjectMocks
    private ConfigurationValidator configurationValidator;

    @Test
    void validateConfiguration_WithValidConfiguration_ShouldSucceed() {
        // Arrange
        setupValidConfiguration();

        // Act & Assert
        assertDoesNotThrow(() -> configurationValidator.validateConfiguration());
    }

    @Test
    void validateConfiguration_WithMissingDatabaseUrl_ShouldThrowException() {
        // Arrange
        setupValidConfiguration();
        when(environment.getProperty("spring.datasource.url")).thenReturn(null);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> configurationValidator.validateConfiguration());

        assertTrue(exception.getMessage().contains("Configuration validation failed"));
    }

    @Test
    void validateConfiguration_WithInvalidDatabaseUrl_ShouldThrowException() {
        // Arrange
        setupValidConfiguration();
        when(environment.getProperty("spring.datasource.url")).thenReturn("invalid-url");

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> configurationValidator.validateConfiguration());

        assertTrue(exception.getMessage().contains("Configuration validation failed"));
    }

    @Test
    void validateConfiguration_WithMissingKafkaBootstrapServers_ShouldThrowException() {
        // Arrange
        setupValidConfiguration();
        when(environment.getProperty("spring.kafka.bootstrap-servers")).thenReturn(null);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> configurationValidator.validateConfiguration());

        assertTrue(exception.getMessage().contains("Configuration validation failed"));
    }

    @Test
    void validateConfiguration_WithSSLEnabledButMissingTruststore_ShouldThrowException() {
        // Arrange
        setupValidConfiguration();
        when(environment.getProperty("spring.kafka.consumer.properties.security.protocol")).thenReturn("SSL");
        when(environment.getProperty("spring.kafka.consumer.properties.ssl.truststore.location")).thenReturn(null);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> configurationValidator.validateConfiguration());

        assertTrue(exception.getMessage().contains("Configuration validation failed"));
    }

    @Test
    void validateConfiguration_WithInvalidServiceUrl_ShouldThrowException() {
        // Arrange
        setupValidConfiguration();
        var orderService = new ApplicationProperties.SourceServices.ServiceConfig(
                "invalid-url", "api-key", "X-API-Key");
        var sourceServices = new ApplicationProperties.SourceServices(
                orderService,
                mock(ApplicationProperties.SourceServices.ServiceConfig.class),
                mock(ApplicationProperties.SourceServices.ServiceConfig.class),
                5000, 3000, 5000);
        when(applicationProperties.sourceServices()).thenReturn(sourceServices);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> configurationValidator.validateConfiguration());

        assertTrue(exception.getMessage().contains("Configuration validation failed"));
    }

    @Test
    void validateConfiguration_WithShortJwtSecret_ShouldThrowException() {
        // Arrange
        setupValidConfiguration();
        var jwt = new ApplicationProperties.Security.Jwt("short", 3600000L, "issuer", "audience");
        var security = new ApplicationProperties.Security(
                jwt,
                mock(ApplicationProperties.Security.RateLimit.class),
                mock(ApplicationProperties.Security.Tls.class),
                mock(ApplicationProperties.Security.Audit.class));
        when(applicationProperties.security()).thenReturn(security);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> configurationValidator.validateConfiguration());

        assertTrue(exception.getMessage().contains("Configuration validation failed"));
    }

    private void setupValidConfiguration() {
        // Mock validator to return no violations
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        
        // Database configuration
        when(environment.getProperty("spring.datasource.url"))
                .thenReturn("jdbc:postgresql://localhost:5432/test_db");
        when(environment.getProperty("spring.datasource.username"))
                .thenReturn("test_user");
        when(environment.getProperty("spring.datasource.password"))
                .thenReturn("test_password");
        when(environment.getProperty("spring.datasource.hikari.maximum-pool-size", Integer.class))
                .thenReturn(10);

        // Kafka configuration
        when(environment.getProperty("spring.kafka.bootstrap-servers"))
                .thenReturn("localhost:9092");
        when(environment.getProperty("spring.kafka.consumer.group-id"))
                .thenReturn("test-group");
        when(environment.getProperty("spring.kafka.consumer.properties.security.protocol"))
                .thenReturn("PLAINTEXT");

        // Redis configuration removed - using simple cache

        // Application properties
        var features = new ApplicationProperties.Features(
                false, false, true, true, true, false, true, false);
        when(applicationProperties.features()).thenReturn(features);

        var jwt = new ApplicationProperties.Security.Jwt(
                "test-secret-key-32-characters-minimum", 3600000L, "issuer", "audience");
        var rateLimit = new ApplicationProperties.Security.RateLimit(true, 60, 10);
        var tls = new ApplicationProperties.Security.Tls(false, null, null);
        var audit = new ApplicationProperties.Security.Audit(false, false, true);
        var security = new ApplicationProperties.Security(jwt, rateLimit, tls, audit);
        when(applicationProperties.security()).thenReturn(security);

        var orderService = new ApplicationProperties.SourceServices.ServiceConfig(
                "http://localhost:8081", "order-api-key", "X-API-Key");
        var collectionService = new ApplicationProperties.SourceServices.ServiceConfig(
                "http://localhost:8082", "collection-api-key", "X-API-Key");
        var distributionService = new ApplicationProperties.SourceServices.ServiceConfig(
                "http://localhost:8083", "distribution-api-key", "X-API-Key");
        var sourceServices = new ApplicationProperties.SourceServices(
                orderService, collectionService, distributionService, 5000, 3000, 5000);
        when(applicationProperties.sourceServices()).thenReturn(sourceServices);

        var databaseRetry = new ApplicationProperties.Database.Retry(true, 5, 1000L, 2.0, 30000L);
        var database = new ApplicationProperties.Database(databaseRetry);
        when(applicationProperties.database()).thenReturn(database);

        var deadLetter = new ApplicationProperties.Kafka.DeadLetter(true, ".DLT", 5, 1000L);
        var kafka = new ApplicationProperties.Kafka(deadLetter, null);
        when(applicationProperties.kafka()).thenReturn(kafka);

        var processing = new ApplicationProperties.Exception.Processing(100, 30000L, 10);
        var retry = new ApplicationProperties.Exception.Retry(5, 1000L);
        var alert = new ApplicationProperties.Exception.Alert(5, 300L);
        var cleanup = new ApplicationProperties.Exception.Cleanup(true, 90, 1000);
        var exception = new ApplicationProperties.Exception(processing, retry, alert, cleanup);
        when(applicationProperties.exception()).thenReturn(exception);

        // Environment properties for logging
        when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });
        when(environment.getProperty("spring.application.name")).thenReturn("test-app");
        when(environment.getProperty("server.port")).thenReturn("8080");
    }
}