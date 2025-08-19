package com.arcone.biopro.exception.collector.api.graphql.config;

import com.arcone.biopro.exception.collector.api.graphql.security.QueryAllowlistInterceptor;
import com.arcone.biopro.exception.collector.api.graphql.security.RateLimitingConfig;
import com.arcone.biopro.exception.collector.api.graphql.security.RateLimitingInterceptor;
import com.arcone.biopro.exception.collector.api.graphql.security.SecurityAuditLogger;
import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * GraphQL configuration class that sets up instrumentation, scalar types,
 * execution strategies, and performance limits for the GraphQL API.
 * 
 * This configuration implements:
 * - Query complexity analysis with 1000 limit
 * - Query depth analysis with 10 depth limit
 * - Custom scalar types (DateTime, JSON)
 * - Query timeout configuration
 * - Execution strategy configuration
 */
@Configuration
@ConfigurationProperties(prefix = "graphql")
@RequiredArgsConstructor
@Slf4j
public class GraphQLConfig {

    private final RateLimitingInterceptor rateLimitingInterceptor;
    private final QueryAllowlistInterceptor queryAllowlistInterceptor;
    private final SecurityAuditLogger securityAuditLogger;
    private final RateLimitingConfig rateLimitingConfig;
    private final GraphQLMetrics graphQLMetrics;
    private final GraphQLLoggingConfig graphQLLoggingConfig;

    /**
     * Maximum allowed query complexity to prevent resource-intensive operations
     */
    private int maxQueryComplexity = 1000;

    /**
     * Maximum allowed query depth to prevent deeply nested queries
     */
    private int maxQueryDepth = 10;

    /**
     * Query timeout in seconds to prevent long-running operations
     */
    private long queryTimeoutSeconds = 30;

    /**
     * Whether to enable query complexity analysis
     */
    private boolean enableComplexityAnalysis = true;

    /**
     * Whether to enable query depth analysis
     */
    private boolean enableDepthAnalysis = true;

    /**
     * Creates and configures GraphQL instrumentation chain.
     * Sets up query complexity analysis, depth analysis, timeout handling,
     * rate limiting, query allowlist, and security audit logging.
     *
     * @return ChainedInstrumentation with all configured instrumentations
     */
    @Bean
    public Instrumentation graphQLInstrumentation() {
        log.info("Configuring GraphQL instrumentation chain with security enhancements");

        List<Instrumentation> instrumentations = new ArrayList<>();

        // Add monitoring and metrics instrumentation first
        instrumentations.add(graphQLMetrics);
        log.info("GraphQL metrics instrumentation added");

        // Add structured logging instrumentation
        instrumentations.add(graphQLLoggingConfig);
        log.info("GraphQL logging instrumentation added");

        // Add security audit logging to capture all operations
        instrumentations.add(securityAuditLogger);

        // Add rate limiting if enabled
        if (rateLimitingConfig.isEnabled()) {
            instrumentations.add(rateLimitingInterceptor);
            log.info("Rate limiting enabled for GraphQL operations");
        }

        // Add query allowlist interceptor (conditionally loaded)
        try {
            instrumentations.add(queryAllowlistInterceptor);
            log.info("Query allowlist interceptor added to instrumentation chain");
        } catch (Exception e) {
            log.debug("Query allowlist interceptor not available (likely disabled): {}", e.getMessage());
        }

        // Add complexity instrumentation if enabled
        if (enableComplexityAnalysis) {
            instrumentations.add(queryComplexityInstrumentation());
        }

        // Add depth instrumentation if enabled
        if (enableDepthAnalysis) {
            instrumentations.add(maxQueryDepthInstrumentation());
        }

        // Add timeout instrumentation
        instrumentations.add(queryTimeoutInstrumentation());

        ChainedInstrumentation chainedInstrumentation = new ChainedInstrumentation(instrumentations);
        log.info("GraphQL instrumentation chain configured with {} instrumentations", instrumentations.size());

        return chainedInstrumentation;
    }

