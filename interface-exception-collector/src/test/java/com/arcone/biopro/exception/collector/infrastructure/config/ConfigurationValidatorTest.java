package com.arcone.biopro.exception.collector.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.when;

/**
 * Comprehensive tests for ConfigurationValidator to verify proper configuration validation,
 * error handling, and fallback mechanisms for RSocket connection parameters.
 */
@ExtendWith(MockitoExtension.class)
class ConfigurationValidatorTest {

    @Mock
    private RSocketProperties rSocketProperties;

    @Mock
    private FeatureFlagsProperties featureFlagsProperties;

    @Mock
    private Environment environment;

    @Mock
    private RSocketProperties.MockServer mockServer;

    @Mock
    private RSocketProperties.PartnerOrderService partnerOrderService;

    @Mock
    private RSocketProperties.CircuitBreaker circuitBreaker;

    @Mock
    private RSocketProperties.Retry retry;

    private ConfigurationValidator validator;

    @BeforeEach
    void setUp() {
        when(rSocketProperties.getMockServer()).thenReturn(mockServer);
        when(rSocketProperties.getPartnerOrderService()).thenReturn(partnerOrderService);
        when(mockServer.getCircuitBreaker()).thenReturn(circuitBreaker);
        when(mockServer.getRetry()).thenReturn(retry);
        
        // Set up default valid configuration
        setupValidMockServerConfiguration();
        setupValidCircuitBreakerConfiguration();
        setupValidRetryConfiguration();
        setupValidFeatureFlags();
        
        validator = new ConfigurationValidator(rSocketProperties, featureFlagsProperties, environment);
    }

    private void setupValidMockServerConfiguration() {
        when(mockServer.isEnabled()).thenReturn(true);
        when(mockServer.getHost()).thenReturn("localhost");
        when(mockServer.getPort()).thenReturn(7000);
        when(mockServer.getTimeout()).thenReturn(Duration.ofSeconds(5));
        when(mockServer.getConnectionTimeout()).thenReturn(Duration.ofSeconds(10));
        when(mockServer.getKeepAliveInterval()).thenReturn(Duration.ofSeconds(30));
        when(mockServer.getKeepAliveMaxLifetime()).thenReturn(Duration.ofSeconds(300));
        when(mockServer.isDebugLogging()).thenReturn(false);
    }

    private void setupValidCircuitBreakerConfiguration() {
        when(circuitBreaker.isEnabled()).thenReturn(true);
        when(circuitBreaker.getFailureRateThreshold()).thenReturn(50);
        when(circuitBreaker.getWaitDurationInOpenState()).thenReturn(Duration.ofSeconds(30));
        when(circuitBreaker.getSlidingWindowSize()).thenReturn(10);
        when(circuitBreaker.getMinimumNumberOfCalls()).thenReturn(5);
        when(circuitBreaker.getPermittedCallsInHalfOpen()).thenReturn(3);
    }

    private void setupValidRetryConfiguration() {
        when(retry.isEnabled()).thenReturn(true);
        when(retry.getMaxAttempts()).thenReturn(3);
        when(retry.getWaitDuration()).thenReturn(Duration.ofSeconds(1));
        when(retry.getExponentialBackoffMultiplier()).thenReturn(2.0);
    }

    private void setupValidFeatureFlags() {
        when(featureFlagsProperties.isEnhancedLogging()).thenReturn(true);
        when(featureFlagsProperties.isDebugMode()).thenReturn(false);
        when(featureFlagsProperties.isCircuitBreaker()).thenReturn(true);
        when(featureFlagsProperties.isRetryMechanism()).thenReturn(true);
        when(featureFlagsProperties.isPayloadCaching()).thenReturn(true);
        when(featureFlagsProperties.isMetricsCollection()).thenReturn(true);
        when(featureFlagsProperties.isAuditLogging()).thenReturn(true);
    }

