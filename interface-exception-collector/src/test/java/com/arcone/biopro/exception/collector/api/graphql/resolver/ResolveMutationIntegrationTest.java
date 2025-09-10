package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.graphql.dto.ResolveExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.ResolveExceptionResult;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.ResolutionMethod;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the enhanced resolve mutation functionality.
 * Tests the complete flow from GraphQL input to database persistence with enhanced validation.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ResolveMutationIntegrationTest {

    @Autowired
    private RetryMutationResolver retryMutationResolver;

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    private Authentication testAuthentication;
    private InterfaceException testException;

    @BeforeEach
    void setUp() {
        // Create test authentication with OPERATIONS role
        testAuthentication = new UsernamePasswordAuthenticationToken(
            "test-user", 
            "password", 
            List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS"))
        );

        // Create and save test exception
        testException = new InterfaceException();
        testException.setTransactionId("TEST-TXN-" + System.currentTimeMillis());
        testException.setStatus(ExceptionStatus.ACKNOWLEDGED);
        testException.setCreatedAt(OffsetDateTime.now());
        testException.setUpdatedAt(OffsetDateTime.now());
        testException.setErrorMessage("Test exception for resolution");
        testException.setInterfaceType("TEST_INTERFACE");
        testException.setPayload("{}");
        
        testException = exceptionRepository.save(testException);
    }

    @Test
    void resolveException_WithValidInput_ShouldSucceed() throws Exception {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId(testException.getTransactionId())
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("Test resolution notes")
                .build();

        // When
        CompletableFuture<ResolveExceptionResult> future = retryMutationResolver.resolveException(input, testAuthentication);
        ResolveExceptionResult result = future.get();

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getException()).isNotNull();
        assertThat(result.getException().getStatus()).isEqualTo(ExceptionStatus.RESOLVED);
        
        // Enhanced metadata assertions
        assertThat(result.hasOperationMetadata()).isTrue();
        assertThat(result.getOperationId()).isNotNull();
        assertThat(result.getPerformedBy()).isEqualTo("test-user");
        assertThat(result.getResolutionMethod()).isEqualTo(ResolutionMethod.MANUAL_RESOLUTION);
        assertThat(result.getResolutionNotes()).isEqualTo("Test resolution notes");
        assertThat(result.getTimestamp()).isNotNull();

        // Verify database state
        InterfaceException updatedException = exceptionRepository.findByTransactionId(testException.getTransactionId()).orElse(null);
        assertThat(updatedException).isNotNull();
        assertThat(updatedException.getStatus()).isEqualTo(ExceptionStatus.RESOLVED);
    }

    @Test
    void resolveException_WithInvalidTransactionId_ShouldReturnError() throws Exception {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId("NONEXISTENT-TXN")
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("Test resolution notes")
                .build();

        // When
        CompletableFuture<ResolveExceptionResult> future = retryMutationResolver.resolveException(input, testAuthentication);
        ResolveExceptionResult result = future.get();

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("BUSINESS_001");
        assertThat(result.getErrors().get(0).getMessage()).contains("Exception not found");
        
        // Enhanced metadata should still be present for failed operations
        assertThat(result.getOperationId()).isNotNull();
        assertThat(result.getPerformedBy()).isEqualTo("test-user");
        assertThat(result.getTimestamp()).isNotNull();
    }

    @Test
    void resolveException_WithAlreadyResolvedStatus_ShouldReturnError() throws Exception {
        // Given - Set exception to already resolved
        testException.setStatus(ExceptionStatus.RESOLVED);
        exceptionRepository.save(testException);

        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId(testException.getTransactionId())
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("Test resolution notes")
                .build();

        // When
        CompletableFuture<ResolveExceptionResult> future = retryMutationResolver.resolveException(input, testAuthentication);
        ResolveExceptionResult result = future.get();

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("BUSINESS_006");
        assertThat(result.getErrors().get(0).getMessage()).contains("already resolved");
    }

    @Test
    void resolveException_WithInvalidResolutionMethod_ShouldReturnError() throws Exception {
        // Given - Set exception to RETRIED_SUCCESS but use wrong resolution method
        testException.setStatus(ExceptionStatus.RETRIED_SUCCESS);
        exceptionRepository.save(testException);

        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId(testException.getTransactionId())
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION) // Invalid for RETRIED_SUCCESS
                .resolutionNotes("Test resolution notes")
                .build();

        // When
        CompletableFuture<ResolveExceptionResult> future = retryMutationResolver.resolveException(input, testAuthentication);
        ResolveExceptionResult result = future.get();

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("RESOLVE_002");
        assertThat(result.getErrors().get(0).getMessage()).contains("MANUAL_RESOLUTION cannot be used for exceptions with RETRIED_SUCCESS status");
    }

    @Test
    void resolveException_WithRetriedSuccessAndCorrectMethod_ShouldSucceed() throws Exception {
        // Given - Set exception to RETRIED_SUCCESS and use correct resolution method
        testException.setStatus(ExceptionStatus.RETRIED_SUCCESS);
        exceptionRepository.save(testException);

        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId(testException.getTransactionId())
                .resolutionMethod(ResolutionMethod.RETRY_SUCCESS) // Correct for RETRIED_SUCCESS
                .resolutionNotes("Retry was successful")
                .build();

        // When
        CompletableFuture<ResolveExceptionResult> future = retryMutationResolver.resolveException(input, testAuthentication);
        ResolveExceptionResult result = future.get();

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getException().getStatus()).isEqualTo(ExceptionStatus.RESOLVED);
        assertThat(result.getResolutionMethod()).isEqualTo(ResolutionMethod.RETRY_SUCCESS);
    }

    @Test
    void resolveException_WithCustomerResolvedForEscalated_ShouldSucceed() throws Exception {
        // Given - Set exception to ESCALATED and use CUSTOMER_RESOLVED method
        testException.setStatus(ExceptionStatus.ESCALATED);
        exceptionRepository.save(testException);

        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId(testException.getTransactionId())
                .resolutionMethod(ResolutionMethod.CUSTOMER_RESOLVED)
                .resolutionNotes("Customer resolved the issue")
                .build();

        // When
        CompletableFuture<ResolveExceptionResult> future = retryMutationResolver.resolveException(input, testAuthentication);
        ResolveExceptionResult result = future.get();

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getException().getStatus()).isEqualTo(ExceptionStatus.RESOLVED);
        assertThat(result.getResolutionMethod()).isEqualTo(ResolutionMethod.CUSTOMER_RESOLVED);
    }

    @Test
    void resolveException_WithEmptyResolutionNotes_ShouldSucceed() throws Exception {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId(testException.getTransactionId())
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("   ") // Whitespace only - should be trimmed to null
                .build();

        // When
        CompletableFuture<ResolveExceptionResult> future = retryMutationResolver.resolveException(input, testAuthentication);
        ResolveExceptionResult result = future.get();

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResolutionNotes()).isNull(); // Should be trimmed to null
    }

    @Test
    void resolveException_WithExcessiveResolutionNotes_ShouldReturnValidationError() throws Exception {
        // Given
        String longNotes = "A".repeat(2001); // Exceeds 2000 character limit
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId(testException.getTransactionId())
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes(longNotes)
                .build();

        // When
        CompletableFuture<ResolveExceptionResult> future = retryMutationResolver.resolveException(input, testAuthentication);
        ResolveExceptionResult result = future.get();

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("VALIDATION_005");
        assertThat(result.getErrors().get(0).getMessage()).contains("must not exceed 2000 characters");
    }

    @Test
    void resolveException_WithClosedStatus_ShouldReturnBusinessRuleError() throws Exception {
        // Given - Set exception to CLOSED
        testException.setStatus(ExceptionStatus.CLOSED);
        exceptionRepository.save(testException);

        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId(testException.getTransactionId())
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("Attempting to resolve closed exception")
                .build();

        // When
        CompletableFuture<ResolveExceptionResult> future = retryMutationResolver.resolveException(input, testAuthentication);
        ResolveExceptionResult result = future.get();

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("BUSINESS_010");
        assertThat(result.getErrors().get(0).getMessage()).contains("closed and cannot be resolved");
    }
}
