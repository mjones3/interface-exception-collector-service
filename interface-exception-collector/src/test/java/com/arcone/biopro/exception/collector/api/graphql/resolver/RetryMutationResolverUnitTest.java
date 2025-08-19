package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.BulkAcknowledgeInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.BulkAcknowledgeResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.BulkRetryInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.BulkRetryResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionResult;
import com.arcone.biopro.exception.collector.api.graphql.service.GraphQLAcknowledgmentService;
import com.arcone.biopro.exception.collector.api.graphql.service.GraphQLRetryService;
import com.arcone.biopro.exception.collector.api.graphql.service.GraphQLSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RetryMutationResolver.
 * Tests GraphQL mutation operations for retry and acknowledgment with security checks.
 */
@ExtendWith(MockitoExtension.class)
class RetryMutationResolverUnitTest {

    @Mock
    private GraphQLRetryService retryService;

    @Mock
    private GraphQLAcknowledgmentService acknowledgmentService;

    @Mock
    private GraphQLSecurityService securityService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RetryMutationResolver retryMutationResolver;

    private RetryExceptionInput retryInput;
    private AcknowledgeExceptionInput acknowledgeInput;
    private BulkRetryInput bulkRetryInput;
    private BulkAcknowledgeInput bulkAcknowledgeInput;

