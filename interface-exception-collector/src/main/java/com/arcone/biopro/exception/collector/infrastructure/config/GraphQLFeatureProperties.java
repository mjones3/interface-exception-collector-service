package com.arcone.biopro.exception.collector.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for GraphQL feature flags.
 * Allows enabling/disabling GraphQL features independently for gradual rollout
 * and production deployment control.
 */
@Configuration
@ConfigurationProperties(prefix = "graphql.features")
@Data
public class GraphQLFeatureProperties {

    /**
     * Master switch for all GraphQL functionality.
     * When false, disables all GraphQL endpoints and components.
     */
    private boolean enabled = true;

    /**
     * Enable/disable GraphQL query endpoint (/graphql).
     */
    private boolean queryEnabled = true;

    /**
     * Enable/disable GraphQL mutation operations.
     */
    private boolean mutationEnabled = true;

    /**
     * Enable/disable GraphQL subscription operations and WebSocket support.
     */
    private boolean subscriptionEnabled = true;

    /**
     * Enable/disable GraphiQL development interface.
     * Should be false in production environments.
     */
    private boolean graphiqlEnabled = false;

    /**
     * Enable/disable GraphQL schema introspection.
     * Should be false in production environments for security.
     */
    private boolean introspectionEnabled = false;

    /**
     * Enable/disable DataLoader functionality for N+1 query prevention.
     */
    private boolean dataLoaderEnabled = true;

    /**
     * Enable/disable query complexity analysis and limits.
     */
    private boolean complexityAnalysisEnabled = true;

    /**
     * Enable/disable query depth analysis and limits.
     */
    private boolean depthAnalysisEnabled = true;

    /**
     * Enable/disable GraphQL metrics collection and monitoring.
     */
    private boolean metricsEnabled = true;

    /**
     * Enable/disable GraphQL security audit logging.
     */
    private boolean auditLoggingEnabled = true;

    /**
     * Enable/disable GraphQL query caching.
     */
    private boolean queryCacheEnabled = true;

    /**
     * Enable/disable GraphQL rate limiting.
     */
    private boolean rateLimitingEnabled = true;

    /**
     * Enable/disable GraphQL query allowlist security feature.
     */
    private boolean queryAllowlistEnabled = false;

    /**
     * Enable/disable WebSocket heartbeat for subscriptions.
     */
    private boolean websocketHeartbeatEnabled = true;

    /**
     * Enable/disable GraphQL health checks.
     */
    private boolean healthChecksEnabled = true;

    /**
     * Enable/disable GraphQL alerting and monitoring.
     */
    private boolean alertingEnabled = true;

    /**
     * Enable/disable GraphQL performance optimizations.
     */
    private boolean performanceOptimizationsEnabled = true;

    /**
     * Enable/disable GraphQL CORS support.
     */
    private boolean corsEnabled = true;

    /**
     * Enable/disable GraphQL development tools and debugging features.
     */
    private boolean developmentToolsEnabled = false;

    /**
     * Check if GraphQL is completely disabled.
     * 
     * @return true if GraphQL should be completely disabled
     */
    public boolean isCompletelyDisabled() {
        return !enabled;
    }

    /**
     * Check if any GraphQL endpoint should be enabled.
     * 
     * @return true if at least one GraphQL endpoint should be active
     */
    public boolean hasAnyEndpointEnabled() {
        return enabled && (queryEnabled || mutationEnabled || subscriptionEnabled);
    }

    /**
     * Check if development features should be enabled.
     * 
     * @return true if development features should be active
     */
    public boolean isDevelopmentMode() {
        return graphiqlEnabled || introspectionEnabled || developmentToolsEnabled;
    }

    /**
     * Check if production security features should be enabled.
     * 
     * @return true if production security features should be active
     */
    public boolean isProductionSecurityMode() {
        return !graphiqlEnabled && !introspectionEnabled && queryAllowlistEnabled;
    }
}