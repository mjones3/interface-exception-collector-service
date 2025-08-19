package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.graphql.dataloader.PayloadDataLoader;
import com.arcone.biopro.exception.collector.api.graphql.dataloader.RetryHistoryDataLoader;
import com.arcone.biopro.exception.collector.api.graphql.dataloader.StatusChangeDataLoader;
import com.arcone.biopro.exception.collector.api.graphql.service.GraphQLSecurityService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.entity.StatusChange;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import graphql.schema.DataFetchingEnvironment;
import org.dataloader.DataLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ExceptionFieldResolver.
 * Tests field-level resolvers for nested GraphQL data with security checks.
 */
@ExtendWith(MockitoExtension.class)
class ExceptionFieldResolverUnitTest {

    @Mock
    private PayloadDataLoader payloadDataLoader;

    @Mock
    private RetryHistoryDataLoader retryHistoryDataLoader;

    @Mock
    private StatusChangeDataLoader statusChangeDataLoader;

    @Mock
    private GraphQLSecurityService securityService;

    @Mock
    private DataFetchingEnvironment environment;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ExceptionFieldResolver exceptionFieldResolver;

    private InterfaceException testException;
    private RetryAttempt testRetryAttempt;
    private StatusChange testStatusChange;

    @BeforeEach
    void setUp() {
        testException = InterfaceException.builder()
                .id(1L)
                .transactionId("TXN-001")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Test exception")
                .operation("CREATE_ORDER")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.HIGH)
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .retryable(true)
                .retryCount(0)
                .build();

        testRetryAttempt = RetryAttempt.builder()
                .interfaceException(testException)
                .attemptNumber(1)
                .status(RetryStatus.SUCCESS)
                .initiatedBy("test-user")
                .initiatedAt(OffsetDateTime.now())
                .resultSuccess(true)
                .build();

