package com.arcone.biopro.exception.collector.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Production-specific GraphQL security configuration.
 * Enforces strict security policies for production deployment.
 */
@Configuration
@Profile({ "production", "prod" })
@ConditionalOnProperty(name = "graphql.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class GraphQLProductionSecurityConfig {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(GraphQLProductionSecurityConfig.class);

    /**
     * Configure CORS for GraphQL endpoints in production.
     * Uses restrictive CORS policy for security.
     */
    @Bean
    @ConditionalOnProperty(name = "graphql.features.cors-enabled", havingValue = "true", matchIfMissing = true)
    public CorsConfigurationSource graphqlCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Production CORS settings - restrictive
        configuration.setAllowedOriginPatterns(getProductionAllowedOrigins());
        configuration.setAllowedMethods(Arrays.asList("POST", "GET", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "X-Correlation-ID",
                "Accept"));
        configuration.setExposedHeaders(Arrays.asList(
                "X-Correlation-ID",
                "X-Response-Time"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1 hour cache

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/graphql", configuration);
        source.registerCorsConfiguration("/subscriptions", configuration);

        log.info("Production CORS configuration applied for GraphQL endpoints");
        return source;
    }

    /**
     * Get production-allowed origins from environment variables.
     * Defaults to localhost for development if not configured.
     */
    private List<String> getProductionAllowedOrigins() {
        String allowedOrigins = System.getenv("GRAPHQL_ALLOWED_ORIGINS");
        if (allowedOrigins != null && !allowedOrigins.trim().isEmpty()) {
            return Arrays.asList(allowedOrigins.split(","));
        }

        // Default restrictive origins for production
        return Arrays.asList(
                "https://*.yourdomain.com",
                "https://yourdomain.com");
    }

    /**
     * Production GraphQL request validation.
     */
    @Bean
    @ConditionalOnProperty(name = "graphql.features.query-allowlist-enabled", havingValue = "true")
    public GraphQLRequestValidator graphqlRequestValidator() {
        return new GraphQLRequestValidator();
    }

    /**
     * Custom request validator for production GraphQL requests.
     */
    public static class GraphQLRequestValidator {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GraphQLRequestValidator.class);
        private static final int MAX_QUERY_LENGTH = 10000; // 10KB max query size
        private static final int MAX_VARIABLES_SIZE = 5000; // 5KB max variables size

        /**
         * Validate GraphQL request for production security.
         */
        public boolean validateRequest(String query, Object variables) {
            // Check query length
            if (query != null && query.length() > MAX_QUERY_LENGTH) {
                log.warn("GraphQL query exceeds maximum length: {} characters", query.length());
                return false;
            }

            // Check variables size
            if (variables != null) {
                String variablesStr = variables.toString();
                if (variablesStr.length() > MAX_VARIABLES_SIZE) {
                    log.warn("GraphQL variables exceed maximum size: {} characters", variablesStr.length());
                    return false;
                }
            }

            // Check for potentially dangerous operations
            if (query != null) {
                String lowerQuery = query.toLowerCase();
                if (lowerQuery.contains("__schema") || lowerQuery.contains("__type")) {
                    log.warn("GraphQL introspection query blocked in production");
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Production-specific GraphQL error handler.
     */
    @Bean
    public GraphQLProductionErrorHandler graphqlProductionErrorHandler() {
        return new GraphQLProductionErrorHandler();
    }

    /**
     * Custom error handler that sanitizes error messages in production.
     */
    public static class GraphQLProductionErrorHandler {

        /**
         * Sanitize error messages for production to avoid information disclosure.
         */
        public String sanitizeErrorMessage(String originalMessage) {
            if (originalMessage == null) {
                return "An error occurred";
            }

            // Remove sensitive information from error messages
            String sanitized = originalMessage
                    .replaceAll("(?i)password", "[REDACTED]")
                    .replaceAll("(?i)token", "[REDACTED]")
                    .replaceAll("(?i)secret", "[REDACTED]")
                    .replaceAll("(?i)key", "[REDACTED]");

            // Limit error message length
            if (sanitized.length() > 200) {
                sanitized = sanitized.substring(0, 200) + "...";
            }

            return sanitized;
        }
    }
}