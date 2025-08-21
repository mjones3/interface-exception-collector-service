package com.arcone.biopro.exception.collector.api.graphql.monitoring;

import graphql.ExecutionResult;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import graphql.execution.instrumentation.parameters.InstrumentationFieldFetchParameters;
import graphql.schema.DataFetcher;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * GraphQL metrics collection and instrumentation for monitoring query
 * performance,
 * error rates, and usage patterns. Integrates with existing Micrometer
 * infrastructure.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "graphql.monitoring.metrics.enabled", havingValue = "true", matchIfMissing = true)
public class GraphQLMetrics extends SimpleInstrumentation {

    private final MeterRegistry meterRegistry;

    // Query execution metrics
    private final Counter queryCounter;
    private final Timer queryTimer;
    private final Counter errorCounter;
    private final Counter complexityCounter;
    private final Timer fieldFetchTimer;

    // Subscription metrics
    private final Counter subscriptionCounter;
    private final Timer subscriptionTimer;
    private final AtomicLong activeSubscriptions = new AtomicLong(0);

    // Mutation metrics
    private final Counter mutationCounter;
    private final Timer mutationTimer;

    // Cache metrics
    private final Counter cacheAccessCounter;

    // DataLoader metrics
    private final Timer dataLoaderBatchTimer;
    private final Counter dataLoaderBatchCounter;

    public GraphQLMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize counters and timers with consistent naming for dashboard
        // compatibility
        this.queryCounter = Counter.builder("graphql_query_count_total")
                .description("Total number of GraphQL queries executed")
                .register(meterRegistry);

        this.queryTimer = Timer.builder("graphql_query_duration_seconds")
                .description("GraphQL query execution duration")
                .register(meterRegistry);

        this.errorCounter = Counter.builder("graphql_error_count_total")
                .description("Total number of GraphQL errors")
                .register(meterRegistry);

        this.complexityCounter = Counter.builder("graphql_query_complexity_total")
                .description("GraphQL query complexity score")
                .register(meterRegistry);

        this.fieldFetchTimer = Timer.builder("graphql_field_fetch_duration_seconds")
                .description("GraphQL field fetch duration")
                .register(meterRegistry);

        this.subscriptionCounter = Counter.builder("graphql_subscription_count_total")
                .description("Total number of GraphQL subscriptions")
                .register(meterRegistry);

        this.subscriptionTimer = Timer.builder("graphql_subscription_duration_seconds")
                .description("GraphQL subscription execution duration")
                .register(meterRegistry);

        this.mutationCounter = Counter.builder("graphql_mutation_count_total")
                .description("Total number of GraphQL mutations executed")
                .register(meterRegistry);

        this.mutationTimer = Timer.builder("graphql_mutation_duration_seconds")
                .description("GraphQL mutation execution duration")
                .register(meterRegistry);

        this.cacheAccessCounter = Counter.builder("graphql_cache_access_total")
                .description("GraphQL cache access")
                .register(meterRegistry);

        this.dataLoaderBatchTimer = Timer.builder("graphql_dataloader_batch_seconds")
                .description("DataLoader batch execution duration")
                .register(meterRegistry);

        this.dataLoaderBatchCounter = Counter.builder("graphql_dataloader_batch_total")
                .description("Total DataLoader batch operations")
                .register(meterRegistry);

        // Register active subscriptions gauge
        Gauge.builder("graphql_subscription_connections_active", activeSubscriptions, AtomicLong::doubleValue)
                .description("Number of active GraphQL subscription connections")
                .register(meterRegistry);

