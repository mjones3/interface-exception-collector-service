package com.arcone.biopro.exception.collector.infrastructure.config;

import com.arcone.biopro.exception.collector.api.graphql.logging.GraphQLLoggingInstrumentation;
import com.arcone.biopro.exception.collector.api.graphql.monitoring.GraphQLMetrics;
import com.arcone.biopro.exception.collector.api.graphql.monitoring.GraphQLMonitoringInstrumentation;
import com.arcone.biopro.exception.collector.api.graphql.security.SecurityAuditLogger;
import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import graphql.scalars.ExtendedScalars;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoaderRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.DataLoaderRegistrar;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * GraphQL configuration class that sets up query complexity limits,
 * custom scalars, DataLoader integration, and security controls.
 * Uses feature flags to enable/disable GraphQL functionality.
 */
@Configuration
@ConditionalOnProperty(name = "graphql.features.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class GraphQLConfig {

    private final com.arcone.biopro.exception.collector.api.graphql.config.DataLoaderConfig dataLoaderConfig;
    private final SecurityAuditLogger securityAuditLogger;
    private final GraphQLLoggingInstrumentation loggingInstrumentation;
    private final GraphQLMonitoringInstrumentation monitoringInstrumentation;
    private final GraphQLMetrics graphqlMetrics;
    private final GraphQLFeatureProperties featureProperties;

    @Value("${graphql.max-query-complexity:1000}")
    private int maxQueryComplexity;

    @Value("${graphql.max-query-depth:10}")
    private int maxQueryDepth;

    /**
     * Creates and configures GraphQL instrumentation chain.
     * Sets up query complexity analysis, depth analysis, security audit logging,
     * comprehensive logging, and monitoring/alerting based on feature flags.
     *
     * @return ChainedInstrumentation with all configured instrumentations
     */
    @Bean
    public Instrumentation graphQLInstrumentation() {
        log.info(
                "Configuring GraphQL instrumentation chain with security, logging, monitoring and performance controls");

        List<Instrumentation> instrumentations = new ArrayList<>();

        // Add comprehensive logging instrumentation first (always enabled when GraphQL
        // is enabled)
        instrumentations.add(loggingInstrumentation);
        log.info("GraphQL comprehensive logging instrumentation added");

        // Add metrics instrumentation for detailed performance monitoring
        if (featureProperties.isMetricsEnabled()) {
            instrumentations.add(graphqlMetrics);
            log.info("GraphQL metrics instrumentation added");
        }

        // Add monitoring and alerting instrumentation
        if (featureProperties.isAlertingEnabled()) {
            instrumentations.add(monitoringInstrumentation);
            log.info("GraphQL monitoring and alerting instrumentation added");
        }

        // Add security audit logging to capture all operations
        if (featureProperties.isAuditLoggingEnabled()) {
            instrumentations.add(securityAuditLogger);
            log.info("GraphQL security audit logging added");
        }

        // Add complexity instrumentation if enabled
        if (featureProperties.isComplexityAnalysisEnabled()) {
            instrumentations.add(maxQueryComplexityInstrumentation());
            log.info("Query complexity analysis enabled with max complexity: {}", maxQueryComplexity);
        }

        // Add depth instrumentation if enabled
        if (featureProperties.isDepthAnalysisEnabled()) {
            instrumentations.add(maxQueryDepthInstrumentation());
            log.info("Query depth analysis enabled with max depth: {}", maxQueryDepth);
        }

        ChainedInstrumentation chainedInstrumentation = new ChainedInstrumentation(instrumentations);
        log.info("GraphQL instrumentation chain configured with {} instrumentations", instrumentations.size());

        return chainedInstrumentation;
    }

    /**
     * Configure query complexity instrumentation to prevent resource-intensive
     * operations.
     */
    @Bean
    @ConditionalOnProperty(name = "graphql.features.complexity-analysis-enabled", havingValue = "true", matchIfMissing = true)
    public MaxQueryComplexityInstrumentation maxQueryComplexityInstrumentation() {
        MaxQueryComplexityInstrumentation instrumentation = new MaxQueryComplexityInstrumentation(maxQueryComplexity);
        log.info("Query complexity instrumentation configured with max complexity: {}", maxQueryComplexity);
        return instrumentation;
    }

    /**
     * Configure query depth instrumentation to prevent deeply nested queries.
     */
    @Bean
    @ConditionalOnProperty(name = "graphql.features.depth-analysis-enabled", havingValue = "true", matchIfMissing = true)
    public MaxQueryDepthInstrumentation maxQueryDepthInstrumentation() {
        MaxQueryDepthInstrumentation instrumentation = new MaxQueryDepthInstrumentation(maxQueryDepth);
        log.info("Query depth instrumentation configured with max depth: {}", maxQueryDepth);
        return instrumentation;
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
    @ConditionalOnProperty(name = "graphql.features.data-loader-enabled", havingValue = "true", matchIfMissing = true)
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
    @ConditionalOnProperty(name = "graphql.features.graphiql-enabled", havingValue = "true")
    public String graphiqlConfiguration() {
        // GraphiQL is automatically configured by Spring Boot when enabled
        // This bean serves as a marker for conditional configuration
        log.info("GraphiQL interface enabled for development");
        return "graphiql-enabled";
    }

}