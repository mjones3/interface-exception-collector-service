package com.arcone.biopro.exception.collector.infrastructure.config;

import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.scalars.ExtendedScalars;
import lombok.RequiredArgsConstructor;
import org.dataloader.DataLoaderRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.DataLoaderRegistrar;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

/**
 * GraphQL configuration class that sets up query complexity limits,
 * custom scalars, DataLoader integration, and other GraphQL-specific
 * configurations.
 */
@Configuration
@RequiredArgsConstructor
public class GraphQLConfig {

    private final com.arcone.biopro.exception.collector.api.graphql.config.DataLoaderConfig dataLoaderConfig;

    /**
     * Configure query complexity instrumentation to prevent resource-intensive
     * operations.
     * Limits queries to a maximum complexity of 1000 points.
     */
    @Bean
    public MaxQueryComplexityInstrumentation maxQueryComplexityInstrumentation() {
        return new MaxQueryComplexityInstrumentation(1000);
    }

    /**
     * Configure query depth instrumentation to prevent deeply nested queries.
     * Limits queries to a maximum depth of 10 levels.
     */
    @Bean
    public MaxQueryDepthInstrumentation maxQueryDepthInstrumentation() {
        return new MaxQueryDepthInstrumentation(10);
    }

    /**
     * Configure custom scalar types for GraphQL schema.
     * Adds support for DateTime, JSON, and other extended scalars.
     */
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(ExtendedScalars.DateTime)
                .scalar(ExtendedScalars.Json)
                .scalar(ExtendedScalars.GraphQLLong)
                .scalar(ExtendedScalars.GraphQLBigDecimal)
                .scalar(ExtendedScalars.PositiveInt)
                .scalar(ExtendedScalars.NonNegativeInt);
    }

    /**
     * Configure DataLoader registrar to integrate DataLoaders with GraphQL
     * execution.
     * This ensures DataLoaders are available during GraphQL query execution.
     */
    @Bean
    public DataLoaderRegistrar dataLoaderRegistrar() {
        return (registry, context) -> {
            // Get a fresh DataLoader registry for each request
            DataLoaderRegistry requestRegistry = dataLoaderConfig.requestScopedDataLoaderRegistry();

            // Register all DataLoaders from the request-scoped registry
            requestRegistry.getKeys().forEach(key -> {
                registry.register(key, requestRegistry.getDataLoader(key));
            });
        };
    }

    /**
     * Configure GraphiQL interface for development environment only.
     * This provides an interactive GraphQL query interface for testing.
     */
    @Bean
    @ConditionalOnProperty(name = "spring.graphql.graphiql.enabled", havingValue = "true")
    public String graphiqlConfiguration() {
        // GraphiQL is automatically configured by Spring Boot when enabled
        // This bean serves as a marker for conditional configuration
        return "graphiql-enabled";
    }
}