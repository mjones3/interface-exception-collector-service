package com.arcone.biopro.exception.collector.api.graphql.config;

import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.ExecutionStrategy;
import graphql.execution.SubscriptionExecutionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.lang.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

/**
 * GraphQL execution configuration for optimizing query and subscription
 * execution.
 * Configures execution strategies, thread pools, and error handling for GraphQL
 * operations.
 */
@Configuration
@Slf4j
public class GraphQLExecutionConfig {

    /**
     * Configures the query execution strategy for optimal performance.
     * Uses AsyncExecutionStrategy with custom thread pool for non-blocking
     * execution.
     *
     * @return ExecutionStrategy for query operations
     */
    @Bean
    public ExecutionStrategy queryExecutionStrategy() {
        log.info("Configuring GraphQL query execution strategy");

        // Use AsyncExecutionStrategy for non-blocking query execution
        AsyncExecutionStrategy strategy = new AsyncExecutionStrategy();

        log.info("GraphQL query execution strategy configured: AsyncExecutionStrategy");
        return strategy;
    }

    /**
     * Configures the subscription execution strategy for real-time operations.
     * Uses SubscriptionExecutionStrategy optimized for streaming data.
     *
     * @return ExecutionStrategy for subscription operations
     */
    @Bean
    public SubscriptionExecutionStrategy subscriptionExecutionStrategy() {
        log.info("Configuring GraphQL subscription execution strategy");

        SubscriptionExecutionStrategy strategy = new SubscriptionExecutionStrategy();

        log.info("GraphQL subscription execution strategy configured");
        return strategy;
    }

    /**
     * Configures a custom thread pool for GraphQL operations.
     * Optimizes thread pool size for GraphQL workloads.
     *
     * @return ForkJoinPool configured for GraphQL operations
     */
    @Bean("graphqlExecutorPool")
    public ForkJoinPool graphqlExecutorPool() {
        int parallelism = Math.max(4, Runtime.getRuntime().availableProcessors());

        ForkJoinPool pool = new ForkJoinPool(
                parallelism,
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null,
                true // Enable async mode for better throughput
        );

        log.info("GraphQL executor pool configured with parallelism: {}", parallelism);
        return pool;
    }

    /**
     * Configures GraphQL exception resolver for proper error handling.
     * Maps application exceptions to appropriate GraphQL error types.
     *
     * @return DataFetcherExceptionResolverAdapter for exception handling
     */
    @Bean
    public DataFetcherExceptionResolverAdapter exceptionResolver() {
        return new DataFetcherExceptionResolverAdapter() {
            @Override
            protected ErrorType getErrorType(@NonNull Throwable exception) {
                log.debug("Resolving GraphQL exception: {}", exception.getClass().getSimpleName());

                // Map specific exceptions to GraphQL error types
                if (exception instanceof IllegalArgumentException) {
                    return ErrorType.BAD_REQUEST;
                } else if (exception instanceof SecurityException) {
                    return ErrorType.FORBIDDEN;
                } else if (exception instanceof java.util.concurrent.TimeoutException) {
                    return ErrorType.INTERNAL_ERROR;
                } else if (exception instanceof java.util.NoSuchElementException) {
                    return ErrorType.NOT_FOUND;
                }

                // Default to internal error for unhandled exceptions
                return ErrorType.INTERNAL_ERROR;
            }

            @Override
            protected CompletableFuture<Object> handleException(
                    @NonNull Throwable exception) {

                log.error("GraphQL execution exception: {}", exception.getMessage(), exception);

                // Return null to use default error handling
                return CompletableFuture.completedFuture(null);
            }
        };
    }
}