        log.info("GraphQL metrics instrumentation initialized with {} registered meters",
                meterRegistry.getMeters().size());
    }

    @Override
    public InstrumentationContext<ExecutionResult> beginExecution(
            InstrumentationExecutionParameters parameters,
            InstrumentationState state) {

        String operationName = parameters.getQuery() != null ? "query" : "anonymous";
        String operationType = getOperationType(parameters);

        // Add correlation ID to MDC for structured logging
        String correlationId = generateCorrelationId();
        MDC.put("correlationId", correlationId);
        MDC.put("graphql.operation", operationName);
        MDC.put("graphql.operationType", operationType);

        Timer.Sample sample = Timer.start(meterRegistry);

        log.debug("GraphQL {} operation '{}' started with correlationId: {}",
                operationType, operationName, correlationId);

        return new InstrumentationContext<ExecutionResult>() {
            @Override
            public void onCompleted(ExecutionResult result, Throwable t) {
                sample.stop(getTimerForOperationType(operationType));

                Tags tags = Tags.of(
                        "operation", operationName != null ? operationName : "unknown",
                        "operationType", operationType,
                        "status", result.getErrors().isEmpty() ? "success" : "error");

                // Record metrics based on operation type
                switch (operationType.toLowerCase()) {
                    case "query":
                        queryCounter.increment();
                        break;
                    case "mutation":
                        mutationCounter.increment();
                        break;
                    case "subscription":
                        subscriptionCounter.increment();
                        if (result.getErrors().isEmpty()) {
                            activeSubscriptions.incrementAndGet();
                        }
                        break;
                }

                // Record errors if any
                if (!result.getErrors().isEmpty()) {
                    result.getErrors().forEach(error -> {
                        errorCounter.increment();
                    });

                    log.warn("GraphQL {} operation '{}' completed with {} errors",
                            operationType, operationName, result.getErrors().size());
                } else {
                    log.debug("GraphQL {} operation '{}' completed successfully",
                            operationType, operationName);
                }

                // Record query complexity if available
                Object complexity = parameters.getGraphQLContext().get("queryComplexity");
                if (complexity instanceof Number) {
                    complexityCounter.increment();
                }

                // Clear MDC
                MDC.remove("correlationId");
                MDC.remove("graphql.operation");
                MDC.remove("graphql.operationType");
            }

            @Override
            public void onDispatched(CompletableFuture<ExecutionResult> result) {
                // Handle async completion
                result.whenComplete((executionResult, throwable) -> {
                    if (throwable != null) {
                        log.error("GraphQL {} operation '{}' failed asynchronously",
                                operationType, operationName, throwable);
                        errorCounter.increment();
                    }
                });
            }
        };
    }

    @Override
    public InstrumentationContext<Object> beginFieldFetch(
            InstrumentationFieldFetchParameters parameters,
            InstrumentationState state) {

        String fieldName = parameters.getField().getName();
        String parentType = "unknown";

        Timer.Sample sample = Timer.start(meterRegistry);

        return new InstrumentationContext<Object>() {
            @Override
            public void onDispatched(CompletableFuture<Object> result) {
                // Field fetch dispatched
            }

            @Override
            public void onCompleted(Object result, Throwable t) {
                sample.stop(fieldFetchTimer);

                Tags tags = Tags.of(
                        "field", fieldName,
                        "parentType", parentType,
                        "status", t == null ? "success" : "error");

                // Log slow field fetches based on configured threshold
                long slowFieldThreshold = Long.parseLong(
                        System.getProperty("graphql.monitoring.slow-field-threshold-ms", "100"));
                if (slowFieldThreshold > 0) {
                    log.debug("Field fetch completed: {}.{}", parentType, fieldName);
                }

                if (t != null) {
                    log.error("GraphQL field fetch failed: {}.{}",
                            parentType, fieldName, t);
                }
            }
        };
    }

    @Override
    public DataFetcher<?> instrumentDataFetcher(
            DataFetcher<?> dataFetcher,
            InstrumentationFieldFetchParameters parameters,
            InstrumentationState state) {

        return environment -> {
            String fieldName = parameters.getField().getName();
            String parentType = "unknown";

            // Add field context to MDC
            MDC.put("graphql.field", parentType + "." + fieldName);

            try {
                Object result = dataFetcher.get(environment);

                // Handle CompletableFuture results
                if (result instanceof CompletableFuture) {
                    return ((CompletableFuture<?>) result).whenComplete((res, throwable) -> {
                        if (throwable != null) {
                            log.error("Async data fetcher failed for field {}.{}",
                                    parentType, fieldName, throwable);
                        }
                        MDC.remove("graphql.field");
                    });
                }

                return result;
            } catch (Exception e) {
                log.error("Data fetcher failed for field {}.{}",
                        parentType, fieldName, e);
                throw e;
            } finally {
                MDC.remove("graphql.field");
            }
        };
    }

    /**
     * Get the operation type from execution parameters
     */
    private String getOperationType(InstrumentationExecutionParameters parameters) {
        if (parameters.getQuery() != null) {
            if (parameters.getQuery().contains("mutation")) {
                return "mutation";
            } else if (parameters.getQuery().contains("subscription")) {
                return "subscription";
            } else {
                return "query";
            }
        }
        return "unknown";
    }

    /**
     * Get the appropriate timer for the operation type
     */
    private Timer getTimerForOperationType(String operationType) {
        switch (operationType.toLowerCase()) {
            case "query":
                return queryTimer;
            case "mutation":
                return mutationTimer;
            case "subscription":
                return subscriptionTimer;
            default:
                return queryTimer;
        }
    }

    /**
     * Generate a unique correlation ID for request tracking
     */
    private String generateCorrelationId() {
        return java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Record custom metrics for business operations
     */
    public void recordBusinessMetric(String metricName, String operation,
            Duration duration, boolean success) {
        Timer businessTimer = Timer.builder("graphql_business_" + metricName + "_duration_seconds")
                .description("Business operation: " + metricName)
                .register(meterRegistry);
        businessTimer.record(duration.toMillis(), TimeUnit.MILLISECONDS);

        Counter businessCounter = Counter.builder("graphql_business_" + metricName + "_total")
                .description("Business operation count: " + metricName)
                .register(meterRegistry);
        businessCounter.increment();
    }

    /**
     * Record cache hit/miss metrics
     */
    public void recordCacheMetric(String cacheName, boolean hit) {
        cacheAccessCounter.increment();
    }

    /**
     * Record DataLoader batch metrics
     */
    public void recordDataLoaderBatch(String loaderName, int batchSize, Duration duration) {
        dataLoaderBatchTimer.record(duration.toMillis(), TimeUnit.MILLISECONDS);
        dataLoaderBatchCounter.increment();
    }

    /**
     * Record subscription connection events
     */
    public void recordSubscriptionConnection(boolean connected) {
        if (connected) {
            activeSubscriptions.incrementAndGet();
        } else {
            activeSubscriptions.decrementAndGet();
        }
    }

    /**
     * Get current active subscription count
     */
    public long getActiveSubscriptionCount() {
        return activeSubscriptions.get();
    }
}