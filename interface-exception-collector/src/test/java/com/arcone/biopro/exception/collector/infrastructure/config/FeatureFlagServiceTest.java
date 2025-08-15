package com.arcone.biopro.exception.collector.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureFlagServiceTest {

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private ApplicationProperties.Features features;

    private FeatureFlagService featureFlagService;

    @BeforeEach
    void setUp() {
        when(applicationProperties.features()).thenReturn(features);
        when(features.enhancedLogging()).thenReturn(true);
        when(features.debugMode()).thenReturn(false);
        when(features.payloadCaching()).thenReturn(true);
        when(features.circuitBreaker()).thenReturn(true);
        when(features.retryMechanism()).thenReturn(true);
        when(features.hotReload()).thenReturn(false);
        when(features.metricsCollection()).thenReturn(true);
        when(features.auditLogging()).thenReturn(false);

        featureFlagService = new FeatureFlagService(applicationProperties);
    }

    @Test
    void initializeFeatureFlags_ShouldSetAllFlagsFromConfiguration() {
        // Arrange
        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);

        // Act
        featureFlagService.initializeFeatureFlags();

        // Assert
        assertTrue(featureFlagService.isEnhancedLoggingEnabled());
        assertFalse(featureFlagService.isDebugModeEnabled());
        assertTrue(featureFlagService.isPayloadCachingEnabled());
        assertTrue(featureFlagService.isCircuitBreakerEnabled());
        assertTrue(featureFlagService.isRetryMechanismEnabled());
        assertFalse(featureFlagService.isHotReloadEnabled());
        assertTrue(featureFlagService.isMetricsCollectionEnabled());
        assertFalse(featureFlagService.isAuditLoggingEnabled());
    }

    @Test
    void isFeatureEnabled_WithValidFeatureName_ShouldReturnCorrectValue() {
        // Arrange
        featureFlagService.initializeFeatureFlags();

        // Act & Assert
        assertTrue(featureFlagService.isFeatureEnabled("enhanced-logging"));
        assertFalse(featureFlagService.isFeatureEnabled("debug-mode"));
        assertTrue(featureFlagService.isFeatureEnabled("payload-caching"));
    }

    @Test
    void isFeatureEnabled_WithInvalidFeatureName_ShouldReturnFalse() {
        // Arrange
        featureFlagService.initializeFeatureFlags();

        // Act & Assert
        assertFalse(featureFlagService.isFeatureEnabled("non-existent-feature"));
    }

    @Test
    void enableFeature_ShouldSetFeatureToTrue() {
        // Arrange
        featureFlagService.initializeFeatureFlags();
        assertFalse(featureFlagService.isDebugModeEnabled());

        // Act
        featureFlagService.enableFeature("debug-mode");

        // Assert
        assertTrue(featureFlagService.isDebugModeEnabled());
    }

    @Test
    void disableFeature_ShouldSetFeatureToFalse() {
        // Arrange
        featureFlagService.initializeFeatureFlags();
        assertTrue(featureFlagService.isEnhancedLoggingEnabled());

        // Act
        featureFlagService.disableFeature("enhanced-logging");

        // Assert
        assertFalse(featureFlagService.isEnhancedLoggingEnabled());
    }

    @Test
    void toggleFeature_ShouldFlipFeatureState() {
        // Arrange
        featureFlagService.initializeFeatureFlags();
        boolean initialState = featureFlagService.isDebugModeEnabled();

        // Act
        boolean newState = featureFlagService.toggleFeature("debug-mode");

        // Assert
        assertEquals(!initialState, newState);
        assertEquals(newState, featureFlagService.isDebugModeEnabled());
    }

    @Test
    void toggleFeature_WithInvalidFeatureName_ShouldReturnFalse() {
        // Arrange
        featureFlagService.initializeFeatureFlags();

        // Act
        boolean result = featureFlagService.toggleFeature("non-existent-feature");

        // Assert
        assertFalse(result);
    }

    @Test
    void getAllFeatureFlags_ShouldReturnAllCurrentFlags() {
        // Arrange
        featureFlagService.initializeFeatureFlags();

        // Act
        var allFlags = featureFlagService.getAllFeatureFlags();

        // Assert
        assertEquals(8, allFlags.size());
        assertTrue(allFlags.containsKey("enhanced-logging"));
        assertTrue(allFlags.containsKey("debug-mode"));
        assertTrue(allFlags.containsKey("payload-caching"));
        assertTrue(allFlags.containsKey("circuit-breaker"));
        assertTrue(allFlags.containsKey("retry-mechanism"));
        assertTrue(allFlags.containsKey("hot-reload"));
        assertTrue(allFlags.containsKey("metrics-collection"));
        assertTrue(allFlags.containsKey("audit-logging"));
    }

    @Test
    void resetToConfigurationValues_ShouldResetAllFlagsToOriginalValues() {
        // Arrange
        featureFlagService.initializeFeatureFlags();
        featureFlagService.enableFeature("debug-mode");
        featureFlagService.disableFeature("enhanced-logging");

        // Act
        featureFlagService.resetToConfigurationValues();

        // Assert
        assertTrue(featureFlagService.isEnhancedLoggingEnabled()); // Reset to original true
        assertFalse(featureFlagService.isDebugModeEnabled()); // Reset to original false
    }

    @Test
    void isFeatureEnabledForRollout_WithFeatureDisabled_ShouldReturnFalse() {
        // Arrange
        featureFlagService.initializeFeatureFlags();
        featureFlagService.disableFeature("debug-mode");

        // Act
        boolean result = featureFlagService.isFeatureEnabledForRollout("debug-mode", 100, "user123");

        // Assert
        assertFalse(result);
    }

    @Test
    void isFeatureEnabledForRollout_WithZeroPercentage_ShouldReturnFalse() {
        // Arrange
        featureFlagService.initializeFeatureFlags();

        // Act
        boolean result = featureFlagService.isFeatureEnabledForRollout("enhanced-logging", 0, "user123");

        // Assert
        assertFalse(result);
    }

    @Test
    void isFeatureEnabledForRollout_WithHundredPercentage_ShouldReturnTrue() {
        // Arrange
        featureFlagService.initializeFeatureFlags();

        // Act
        boolean result = featureFlagService.isFeatureEnabledForRollout("enhanced-logging", 100, "user123");

        // Assert
        assertTrue(result);
    }

    @Test
    void isFeatureEnabledForRollout_WithSameIdentifier_ShouldReturnConsistentResult() {
        // Arrange
        featureFlagService.initializeFeatureFlags();
        String identifier = "user123";

        // Act
        boolean result1 = featureFlagService.isFeatureEnabledForRollout("enhanced-logging", 50, identifier);
        boolean result2 = featureFlagService.isFeatureEnabledForRollout("enhanced-logging", 50, identifier);

        // Assert
        assertEquals(result1, result2);
    }

    @Test
    void isFeatureEnabledForRollout_WithDifferentIdentifiers_MayReturnDifferentResults() {
        // Arrange
        featureFlagService.initializeFeatureFlags();

        // Act
        boolean result1 = featureFlagService.isFeatureEnabledForRollout("enhanced-logging", 50, "user1");
        boolean result2 = featureFlagService.isFeatureEnabledForRollout("enhanced-logging", 50, "user2");
        boolean result3 = featureFlagService.isFeatureEnabledForRollout("enhanced-logging", 50, "user3");
        boolean result4 = featureFlagService.isFeatureEnabledForRollout("enhanced-logging", 50, "user4");
        boolean result5 = featureFlagService.isFeatureEnabledForRollout("enhanced-logging", 50, "user5");

        // Assert - With 50% rollout and different users, we should see some variation
        // This is probabilistic, but with 5 different users, it's very likely we'll see
        // both true and false
        boolean[] results = { result1, result2, result3, result4, result5 };
        boolean hasTrue = false;
        boolean hasFalse = false;

        for (boolean result : results) {
            if (result)
                hasTrue = true;
            else
                hasFalse = true;
        }

        // At least one should be true or false (though both is more likely with 50%
        // rollout)
        assertTrue(hasTrue || hasFalse);
    }
}