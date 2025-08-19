package com.arcone.biopro.exception.collector.api.graphql.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SecurityAuditLogger.
 */
@ExtendWith(MockitoExtension.class)
class SecurityAuditLoggerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private InstrumentationExecutionParameters parameters;

    @Mock
    private ExecutionResult executionResult;

    @Mock
    private GraphQLError graphQLError;

    private SecurityAuditLogger securityAuditLogger;

    @BeforeEach
    void setUp() {
        securityAuditLogger = new SecurityAuditLogger(objectMapper);
        
        // Setup security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

@Test
    void shouldLogOperationStartForAuthenticatedUser() throws Exception {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER")));
        when(parameters.getOperation()).thenReturn("GetExceptions");
        when(parameters.getQuery()).thenReturn("query GetExceptions { exceptions { id } }");
        when(parameters.getVariables()).thenReturn(Map.of());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        var context = securityAuditLogger.beginExecution(parameters, null);
        context.onDispatched(null);

        // Then
        verify(objectMapper).writeValueAsString(argThat(auditEvent -> {
            Map<String, Object> event = (Map<String, Object>) auditEvent;
            return "GRAPHQL_OPERATION_START".equals(event.get("event")) &&
                   "testuser".equals(event.get("user_id")) &&
                   Boolean.TRUE.equals(event.get("authenticated"));
        }));
    }

    @Test
    void shouldLogOperationStartForUnauthenticatedUser() throws Exception {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);
        when(parameters.getOperation()).thenReturn("GetExceptions");
        when(parameters.getQuery()).thenReturn("query GetExceptions { exceptions { id } }");
        when(parameters.getVariables()).thenReturn(Map.of());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        var context = securityAuditLogger.beginExecution(parameters, null);
        context.onDispatched(null);

        // Then
        verify(objectMapper).writeValueAsString(argThat(auditEvent -> {
            Map<String, Object> event = (Map<String, Object>) auditEvent;
            return "GRAPHQL_OPERATION_START".equals(event.get("event")) &&
                   Boolean.FALSE.equals(event.get("authenticated"));
        }));
    }

    @Test
    void shouldLogSuccessfulOperationCompletion() throws Exception {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER")));
        when(parameters.getOperation()).thenReturn("GetExceptions");
        when(parameters.getQuery()).thenReturn("query GetExceptions { exceptions { id } }");
        when(parameters.getVariables()).thenReturn(Map.of());
        when(executionResult.getErrors()).thenReturn(List.of());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        var context = securityAuditLogger.beginExecution(parameters, null);
        context.onCompleted(executionResult, null);

        // Then
        verify(objectMapper).writeValueAsString(argThat(auditEvent -> {
            Map<String, Object> event = (Map<String, Object>) auditEvent;
            return "GRAPHQL_OPERATION_COMPLETE".equals(event.get("event")) &&
                   Boolean.TRUE.equals(event.get("success")) &&
                   Integer.valueOf(0).equals(event.get("error_count"));
        }));
    }

    @Test
    void shouldLogOperationWithErrors() throws Exception {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER")));
        when(parameters.getOperation()).thenReturn("GetExceptions");
        when(parameters.getQuery()).thenReturn("query GetExceptions { exceptions { id } }");
        when(parameters.getVariables()).thenReturn(Map.of());
        when(executionResult.getErrors()).thenReturn(List.of(graphQLError));
        when(graphQLError.getErrorType()).thenReturn(graphql.ErrorType.ValidationError);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        var context = securityAuditLogger.beginExecution(parameters, null);
        context.onCompleted(executionResult, null);

        // Then
        verify(objectMapper).writeValueAsString(argThat(auditEvent -> {
            Map<String, Object> event = (Map<String, Object>) auditEvent;
            return "GRAPHQL_OPERATION_COMPLETE".equals(event.get("event")) &&
                   Boolean.FALSE.equals(event.get("success")) &&
                   Integer.valueOf(1).equals(event.get("error_count"));
        }));
    }

    @Test
    void shouldLogSecurityViolationAtHigherLevel() throws Exception {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER")));
        when(parameters.getOperation()).thenReturn("GetExceptions");
        when(parameters.getQuery()).thenReturn("query GetExceptions { exceptions { id } }");
        when(parameters.getVariables()).thenReturn(Map.of());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        AccessDeniedException securityException = new AccessDeniedException("Access denied");

        // When
        var context = securityAuditLogger.beginExecution(parameters, null);
        context.onCompleted(null, securityException);

        // Then
        verify(objectMapper).writeValueAsString(argThat(auditEvent -> {
            Map<String, Object> event = (Map<String, Object>) auditEvent;
            return "GRAPHQL_OPERATION_COMPLETE".equals(event.get("event")) &&
                   Boolean.FALSE.equals(event.get("success")) &&
                   "AccessDeniedException".equals(event.get("exception_type")) &&
                   Boolean.TRUE.equals(event.get("security_violation"));
        }));
    }

    @Test
    void shouldLogRateLimitViolation() throws Exception {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER")));
        when(parameters.getOperation()).thenReturn("GetExceptions");
        when(parameters.getQuery()).thenReturn("query GetExceptions { exceptions { id } }");
        when(parameters.getVariables()).thenReturn(Map.of());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        RateLimitExceededException rateLimitException = new RateLimitExceededException("Rate limit exceeded");

        // When
        var context = securityAuditLogger.beginExecution(parameters, null);
        context.onCompleted(null, rateLimitException);

        // Then
        verify(objectMapper).writeValueAsString(argThat(auditEvent -> {
            Map<String, Object> event = (Map<String, Object>) auditEvent;
            return "GRAPHQL_OPERATION_COMPLETE".equals(event.get("event")) &&
                   Boolean.FALSE.equals(event.get("success")) &&
                   "RateLimitExceededException".equals(event.get("exception_type")) &&
                   Boolean.TRUE.equals(event.get("security_violation"));
        }));
    }

    @Test
    void shouldHandleJsonSerializationErrors() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER")));
        when(parameters.getOperation()).thenReturn("GetExceptions");
        when(parameters.getQuery()).thenReturn("query GetExceptions { exceptions { id } }");
        when(parameters.getVariables()).thenReturn(Map.of());
        
        try {
            when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("JSON error"));
        } catch (Exception e) {
            // This shouldn't happen in the test setup
        }

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            var context = securityAuditLogger.beginExecution(parameters, null);
            context.onDispatched(null);
        });
    }

    @Test
    void shouldIncludeDurationInCompletionLog() throws Exception {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER")));
        when(parameters.getOperation()).thenReturn("GetExceptions");
        when(parameters.getQuery()).thenReturn("query GetExceptions { exceptions { id } }");
        when(parameters.getVariables()).thenReturn(Map.of());
        when(executionResult.getErrors()).thenReturn(List.of());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        var context = securityAuditLogger.beginExecution(parameters, null);
        Thread.sleep(10); // Small delay to ensure duration > 0
        context.onCompleted(executionResult, null);

        // Then
        verify(objectMapper).writeValueAsString(argThat(auditEvent -> {
            Map<String, Object> event = (Map<String, Object>) auditEvent;
            Object duration = event.get("duration_ms");
            return duration instanceof Long && ((Long) duration) >= 0;
        }));
    }
}