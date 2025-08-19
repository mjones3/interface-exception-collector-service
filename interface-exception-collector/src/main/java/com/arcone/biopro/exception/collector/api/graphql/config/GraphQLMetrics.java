package com.arcone.biopro.exception.collector.api.graphql.config;

import graphql.ExecutionResult;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import graphql.execution.instrumentation.parameters.InstrumentationFieldFetchParameters;
import graphql.schema.DataFetcher;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * GraphQL metrics collection and instrumentation for monitoring query performance,
 * error rates, and usage patterns.
 */
@Slf4j
@Component
@RequiredArgsConstructor
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
    
    // Mutation metrics
    private final Counter mutationCounter;
    private final Timer mutationTimer;

    public GraphQLMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize counters and timers
        this.queryCounter = Counter.builder("graphql.query.count")
                .description("Total number of GraphQL queries executed")
                .register(meterRegistry);
                
        this.queryTimer = Timer.builder("graphql.query.duration")
                .description("GraphQL query execution duration")
                .register(meterRegistry);
                
        this.errorCounter = Counter.builder("graphql.error.count")
                .description("Total number of GraphQL errors")
                .register(meterRegistry);
                
        this.complexityCounter = Counter.builder("graphql.query.complexity")
                .description("GraphQL query complexity score")
                .register(meterRegistry);
                
        this.fieldFetchTimer = Timer.builder("graphql.field.fetch.duration")
                .description("GraphQL field fetch duration")
                .register(meterRegistry);
                
        this.subscriptionCounter = Counter.builder("graphql.subscription.count")
                .description("Total number of GraphQL subscriptions")
                .register(meterRegistry);
                
        this.subscriptionTimer = Timer.builder("graphql.subscription.duration")
                .description("GraphQL subscription execution duration")
                .register(meterRegistry);
                
        this.mutationCounter = Counter.builder("graphql.mutation.count")
                .description("Total number of GraphQL mutations executed")
                .register(meterRegistry);
                
        this.mutationTimer = Timer.builder("graphql.mutation.duration")
                .description("GraphQL mutation execution duration")
                .register(meterRegistry);
    }

    @Override
    public InstrumentationContext<ExecutionResult> beginExecution(
            InstrumentationExecutionParameters parameters,
            InstrumentationState state) {
        
        String operationName = parameters.getOperation() != null ? 
            parameters.getOperation().getName() : "anonymous";
        String operationType = getOperationType(parameters);
        
        // Add correlation ID to MDC for structured logging
        String correlationId = generateCorrelationId();
        MDC.put("correlationId", correlationId);
        MDC.put("graphql.operation", operationName);
        MDC.put("graphql.operationType", operationType);
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        log.info("GraphQL {} operation '{}' started with correlationId: {}", 
                operationType, operationName, correlationId);
        
        return new InstrumentationContext<ExecutionResult>() {
            @Override
            public void onCompleted(ExecutionResult result, Throwable t) {
                Duration duration = sample.stop(getTimerForOperationType(operationType));
                
                Tags tags = Tags.of(
                    "operation", operationName != null ? operationName : "unknown",
                    "operationType", operationType,
                    "status", result.getErrors().isEmpty() ? "success" : "error"
                );
                
                // Record metrics based on operation type
                switch (operationType.toLowerCase()) {
                    case "query":
                        queryCounter.increment(tags);
                        break;
                    case "mutation":
                        mutationCounter.increment(tags);
                        break;
                    case "subscription":
                        subscriptionCounter.increment(tags);
                        break;
                }
                
                // Record errors if any
                if (!result.getErrors().isEmpty()) {
                    result.getErrors().forEach(error -> {
                        errorCounter.increment(Tags.of(
                            "operation", operationName != null ? operationName : "unknown",
                            "errorType", error.getErrorType() != null ? 
                                error.getErrorType().toString() : "unknown"
                        ));
                    });
                    
                    log.warn("GraphQL {} operation '{}' completed with {} errors in {}ms", 
                            operationType, operationName, result.getErrors().size(), 
                            duration.toMillis());
                } else {
                    log.info("GraphQL {} operation '{}' completed successfully in {}ms", 
                            operationType, operationName, duration.toMillis());
                }
                
                // Record query complexity if available
                Object complexity = parameters.getGraphQLContext().get("queryComplexity");
                if (complexity instanceof Number) {
                    complexityCounter.increment(Tags.of(
                        "operation", operationName != null ? operationName : "unknown",
                        "complexity", String.valueOf(((Number) complexity).intValue())
                    ));
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
                        errorCounter.increment(Tags.of(
                            "operation", operationName != null ? operationName : "unknown",
                            "errorType", "async_failure"
                        ));
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
        String parentType = parameters.getField().getParentType().getName();
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        return new InstrumentationContext<Object>() {
            @Override
            public void onCompleted(Object result, Throwable t) {
                Duration duration = sample.stop(fieldFetchTimer);
                
                Tags tags = Tags.of(
                    "field", fieldName,
                    "parentType", parentType,
                    "status", t == null ? "success" : "error"
                );
                
                // Log slow field fetches (> 100ms)
                if (duration.toMillis() > 100) {
                    log.warn("Slow GraphQL field fetch: {}.{} took {}ms", 
                            parentType, fieldName, duration.toMillis());
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
            String parentType = parameters.getField().getParentType().getName();
            
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
        if (parameters.getOperation() != null && 
            parameters.getOperation().getOperation() != null) {
            return parameters.getOperation().getOperation().toString().toLowerCase();
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
        Timer.builder("graphql.business." + metricName)
                .description("Business operation: " + metricName)
                .tags(Tags.of(
                    "operation", operation,
                    "status", success ? "success" : "error"
                ))
                .register(meterRegistry)
                .record(duration);
    }

    /**
     * Record cache hit/miss metrics
     */
    public void recordCacheMetric(String cacheName, boolean hit) {
        Counter.builder("graphql.cache.access")
                .description("GraphQL cache access")
                .tags(Tags.of(
                    "cache", cacheName,
                    "result", hit ? "hit" : "miss"
                ))
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record DataLoader batch metrics
     */
    public void recordDataLoaderBatch(String loaderName, int batchSize, Duration duration) {
        Timer.builder("graphql.dataloader.batch")
                .description("DataLoader batch execution")
                .tags(Tags.of(
                    "loader", loaderName,
                    "batchSize", String.valueOf(batchSize)
                ))
                .register(meterRegistry)
                .record(duration);
    }
}