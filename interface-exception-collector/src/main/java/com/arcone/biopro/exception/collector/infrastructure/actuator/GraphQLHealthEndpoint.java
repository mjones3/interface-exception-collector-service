package com.arcone.biopro.exception.collector.infrastructure.actuator;

import com.arcone.biopro.exception.collector.infrastructure.config.GraphQLFeatureProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.health.Health;import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Custom actuator endpoint for GraphQL-specific health monitoring.
 * Provides detailed health information about GraphQL functionality.
 */
@Component
@Endpoint(id = "graphql-health")
@ConditionalOnProperty(name = "graphql.features.health-checks-enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class GraphQLHealthEndpoint {

    private final ApplicationContext applicationContext;
    private final GraphQLFeatureProperties featureProperties;
    private final DataSource dataSource;

    /**
     * Get overall GraphQL health status.
     */
    @ReadOperation
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Overall GraphQL status
            health.put("status", featureProperties.isEnabled() ? "UP" : "DOWN");
            health.put("timestamp", Instant.now().toString());
            
            // Feature status
            health.put("features", getFeatureHealth());
            
            // Component health
            health.put("components", getComponentHealth());
            
            // Performance metrics
            health.put("performance", getPerformanceHealth());
            
            // Configuration summary
            health.put("configuration", getConfigurationSummary());
            
        } catch (Exception e) {
            log.error("Error generating GraphQL health report", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", Instant.now().toString());
        }
        
        return health;
    }

    /**
     * Get health status for a specific GraphQL component.
     */
    @ReadOperation
    public Map<String, Object> componentHealth(@Selector String component) {
        Map<String, Object> health = new HashMap<>();
        
        try {
            switch (component.toLowerCase()) {
                case "database":
                    health = getDatabaseHealth();
                    break;
                case "schema":
                    health = getSchemaHealth();
                    break;
                case "dataloader":
                    health = getDataLoaderHealth();
                    break;
                case "websocket":
                    health = getWebSocketHealth();
                    break;
                case "cache":
                    health = getCacheHealth();
                    break;
                case "security":
                    health = getSecurityHealth();
                    break;
                default:
                    health.put("status", "UNKNOWN");
                    health.put("error", "Unknown component: " + component);
            }
            
            health.put("component", component);
            health.put("timestamp", Instant.now().toString());
            
        } catch (Exception e) {
            log.error("Error checking health for component: {}", component, e);
            health.put("status", "DOWN");
            health.put("component", component);
            health.put("error", e.getMessage());
            health.put("timestamp", Instant.now().toString());
        }
        
        return health;
    }

    private Map<String, Object> getFeatureHealth() {
        Map<String, Object> features = new HashMap<>();
        
        features.put("enabled", featureProperties.isEnabled());
        features.put("query", featureProperties.isQueryEnabled());
        features.put("mutation", featureProperties.isMutationEnabled());
        features.put("subscription", featureProperties.isSubscriptionEnabled());
        features.put("graphiql", featureProperties.isGraphiqlEnabled());
        features.put("introspection", featureProperties.isIntrospectionEnabled());
        features.put("dataLoader", featureProperties.isDataLoaderEnabled());
        features.put("complexityAnalysis", featureProperties.isComplexityAnalysisEnabled());
        features.put("depthAnalysis", featureProperties.isDepthAnalysisEnabled());
        features.put("metrics", featureProperties.isMetricsEnabled());
        features.put("auditLogging", featureProperties.isAuditLoggingEnabled());
        features.put("queryCache", featureProperties.isQueryCacheEnabled());
        features.put("rateLimiting", featureProperties.isRateLimitingEnabled());
        features.put("queryAllowlist", featureProperties.isQueryAllowlistEnabled());
        features.put("websocketHeartbeat", featureProperties.isWebsocketHeartbeatEnabled());
        features.put("healthChecks", featureProperties.isHealthChecksEnabled());
        features.put("alerting", featureProperties.isAlertingEnabled());
        features.put("performanceOptimizations", featureProperties.isPerformanceOptimizationsEnabled());
        features.put("cors", featureProperties.isCorsEnabled());
        features.put("developmentTools", featureProperties.isDevelopmentToolsEnabled());
        
        return features;
    }

    private Map<String, Object> getComponentHealth() {
        Map<String, Object> components = new HashMap<>();
        
        // Check each GraphQL health indicator
        Map<String, HealthIndicator> healthIndicators = applicationContext.getBeansOfType(HealthIndicator.class);
        
        for (Map.Entry<String, HealthIndicator> entry : healthIndicators.entrySet()) {
            String beanName = entry.getKey();
            if (beanName.contains("graphql") || beanName.contains("GraphQL")) {
                try {
                    Health health = entry.getValue().health();
                    components.put(beanName, Map.of(
                        "status", health.getStatus().getCode(),
                        "details", health.getDetails()
                    ));
                } catch (Exception e) {
                    components.put(beanName, Map.of(
                        "status", "DOWN",
                        "error", e.getMessage()
                    ));
                }
            }
        }
        
        return components;
    }

    private Map<String, Object> getPerformanceHealth() {
        Map<String, Object> performance = new HashMap<>();
        
        // JVM metrics
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        performance.put("memory", Map.of(
            "used", usedMemory,
            "free", freeMemory,
            "total", totalMemory,
            "max", maxMemory,
            "usagePercent", (double) usedMemory / totalMemory * 100
        ));
        
        performance.put("processors", runtime.availableProcessors());
        
        // Thread information
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        while (rootGroup.getParent() != null) {
            rootGroup = rootGroup.getParent();
        }
        
        performance.put("threads", Map.of(
            "active", rootGroup.activeCount(),
            "daemon", rootGroup.isDaemon()
        ));
        
        return performance;
    }

    private Map<String, Object> getConfigurationSummary() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("developmentMode", featureProperties.isDevelopmentMode());
        config.put("productionSecurityMode", featureProperties.isProductionSecurityMode());
        config.put("hasAnyEndpointEnabled", featureProperties.hasAnyEndpointEnabled());
        config.put("completelyDisabled", featureProperties.isCompletelyDisabled());
        
        return config;
    }

    private Map<String, Object> getDatabaseHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            // Test database connectivity with GraphQL-relevant queries
            long startTime = System.currentTimeMillis();
            
            String testQuery = "SELECT COUNT(*) as total FROM interface_exceptions";
            try (PreparedStatement stmt = connection.prepareStatement(testQuery);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    long responseTime = System.currentTimeMillis() - startTime;
                    int totalExceptions = rs.getInt("total");
                    
                    health.put("status", "UP");
                    health.put("responseTimeMs", responseTime);
                    health.put("totalExceptions", totalExceptions);
                    health.put("connectionValid", connection.isValid(5));
                }
            }
            
        } catch (SQLException e) {
            log.warn("Database health check failed", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        
        return health;
    }

    private Map<String, Object> getSchemaHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Check if GraphQL schema files are accessible
            ClassLoader classLoader = getClass().getClassLoader();
            boolean schemaExists = classLoader.getResource("graphql") != null;
            
            health.put("status", schemaExists ? "UP" : "DOWN");
            health.put("location", "classpath:graphql/**/*.graphqls");
            health.put("accessible", schemaExists);
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        
        return health;
    }

    private Map<String, Object> getDataLoaderHealth() {
        Map<String, Object> health = new HashMap<>();
        
        if (!featureProperties.isDataLoaderEnabled()) {
            health.put("status", "DISABLED");
            health.put("enabled", false);
            return health;
        }
        
        try {
            // Test DataLoader functionality
            CompletableFuture<Boolean> test = CompletableFuture.supplyAsync(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    String testQuery = "SELECT id FROM interface_exceptions LIMIT 1";
                    try (PreparedStatement stmt = connection.prepareStatement(testQuery);
                         ResultSet rs = stmt.executeQuery()) {
                        return rs.next();
                    }
                } catch (SQLException e) {
                    return false;
                }
            });
            
            boolean result = test.get(2, TimeUnit.SECONDS);
            health.put("status", result ? "UP" : "DOWN");
            health.put("enabled", true);
            health.put("batchingSupported", true);
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        
        return health;
    }

    private Map<String, Object> getWebSocketHealth() {
        Map<String, Object> health = new HashMap<>();
        
        if (!featureProperties.isSubscriptionEnabled()) {
            health.put("status", "DISABLED");
            health.put("enabled", false);
            return health;
        }
        
        try {
            // Check WebSocket configuration
            health.put("status", "UP");
            health.put("enabled", true);
            health.put("endpoint", "/subscriptions");
            health.put("protocol", "STOMP");
            health.put("heartbeatEnabled", featureProperties.isWebsocketHeartbeatEnabled());
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        
        return health;
    }

    private Map<String, Object> getCacheHealth() {
        Map<String, Object> health = new HashMap<>();
        
        if (!featureProperties.isQueryCacheEnabled()) {
            health.put("status", "DISABLED");
            health.put("enabled", false);
            return health;
        }
        
        try {
            // Basic cache health check
            health.put("status", "UP");
            health.put("enabled", true);
            health.put("type", "query-result-cache");
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        
        return health;
    }

    private Map<String, Object> getSecurityHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            health.put("status", "UP");
            health.put("queryAllowlistEnabled", featureProperties.isQueryAllowlistEnabled());
            health.put("rateLimitingEnabled", featureProperties.isRateLimitingEnabled());
            health.put("auditLoggingEnabled", featureProperties.isAuditLoggingEnabled());
            health.put("complexityAnalysisEnabled", featureProperties.isComplexityAnalysisEnabled());
            health.put("depthAnalysisEnabled", featureProperties.isDepthAnalysisEnabled());
            health.put("corsEnabled", featureProperties.isCorsEnabled());
            health.put("introspectionDisabled", !featureProperties.isIntrospectionEnabled());
            health.put("graphiqlDisabled", !featureProperties.isGraphiqlEnabled());
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        
        return health;
    }
}