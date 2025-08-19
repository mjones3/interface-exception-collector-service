package com.arcone.biopro.exception.collector.api.graphql.security;

import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QueryAllowlistInterceptor.
 */
@ExtendWith(MockitoExtension.class)
class QueryAllowlistInterceptorTest {

    @Mock
    private QueryAllowlistConfig allowlistConfig;

    @Mock
    private InstrumentationExecutionParameters parameters;

    private QueryAllowlistInterceptor queryAllowlistInterceptor;

    @BeforeEach
    void setUp() {
        queryAllowlistInterceptor = new QueryAllowlistInterceptor(allowlistConfig);
    }

    @Test
    void shouldAllowQueryWhenAllowlistDisabled() {
        // Given
        when(allowlistConfig.isEnabled()).thenReturn(false);
        when(parameters.getQuery()).thenReturn("query { exceptions { id } }");

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            var context = queryAllowlistInterceptor.beginExecution(parameters, null);
            context.onDispatched(null);
        });
    }

    @Test
    void shouldAllowQueryWhenHashIsInAllowlist() {
        // Given
        String query = "query { exceptions { id } }";
        String normalizedQuery = "query { exceptions { id } }";
        String queryHash = calculateExpectedHash(normalizedQuery);

        when(allowlistConfig.isEnabled()).thenReturn(true);
        when(allowlistConfig.getAllowedQueryHashes()).thenReturn(Set.of(queryHash));
        when(parameters.getQuery()).thenReturn(query);

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            var context = queryAllowlistInterceptor.beginExecution(parameters, null);
            context.onDispatched(null);
        });
    }

    @Test
    void shouldRejectQueryWhenHashNotInAllowlist() {
        // Given
        String query = "query { exceptions { id } }";
        when(allowlistConfig.isEnabled()).thenReturn(true);
        when(allowlistConfig.getAllowedQueryHashes()).thenReturn(Set.of("different_hash"));
        when(parameters.getQuery()).thenReturn(query);

        // When & Then
        assertThrows(QueryNotAllowedException.class, () -> {
            var context = queryAllowlistInterceptor.beginExecution(parameters, null);
            context.onDispatched(null);
        });
    }

    @Test
    void shouldNormalizeQueryBeforeHashing() {
        // Given - Query with extra whitespace and comments
        String queryWithWhitespace = """
                # This is a comment
                query    {
                    exceptions   {
                        id
                    }
                }
                """;
        String normalizedQuery = "query { exceptions { id } }";
        String queryHash = calculateExpectedHash(normalizedQuery);

        when(allowlistConfig.isEnabled()).thenReturn(true);
        when(allowlistConfig.getAllowedQueryHashes()).thenReturn(Set.of(queryHash));
        when(parameters.getQuery()).thenReturn(queryWithWhitespace);

        // When & Then - Should not throw exception (normalized query should match)
        assertDoesNotThrow(() -> {
            var context = queryAllowlistInterceptor.beginExecution(parameters, null);
            context.onDispatched(null);
        });
    }

    @Test
    void shouldHandleNullQuery() {
        // Given
        when(allowlistConfig.isEnabled()).thenReturn(true);
        when(allowlistConfig.getAllowedQueryHashes()).thenReturn(Set.of("some_hash"));
        when(parameters.getQuery()).thenReturn(null);

        // When & Then
        assertThrows(QueryNotAllowedException.class, () -> {
            var context = queryAllowlistInterceptor.beginExecution(parameters, null);
            context.onDispatched(null);
        });
    }

    @Test
    void shouldHandleEmptyQuery() {
        // Given
        when(allowlistConfig.isEnabled()).thenReturn(true);
        when(allowlistConfig.getAllowedQueryHashes()).thenReturn(Set.of("some_hash"));
        when(parameters.getQuery()).thenReturn("");

        // When & Then
        assertThrows(QueryNotAllowedException.class, () -> {
            var context = queryAllowlistInterceptor.beginExecution(parameters, null);
            context.onDispatched(null);
        });
    }

    /**
     * Helper method to calculate expected hash for testing.
     * This should match the logic in QueryAllowlistInterceptor.
     */
    private String calculateExpectedHash(String query) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}