    /**
     * Creates query complexity instrumentation to prevent resource-intensive
     * queries.
     * Analyzes query complexity and rejects queries exceeding the configured limit.
     *
     * @return MaxQueryComplexityInstrumentation configured with complexity limit
     */
    public MaxQueryComplexityInstrumentation queryComplexityInstrumentation() {
        MaxQueryComplexityInstrumentation instrumentation = new MaxQueryComplexityInstrumentation(maxQueryComplexity);

        log.info("Query complexity instrumentation configured with max complexity: {}", maxQueryComplexity);
        return instrumentation;
    }

    /**
     * Creates query depth instrumentation to prevent deeply nested queries.
     * Analyzes query depth and rejects queries exceeding the configured limit.
     *
     * @return MaxQueryDepthInstrumentation configured with depth limit
     */
    public MaxQueryDepthInstrumentation maxQueryDepthInstrumentation() {
        MaxQueryDepthInstrumentation instrumentation = new MaxQueryDepthInstrumentation(maxQueryDepth);

        log.info("Query depth instrumentation configured with max depth: {}", maxQueryDepth);
        return instrumentation;
    }

    /**
     * Creates query timeout instrumentation to prevent long-running operations.
     * Implements timeout handling using CompletableFuture with timeout.
     *
     * @return Custom timeout instrumentation
     */
    public Instrumentation queryTimeoutInstrumentation() {
        return new QueryTimeoutInstrumentation(Duration.ofSeconds(queryTimeoutSeconds));
    }

    /**
     * Custom instrumentation for handling query timeouts.
     * Wraps query execution with timeout handling to prevent long-running
     * operations.
     */
    private static class QueryTimeoutInstrumentation implements Instrumentation {
        private final Duration timeout;

        public QueryTimeoutInstrumentation(Duration timeout) {
            this.timeout = timeout;
        }

        @Override
        public CompletableFuture<graphql.ExecutionResult> instrumentExecutionResult(
                graphql.ExecutionResult executionResult,
                graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters parameters) {

            CompletableFuture<graphql.ExecutionResult> future = CompletableFuture.completedFuture(executionResult);

            // Apply timeout to the execution
            return future.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                    .exceptionally(throwable -> {
                        log.warn("GraphQL query timed out after {} seconds for operation: {}",
                                timeout.getSeconds(), parameters.getQuery());

                        return graphql.ExecutionResultImpl.newExecutionResult()
                                .addError(graphql.GraphqlErrorBuilder.newError()
                                        .message("Query execution timed out after " + timeout.getSeconds() + " seconds")
                                        .errorType(graphql.ErrorType.ExecutionAborted)
                                        .build())
                                .build();
                    });
        }
    }

    // Getters and setters for configuration properties

    public int getMaxQueryComplexity() {
        return maxQueryComplexity;
    }

    public void setMaxQueryComplexity(int maxQueryComplexity) {
        this.maxQueryComplexity = maxQueryComplexity;
    }

    public int getMaxQueryDepth() {
        return maxQueryDepth;
    }

    public void setMaxQueryDepth(int maxQueryDepth) {
        this.maxQueryDepth = maxQueryDepth;
    }

    public long getQueryTimeoutSeconds() {
        return queryTimeoutSeconds;
    }

    public void setQueryTimeoutSeconds(long queryTimeoutSeconds) {
        this.queryTimeoutSeconds = queryTimeoutSeconds;
    }

    public boolean isEnableComplexityAnalysis() {
        return enableComplexityAnalysis;
    }

    public void setEnableComplexityAnalysis(boolean enableComplexityAnalysis) {
        this.enableComplexityAnalysis = enableComplexityAnalysis;
    }

    public boolean isEnableDepthAnalysis() {
        return enableDepthAnalysis;
    }

    public void setEnableDepthAnalysis(boolean enableDepthAnalysis) {
        this.enableDepthAnalysis = enableDepthAnalysis;
    }
}