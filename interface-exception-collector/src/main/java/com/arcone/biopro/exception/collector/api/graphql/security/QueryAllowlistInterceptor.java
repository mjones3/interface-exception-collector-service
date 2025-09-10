package com.arcone.biopro.exception.collector.api.graphql.security;

import graphql.ExecutionResult;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * GraphQL query allowlist interceptor for production environments.
 * Only allows pre-approved queries to be executed based on query hash.
 */
@Component
@ConditionalOnProperty(name = "graphql.security.query-allowlist.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class QueryAllowlistInterceptor extends SimpleInstrumentation {

    private final QueryAllowlistConfig allowlistConfig;

    @Override
    public InstrumentationContext<ExecutionResult> beginExecution(
            InstrumentationExecutionParameters parameters,
            InstrumentationState state) {

        return new InstrumentationContext<ExecutionResult>() {
            @Override
            public void onDispatched(CompletableFuture<ExecutionResult> result) {
                validateQueryAllowlist(parameters);
            }

            @Override
            public void onCompleted(ExecutionResult result, Throwable t) {
                // No action needed on completion for query allowlist
            }
        };
    }

    /**
     * Validates that the query is in the allowlist.
     * Throws QueryNotAllowedException if the query is not approved.
     */
    private void validateQueryAllowlist(InstrumentationExecutionParameters parameters) {
        if (!allowlistConfig.isEnabled()) {
            return;
        }

        String query = normalizeQuery(parameters.getQuery());
        String queryHash = calculateQueryHash(query);

        Set<String> allowedHashes = allowlistConfig.getAllowedQueryHashes();

        if (!allowedHashes.contains(queryHash)) {
            log.warn("Query not in allowlist. Hash: {}, Query: {}", queryHash,
                    query.length() > 100 ? query.substring(0, 100) + "..." : query);

            throw new QueryNotAllowedException("QUERY_NOT_ALLOWED",
                    "Query not approved for production use. Hash: " + queryHash);
        }

        log.debug("Query validated against allowlist. Hash: {}", queryHash);
    }

    /**
     * Normalizes a GraphQL query by removing extra whitespace and comments.
     */
    private String normalizeQuery(String query) {
        if (query == null) {
            return "";
        }

        // Remove comments
        String normalized = query.replaceAll("#[^\r\n]*", "");

        // Normalize whitespace
        normalized = normalized.replaceAll("\\s+", " ").trim();

        return normalized;
    }

    /**
     * Calculates SHA-256 hash of the normalized query.
     */
    private String calculateQueryHash(String query) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(query.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("Unable to calculate query hash", e);
        }
    }
}