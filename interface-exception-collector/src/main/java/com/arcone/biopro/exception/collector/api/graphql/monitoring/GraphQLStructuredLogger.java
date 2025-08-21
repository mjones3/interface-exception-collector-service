package com.arcone.biopro.exception.collector.api.graphql.monitoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Structured logger for GraphQL operations that integrates with existing
 * logging infrastructure and provides consistent log formatting for
 * monitoring and alerting systems.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "graphql.monitoring.logging.structured", havingValue = "true", matchIfMissing = true)
public class GraphQLStructuredLogger {

    private final ObjectMapper objectMapper;

    /**
     * Handles GraphQL operation events and logs them in structured format
     */
    @EventListener
    public void handleGraphQLOperationEvent(GraphQLOperationEvent event) {
        try {
            Map<String, Object> logEvent = createStructuredLogEvent(event);

            // Log at appropriate level based on success and performance
            if (!event.isSuccess()) {
                log.error("GraphQL Operation Failed: {}", objectMapper.writeValueAsString(logEvent));
            } else if (event.getDurationMs() > getSlowOperationThreshold()) {
                log.warn("GraphQL Operation Slow: {}", objectMapper.writeValueAsString(logEvent));
            } else {
                log.info("GraphQL Operation: {}", objectMapper.writeValueAsString(logEvent));
            }

        } catch (Exception e) {
            log.error("Error creating structured log for GraphQL operation", e);
        }
    }

    /**
     * Log GraphQL performance metrics
     */
    public void logPerformanceMetrics(String operationType, String operationName, 
                                    long durationMs, boolean success, Map<String, Object> metrics) {
        try {
            Map<String, Object> logEvent = new HashMap<>();
            logEvent.put("event_type", "graphql_performance");
            logEvent.put("timestamp", Instant.now().toString());
            logEvent.put("operation_type", operationType);
            logEvent.put("operation_name", operationName);
            logEvent.put("duration_ms", durationMs);
            logEvent.put("success", success);
            logEvent.put("service", "interface-exception-collector-service");
            logEvent.put("api_type", "graphql");
            
            // Add correlation ID from MDC if available
            String correlationId = MDC.get("correlationId");
            if (correlationId != null) {
                logEvent.put("correlation_id", correlationId);
            }
            
            // Add performance metrics
            if (metrics != null) {
                logEvent.putAll(metrics);
            }
            
            log.info("GraphQL Performance: {}", objectMapper.writeValueAsString(logEvent));
            
        } catch (Exception e) {
            log.error("Error logging GraphQL performance metrics", e);
        }
    }

/**
     * Log GraphQL security events
     */
    public void logSecurityEvent(String eventType, String operationName, String userId, 
                               String clientIp, String details) {
        try {
            Map<String, Object> logEvent = new HashMap<>();
            logEvent.put("event_type", "graphql_security");
            logEvent.put("security_event", eventType);
            logEvent.put("timestamp", Instant.now().toString());
            logEvent.put("operation_name", operationName);
            logEvent.put("user_id", userId);
            logEvent.put("client_ip", clientIp);
            logEvent.put("details", details);
            logEvent.put("service", "interface-exception-collector-service");
            logEvent.put("api_type", "graphql");
            
            // Add correlation ID from MDC if available
            String correlationId = MDC.get("correlationId");
            if (correlationId != null) {
                logEvent.put("correlation_id", correlationId);
            }
            
            log.warn("GraphQL Security Event: {}", objectMapper.writeValueAsString(logEvent));
            
        } catch (Exception e) {
            log.error("Error logging GraphQL security event", e);
        }
    }

    /**
     * Log GraphQL cache events
     */
    public void logCacheEvent(String cacheType, String operation, boolean hit, 
                            long durationMs, String key) {
        try {
            Map<String, Object> logEvent = new HashMap<>();
            logEvent.put("event_type", "graphql_cache");
            logEvent.put("timestamp", Instant.now().toString());
            logEvent.put("cache_type", cacheType);
            logEvent.put("cache_operation", operation);
            logEvent.put("cache_hit", hit);
            logEvent.put("duration_ms", durationMs);
            logEvent.put("cache_key_hash", key != null ? String.valueOf(key.hashCode()) : null);
            logEvent.put("service", "interface-exception-collector-service");
            logEvent.put("api_type", "graphql");
            
            // Add correlation ID from MDC if available
            String correlationId = MDC.get("correlationId");
            if (correlationId != null) {
                logEvent.put("correlation_id", correlationId);
            }
            
            log.debug("GraphQL Cache Event: {}", objectMapper.writeValueAsString(logEvent));
            
        } catch (Exception e) {
            log.error("Error logging GraphQL cache event", e);
        }
    }

