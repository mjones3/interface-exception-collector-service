package com.arcone.biopro.exception.collector.infrastructure.monitoring;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.dynatrace.oneagent.sdk.OneAgentSDK;
import com.dynatrace.oneagent.sdk.api.CustomServiceTracer;
import com.dynatrace.oneagent.sdk.api.IncomingWebRequestTracer;
import lombok.RequiredArgsConstructor;import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Aspect for automatic Dynatrace instrumentation of key business operations.
 * Provides distributed tracing and performance monitoring for critical
 * interface exception management operations.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "dynatrace.enabled", havingValue = "true", matchIfMissing = true)
public class DynatraceInstrumentationAspect {

    private final OneAgentSDK oneAgentSDK;
    private final DynatraceBusinessMetricsService businessMetricsService;

    /**
     * Instruments exception processing operations with Dynatrace tracing.
     */
    @Around("execution(* com.arcone.biopro.exception.collector.application.service.ExceptionProcessingService.processException(..))")
    public Object instrumentExceptionProcessing(ProceedingJoinPoint joinPoint) throws Throwable {
        if (oneAgentSDK.getCurrentState() != OneAgentSDK.State.ACTIVE) {
            return joinPoint.proceed();
        }

        Object[] args = joinPoint.getArgs();
        String transactionId = extractTransactionId(args);

        CustomServiceTracer tracer = oneAgentSDK.traceCustomService("processException", "ExceptionProcessingService")
                .addCustomAttribute("transaction_id", transactionId)
                .addCustomAttribute("operation", "exception_processing")
                .start();

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Record business metrics if result is an InterfaceException
            if (result instanceof InterfaceException) {
                InterfaceException exception = (InterfaceException) result;
                businessMetricsService.recordExceptionProcessed(exception, processingTime);
            }

            tracer.addCustomAttribute("processing_time_ms", String.valueOf(processingTime));
            tracer.end();
            
            log.debug("Instrumented exception processing for transaction: {} ({}ms)", transactionId, processingTime);
            
            return result;
        } catch (Exception e) {
            tracer.addCustomAttribute("error", e.getMessage());
            tracer.markFailed(e.getMessage());
            tracer.end();
            
            log.error("Exception processing failed for transaction: {}", transactionId, e);
            throw e;
        }
    }

    /**
     * Instruments retry operations with Dynatrace tracing.
     */
    @Around("execution(* com.arcone.biopro.exception.collector.application.service.RetryService.retryException(..))")
    public Object instrumentRetryOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        if (oneAgentSDK.getCurrentState() != OneAgentSDK.State.ACTIVE) {
            return joinPoint.proceed();
        }

        Object[] args = joinPoint.getArgs();
        String transactionId = extractTransactionId(args);

        CustomServiceTracer tracer = oneAgentSDK.traceCustomService("retryException", "RetryService")
                .addCustomAttribute("transaction_id", transactionId)
                .addCustomAttribute("operation", "retry_operation")
                .start();

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            
            long retryTime = System.currentTimeMillis() - startTime;
            boolean success = determineRetrySuccess(result);
            
            // Record business metrics if result is a RetryAttempt
            if (result instanceof RetryAttempt) {
                RetryAttempt retryAttempt = (RetryAttempt) result;
                businessMetricsService.recordRetryAttempt(retryAttempt, success, retryTime);
            }

            tracer.addCustomAttribute("retry_time_ms", String.valueOf(retryTime));
            tracer.addCustomAttribute("retry_success", String.valueOf(success));
            tracer.end();
            
            log.debug("Instrumented retry operation for transaction: {} ({}ms, success: {})", 
                    transactionId, retryTime, success);
            
            return result;
        } catch (Exception e) {
            tracer.addCustomAttribute("error", e.getMessage());
            tracer.markFailed(e.getMessage());
            tracer.end();
            
            log.error("Retry operation failed for transaction: {}", transactionId, e);
            throw e;
        }
    }

    /**
     * Instruments payload retrieval operations with Dynatrace tracing.
     */
    @Around("execution(* com.arcone.biopro.exception.collector.application.service.PayloadRetrievalService.getOriginalPayload(..))")
    public Object instrumentPayloadRetrieval(ProceedingJoinPoint joinPoint) throws Throwable {
        if (oneAgentSDK.getCurrentState() != OneAgentSDK.State.ACTIVE) {
            return joinPoint.proceed();
        }

        Object[] args = joinPoint.getArgs();
        String transactionId = extractTransactionId(args);

        CustomServiceTracer tracer = oneAgentSDK.traceCustomService("getOriginalPayload", "PayloadRetrievalService")
                .addCustomAttribute("transaction_id", transactionId)
                .addCustomAttribute("operation", "payload_retrieval")
                .start();

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            
            long retrievalTime = System.currentTimeMillis() - startTime;
            boolean success = result != null;
            
            // Extract interface type from arguments if available
            if (args.length > 0 && args[0] instanceof InterfaceException) {
                InterfaceException exception = (InterfaceException) args[0];
                businessMetricsService.recordPayloadRetrieval(
                    exception.getTransactionId(), 
                    exception.getInterfaceType(), 
                    success, 
                    retrievalTime
                );
            }

            tracer.addCustomAttribute("retrieval_time_ms", String.valueOf(retrievalTime));
            tracer.addCustomAttribute("retrieval_success", String.valueOf(success));
            tracer.end();
            
            log.debug("Instrumented payload retrieval for transaction: {} ({}ms, success: {})", 
                    transactionId, retrievalTime, success);
            
            return result;
        } catch (Exception e) {
            tracer.addCustomAttribute("error", e.getMessage());
            tracer.markFailed(e.getMessage());
            tracer.end();
            
            log.error("Payload retrieval failed for transaction: {}", transactionId, e);
            throw e;
        }
    }

    /**
     * Instruments GraphQL query operations with Dynatrace tracing.
     */
    @Around("execution(* com.arcone.biopro.exception.collector.api.graphql.resolver.*.*(..)) && @annotation(org.springframework.graphql.data.method.annotation.QueryMapping)")
    public Object instrumentGraphQLQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        if (oneAgentSDK.getCurrentState() != OneAgentSDK.State.ACTIVE) {
            return joinPoint.proceed();
        }

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        CustomServiceTracer tracer = oneAgentSDK.traceCustomService("graphql_query_" + methodName, className)
                .addCustomAttribute("query_name", methodName)
                .addCustomAttribute("resolver_class", className)
                .addCustomAttribute("operation", "graphql_query")
                .start();

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            
            long queryTime = System.currentTimeMillis() - startTime;
            
            tracer.addCustomAttribute("query_time_ms", String.valueOf(queryTime));
            tracer.addCustomAttribute("result_size", String.valueOf(getResultSize(result)));
            tracer.end();
            
            log.debug("Instrumented GraphQL query: {}.{} ({}ms)", className, methodName, queryTime);
            
            return result;
        } catch (Exception e) {
            tracer.addCustomAttribute("error", e.getMessage());
            tracer.markFailed(e.getMessage());
            tracer.end();
            
            log.error("GraphQL query failed: {}.{}", className, methodName, e);
            throw e;
        }
    }

    /**
     * Instruments GraphQL mutation operations with Dynatrace tracing.
     */
    @Around("execution(* com.arcone.biopro.exception.collector.api.graphql.resolver.*.*(..)) && @annotation(org.springframework.graphql.data.method.annotation.MutationMapping)")
    public Object instrumentGraphQLMutation(ProceedingJoinPoint joinPoint) throws Throwable {
        if (oneAgentSDK.getCurrentState() != OneAgentSDK.State.ACTIVE) {
            return joinPoint.proceed();
        }

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        CustomServiceTracer tracer = oneAgentSDK.traceCustomService("graphql_mutation_" + methodName, className)
                .addCustomAttribute("mutation_name", methodName)
                .addCustomAttribute("resolver_class", className)
                .addCustomAttribute("operation", "graphql_mutation")
                .start();

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            
            long mutationTime = System.currentTimeMillis() - startTime;
            
            tracer.addCustomAttribute("mutation_time_ms", String.valueOf(mutationTime));
            tracer.end();
            
            log.debug("Instrumented GraphQL mutation: {}.{} ({}ms)", className, methodName, mutationTime);
            
            return result;
        } catch (Exception e) {
            tracer.addCustomAttribute("error", e.getMessage());
            tracer.markFailed(e.getMessage());
            tracer.end();
            
            log.error("GraphQL mutation failed: {}.{}", className, methodName, e);
            throw e;
        }
    }

    /**
     * Instruments Kafka message processing with Dynatrace tracing.
     */
    @Around("execution(* com.arcone.biopro.exception.collector.infrastructure.messaging.*.*(..)) && @annotation(org.springframework.kafka.annotation.KafkaListener)")
    public Object instrumentKafkaMessageProcessing(ProceedingJoinPoint joinPoint) throws Throwable {
        if (oneAgentSDK.getCurrentState() != OneAgentSDK.State.ACTIVE) {
            return joinPoint.proceed();
        }

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        CustomServiceTracer tracer = oneAgentSDK.traceCustomService("kafka_message_" + methodName, className)
                .addCustomAttribute("listener_method", methodName)
                .addCustomAttribute("listener_class", className)
                .addCustomAttribute("operation", "kafka_message_processing")
                .start();

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            tracer.addCustomAttribute("processing_time_ms", String.valueOf(processingTime));
            tracer.end();
            
            log.debug("Instrumented Kafka message processing: {}.{} ({}ms)", className, methodName, processingTime);
            
            return result;
        } catch (Exception e) {
            tracer.addCustomAttribute("error", e.getMessage());
            tracer.markFailed(e.getMessage());
            tracer.end();
            
            log.error("Kafka message processing failed: {}.{}", className, methodName, e);
            throw e;
        }
    }

    /**
     * Extracts transaction ID from method arguments.
     */
    private String extractTransactionId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof InterfaceException) {
                return ((InterfaceException) arg).getTransactionId();
            } else if (arg instanceof String && arg.toString().startsWith("TXN-")) {
                return (String) arg;
            }
        }
        return "unknown";
    }

    /**
     * Determines if a retry operation was successful based on the result.
     */
    private boolean determineRetrySuccess(Object result) {
        if (result instanceof RetryAttempt) {
            RetryAttempt attempt = (RetryAttempt) result;
            return attempt.isResultSuccess();
        }
        return result != null;
    }

    /**
     * Gets the size of the result for metrics.
     */
    private int getResultSize(Object result) {
        if (result == null) {
            return 0;
        } else if (result instanceof java.util.Collection) {
            return ((java.util.Collection<?>) result).size();
        } else if (result.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(result);
        } else {
            return 1;
        }
    }
}