package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.graphql.dto.*;
import com.arcone.biopro.exception.collector.api.graphql.service.GraphQLExceptionService;
import com.arcone.biopro.exception.collector.application.service.ExceptionManagementService;
import com.arcone.biopro.exception.collector.application.service.RetryService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for GraphQL resolvers using existing test infrastructure.
 * Tests GraphQL resolver integration with service layer and security.
 */
@ExtendWith(MockitoExtension.class)
class GraphQLResolverUnitTest {

    @Mock
    private GraphQLExceptionService graphQLExceptionService;

    @Mock
    private ExceptionManagementService exceptionManagementService;

    @Mock
    private RetryService retryService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ExceptionQueryResolver exceptionQueryResolver;

    @InjectMocks
    private ExceptionMutationResolver exceptionMutationResolver;

    private InterfaceException testException;
    private ExceptionConnection testConnection;

    @BeforeEach
    void setUp() {
        // Set up security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test-user");
        when(authentication.isAuthenticated()).thenReturn(true);

        // Create test data
        testException = InterfaceException.builder()
                .id(1L)
                .transactionId("TXN-001")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Test exception")
                .operation("CREATE_ORDER")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.HIGH)
                .category(ExceptionCategory.VALIDATION)
                .customerId("CUST-001")
                .locationCode("LOC-001")
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .retryable(true)
                .retryCount(0)
                .maxRetries(3)
                .build();

        ExceptionConnection.ExceptionEdge edge = ExceptionConnection.ExceptionEdge.builder()
                .node(testException)
                .cursor("cursor-1")
                .build();

        ExceptionConnection.PageInfo pageInfo = ExceptionConnection.PageInfo.builder()
                .hasNextPage(false)
                .hasPreviousPage(false)
                .startCursor("cursor-1")
                .endCursor("cursor-1")
                .build();

