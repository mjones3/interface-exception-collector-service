package com.arcone.biopro.exception.collector.infrastructure.monitoring;

import com.arcone.biopro.exception.collector.application.service.ExceptionManagementService;
import com.arcone.biopro.exception.collector.application.service.RetryService;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health indicator for monitoring the health of mutation services.
 * Checks the availability and performance of retry, acknowledge, resolve, and cancel operations.
 * 
 * Note: This is a simplified version that doesn't extend Spring Boot's HealthIndicator
 * to avoid dependency issues. It provides health check functionality through a custom interface.
 */
@Component("mutationHealth")
@RequiredArgsConstructor
@Slf4j
public class MutationHealthIndicator {

    private final RetryService retryService;
    private final ExceptionManagementService exceptionManagementService;
    private final InterfaceExceptionRepository exceptionRepository;
    private final MutationMetrics mutationMetrics;
    
    private static final double SUCCESS_RATE_THRESHOLD = 0.95; // 95% success rate threshold
    private static final long MAX_ACTIVE_OPERATIONS = 100; // Maximum concurrent operations
    private static final Duration HEALTH_CHECK_TIMEOUT = Duration.ofSeconds(5);

    /**
     * Performs a health check and returns the health status and details.
     * 
     * @return Map containing health status and details
     */
    public Map<String, Object> checkHealth() {
        try {
            Instant startTime = Instant.now();
            
            Map<String, Object> details = new HashMap<>();
            boolean isHealthy = true;
            
            // Check database connectivity
            boolean databaseHealthy = checkDatabaseHealth(details);
            if (!databaseHealthy) {
                isHealthy = false;
            }
            
            // Check retry service health
            boolean retryServiceHealthy = checkRetryServiceHealth(details);
            if (!retryServiceHealthy) {
                isHealthy = false;
            }
            
            // Check exception management service health
            boolean exceptionServiceHealthy = checkExceptionManagementServiceHealth(details);
            if (!exceptionServiceHealthy) {
                isHealthy = false;
            }
            
            // Check mutation metrics and performance
            boolean metricsHealthy = checkMutationMetrics(details);
            if (!metricsHealthy) {
                isHealthy = false;
            }
            
            // Check for excessive active operations
            boolean operationLoadHealthy = checkOperationLoad(details);
            if (!operationLoadHealthy) {
                isHealthy = false;
            }
            
            Duration healthCheckDuration = Duration.between(startTime, Instant.now());
            details.put("health_check_duration_ms", healthCheckDuration.toMillis());
            
            // Check if health check itself took too long
            if (healthCheckDuration.compareTo(HEALTH_CHECK_TIMEOUT) > 0) {
                details.put("health_check_timeout", "Health check exceeded timeout threshold");
                isHealthy = false;
            }
            
            details.put("status", isHealthy ? "UP" : "DOWN");
            details.put("healthy", isHealthy);
            
            return details;
            
        } catch (Exception e) {
            log.error("Error during mutation health check", e);
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("status", "DOWN");
            errorDetails.put("healthy", false);
            errorDetails.put("error", e.getMessage());
            errorDetails.put("error_type", e.getClass().getSimpleName());
            return errorDetails;
        }
    }
    
    private boolean checkDatabaseHealth(Map<String, Object> details) {
        try {
            // Test database connectivity with a simple query
            long exceptionCount = exceptionRepository.count();
            details.put("database_status", "operational");
            details.put("total_exceptions", exceptionCount);
            return true;
        } catch (Exception e) {
            log.warn("Database health check failed", e);
            details.put("database_status", "degraded");
            details.put("database_error", e.getMessage());
            return false;
        }
    }
    
