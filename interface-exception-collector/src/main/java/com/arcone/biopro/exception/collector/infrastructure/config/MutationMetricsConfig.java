package com.arcone.biopro.exception.collector.infrastructure.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for mutation-specific metrics and monitoring.
 */
@Configuration
public class MutationMetricsConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> mutationMetricsCustomizer() {
        return registry -> {
            // Add common tags for mutation metrics
            registry.config().commonTags(
                "service", "interface-exception-collector",
                "component", "graphql-mutations"
            );
        };
    }
}