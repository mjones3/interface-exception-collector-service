package com.arcone.biopro.exception.collector.infrastructure.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.junit.jupiter.api.Test;
// Object import removed
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for MutationMetricsConfig.
 * Tests Micrometer and Prometheus configuration for mutation metrics.
 */
@SpringBootTest(classes = {MutationMetricsConfig.class, MutationMetricsConfigTest.TestConfig.class})
@TestPropertySource(properties = {
        "management.metrics.export.prometheus.enabled=true",
        "management.endpoints.web.exposure.include=prometheus,metrics"
})
class MutationMetricsConfigTest {

    @TestConfiguration
    static class TestConfig {
        // Test configuration if needed
    }

    @Test
    void shouldCreateMutationMetricsCustomizer() {
        // Given
        MutationMetricsConfig config = new MutationMetricsConfig();

        // When
        Object<MeterRegistry> customizer = config.mutationMetricsCustomizer();

        // Then
        assertThat(customizer).isNotNull();
    }

    @Test
    void shouldCreatePrometheusMeterRegistry() {
        // Given
        MutationMetricsConfig config = new MutationMetricsConfig();

        // When
        PrometheusMeterRegistry registry = config.prometheusMeterRegistry();

        // Then
        assertThat(registry).isNotNull();
        assertThat(registry.getPrometheusRegistry()).isNotNull();
    }

    @Test
    void shouldCreatePrometheusMetricsCustomizer() {
        // Given
        MutationMetricsConfig config = new MutationMetricsConfig();

        // When
        Object<PrometheusMeterRegistry> customizer = config.prometheusMetricsCustomizer();

        // Then
        assertThat(customizer).isNotNull();
    }
}