    @BeforeEach
    void setUp() {
        retryInput = RetryExceptionInput.builder()
                .transactionId("TXN-001")
                .reason("Manual retry requested")
                .priority(RetryExceptionInput.RetryPriority.HIGH)
                .build();

        acknowledgeInput = AcknowledgeExceptionInput.builder()
                .transactionId("TXN-001")
                .reason("Acknowledged by operations team")
                .notes("Issue has been reviewed")
                .build();

        bulkRetryInput = BulkRetryInput.builder()
                .transactionIds(List.of("TXN-001", "TXN-002", "TXN-003"))
                .reason("Bulk retry after system fix")
                .priority(RetryExceptionInput.RetryPriority.NORMAL)
                .build();

        bulkAcknowledgeInput = BulkAcknowledgeInput.builder()
                .transactionIds(List.of("TXN-001", "TXN-002"))
                .reason("Bulk acknowledgment")
                .notes("All issues reviewed")
                .build();

        when(authentication.getName()).thenReturn("test-user");
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS"))
        );
    }

    @Test
    void retryException_WithValidInput_ShouldReturnSuccessResult() {
        // Given
        RetryExceptionResult expectedResult = RetryExceptionResult.builder()
                .success(true)
                .transactionId("TXN-001")
                .message("Retry initiated successfully")
                .build();

        when(retryService.retryException(eq(retryInput), eq("test-user")))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        CompletableFuture<RetryExceptionResult> result = retryMutationResolver.retryException(
                retryInput, authentication
        );

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        assertThat(result.join()).isEqualTo(expectedResult);
        verify(retryService).retryException(eq(retryInput), eq("test-user"));
    }

    @Test
    void retryException_WithInsufficientPermissions_ShouldThrowAccessDeniedException() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER"))
        );

        // When & Then
        assertThatThrownBy(() -> retryMutationResolver.retryException(retryInput, authentication))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Insufficient permissions for retry operation");
    }

    @Test
    void retryException_WithNullAuthentication_ShouldThrowAccessDeniedException() {
        // When & Then
        assertThatThrownBy(() -> retryMutationResolver.retryException(retryInput, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Authentication required");
    }

    @Test
    void acknowledgeException_WithValidInput_ShouldReturnSuccessResult() {
        // Given
        AcknowledgeExceptionResult expectedResult = AcknowledgeExceptionResult.builder()
                .success(true)
                .transactionId("TXN-001")
                .message("Exception acknowledged successfully")
                .build();

        when(acknowledgmentService.acknowledgeException(eq(acknowledgeInput), eq("test-user")))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        CompletableFuture<AcknowledgeExceptionResult> result = retryMutationResolver.acknowledgeException(
                acknowledgeInput, authentication
        );

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        assertThat(result.join()).isEqualTo(expectedResult);
        verify(acknowledgmentService).acknowledgeException(eq(acknowledgeInput), eq("test-user"));
    }

    @Test
    void acknowledgeException_WithInsufficientPermissions_ShouldThrowAccessDeniedException() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER"))
        );

        // When & Then
        assertThatThrownBy(() -> retryMutationResolver.acknowledgeException(acknowledgeInput, authentication))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Insufficient permissions for acknowledgment operation");
    }

    @Test
    void bulkRetryExceptions_WithValidInput_ShouldReturnBulkResult() {
        // Given
        BulkRetryResult expectedResult = BulkRetryResult.builder()
                .totalRequested(3)
                .successCount(2)
                .failureCount(1)
                .results(List.of(
                        RetryExceptionResult.builder()
                                .success(true)
                                .transactionId("TXN-001")
                                .build(),
                        RetryExceptionResult.builder()
                                .success(true)
                                .transactionId("TXN-002")
                                .build(),
                        RetryExceptionResult.builder()
                                .success(false)
                                .transactionId("TXN-003")
                                .errorMessage("Exception not retryable")
                                .build()
                ))
                .build();

        when(retryService.bulkRetryExceptions(eq(bulkRetryInput), eq("test-user")))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        CompletableFuture<BulkRetryResult> result = retryMutationResolver.bulkRetryExceptions(
                bulkRetryInput, authentication
        );

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        BulkRetryResult bulkResult = result.join();
        assertThat(bulkResult.getTotalRequested()).isEqualTo(3);
        assertThat(bulkResult.getSuccessCount()).isEqualTo(2);
        assertThat(bulkResult.getFailureCount()).isEqualTo(1);
        assertThat(bulkResult.getResults()).hasSize(3);
        
        verify(retryService).bulkRetryExceptions(eq(bulkRetryInput), eq("test-user"));
    }

    @Test
    void bulkRetryExceptions_WithAdminRole_ShouldAllowOperation() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        BulkRetryResult expectedResult = BulkRetryResult.builder()
                .totalRequested(3)
                .successCount(3)
                .failureCount(0)
                .results(List.of())
                .build();

        when(retryService.bulkRetryExceptions(eq(bulkRetryInput), eq("test-user")))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        CompletableFuture<BulkRetryResult> result = retryMutationResolver.bulkRetryExceptions(
                bulkRetryInput, authentication
        );

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        verify(retryService).bulkRetryExceptions(eq(bulkRetryInput), eq("test-user"));
    }

    @Test
    void bulkAcknowledgeExceptions_WithValidInput_ShouldReturnBulkResult() {
        // Given
        BulkAcknowledgeResult expectedResult = BulkAcknowledgeResult.builder()
                .totalRequested(2)
                .successCount(2)
                .failureCount(0)
                .results(List.of(
                        AcknowledgeExceptionResult.builder()
                                .success(true)
                                .transactionId("TXN-001")
                                .build(),
                        AcknowledgeExceptionResult.builder()
                                .success(true)
                                .transactionId("TXN-002")
                                .build()
                ))
                .build();

        when(acknowledgmentService.bulkAcknowledgeExceptions(eq(bulkAcknowledgeInput), eq("test-user")))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        CompletableFuture<BulkAcknowledgeResult> result = retryMutationResolver.bulkAcknowledgeExceptions(
                bulkAcknowledgeInput, authentication
        );

        // Then
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        BulkAcknowledgeResult bulkResult = result.join();
        assertThat(bulkResult.getTotalRequested()).isEqualTo(2);
        assertThat(bulkResult.getSuccessCount()).isEqualTo(2);
        assertThat(bulkResult.getFailureCount()).isEqualTo(0);
        
        verify(acknowledgmentService).bulkAcknowledgeExceptions(eq(bulkAcknowledgeInput), eq("test-user"));
    }

    @Test
    void bulkAcknowledgeExceptions_WithTooManyItems_ShouldThrowException() {
        // Given
        List<String> tooManyIds = java.util.stream.IntStream.range(0, 101)
                .mapToObj(i -> "TXN-" + String.format("%03d", i))
                .toList();

        BulkAcknowledgeInput invalidInput = BulkAcknowledgeInput.builder()
                .transactionIds(tooManyIds) // Exceeds limit of 100
                .reason("Bulk acknowledgment")
                .build();

        // When & Then
        assertThatThrownBy(() -> retryMutationResolver.bulkAcknowledgeExceptions(invalidInput, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bulk operations limited to 100 items");
    }

    @Test
    void retryException_WithServiceException_ShouldPropagateException() {
        // Given
        when(retryService.retryException(any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Service unavailable")));

        // When
        CompletableFuture<RetryExceptionResult> result = retryMutationResolver.retryException(
                retryInput, authentication
        );

        // Then
        assertThat(result).failsWithin(java.time.Duration.ofSeconds(1))
                .withThrowableOfType(java.util.concurrent.ExecutionException.class)
                .withMessageContaining("Service unavailable");
    }

    @Test
    void acknowledgeException_WithServiceException_ShouldPropagateException() {
        // Given
        when(acknowledgmentService.acknowledgeException(any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

        // When
        CompletableFuture<AcknowledgeExceptionResult> result = retryMutationResolver.acknowledgeException(
                acknowledgeInput, authentication
        );

        // Then
        assertThat(result).failsWithin(java.time.Duration.ofSeconds(1))
                .withThrowableOfType(java.util.concurrent.ExecutionException.class)
                .withMessageContaining("Database error");
    }

    @Test
    void validateBulkOperationSize_WithValidSize_ShouldNotThrow() {
        // Given
        List<String> validIds = List.of("TXN-001", "TXN-002", "TXN-003");

        // When & Then - Should not throw exception
        retryMutationResolver.validateBulkOperationSize(validIds);
    }

    @Test
    void validateBulkOperationSize_WithExcessiveSize_ShouldThrowException() {
        // Given
        List<String> tooManyIds = java.util.stream.IntStream.range(0, 101)
                .mapToObj(i -> "TXN-" + String.format("%03d", i))
                .toList();

        // When & Then
        assertThatThrownBy(() -> retryMutationResolver.validateBulkOperationSize(tooManyIds))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bulk operations limited to 100 items");
    }

    @Test
    void extractUserId_ShouldReturnAuthenticationName() {
        // When
        String userId = retryMutationResolver.extractUserId(authentication);

        // Then
        assertThat(userId).isEqualTo("test-user");
    }

    @Test
    void hasRequiredRole_WithOperationsRole_ShouldReturnTrue() {
        // When
        boolean hasRole = retryMutationResolver.hasRequiredRole(authentication);

        // Then
        assertThat(hasRole).isTrue();
    }

    @Test
    void hasRequiredRole_WithAdminRole_ShouldReturnTrue() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When
        boolean hasRole = retryMutationResolver.hasRequiredRole(authentication);

        // Then
        assertThat(hasRole).isTrue();
    }

    @Test
    void hasRequiredRole_WithViewerRole_ShouldReturnFalse() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER"))
        );

        // When
        boolean hasRole = retryMutationResolver.hasRequiredRole(authentication);

        // Then
        assertThat(hasRole).isFalse();
    }
}