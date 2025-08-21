package com.arcone.biopro.exception.collector.api.graphql.monitoring;

import graphql.ExecutionResult;
import graphql.GraphQLError;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import graphql.language.Document;
import graphql.language.OperationDefinition;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Test class for GraphQLMetrics instrumentation.
 * Verifies that metrics are correctly recorded for GraphQL operations.
 */
@ExtendWith(MockitoExtension.class)
class GraphQLMetricsTest {

    private MeterRegistry meterRegistry;
    private GraphQLMetrics graphqlMetrics;

    @Mock
    private InstrumentationExecutionParameters executionParameters;

    @Mock
    private ExecutionResult executionResult;

    @Mock
    private OperationDefinition operationDefinition;

    @Mock
    private Document document;

    @Mock
    private GraphQLError graphqlError;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        graphqlMetrics = new GraphQLMetrics(meterRegistry);
    }

    @Test
    void shouldRecordQueryMetrics() {
        // Given
        when(executionParameters.getOperation()).thenReturn(operationDefinition);
        when(operationDefinition.getName()).thenReturn("getExceptions");
        when(operationDefinition.getOperation()).thenReturn(OperationDefinition.Operation.QUERY);
        when(executionResult.getErrors()).thenReturn(Collections.emptyList());

        // When
        InstrumentationContext<ExecutionResult> context = graphqlMetrics.beginExecution(
                executionParameters, new InstrumentationState() {
                });
        context.onCompleted(executionResult, null);

        // Then
        Counter queryCounter = meterRegistry.find("graphql_query_count_total").counter();
        assertThat(queryCounter).isNotNull();
        assertThat(queryCounter.count()).isEqualTo(1.0);

        Timer queryTimer = meterRegistry.find("graphql_query_duration_seconds").timer();
        assertThat(queryTimer).isNotNull();
        assertThat(queryTimer.count()).isEqualTo(1);
    }

    @Test
    void shouldRecordMutationMetrics() {
        // Given
        when(executionParameters.getOperation()).thenReturn(operationDefinition);
        when(operationDefinition.getName()).thenReturn("retryException");
        when(operationDefinition.getOperation()).thenReturn(OperationDefinition.Operation.MUTATION);
        when(executionResult.getErrors()).thenReturn(Collections.emptyList());

        // When
        InstrumentationContext<ExecutionResult> context = graphqlMetrics.beginExecution(
                executionParameters, new InstrumentationState() {
                });
        context.onCompleted(executionResult, null);

        // Then
        Counter mutationCounter = meterRegistry.find("graphql_mutation_count_total").counter();
        assertThat(mutationCounter).isNotNull();
        assertThat(mutationCounter.count()).isEqualTo(1.0);

        Timer mutationTimer = meterRegistry.find("graphql_mutation_duration_seconds").timer();
        assertThat(mutationTimer).isNotNull();
        assertThat(mutationTimer.count()).isEqualTo(1);
    }

    @Test
    void shouldRecordSubscriptionMetrics() {
        // Given
        when(executionParameters.getOperation()).thenReturn(operationDefinition);
        when(operationDefinition.getName()).thenReturn("exceptionUpdates");
        when(operationDefinition.getOperation()).thenReturn(OperationDefinition.Operation.SUBSCRIPTION);
        when(executionResult.getErrors()).thenReturn(Collections.emptyList());

        // When
        InstrumentationContext<ExecutionResult> context = graphqlMetrics.beginExecution(
                executionParameters, new InstrumentationState() {
                });
        context.onCompleted(executionResult, null);

        // Then
        Counter subscriptionCounter = meterRegistry.find("graphql_subscription_count_total").counter();
        assertThat(subscriptionCounter).isNotNull();
        assertThat(subscriptionCounter.count()).isEqualTo(1.0);

        Timer subscriptionTimer = meterRegistry.find("graphql_subscription_duration_seconds").timer();
        assertThat(subscriptionTimer).isNotNull();
        assertThat(subscriptionTimer.count()).isEqualTo(1);

        // Verify active subscriptions gauge is updated
        assertThat(graphqlMetrics.getActiveSubscriptionCount()).isEqualTo(1);
    }

    @Test
    void shouldRecordErrorMetrics() {
        // Given
        when(executionParameters.getOperation()).thenReturn(operationDefinition);
        when(operationDefinition.getName()).thenReturn("getExceptions");
        when(operationDefinition.getOperation()).thenReturn(OperationDefinition.Operation.QUERY);
        when(executionResult.getErrors()).thenReturn(List.of(graphqlError));
        when(graphqlError.getErrorType()).thenReturn(graphql.ErrorType.ValidationError);

        // When
        InstrumentationContext<ExecutionResult> context = graphqlMetrics.beginExecution(
                executionParameters, new InstrumentationState() {
                });
        context.onCompleted(executionResult, null);

        // Then
        Counter errorCounter = meterRegistry.find("graphql_error_count_total").counter();
        assertThat(errorCounter).isNotNull();
        assertThat(errorCounter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordBusinessMetrics() {
        // When
        graphqlMetrics.recordBusinessMetric("exception_processing", "retry",
                Duration.ofMillis(150), true);

        // Then
        Timer businessTimer = meterRegistry.find("graphql_business_exception_processing_duration_seconds").timer();
        assertThat(businessTimer).isNotNull();
        assertThat(businessTimer.count()).isEqualTo(1);

        Counter businessCounter = meterRegistry.find("graphql_business_exception_processing_total").counter();
        assertThat(businessCounter).isNotNull();
        assertThat(businessCounter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordCacheMetrics() {
        // When
        graphqlMetrics.recordCacheMetric("exception-list", true);
        graphqlMetrics.recordCacheMetric("exception-list", false);

        // Then
        Counter cacheCounter = meterRegistry.find("graphql_cache_access_total").counter();
        assertThat(cacheCounter).isNotNull();
        assertThat(cacheCounter.count()).isEqualTo(2.0);
    }

    @Test
    void shouldRecordDataLoaderMetrics() {
        // When
        graphqlMetrics.recordDataLoaderBatch("exceptionLoader", 25, Duration.ofMillis(50));

        // Then
        Timer dataLoaderTimer = meterRegistry.find("graphql_dataloader_batch_seconds").timer();
        assertThat(dataLoaderTimer).isNotNull();
        assertThat(dataLoaderTimer.count()).isEqualTo(1);

        Counter dataLoaderCounter = meterRegistry.find("graphql_dataloader_batch_total").counter();
        assertThat(dataLoaderCounter).isNotNull();
        assertThat(dataLoaderCounter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldTrackSubscriptionConnections() {
        // When
        graphqlMetrics.recordSubscriptionConnection(true);
        graphqlMetrics.recordSubscriptionConnection(true);
        graphqlMetrics.recordSubscriptionConnection(false);

        // Then
        assertThat(graphqlMetrics.getActiveSubscriptionCount()).isEqualTo(1);
    }

    @Test
    void shouldHandleAnonymousOperations() {
        // Given
        when(executionParameters.getOperation()).thenReturn(null);
        when(executionResult.getErrors()).thenReturn(Collections.emptyList());

        // When
        InstrumentationContext<ExecutionResult> context = graphqlMetrics.beginExecution(
                executionParameters, new InstrumentationState() {
                });
        context.onCompleted(executionResult, null);

        // Then - should not throw exception and should record metrics with "unknown"
        // operation
        Counter queryCounter = meterRegistry.find("graphql_query_count_total").counter();
        assertThat(queryCounter).isNotNull();
        assertThat(queryCounter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldInitializeAllRequiredMeters() {
        // Then - verify all expected meters are registered
        assertThat(meterRegistry.find("graphql_query_count_total").counter()).isNotNull();
        assertThat(meterRegistry.find("graphql_query_duration_seconds").timer()).isNotNull();
        assertThat(meterRegistry.find("graphql_mutation_count_total").counter()).isNotNull();
        assertThat(meterRegistry.find("graphql_mutation_duration_seconds").timer()).isNotNull();
        assertThat(meterRegistry.find("graphql_subscription_count_total").counter()).isNotNull();
        assertThat(meterRegistry.find("graphql_subscription_duration_seconds").timer()).isNotNull();
        assertThat(meterRegistry.find("graphql_error_count_total").counter()).isNotNull();
        assertThat(meterRegistry.find("graphql_field_fetch_duration_seconds").timer()).isNotNull();
        assertThat(meterRegistry.find("graphql_cache_access_total").counter()).isNotNull();
        assertThat(meterRegistry.find("graphql_subscription_connections_active").gauge()).isNotNull();
    }
}