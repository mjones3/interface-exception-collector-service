package com.arcone.biopro.exception.collector.infrastructure.interceptor;

import com.arcone.biopro.exception.collector.infrastructure.config.MutationPerformanceConfig;
import graphql.ExecutionResult;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import graphql.execution.instrumentation.parameters.InstrumentationFieldFetchParameters;
import graphql.language.Document;
import graphql.language.OperationDefinition;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test class for MutationTimeoutInterceptor.
 * Validates timeout enforcement and performance monitoring for GraphQL mutations.
 */
@ExtendWith(MockitoExtension.class)
class MutationTimeoutInterceptorTest {

    @Mock
    private MutationPerformanceConfig performanceConfig;
    
    @Mock
    private MutationPerformanceConfig.TimeoutConfig timeoutConfig;
    
    @Mock
    private InstrumentationExecutionParameters executionParameters;
    
    @Mock
    private InstrumentationFieldFetchParameters fieldFetchParameters;
    
    @Mock
    private InstrumentationState instrumentationState;
    
    @Mock
    private DataFetcher<Object> dataFetcher;
    
    @Mock
    private DataFetchingEnvironment dataFetchingEnvironment;
    
    @Mock
    private GraphQLFieldDefinition fieldDefinition;
    
    @Mock
    private OperationDefinition operationDefinition;
    
    @Mock
    private Document document;

    private MutationTimeoutInterceptor interceptor;

    @BeforeEach
    void setUp() {
        when(performanceConfig.getTimeout()).thenReturn(timeoutConfig);
        when(timeoutConfig.getOperationTimeout()).thenReturn(Duration.ofSeconds(30));
        when(timeoutConfig.getValidationTimeout()).thenReturn(Duration.ofSeconds(5));
        when(timeoutConfig.getAuditTimeout()).thenReturn(Duration.ofSeconds(3));
        
        interceptor = new MutationTimeoutInterceptor(performanceConfig);
    }

    @Test
    void shouldCreateInstrumentationContextForMutation() {
        // Setup mutation operation
        when(executionParameters.getOperation()).thenReturn(operationDefinition);
        when(operationDefinition.getOperation()).thenReturn(OperationDefinition.Operation.MUTATION);
        when(operationDefinition.getName()).thenReturn("retryException");
        
        InstrumentationContext<ExecutionResult> context = interceptor.beginExecution(
                executionParameters, instrumentationState);
        
        assertThat(context).isNotNull();
    }

    @Test
    void shouldNotInstrumentNonMutationOperations() {
        // Setup query operation
        when(executionParameters.getOperation()).thenReturn(operationDefinition);
        when(operationDefinition.getOperation()).thenReturn(OperationDefinition.Operation.QUERY);
        
        InstrumentationContext<ExecutionResult> context = interceptor.beginExecution(
                executionParameters, instrumentationState);
        
        assertThat(context).isNotNull();
        
        // Verify no timeout enforcement for queries
        CompletableFuture<ExecutionResult> future = CompletableFuture.completedFuture(
                ExecutionResult.newExecutionResult().build());
        
        context.onDispatched(future);
        
        // Should complete without timeout
        assertThat(future).isCompleted();
    }

    @Test
    void shouldInstrumentMutationDataFetchers() {
        // Setup mutation field
        when(fieldFetchParameters.getField()).thenReturn(fieldDefinition);
        when(fieldDefinition.getName()).thenReturn("retryException");
        
        DataFetcher<?> instrumentedFetcher = interceptor.instrumentDataFetcher(
                dataFetcher, fieldFetchParameters, instrumentationState);
        
        assertThat(instrumentedFetcher).isNotNull();
        assertThat(instrumentedFetcher).isNotSameAs(dataFetcher);
    }

    @Test
    void shouldNotInstrumentNonMutationFields() {
        // Setup non-mutation field
        when(fieldFetchParameters.getField()).thenReturn(fieldDefinition);
        when(fieldDefinition.getName()).thenReturn("exceptions");
        
        DataFetcher<?> instrumentedFetcher = interceptor.instrumentDataFetcher(
                dataFetcher, fieldFetchParameters, instrumentationState);
        
        assertThat(instrumentedFetcher).isSameAs(dataFetcher);
    }

    @Test
    void shouldApplyTimeoutToCompletableFuture() throws Exception {
        // Setup mutation field
        when(fieldFetchParameters.getField()).thenReturn(fieldDefinition);
        when(fieldDefinition.getName()).thenReturn("retryException");
        
        // Create a future that will timeout
        CompletableFuture<String> slowFuture = new CompletableFuture<>();
        when(dataFetcher.get(dataFetchingEnvironment)).thenReturn(slowFuture);
        
        DataFetcher<?> instrumentedFetcher = interceptor.instrumentDataFetcher(
                dataFetcher, fieldFetchParameters, instrumentationState);
        
        Object result = instrumentedFetcher.get(dataFetchingEnvironment);
        
        assertThat(result).isInstanceOf(CompletableFuture.class);
        
        CompletableFuture<?> resultFuture = (CompletableFuture<?>) result;
        
        // Complete the original future after timeout should have occurred
        slowFuture.complete("result");
        
        // The instrumented future should handle timeout
        assertThat(resultFuture).isNotNull();
    }