        testStatusChange = StatusChange.builder()
                .interfaceException(testException)
                .fromStatus(ExceptionStatus.NEW)
                .toStatus(ExceptionStatus.ACKNOWLEDGED)
                .changedBy("test-user")
                .changedAt(OffsetDateTime.now())
                .reason("Manual acknowledgment")
                .build();
    }

    @Test
    void originalPayload_WithAdminRole_ShouldAllowAccess() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        when(securityService.canViewPayload(testException, authentication)).thenReturn(true);
        
        DataLoader<String, String> mockDataLoader = mock(DataLoader.class);
        when(environment.getDataLoader("payloadDataLoader")).thenReturn(mockDataLoader);
        when(mockDataLoader.load("TXN-001")).thenReturn(CompletableFuture.completedFuture("payload-content"));

        // When
        CompletableFuture<String> result = exceptionFieldResolver.originalPayload(testException, environment, authentication);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        assertThat(result.join()).isEqualTo("payload-content");
        verify(securityService).canViewPayload(testException, authentication);
    }

    @Test
    void originalPayload_WithViewerRole_ShouldDenyAccess() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER"))
        );
        when(securityService.canViewPayload(testException, authentication)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> exceptionFieldResolver.originalPayload(testException, environment, authentication))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("Access denied to original payload");
    }

    @Test
    void retryHistory_ShouldLoadRetryAttempts() {
        // Given
        DataLoader<String, List<RetryAttempt>> mockDataLoader = mock(DataLoader.class);
        when(environment.getDataLoader("retryHistoryDataLoader")).thenReturn(mockDataLoader);
        when(mockDataLoader.load("TXN-001")).thenReturn(
                CompletableFuture.completedFuture(List.of(testRetryAttempt))
        );

        // When
        CompletableFuture<List<RetryAttempt>> result = exceptionFieldResolver.retryHistory(testException, environment);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        assertThat(result.join()).hasSize(1);
        assertThat(result.join().get(0).getAttemptNumber()).isEqualTo(1);
    }

    @Test
    void statusHistory_ShouldLoadStatusChanges() {
        // Given
        DataLoader<String, List<StatusChange>> mockDataLoader = mock(DataLoader.class);
        when(environment.getDataLoader("statusChangeDataLoader")).thenReturn(mockDataLoader);
        when(mockDataLoader.load("TXN-001")).thenReturn(
                CompletableFuture.completedFuture(List.of(testStatusChange))
        );

        // When
        CompletableFuture<List<StatusChange>> result = exceptionFieldResolver.statusHistory(testException, environment);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        assertThat(result.join()).hasSize(1);
        assertThat(result.join().get(0).getFromStatus()).isEqualTo(ExceptionStatus.NEW);
        assertThat(result.join().get(0).getToStatus()).isEqualTo(ExceptionStatus.ACKNOWLEDGED);
    }

    @Test
    void retryHistory_WithEmptyResult_ShouldReturnEmptyList() {
        // Given
        DataLoader<String, List<RetryAttempt>> mockDataLoader = mock(DataLoader.class);
        when(environment.getDataLoader("retryHistoryDataLoader")).thenReturn(mockDataLoader);
        when(mockDataLoader.load("TXN-001")).thenReturn(
                CompletableFuture.completedFuture(List.of())
        );

        // When
        CompletableFuture<List<RetryAttempt>> result = exceptionFieldResolver.retryHistory(testException, environment);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        assertThat(result.join()).isEmpty();
    }

    @Test
    void statusHistory_WithEmptyResult_ShouldReturnEmptyList() {
        // Given
        DataLoader<String, List<StatusChange>> mockDataLoader = mock(DataLoader.class);
        when(environment.getDataLoader("statusChangeDataLoader")).thenReturn(mockDataLoader);
        when(mockDataLoader.load("TXN-001")).thenReturn(
                CompletableFuture.completedFuture(List.of())
        );

        // When
        CompletableFuture<List<StatusChange>> result = exceptionFieldResolver.statusHistory(testException, environment);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        assertThat(result.join()).isEmpty();
    }

    @Test
    void originalPayload_WithDataLoaderError_ShouldPropagateException() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        when(securityService.canViewPayload(testException, authentication)).thenReturn(true);
        
        DataLoader<String, String> mockDataLoader = mock(DataLoader.class);
        when(environment.getDataLoader("payloadDataLoader")).thenReturn(mockDataLoader);
        when(mockDataLoader.load("TXN-001")).thenReturn(
                CompletableFuture.failedFuture(new RuntimeException("DataLoader error"))
        );

        // When
        CompletableFuture<String> result = exceptionFieldResolver.originalPayload(testException, environment, authentication);

        // Then
        assertThat(result).failsWithin(java.time.Duration.ofSeconds(1))
                .withThrowableOfType(java.util.concurrent.ExecutionException.class)
                .withMessageContaining("DataLoader error");
    }

    @Test
    void retryHistory_WithDataLoaderError_ShouldPropagateException() {
        // Given
        DataLoader<String, List<RetryAttempt>> mockDataLoader = mock(DataLoader.class);
        when(environment.getDataLoader("retryHistoryDataLoader")).thenReturn(mockDataLoader);
        when(mockDataLoader.load("TXN-001")).thenReturn(
                CompletableFuture.failedFuture(new RuntimeException("DataLoader error"))
        );

        // When
        CompletableFuture<List<RetryAttempt>> result = exceptionFieldResolver.retryHistory(testException, environment);

        // Then
        assertThat(result).failsWithin(java.time.Duration.ofSeconds(1))
                .withThrowableOfType(java.util.concurrent.ExecutionException.class)
                .withMessageContaining("DataLoader error");
    }

    @Test
    void originalPayload_WithNullAuthentication_ShouldDenyAccess() {
        // When & Then
        assertThatThrownBy(() -> exceptionFieldResolver.originalPayload(testException, environment, null))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("Authentication required");
    }

    @Test
    void originalPayload_WithOperationsRole_ShouldCheckPermissions() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS"))
        );
        when(securityService.canViewPayload(testException, authentication)).thenReturn(true);
        
        DataLoader<String, String> mockDataLoader = mock(DataLoader.class);
        when(environment.getDataLoader("payloadDataLoader")).thenReturn(mockDataLoader);
        when(mockDataLoader.load("TXN-001")).thenReturn(CompletableFuture.completedFuture("payload-content"));

        // When
        CompletableFuture<String> result = exceptionFieldResolver.originalPayload(testException, environment, authentication);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        verify(securityService).canViewPayload(testException, authentication);
    }
}