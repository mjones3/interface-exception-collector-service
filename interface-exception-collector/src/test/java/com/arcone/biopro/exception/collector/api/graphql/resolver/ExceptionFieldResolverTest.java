package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.api.graphql.service.GraphQLSecurityService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.entity.StatusChange;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import graphql.GraphQLException;
import graphql.schema.DataFetchingEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExceptionFieldResolver.
 * Tests field-level security, validation, and error handling.
 */
@ExtendWith(MockitoExtension.class)
class ExceptionFieldResolverTest {

    @Mock
    private GraphQLSecurityService securityService;

    @Mock
    private DataFetchingEnvironment environment;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    private ExceptionFieldResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ExceptionFieldResolver(securityService);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @Test
    void testOriginalPayload_WithValidExceptionAndPermission_ShouldSucceed() {
        // Given
        InterfaceException exception = createTestException();
        when(securityService.canViewPayload(eq(exception), eq(authentication))).thenReturn(true);

        // Mock DataLoader behavior - this would normally be handled by DataLoaderUtil
        // For this test, we'll focus on the security and validation logic

        // When & Then
        assertDoesNotThrow(() -> {
            CompletableFuture<PayloadResponse> result = resolver.originalPayload(exception, environment);
            assertNotNull(result);
        });

        verify(securityService).canViewPayload(exception, authentication);
    }

    @Test
    void testOriginalPayload_WithNullException_ShouldThrowException() {
        // When & Then
        CompletableFuture<PayloadResponse> result = resolver.originalPayload(null, environment);

        assertThrows(ExecutionException.class, () -> {
            result.get();
        });
    }

    @Test
    void testOriginalPayload_WithEmptyTransactionId_ShouldThrowException() {
        // Given
        InterfaceException exception = createTestException();
        exception.setTransactionId("");

        // When & Then
        CompletableFuture<PayloadResponse> result = resolver.originalPayload(exception, environment);

        assertThrows(ExecutionException.class, () -> {
            result.get();
        });
    }

    @Test
    void testOriginalPayload_WithoutPermission_ShouldThrowException() {
        // Given
        InterfaceException exception = createTestException();
        when(securityService.canViewPayload(eq(exception), eq(authentication))).thenReturn(false);

        // When & Then
        CompletableFuture<PayloadResponse> result = resolver.originalPayload(exception, environment);

        assertThrows(ExecutionException.class, () -> {
            result.get();
        });

        verify(securityService).canViewPayload(exception, authentication);
    }

    @Test
    void testRetryHistory_WithValidExceptionAndPermission_ShouldSucceed() {
        // Given
        InterfaceException exception = createTestException();
        when(securityService.canViewRetryHistory(eq(exception), eq(authentication))).thenReturn(true);

        // When & Then
        assertDoesNotThrow(() -> {
            CompletableFuture<List<RetryAttempt>> result = resolver.retryHistory(exception, environment);
            assertNotNull(result);
        });

        verify(securityService).canViewRetryHistory(exception, authentication);
    }

    @Test
    void testRetryHistory_WithNullException_ShouldThrowException() {
        // When & Then
        CompletableFuture<List<RetryAttempt>> result = resolver.retryHistory(null, environment);

        assertThrows(ExecutionException.class, () -> {
            result.get();
        });
    }

    @Test
    void testRetryHistory_WithoutPermission_ShouldThrowException() {
        // Given
        InterfaceException exception = createTestException();
        when(securityService.canViewRetryHistory(eq(exception), eq(authentication))).thenReturn(false);

        // When & Then
        CompletableFuture<List<RetryAttempt>> result = resolver.retryHistory(exception, environment);

        assertThrows(ExecutionException.class, () -> {
            result.get();
        });

        verify(securityService).canViewRetryHistory(exception, authentication);
    }

    @Test
    void testStatusHistory_WithValidExceptionAndPermission_ShouldSucceed() {
        // Given
        InterfaceException exception = createTestException();
        when(securityService.canViewStatusHistory(eq(exception), eq(authentication))).thenReturn(true);

        // When & Then
        assertDoesNotThrow(() -> {
            CompletableFuture<List<StatusChange>> result = resolver.statusHistory(exception, environment);
            assertNotNull(result);
        });

        verify(securityService).canViewStatusHistory(exception, authentication);
    }

    @Test
    void testStatusHistory_WithNullException_ShouldThrowException() {
        // When & Then
        CompletableFuture<List<StatusChange>> result = resolver.statusHistory(null, environment);

        assertThrows(ExecutionException.class, () -> {
            result.get();
        });
    }

    @Test
    void testStatusHistory_WithEmptyTransactionId_ShouldReturnEmptyList()
            throws ExecutionException, InterruptedException {
        // Given
        InterfaceException exception = createTestException();
        exception.setTransactionId("");

        // When
        CompletableFuture<List<StatusChange>> result = resolver.statusHistory(exception, environment);

        // Then
        List<StatusChange> statusChanges = result.get();
        assertNotNull(statusChanges);
        assertTrue(statusChanges.isEmpty());
    }

    @Test
    void testStatusHistory_WithoutPermission_ShouldThrowException() {
        // Given
        InterfaceException exception = createTestException();
        when(securityService.canViewStatusHistory(eq(exception), eq(authentication))).thenReturn(false);

        // When & Then
        CompletableFuture<List<StatusChange>> result = resolver.statusHistory(exception, environment);

        assertThrows(ExecutionException.class, () -> {
            result.get();
        });

        verify(securityService).canViewStatusHistory(exception, authentication);
    }

    private InterfaceException createTestException() {
        return InterfaceException.builder()
                .id(1L)
                .transactionId("TEST-123")
                .status(ExceptionStatus.NEW)
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .build();
    }
}