    @Test
    void shouldThrowExceptionWhenMockServerEnabledInProduction() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(mockServer.isEnabled()).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> validator.validateConfiguration())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Mock RSocket server is enabled in production environment");
    }

    @Test
    void shouldThrowExceptionWhenNoServiceEnabled() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(mockServer.isEnabled()).thenReturn(false);
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> validator.validateConfiguration())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Neither mock RSocket server nor partner order service is enabled");
    }

    @Test
    void shouldThrowExceptionWhenMockServerHostIsEmpty() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(mockServer.isEnabled()).thenReturn(true);
        when(mockServer.getHost()).thenReturn("");
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> validator.validateConfiguration())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Mock RSocket server host cannot be empty");
    }

    @Test
    void shouldThrowExceptionWhenMockServerPortIsInvalid() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(mockServer.isEnabled()).thenReturn(true);
        when(mockServer.getHost()).thenReturn("localhost");
        when(mockServer.getPort()).thenReturn(0);
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> validator.validateConfiguration())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Mock RSocket server port must be between 1 and 65535");
    }

    @Test
    void shouldThrowExceptionWhenMockServerTimeoutIsInvalid() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(mockServer.isEnabled()).thenReturn(true);
        when(mockServer.getHost()).thenReturn("localhost");
        when(mockServer.getPort()).thenReturn(7000);
        when(mockServer.getTimeout()).thenReturn(Duration.ZERO);
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> validator.validateConfiguration())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Mock RSocket server timeout must be positive");
    }

    @Test
    void shouldValidateSuccessfullyWithValidConfiguration() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then - should not throw exception
        assertThatNoException().isThrownBy(() -> validator.validateConfiguration());
    }

    // Environment-specific validation tests
    @ParameterizedTest
    @ValueSource(strings = {"prod", "production", "PROD", "Production"})
    void shouldThrowExceptionWhenMockServerEnabledInProductionEnvironments(String profile) {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{profile});
        when(mockServer.isEnabled()).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> validator.validateConfiguration())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Mock RSocket server is enabled in production environment");
    }

    @Test
    void shouldValidateSuccessfullyInProductionWithMockServerDisabled() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(mockServer.isEnabled()).thenReturn(false);
        when(partnerOrderService.isEnabled()).thenReturn(true);
        when(partnerOrderService.getHost()).thenReturn("partner-service");
        when(partnerOrderService.getPort()).thenReturn(8090);
        when(partnerOrderService.getTimeout()).thenReturn(Duration.ofSeconds(30));
        when(partnerOrderService.getConnectionTimeout()).thenReturn(Duration.ofSeconds(20));

        // When & Then
        assertThatNoException().isThrownBy(() -> validator.validateConfiguration());
    }

    // Host validation tests
    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "invalid..host", "host..with..double..dots"})
    void shouldThrowExceptionForInvalidHosts(String invalidHost) {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(mockServer.getHost()).thenReturn(invalidHost);
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> validator.validateConfiguration())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("host");
    }

    @ParameterizedTest
    @ValueSource(strings = {"localhost", "127.0.0.1", "192.168.1.1", "example.com", "my-service"})
    void shouldValidateSuccessfullyWithValidHosts(String validHost) {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(mockServer.getHost()).thenReturn(validHost);
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then
        assertThatNoException().isThrownBy(() -> validator.validateConfiguration());
    }

    // Port validation tests
    @ParameterizedTest
    @ValueSource(ints = {0, -1, 65536, 100000})
    void shouldThrowExceptionForInvalidPorts(int invalidPort) {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(mockServer.getPort()).thenReturn(invalidPort);
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> validator.validateConfiguration())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("port must be between 1 and 65535");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 80, 443, 7000, 8080, 65535})
    void shouldValidateSuccessfullyWithValidPorts(int validPort) {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(mockServer.getPort()).thenReturn(validPort);
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then
        assertThatNoException().isThrownBy(() -> validator.validateConfiguration());
    }

    // Timeout validation tests
    @Test
    void shouldThrowExceptionForNullTimeout() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(mockServer.getTimeout()).thenReturn(null);
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> validator.validateConfiguration())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("timeout cannot be null");
    }

    @Test
    void shouldThrowExceptionForZeroTimeout() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(mockServer.getTimeout()).thenReturn(Duration.ZERO);
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> validator.validateConfiguration())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("timeout must be positive");
    }

    @Test
    void shouldThrowExceptionForNegativeTimeout() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(mockServer.getTimeout()).thenReturn(Duration.ofSeconds(-1));
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> validator.validateConfiguration())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("timeout must be positive");
    }

    // Connection timeout validation tests
    @Test
    void shouldThrowExceptionForInvalidConnectionTimeout() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(mockServer.getConnectionTimeout()).thenReturn(Duration.ofMillis(-100));
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> validator.validateConfiguration())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("connection timeout must be positive");
    }

    // Keep-alive validation tests
    @Test
    void shouldThrowExceptionForInvalidKeepAliveSettings() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(mockServer.getKeepAliveInterval()).thenReturn(Duration.ofSeconds(60));
        when(mockServer.getKeepAliveMaxLifetime()).thenReturn(Duration.ofSeconds(30)); // Less than interval
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then - Should generate warning but not fail
        assertThatNoException().isThrownBy(() -> validator.validateConfiguration());
    }

    // Circuit breaker validation tests
    @Test
    void shouldThrowExceptionForInvalidCircuitBreakerFailureRate() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(circuitBreaker.getFailureRateThreshold()).thenReturn(150); // Invalid: > 100
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> validator.validateConfiguration())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("failure rate threshold must be between 1 and 100");
    }

    @Test
    void shouldThrowExceptionForInvalidCircuitBreakerWaitDuration() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(circuitBreaker.getWaitDurationInOpenState()).thenReturn(Duration.ofSeconds(-1));
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> validator.validateConfiguration())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("wait duration must be positive");
    }

    @Test
    void shouldThrowExceptionForInvalidCircuitBreakerSlidingWindow() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(circuitBreaker.getSlidingWindowSize()).thenReturn(0);
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> validator.validateConfiguration())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("sliding window size must be positive");
    }

    // Retry validation tests
    @Test
    void shouldThrowExceptionForInvalidRetryMaxAttempts() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(retry.getMaxAttempts()).thenReturn(0);
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> validator.validateConfiguration())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("max attempts must be positive");
    }

    @Test
    void shouldThrowExceptionForInvalidRetryBackoffMultiplier() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(retry.getExponentialBackoffMultiplier()).thenReturn(0.5); // Invalid: < 1.0
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> validator.validateConfiguration())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("exponential backoff multiplier must be >= 1.0");
    }

    // Service availability validation tests
    @Test
    void shouldThrowExceptionWhenNoServiceEnabled() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(mockServer.isEnabled()).thenReturn(false);
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> validator.validateConfiguration())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Neither mock RSocket server nor partner order service is enabled");
    }

    @Test
    void shouldValidateSuccessfullyWhenBothServicesEnabledInDevelopment() {
        // Given - Both services enabled should generate warning but not fail
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(mockServer.isEnabled()).thenReturn(true);
        when(partnerOrderService.isEnabled()).thenReturn(true);
        when(partnerOrderService.getHost()).thenReturn("partner-service");
        when(partnerOrderService.getPort()).thenReturn(8090);
        when(partnerOrderService.getTimeout()).thenReturn(Duration.ofSeconds(30));
        when(partnerOrderService.getConnectionTimeout()).thenReturn(Duration.ofSeconds(20));

        // When & Then - Should not throw exception (warning only)
        assertThatNoException().isThrownBy(() -> validator.validateConfiguration());
    }

    // Feature flags validation tests
    @Test
    void shouldValidateSuccessfullyWithDisabledFeatureFlags() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(featureFlagsProperties.isCircuitBreaker()).thenReturn(false);
        when(featureFlagsProperties.isRetryMechanism()).thenReturn(false);
        when(featureFlagsProperties.isMetricsCollection()).thenReturn(false);
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then - Should not throw exception (warnings only)
        assertThatNoException().isThrownBy(() -> validator.validateConfiguration());
    }

    // Edge case tests
    @Test
    void shouldHandleNullActiveProfiles() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{});
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then - Should not throw exception
        assertThatNoException().isThrownBy(() -> validator.validateConfiguration());
    }

    @Test
    void shouldHandleMultipleActiveProfiles() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev", "local"});
        when(partnerOrderService.isEnabled()).thenReturn(false);

        // When & Then - Should not throw exception
        assertThatNoException().isThrownBy(() -> validator.validateConfiguration());
    }
}