        testConnection = ExceptionConnection.builder()
                .edges(List.of(edge))
                .pageInfo(pageInfo)
                .totalCount(1L)
                .build();
    }

    @Test
    void exceptionQueryResolver_WithValidFilters_ShouldReturnFilteredResults() {
        // Given
        ExceptionFilters filters = ExceptionFilters.builder()
                .interfaceTypes(List.of(InterfaceType.ORDER))
                .statuses(List.of(ExceptionStatus.NEW))
                .severities(List.of(ExceptionSeverity.HIGH))
                .build();

        when(graphQLExceptionService.findExceptions(eq(filters), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(testConnection));

        // When
        CompletableFuture<ExceptionConnection> result = exceptionQueryResolver.exceptions(filters, null, null);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        ExceptionConnection connection = result.join();
        assertThat(connection.getEdges()).hasSize(1);
        assertThat(connection.getTotalCount()).isEqualTo(1L);
        assertThat(connection.getEdges().get(0).getNode().getInterfaceType()).isEqualTo(InterfaceType.ORDER);
        
        verify(graphQLExceptionService).findExceptions(eq(filters), any(), any());
    }

    @Test
    void exceptionQueryResolver_WithPagination_ShouldApplyPaginationCorrectly() {
        // Given
        PaginationInput pagination = PaginationInput.builder()
                .first(10)
                .after("cursor-after")
                .build();

        when(graphQLExceptionService.findExceptions(any(), eq(pagination), any()))
                .thenReturn(CompletableFuture.completedFuture(testConnection));

        // When
        CompletableFuture<ExceptionConnection> result = exceptionQueryResolver.exceptions(null, pagination, null);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        verify(graphQLExceptionService).findExceptions(any(), eq(pagination), any());
    }

    @Test
    void exceptionQueryResolver_WithSorting_ShouldApplySortingCorrectly() {
        // Given
        SortingInput sorting = SortingInput.builder()
                .field("timestamp")
                .direction(SortingInput.SortDirection.DESC)
                .build();

        when(graphQLExceptionService.findExceptions(any(), any(), eq(sorting)))
                .thenReturn(CompletableFuture.completedFuture(testConnection));

        // When
        CompletableFuture<ExceptionConnection> result = exceptionQueryResolver.exceptions(null, null, sorting);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        verify(graphQLExceptionService).findExceptions(any(), any(), eq(sorting));
    }

    @Test
    void exceptionQueryResolver_WithInvalidPagination_ShouldThrowException() {
        // Given
        PaginationInput invalidPagination = PaginationInput.builder()
                .first(10)
                .last(5) // Both forward and backward pagination
                .build();

        // When & Then
        assertThatThrownBy(() -> exceptionQueryResolver.exceptions(null, invalidPagination, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot use both forward pagination");
    }

    @Test
    void exceptionQueryResolver_ByTransactionId_ShouldReturnException() {
        // Given
        String transactionId = "TXN-001";
        when(graphQLExceptionService.findExceptionByTransactionId(transactionId))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(testException)));

        // When
        CompletableFuture<InterfaceException> result = exceptionQueryResolver.exception(transactionId);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        assertThat(result.join()).isEqualTo(testException);
        verify(graphQLExceptionService).findExceptionByTransactionId(transactionId);
    }

    @Test
    void exceptionMutationResolver_RetryException_ShouldDelegateToService() {
        // Given
        String transactionId = "TXN-001";
        String reason = "Manual retry requested";
        RetryPriority priority = RetryPriority.HIGH;

        RetryAttempt retryAttempt = RetryAttempt.builder()
                .id(1L)
                .attemptNumber(1)
                .status(RetryStatus.PENDING)
                .initiatedBy("test-user")
                .initiatedAt(OffsetDateTime.now())
                .build();

        RetryResponse expectedResponse = RetryResponse.builder()
                .success(true)
                .message("Retry initiated successfully")
                .exception(testException)
                .retryAttempt(retryAttempt)
                .build();

        when(retryService.initiateRetry(eq(transactionId), eq(reason), eq(priority), eq("test-user")))
                .thenReturn(CompletableFuture.completedFuture(expectedResponse));

        // When
        CompletableFuture<RetryResponse> result = exceptionMutationResolver.retryException(transactionId, reason, priority);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        RetryResponse response = result.join();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Retry initiated successfully");
        assertThat(response.getException()).isEqualTo(testException);
        
        verify(retryService).initiateRetry(eq(transactionId), eq(reason), eq(priority), eq("test-user"));
    }

    @Test
    void exceptionMutationResolver_AcknowledgeException_ShouldDelegateToService() {
        // Given
        String transactionId = "TXN-001";
        String reason = "Acknowledged by operator";
        String notes = "Issue understood, working on fix";

        AcknowledgmentResponse expectedResponse = AcknowledgmentResponse.builder()
                .success(true)
                .message("Exception acknowledged successfully")
                .exception(testException)
                .build();

        when(exceptionManagementService.acknowledgeException(eq(transactionId), eq(reason), eq(notes), eq("test-user")))
                .thenReturn(CompletableFuture.completedFuture(expectedResponse));

        // When
        CompletableFuture<AcknowledgmentResponse> result = exceptionMutationResolver.acknowledgeException(transactionId, reason, notes);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        AcknowledgmentResponse response = result.join();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Exception acknowledged successfully");
        assertThat(response.getException()).isEqualTo(testException);
        
        verify(exceptionManagementService).acknowledgeException(eq(transactionId), eq(reason), eq(notes), eq("test-user"));
    }

    @Test
    void exceptionMutationResolver_ResolveException_ShouldDelegateToService() {
        // Given
        String transactionId = "TXN-001";
        String resolution = "Issue fixed in source system";
        String notes = "Root cause identified and resolved";

        ResolutionResponse expectedResponse = ResolutionResponse.builder()
                .success(true)
                .message("Exception resolved successfully")
                .exception(testException)
                .build();

        when(exceptionManagementService.resolveException(eq(transactionId), eq(resolution), eq(notes), eq("test-user")))
                .thenReturn(CompletableFuture.completedFuture(expectedResponse));

        // When
        CompletableFuture<ResolutionResponse> result = exceptionMutationResolver.resolveException(transactionId, resolution, notes);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        ResolutionResponse response = result.join();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Exception resolved successfully");
        assertThat(response.getException()).isEqualTo(testException);
        
        verify(exceptionManagementService).resolveException(eq(transactionId), eq(resolution), eq(notes), eq("test-user"));
    }

    @Test
    void exceptionMutationResolver_BulkRetry_ShouldProcessMultipleExceptions() {
        // Given
        List<String> transactionIds = List.of("TXN-001", "TXN-002", "TXN-003");
        String reason = "Bulk retry operation";
        RetryPriority priority = RetryPriority.NORMAL;

        BulkRetryResponse expectedResponse = BulkRetryResponse.builder()
                .totalRequested(3)
                .successCount(3)
                .failureCount(0)
                .results(List.of())
                .build();

        when(retryService.bulkRetry(eq(transactionIds), eq(reason), eq(priority), eq("test-user")))
                .thenReturn(CompletableFuture.completedFuture(expectedResponse));

        // When
        CompletableFuture<BulkRetryResponse> result = exceptionMutationResolver.bulkRetryExceptions(transactionIds, reason, priority);

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        BulkRetryResponse response = result.join();
        assertThat(response.getTotalRequested()).isEqualTo(3);
        assertThat(response.getSuccessCount()).isEqualTo(3);
        assertThat(response.getFailureCount()).isEqualTo(0);
        
        verify(retryService).bulkRetry(eq(transactionIds), eq(reason), eq(priority), eq("test-user"));
    }

    @Test
    void exceptionMutationResolver_WithUnauthenticatedUser_ShouldThrowException() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> exceptionMutationResolver.retryException("TXN-001", "reason", RetryPriority.HIGH))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("User not authenticated");
    }

    @Test
    void exceptionQueryResolver_WithNullTransactionId_ShouldThrowValidationException() {
        // When & Then
        assertThatThrownBy(() -> exceptionQueryResolver.exception(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction ID cannot be null or blank");
    }

    @Test
    void exceptionQueryResolver_WithBlankTransactionId_ShouldThrowValidationException() {
        // When & Then
        assertThatThrownBy(() -> exceptionQueryResolver.exception(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction ID cannot be null or blank");
    }

    @Test
    void exceptionMutationResolver_WithNullReason_ShouldThrowValidationException() {
        // When & Then
        assertThatThrownBy(() -> exceptionMutationResolver.retryException("TXN-001", null, RetryPriority.HIGH))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reason cannot be null or blank");
    }

    @Test
    void exceptionQueryResolver_WithServiceException_ShouldPropagateException() {
        // Given
        String transactionId = "TXN-001";
        RuntimeException serviceException = new RuntimeException("Service error");
        
        when(graphQLExceptionService.findExceptionByTransactionId(transactionId))
                .thenReturn(CompletableFuture.failedFuture(serviceException));

        // When
        CompletableFuture<InterfaceException> result = exceptionQueryResolver.exception(transactionId);

        // Then
        assertThat(result).failsWithin(java.time.Duration.ofSeconds(1))
                .withThrowableOfType(RuntimeException.class)
                .withMessage("Service error");
    }

    @Test
    void exceptionMutationResolver_WithServiceException_ShouldPropagateException() {
        // Given
        String transactionId = "TXN-001";
        String reason = "Test retry";
        RetryPriority priority = RetryPriority.HIGH;
        RuntimeException serviceException = new RuntimeException("Retry service error");
        
        when(retryService.initiateRetry(eq(transactionId), eq(reason), eq(priority), eq("test-user")))
                .thenReturn(CompletableFuture.failedFuture(serviceException));

        // When
        CompletableFuture<RetryResponse> result = exceptionMutationResolver.retryException(transactionId, reason, priority);

        // Then
        assertThat(result).failsWithin(java.time.Duration.ofSeconds(1))
                .withThrowableOfType(RuntimeException.class)
                .withMessage("Retry service error");
    }
}