    /**
     * Log GraphQL DataLoader events
     */
    public void logDataLoaderEvent(String loaderName, int batchSize, long durationMs, 
                                 int itemsLoaded, boolean success) {
        try {
            Map<String, Object> logEvent = new HashMap<>();
            logEvent.put("event_type", "graphql_dataloader");
            logEvent.put("timestamp", Instant.now().toString());
            logEvent.put("loader_name", loaderName);
            logEvent.put("batch_size", batchSize);
            logEvent.put("duration_ms", durationMs);
            logEvent.put("items_loaded", itemsLoaded);
            logEvent.put("success", success);
            logEvent.put("efficiency", batchSize > 0 ? (double) itemsLoaded / batchSize : 0.0);
            logEvent.put("service", "interface-exception-collector-service");
            logEvent.put("api_type", "graphql");
            
            // Add correlation ID from MDC if available
            String correlationId = MDC.get("correlationId");
            if (correlationId != null) {
                logEvent.put("correlation_id", correlationId);
            }
            
            // Log at appropriate level based on performance
            if (!success) {
                log.error("GraphQL DataLoader Failed: {}", objectMapper.writeValueAsString(logEvent));
            } else if (durationMs > 100) { // Slow DataLoader batch
                log.warn("GraphQL DataLoader Slow: {}", objectMapper.writeValueAsString(logEvent));
            } else {
                log.debug("GraphQL DataLoader: {}", objectMapper.writeValueAsString(logEvent));
            }
            
        } catch (Exception e) {
            log.error("Error logging GraphQL DataLoader event", e);
        }
    }

    /**
     * Log GraphQL subscription events
     */
    public void logSubscriptionEvent(String eventType, String operationName, 
                                   long connectionId, String clientIp, Map<String, Object> details) {
        try {
            Map<String, Object> logEvent = new HashMap<>();
            logEvent.put("event_type", "graphql_subscription");
            logEvent.put("subscription_event", eventType);
            logEvent.put("timestamp", Instant.now().toString());
            logEvent.put("operation_name", operationName);
            logEvent.put("connection_id", connectionId);
            logEvent.put("client_ip", clientIp);
            logEvent.put("service", "interface-exception-collector-service");
            logEvent.put("api_type", "graphql");
            
            if (details != null) {
                logEvent.putAll(details);
            }
            
            // Add correlation ID from MDC if available
            String correlationId = MDC.get("correlationId");
            if (correlationId != null) {
                logEvent.put("correlation_id", correlationId);
            }
            
            log.info("GraphQL Subscription: {}", objectMapper.writeValueAsString(logEvent));
            
        } catch (Exception e) {
            log.error("Error logging GraphQL subscription event", e);
        }
    }

    /**
     * Log GraphQL business events (exception processing, retries, etc.)
     */
    public void logBusinessEvent(String businessEvent, String operationType, String operationName,
                               Map<String, Object> businessData, boolean success, long durationMs) {
        try {
            Map<String, Object> logEvent = new HashMap<>();
            logEvent.put("event_type", "graphql_business");
            logEvent.put("business_event", businessEvent);
            logEvent.put("timestamp", Instant.now().toString());
            logEvent.put("operation_type", operationType);
            logEvent.put("operation_name", operationName);
            logEvent.put("success", success);
            logEvent.put("duration_ms", durationMs);
            logEvent.put("service", "interface-exception-collector-service");
            logEvent.put("api_type", "graphql");
            
            if (businessData != null) {
                logEvent.putAll(businessData);
            }
            
            // Add correlation ID from MDC if available
            String correlationId = MDC.get("correlationId");
            if (correlationId != null) {
                logEvent.put("correlation_id", correlationId);
            }
            
            if (!success) {
                log.error("GraphQL Business Event Failed: {}", objectMapper.writeValueAsString(logEvent));
            } else {
                log.info("GraphQL Business Event: {}", objectMapper.writeValueAsString(logEvent));
            }
            
        } catch (Exception e) {
            log.error("Error logging GraphQL business event", e);
        }
    }

    /**
     * Create structured log event from GraphQL operation event
     */
    private Map<String, Object> createStructuredLogEvent(GraphQLOperationEvent event) {
        Map<String, Object> logEvent = new HashMap<>();
        
        logEvent.put("event_type", "graphql_operation");
        logEvent.put("timestamp", Instant.now().toString());
        logEvent.put("operation_type", event.getOperationType());
        logEvent.put("operation_name", event.getOperationName());
        logEvent.put("query_hash", event.getQueryHash());
        logEvent.put("duration_ms", event.getDurationMs());
        logEvent.put("success", event.isSuccess());
        logEvent.put("user_id", event.getUserId());
        logEvent.put("client_ip", event.getClientIp());
        logEvent.put("service", "interface-exception-collector-service");
        logEvent.put("api_type", "graphql");
        
        // Add performance classification
        logEvent.put("performance_category", classifyPerformance(event.getDurationMs()));
        
        // Add error information if present
        if (event.getErrors() != null && !event.getErrors().isEmpty()) {
            logEvent.put("error_count", event.getErrors().size());
            logEvent.put("error_types", event.getErrors().stream()
                    .map(GraphQLOperationEvent.ErrorInfo::getType)
                    .distinct()
                    .toList());
            logEvent.put("error_classifications", event.getErrors().stream()
                    .map(GraphQLOperationEvent.ErrorInfo::getClassification)
                    .distinct()
                    .toList());
        }
        
        return logEvent;
    }

    /**
     * Classify performance based on duration
     */
    private String classifyPerformance(long durationMs) {
        if (durationMs < 100) {
            return "fast";
        } else if (durationMs < 500) {
            return "normal";
        } else if (durationMs < 2000) {
            return "slow";
        } else {
            return "very_slow";
        }
    }

    /**
     * Get slow operation threshold from configuration
     */
    private long getSlowOperationThreshold() {
        return Long.parseLong(
            System.getProperty("graphql.monitoring.logging.slow-query-threshold-ms", "1000"));
    }
}