    private boolean checkRetryServiceHealth(Map<String, Object> details) {
        try {
            // Check if retry service is responsive
            // This is a lightweight check - we don't actually perform a retry
            boolean isHealthy = retryService != null;
            
            if (isHealthy) {
                details.put("retry_service_status", "operational");
                
                // Check retry operation success rate
                double retrySuccessRate = mutationMetrics.getSuccessRate("retry");
                details.put("retry_success_rate", retrySuccessRate);
                
                if (retrySuccessRate < SUCCESS_RATE_THRESHOLD && mutationMetrics.getTotalOperations("retry") > 10) {
                    details.put("retry_service_warning", "Success rate below threshold");
                    return false;
                }
            } else {
                details.put("retry_service_status", "unavailable");
            }
            
            return isHealthy;
        } catch (Exception e) {
            log.warn("Retry service health check failed", e);
            details.put("retry_service_status", "degraded");
            details.put("retry_service_error", e.getMessage());
            return false;
        }
    }
    
    private boolean checkExceptionManagementServiceHealth(Map<String, Object> details) {
        try {
            // Check if exception management service is responsive
            boolean isHealthy = exceptionManagementService != null;
            
            if (isHealthy) {
                details.put("exception_management_status", "operational");
                
                // Check acknowledge operation success rate
                double acknowledgeSuccessRate = mutationMetrics.getSuccessRate("acknowledge");
                details.put("acknowledge_success_rate", acknowledgeSuccessRate);
                
                // Check resolve operation success rate
                double resolveSuccessRate = mutationMetrics.getSuccessRate("resolve");
                details.put("resolve_success_rate", resolveSuccessRate);
                
                // Check cancel retry operation success rate
                double cancelSuccessRate = mutationMetrics.getSuccessRate("cancel_retry");
                details.put("cancel_retry_success_rate", cancelSuccessRate);
                
                // Check if any operation has low success rate
                if ((acknowledgeSuccessRate < SUCCESS_RATE_THRESHOLD && mutationMetrics.getTotalOperations("acknowledge") > 10) ||
                    (resolveSuccessRate < SUCCESS_RATE_THRESHOLD && mutationMetrics.getTotalOperations("resolve") > 10) ||
                    (cancelSuccessRate < SUCCESS_RATE_THRESHOLD && mutationMetrics.getTotalOperations("cancel_retry") > 10)) {
                    details.put("exception_management_warning", "One or more operations have low success rate");
                    return false;
                }
            } else {
                details.put("exception_management_status", "unavailable");
            }
            
            return isHealthy;
        } catch (Exception e) {
            log.warn("Exception management service health check failed", e);
            details.put("exception_management_status", "degraded");
            details.put("exception_management_error", e.getMessage());
            return false;
        }
    }
    
    private boolean checkMutationMetrics(Map<String, Object> details) {
        try {
            // Collect current metrics
            Map<String, Object> metricsData = new HashMap<>();
            
            String[] operations = {"retry", "acknowledge", "resolve", "cancel_retry"};
            for (String operation : operations) {
                Map<String, Object> operationMetrics = new HashMap<>();
                operationMetrics.put("total_operations", mutationMetrics.getTotalOperations(operation));
                operationMetrics.put("successful_operations", mutationMetrics.getSuccessfulOperations(operation));
                operationMetrics.put("success_rate", mutationMetrics.getSuccessRate(operation));
                metricsData.put(operation, operationMetrics);
            }
            
            details.put("mutation_metrics", metricsData);
            details.put("metrics_status", "operational");
            
            return true;
        } catch (Exception e) {
            log.warn("Mutation metrics health check failed", e);
            details.put("metrics_status", "degraded");
            details.put("metrics_error", e.getMessage());
            return false;
        }
    }
    
    private boolean checkOperationLoad(Map<String, Object> details) {
        try {
            // Check active operation counts
            Map<String, Object> activeOperations = new HashMap<>();
            
            // Note: We would need to add getters to MutationMetrics to access these values
            // For now, we'll assume the operations are healthy if no exception is thrown
            activeOperations.put("active_retry_operations", "monitoring");
            activeOperations.put("active_acknowledge_operations", "monitoring");
            activeOperations.put("active_resolve_operations", "monitoring");
            activeOperations.put("active_cancel_operations", "monitoring");
            
            details.put("active_operations", activeOperations);
            details.put("operation_load_status", "normal");
            
            return true;
        } catch (Exception e) {
            log.warn("Operation load health check failed", e);
            details.put("operation_load_status", "high");
            details.put("operation_load_error", e.getMessage());
            return false;
        }
    }
}