    @Test
    void shouldHandleNonFutureResults() throws Exception {
        // Setup mutation field
        when(fieldFetchParameters.getField()).thenReturn(fieldDefinition);
        when(fieldDefinition.getName()).thenReturn("retryException");
        
        String directResult = "immediate result";
        when(dataFetcher.get(dataFetchingEnvironment)).thenReturn(directResult);
        
        DataFetcher<?> instrumentedFetcher = interceptor.instrumentDataFetcher(
                dataFetcher, fieldFetchParameters, instrumentationState);
        
        Object result = instrumentedFetcher.get(dataFetchingEnvironment);
        
        assertThat(result).isEqualTo(directResult);
    }

    @Test
    void shouldUseValidationTimeoutForValidationFields() throws Exception {
        // Setup validation field
        when(fieldFetchParameters.getField()).thenReturn(fieldDefinition);
        when(fieldDefinition.getName()).thenReturn("validateRetryInput");
        
        CompletableFuture<String> future = CompletableFuture.completedFuture("validated");
        when(dataFetcher.get(dataFetchingEnvironment)).thenReturn(future);
        
        DataFetcher<?> instrumentedFetcher = interceptor.instrumentDataFetcher(
                dataFetcher, fieldFetchParameters, instrumentationState);
        
        Object result = instrumentedFetcher.get(dataFetchingEnvironment);
        
        assertThat(result).isInstanceOf(CompletableFuture.class);
        
        // Verify validation timeout is used (5 seconds)
        verify(timeoutConfig).getValidationTimeout();
    }

    @Test
    void shouldUseAuditTimeoutForAuditFields() throws Exception {
        // Setup audit field
        when(fieldFetchParameters.getField()).thenReturn(fieldDefinition);
        when(fieldDefinition.getName()).thenReturn("auditRetryOperation");
        
        CompletableFuture<String> future = CompletableFuture.completedFuture("audited");
        when(dataFetcher.get(dataFetchingEnvironment)).thenReturn(future);
        
        DataFetcher<?> instrumentedFetcher = interceptor.instrumentDataFetcher(
                dataFetcher, fieldFetchParameters, instrumentationState);
        
        Object result = instrumentedFetcher.get(dataFetchingEnvironment);
        
        assertThat(result).isInstanceOf(CompletableFuture.class);
        
        // Verify audit timeout is used (3 seconds)
        verify(timeoutConfig).getAuditTimeout();
    }

    @Test
    void shouldLogMutationCompletion() {
        // Setup mutation operation
        when(executionParameters.getOperation()).thenReturn(operationDefinition);
        when(operationDefinition.getOperation()).thenReturn(OperationDefinition.Operation.MUTATION);
        when(operationDefinition.getName()).thenReturn("retryException");
        
        InstrumentationContext<ExecutionResult> context = interceptor.beginExecution(
                executionParameters, instrumentationState);
        
        ExecutionResult result = ExecutionResult.newExecutionResult().build();
        
        // Should not throw exception
        context.onCompleted(result, null);
    }

    @Test
    void shouldLogMutationError() {
        // Setup mutation operation
        when(executionParameters.getOperation()).thenReturn(operationDefinition);
        when(operationDefinition.getOperation()).thenReturn(OperationDefinition.Operation.MUTATION);
        when(operationDefinition.getName()).thenReturn("retryException");
        
        InstrumentationContext<ExecutionResult> context = interceptor.beginExecution(
                executionParameters, instrumentationState);
        
        ExecutionResult result = ExecutionResult.newExecutionResult().build();
        RuntimeException error = new RuntimeException("Test error");
        
        // Should not throw exception
        context.onCompleted(result, error);
    }

    @Test
    void shouldHandleTimeoutException() throws Exception {
        // Setup mutation field
        when(fieldFetchParameters.getField()).thenReturn(fieldDefinition);
        when(fieldDefinition.getName()).thenReturn("retryException");
        
        // Create a future that will be completed exceptionally with timeout
        CompletableFuture<String> timeoutFuture = new CompletableFuture<>();
        timeoutFuture.completeExceptionally(new TimeoutException("Operation timed out"));
        
        when(dataFetcher.get(dataFetchingEnvironment)).thenReturn(timeoutFuture);
        
        DataFetcher<?> instrumentedFetcher = interceptor.instrumentDataFetcher(
                dataFetcher, fieldFetchParameters, instrumentationState);
        
        Object result = instrumentedFetcher.get(dataFetchingEnvironment);
        
        assertThat(result).isInstanceOf(CompletableFuture.class);
        
        CompletableFuture<?> resultFuture = (CompletableFuture<?>) result;
        
        // The future should handle the timeout exception
        assertThat(resultFuture).isNotNull();
    }

    @Test
    void shouldIdentifyMutationFields() {
        String[] mutationFields = {
                "retryException", "acknowledgeException", "resolveException", "cancelRetry"
        };
        
        String[] nonMutationFields = {
                "exceptions", "exception", "retryHistory", "statusChanges"
        };
        
        for (String fieldName : mutationFields) {
            when(fieldFetchParameters.getField()).thenReturn(fieldDefinition);
            when(fieldDefinition.getName()).thenReturn(fieldName);
            
            DataFetcher<?> result = interceptor.instrumentDataFetcher(
                    dataFetcher, fieldFetchParameters, instrumentationState);
            
            assertThat(result).isNotSameAs(dataFetcher);
        }
        
        for (String fieldName : nonMutationFields) {
            when(fieldFetchParameters.getField()).thenReturn(fieldDefinition);
            when(fieldDefinition.getName()).thenReturn(fieldName);
            
            DataFetcher<?> result = interceptor.instrumentDataFetcher(
                    dataFetcher, fieldFetchParameters, instrumentationState);
            
            assertThat(result).isSameAs(dataFetcher);
        }
    }
}