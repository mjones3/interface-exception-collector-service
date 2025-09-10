package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.CancelRetryResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.ResolveExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.ResolveExceptionResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionResult;
import com.arcone.biopro.exception.collector.api.graphql.security.SecurityAuditLogger;
import com.arcone.biopro.exception.collector.api.graphql.service.AcknowledgmentValidationService;
import com.arcone.biopro.exception.collector.api.graphql.service.CancelRetryValidationService;
import com.arcone.biopro.exception.collector.api.graphql.service.ResolutionValidationService;
import com.arcone.biopro.exception.collector.api.graphql.service.RetryValidationService;
import com.arcone.biopro.exception.collector.api.graphql.validation.MutationErrorCode;
import com.arcone.biopro.exception.collector.api.graphql.validation.ValidationResult;
import com.arcone.biopro.exception.collector.application.service.ExceptionManagementService;
import com.arcone.biopro.exception.collector.application.service.RetryService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.ResolutionMethod;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit tests for RetryMutationResolver covering all mutation operations,
 * validation integration, error handling, and edge cases to achieve >95% code coverage.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RetryMutationResolver Comprehensive Tests")
class RetryMutationResolverComprehensiveTest {

    @Mock
    private RetryService retryService;

    @Mock
    private ExceptionManagementService exceptionManagementService;

    @Mock
    private RetryValidationService retryValidationService;

    @Mock
    private AcknowledgmentValidationService acknowledgmentValidationService;

    @Mock
    private ResolutionValidationService resolutionValidationService;

    @Mock
    private CancelRetryValidationService cancelRetryValidationService;

    @Mock
    private SecurityAuditLogger auditLogger;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RetryMutationResolver mutationResolver;

    private InterfaceException testException;
    private RetryAttempt testRetryAttempt;
    private RetryExceptionInput validRetryInput;
    private AcknowledgeExceptionInput validAcknowledgeInput;
    private ResolveExceptionInput validResolveInput;

    @BeforeEach
    void setUp() {
        // Setup test entities
        testException = new InterfaceException();
        testException.setId(1L);
        testException.setTransactionId("TXN-123");
        testException.setStatus(ExceptionStatus.FAILED);
        testException.setRetryable(true);
        testException.setRetryCount(0);
        testException.setMaxRetries(3);
        testException.setCreatedAt(OffsetDateTime.now().minusDays(1));

        testRetryAttempt = new RetryAttempt();
        testRetryAttempt.setId(1L);
        testRetryAttempt.setInterfaceException(testException);
        testRetryAttempt.setAttemptNumber(1);
        testRetryAttempt.setStatus(RetryStatus.PENDING);
        testRetryAttempt.setInitiatedAt(OffsetDateTime.now());

        // Setup valid inputs
        validRetryInput = RetryExceptionInput.builder()
                .transactionId("TXN-123")
                .reason("Test retry reason")
                .priority(RetryExceptionInput.RetryPriority.NORMAL)
                .notes("Test notes")
                .build();

        validAcknowledgeInput = AcknowledgeExceptionInput.builder()
                .transactionId("TXN-123")
                .reason("Test acknowledgment reason")
                .notes("Test notes")
                .build();

        validResolveInput = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("Test resolution notes")
                .build();

        // Setup authentication
        when(authentication.getName()).thenReturn("test-user");
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS")));
    }

    @Nested
    @DisplayName("Retry Exception Mutation Tests")
    class RetryExceptionMutationTests {

        @Test
        @DisplayName("Should successfully retry exception with valid input")
        void shouldSuccessfullyRetryExceptionWithValidInput() throws ExecutionException, InterruptedException {
            // Given
            ValidationResult validationResult = ValidationResult.success("retry", "TXN-123");
            when(retryValidationService.validateRetryOperation(validRetryInput, authentication))
                    .thenReturn(validationResult);
            when(retryService.initiateRetry(eq("TXN-123"), any()))
                    .thenReturn(createSuccessfulRetryResponse());

            // When
            CompletableFuture<RetryExceptionResult> future = mutationResolver.retryException(validRetryInput, authentication);
            RetryExceptionResult result = future.get();

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getException()).isNotNull();
            assertThat(result.getRetryAttempt()).isNotNull();
            assertThat(result.getErrors()).isEmpty();
            assertThat(result.getPerformedBy()).isEqualTo("test-user");

            verify(auditLogger).logRetryAttempt("TXN-123", "test-user");
            verify(retryService).initiateRetry(eq("TXN-123"), any());
        }

        @Test
        @DisplayName("Should fail retry exception with validation errors")
        void shouldFailRetryExceptionWithValidationErrors() throws ExecutionException, InterruptedException {
            // Given
            ValidationResult validationResult = ValidationResult.failure("retry", "TXN-123", 
                    List.of(com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError.builder()
                            .code(MutationErrorCode.NOT_RETRYABLE.getCode())
                            .message("Exception is not retryable")
                            .build()));
            when(retryValidationService.validateRetryOperation(validRetryInput, authentication))
                    .thenReturn(validationResult);

            // When
            CompletableFuture<RetryExceptionResult> future = mutationResolver.retryException(validRetryInput, authentication);
            RetryExceptionResult result = future.get();

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getException()).isNull();
            assertThat(result.getRetryAttempt()).isNull();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.NOT_RETRYABLE.getCode());

            verify(auditLogger).logRetryAttempt("TXN-123", "test-user");
        }

        @Test
        @DisplayName("Should handle retry service exception gracefully")
        void shouldHandleRetryServiceExceptionGracefully() throws ExecutionException, InterruptedException {
            // Given
            ValidationResult validationResult = ValidationResult.success("retry", "TXN-123");
            when(retryValidationService.validateRetryOperation(validRetryInput, authentication))
                    .thenReturn(validationResult);
            when(retryService.initiateRetry(eq("TXN-123"), any()))
                    .thenThrow(new RuntimeException("Service unavailable"));

            // When
            CompletableFuture<RetryExceptionResult> future = mutationResolver.retryException(validRetryInput, authentication);
            RetryExceptionResult result = future.get();

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getMessage()).contains("Service unavailable");
        }

        @Test
        @DisplayName("Should handle null authentication gracefully")
        void shouldHandleNullAuthenticationGracefully() throws ExecutionException, InterruptedException {
            // When
            CompletableFuture<RetryExceptionResult> future = mutationResolver.retryException(validRetryInput, null);
            RetryExceptionResult result = future.get();

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INSUFFICIENT_PERMISSIONS.getCode());
        }

        @Test
        @DisplayName("Should handle null input gracefully")
        void shouldHandleNullInputGracefully() throws ExecutionException, InterruptedException {
            // When
            CompletableFuture<RetryExceptionResult> future = mutationResolver.retryException(null, authentication);
            RetryExceptionResult result = future.get();

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
        }

        private com.arcone.biopro.exception.collector.application.dto.RetryResponse createSuccessfulRetryResponse() {
            return com.arcone.biopro.exception.collector.application.dto.RetryResponse.builder()
                    .success(true)
                    .exception(testException)
                    .retryAttempt(testRetryAttempt)
                    .message("Retry initiated successfully")
                    .build();
        }
    }

    @Nested
    @DisplayName("Acknowledge Exception Mutation Tests")
    class AcknowledgeExceptionMutationTests {

        @Test
        @DisplayName("Should successfully acknowledge exception with valid input")
        void shouldSuccessfullyAcknowledgeExceptionWithValidInput() throws ExecutionException, InterruptedException {
            // Given
            ValidationResult validationResult = ValidationResult.success("acknowledge", "TXN-123");
            when(acknowledgmentValidationService.validateAcknowledgmentOperation(validAcknowledgeInput, authentication))
                    .thenReturn(validationResult);
            when(exceptionManagementService.acknowledgeException(eq("TXN-123"), any()))
                    .thenReturn(createSuccessfulAcknowledgeResponse());

            // When
            CompletableFuture<AcknowledgeExceptionResult> future = mutationResolver.acknowledgeException(validAcknowledgeInput, authentication);
            AcknowledgeExceptionResult result = future.get();

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getException()).isNotNull();
            assertThat(result.getErrors()).isEmpty();
            assertThat(result.getPerformedBy()).isEqualTo("test-user");

            verify(auditLogger).logAcknowledgmentAttempt("TXN-123", "test-user");
            verify(exceptionManagementService).acknowledgeException(eq("TXN-123"), any());
        }

        @Test
        @DisplayName("Should fail acknowledge exception with validation errors")
        void shouldFailAcknowledgeExceptionWithValidationErrors() throws ExecutionException, InterruptedException {
            // Given
            ValidationResult validationResult = ValidationResult.failure("acknowledge", "TXN-123", 
                    List.of(com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError.builder()
                            .code(MutationErrorCode.ALREADY_RESOLVED.getCode())
                            .message("Exception is already resolved")
                            .build()));
            when(acknowledgmentValidationService.validateAcknowledgmentOperation(validAcknowledgeInput, authentication))
                    .thenReturn(validationResult);

            // When
            CompletableFuture<AcknowledgeExceptionResult> future = mutationResolver.acknowledgeException(validAcknowledgeInput, authentication);
            AcknowledgeExceptionResult result = future.get();

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getException()).isNull();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.ALREADY_RESOLVED.getCode());

            verify(auditLogger).logAcknowledgmentAttempt("TXN-123", "test-user");
        }

        @Test
        @DisplayName("Should handle exception management service exception gracefully")
        void shouldHandleExceptionManagementServiceExceptionGracefully() throws ExecutionException, InterruptedException {
            // Given
            ValidationResult validationResult = ValidationResult.success("acknowledge", "TXN-123");
            when(acknowledgmentValidationService.validateAcknowledgmentOperation(validAcknowledgeInput, authentication))
                    .thenReturn(validationResult);
            when(exceptionManagementService.acknowledgeException(eq("TXN-123"), any()))
                    .thenThrow(new RuntimeException("Database error"));

            // When
            CompletableFuture<AcknowledgeExceptionResult> future = mutationResolver.acknowledgeException(validAcknowledgeInput, authentication);
            AcknowledgeExceptionResult result = future.get();

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getMessage()).contains("Database error");
        }

        private com.arcone.biopro.exception.collector.application.dto.AcknowledgeResponse createSuccessfulAcknowledgeResponse() {
            testException.setStatus(ExceptionStatus.ACKNOWLEDGED);
            testException.setAcknowledgedBy("test-user");
            testException.setAcknowledgedAt(OffsetDateTime.now());

            return com.arcone.biopro.exception.collector.application.dto.AcknowledgeResponse.builder()
                    .success(true)
                    .exception(testException)
                    .message("Exception acknowledged successfully")
                    .build();
        }
    }

    @Nested
    @DisplayName("Resolve Exception Mutation Tests")
    class ResolveExceptionMutationTests {

        @Test
        @DisplayName("Should successfully resolve exception with valid input")
        void shouldSuccessfullyResolveExceptionWithValidInput() throws ExecutionException, InterruptedException {
            // Given
            ValidationResult validationResult = ValidationResult.success("resolve", "TXN-123");
            when(resolutionValidationService.validateResolutionOperation(validResolveInput, authentication))
                    .thenReturn(validationResult);
            when(exceptionManagementService.resolveException(eq("TXN-123"), any()))
                    .thenReturn(createSuccessfulResolveResponse());

            // When
            CompletableFuture<ResolveExceptionResult> future = mutationResolver.resolveException(validResolveInput, authentication);
            ResolveExceptionResult result = future.get();

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getException()).isNotNull();
            assertThat(result.getErrors()).isEmpty();
            assertThat(result.getPerformedBy()).isEqualTo("test-user");

            verify(auditLogger).logResolutionAttempt("TXN-123", "test-user");
            verify(exceptionManagementService).resolveException(eq("TXN-123"), any());
        }

        @Test
        @DisplayName("Should fail resolve exception with validation errors")
        void shouldFailResolveExceptionWithValidationErrors() throws ExecutionException, InterruptedException {
            // Given
            ValidationResult validationResult = ValidationResult.failure("resolve", "TXN-123", 
                    List.of(com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError.builder()
                            .code(MutationErrorCode.INVALID_RESOLUTION_METHOD.getCode())
                            .message("Invalid resolution method")
                            .build()));
            when(resolutionValidationService.validateResolutionOperation(validResolveInput, authentication))
                    .thenReturn(validationResult);

            // When
            CompletableFuture<ResolveExceptionResult> future = mutationResolver.resolveException(validResolveInput, authentication);
            ResolveExceptionResult result = future.get();

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getException()).isNull();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_RESOLUTION_METHOD.getCode());

            verify(auditLogger).logResolutionAttempt("TXN-123", "test-user");
        }

        @Test
        @DisplayName("Should handle resolution service exception gracefully")
        void shouldHandleResolutionServiceExceptionGracefully() throws ExecutionException, InterruptedException {
            // Given
            ValidationResult validationResult = ValidationResult.success("resolve", "TXN-123");
            when(resolutionValidationService.validateResolutionOperation(validResolveInput, authentication))
                    .thenReturn(validationResult);
            when(exceptionManagementService.resolveException(eq("TXN-123"), any()))
                    .thenThrow(new RuntimeException("Resolution failed"));

            // When
            CompletableFuture<ResolveExceptionResult> future = mutationResolver.resolveException(validResolveInput, authentication);
            ResolveExceptionResult result = future.get();

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getMessage()).contains("Resolution failed");
        }

        private com.arcone.biopro.exception.collector.application.dto.ResolveResponse createSuccessfulResolveResponse() {
            testException.setStatus(ExceptionStatus.RESOLVED);
            testException.setResolvedBy("test-user");
            testException.setResolvedAt(OffsetDateTime.now());

            return com.arcone.biopro.exception.collector.application.dto.ResolveResponse.builder()
                    .success(true)
                    .exception(testException)
                    .message("Exception resolved successfully")
                    .build();
        }
    }

    @Nested
    @DisplayName("Cancel Retry Mutation Tests")
    class CancelRetryMutationTests {

        @Test
        @DisplayName("Should successfully cancel retry with valid input")
        void shouldSuccessfullyCancelRetryWithValidInput() throws ExecutionException, InterruptedException {
            // Given
            String transactionId = "TXN-123";
            String reason = "User requested cancellation";
            ValidationResult validationResult = ValidationResult.success("cancel_retry", transactionId);
            
            when(cancelRetryValidationService.validateCancelRetryOperation(transactionId, reason, authentication))
                    .thenReturn(validationResult);
            when(retryService.cancelRetry(eq(transactionId), any()))
                    .thenReturn(createSuccessfulCancelResponse());

            // When
            CompletableFuture<CancelRetryResult> future = mutationResolver.cancelRetry(transactionId, reason, authentication);
            CancelRetryResult result = future.get();

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getException()).isNotNull();
            assertThat(result.getErrors()).isEmpty();
            assertThat(result.getPerformedBy()).isEqualTo("test-user");

            verify(auditLogger).logCancellationAttempt(transactionId, "test-user");
            verify(retryService).cancelRetry(eq(transactionId), any());
        }

        @Test
        @DisplayName("Should fail cancel retry with validation errors")
        void shouldFailCancelRetryWithValidationErrors() throws ExecutionException, InterruptedException {
            // Given
            String transactionId = "TXN-123";
            String reason = "User requested cancellation";
            ValidationResult validationResult = ValidationResult.failure("cancel_retry", transactionId, 
                    List.of(com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError.builder()
                            .code(MutationErrorCode.NO_PENDING_RETRY.getCode())
                            .message("No pending retry found")
                            .build()));
            
            when(cancelRetryValidationService.validateCancelRetryOperation(transactionId, reason, authentication))
                    .thenReturn(validationResult);

            // When
            CompletableFuture<CancelRetryResult> future = mutationResolver.cancelRetry(transactionId, reason, authentication);
            CancelRetryResult result = future.get();

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getException()).isNull();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.NO_PENDING_RETRY.getCode());

            verify(auditLogger).logCancellationAttempt(transactionId, "test-user");
        }

        @Test
        @DisplayName("Should handle cancel retry service exception gracefully")
        void shouldHandleCancelRetryServiceExceptionGracefully() throws ExecutionException, InterruptedException {
            // Given
            String transactionId = "TXN-123";
            String reason = "User requested cancellation";
            ValidationResult validationResult = ValidationResult.success("cancel_retry", transactionId);
            
            when(cancelRetryValidationService.validateCancelRetryOperation(transactionId, reason, authentication))
                    .thenReturn(validationResult);
            when(retryService.cancelRetry(eq(transactionId), any()))
                    .thenThrow(new RuntimeException("Cancellation failed"));

            // When
            CompletableFuture<CancelRetryResult> future = mutationResolver.cancelRetry(transactionId, reason, authentication);
            CancelRetryResult result = future.get();

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getMessage()).contains("Cancellation failed");
        }

        private com.arcone.biopro.exception.collector.application.dto.CancelRetryResponse createSuccessfulCancelResponse() {
            testRetryAttempt.setStatus(RetryStatus.CANCELLED);
            testRetryAttempt.setCancelledAt(OffsetDateTime.now());
            testRetryAttempt.setCancelledBy("test-user");

            return com.arcone.biopro.exception.collector.application.dto.CancelRetryResponse.builder()
                    .success(true)
                    .exception(testException)
                    .cancelledRetryAttempt(testRetryAttempt)
                    .message("Retry cancelled successfully")
                    .build();
        }
    }

    @Nested
    @DisplayName("Audit Logging Tests")
    class AuditLoggingTests {

        @Test
        @DisplayName("Should log audit events even when validation fails")
        void shouldLogAuditEventsEvenWhenValidationFails() throws ExecutionException, InterruptedException {
            // Given
            ValidationResult validationResult = ValidationResult.failure("retry", "TXN-123", 
                    List.of(com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError.builder()
                            .code(MutationErrorCode.NOT_RETRYABLE.getCode())
                            .message("Exception is not retryable")
                            .build()));
            when(retryValidationService.validateRetryOperation(validRetryInput, authentication))
                    .thenReturn(validationResult);

            // When
            CompletableFuture<RetryExceptionResult> future = mutationResolver.retryException(validRetryInput, authentication);
            future.get();

            // Then
            verify(auditLogger).logRetryAttempt("TXN-123", "test-user");
        }

        @Test
        @DisplayName("Should handle audit logging exceptions gracefully")
        void shouldHandleAuditLoggingExceptionsGracefully() throws ExecutionException, InterruptedException {
            // Given
            ValidationResult validationResult = ValidationResult.success("retry", "TXN-123");
            when(retryValidationService.validateRetryOperation(validRetryInput, authentication))
                    .thenReturn(validationResult);
            when(retryService.initiateRetry(eq("TXN-123"), any()))
                    .thenReturn(createSuccessfulRetryResponse());
            doThrow(new RuntimeException("Audit logging failed"))
                    .when(auditLogger).logRetryAttempt(anyString(), anyString());

            // When
            CompletableFuture<RetryExceptionResult> future = mutationResolver.retryException(validRetryInput, authentication);
            RetryExceptionResult result = future.get();

            // Then
            assertThat(result.isSuccess()).isTrue(); // Should still succeed despite audit logging failure
        }

        private com.arcone.biopro.exception.collector.application.dto.RetryResponse createSuccessfulRetryResponse() {
            return com.arcone.biopro.exception.collector.application.dto.RetryResponse.builder()
                    .success(true)
                    .exception(testException)
                    .retryAttempt(testRetryAttempt)
                    .message("Retry initiated successfully")
                    .build();
        }
    }

    @Nested
    @DisplayName("Concurrent Operation Tests")
    class ConcurrentOperationTests {

        @Test
        @DisplayName("Should handle concurrent retry requests gracefully")
        void shouldHandleConcurrentRetryRequestsGracefully() throws ExecutionException, InterruptedException {
            // Given
            ValidationResult validationResult = ValidationResult.success("retry", "TXN-123");
            when(retryValidationService.validateRetryOperation(validRetryInput, authentication))
                    .thenReturn(validationResult);
            when(retryService.initiateRetry(eq("TXN-123"), any()))
                    .thenReturn(createSuccessfulRetryResponse());

            // When - Simulate concurrent requests
            CompletableFuture<RetryExceptionResult> future1 = mutationResolver.retryException(validRetryInput, authentication);
            CompletableFuture<RetryExceptionResult> future2 = mutationResolver.retryException(validRetryInput, authentication);

            RetryExceptionResult result1 = future1.get();
            RetryExceptionResult result2 = future2.get();

            // Then
            assertThat(result1.isSuccess()).isTrue();
            assertThat(result2.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should handle concurrent operations on different transactions")
        void shouldHandleConcurrentOperationsOnDifferentTransactions() throws ExecutionException, InterruptedException {
            // Given
            RetryExceptionInput input1 = RetryExceptionInput.builder()
                    .transactionId("TXN-001")
                    .reason("Test retry 1")
                    .build();
            RetryExceptionInput input2 = RetryExceptionInput.builder()
                    .transactionId("TXN-002")
                    .reason("Test retry 2")
                    .build();

            when(retryValidationService.validateRetryOperation(any(), eq(authentication)))
                    .thenReturn(ValidationResult.success("retry", "TXN-001"))
                    .thenReturn(ValidationResult.success("retry", "TXN-002"));
            when(retryService.initiateRetry(anyString(), any()))
                    .thenReturn(createSuccessfulRetryResponse());

            // When
            CompletableFuture<RetryExceptionResult> future1 = mutationResolver.retryException(input1, authentication);
            CompletableFuture<RetryExceptionResult> future2 = mutationResolver.retryException(input2, authentication);

            RetryExceptionResult result1 = future1.get();
            RetryExceptionResult result2 = future2.get();

            // Then
            assertThat(result1.isSuccess()).isTrue();
            assertThat(result2.isSuccess()).isTrue();
        }

        private com.arcone.biopro.exception.collector.application.dto.RetryResponse createSuccessfulRetryResponse() {
            return com.arcone.biopro.exception.collector.application.dto.RetryResponse.builder()
                    .success(true)
                    .exception(testException)
                    .retryAttempt(testRetryAttempt)
                    .message("Retry initiated successfully")
                    .build();
        }
    }

    @Nested
    @DisplayName("Error Handling Edge Cases")
    class ErrorHandlingEdgeCasesTests {

        @Test
        @DisplayName("Should handle CompletableFuture exceptions gracefully")
        void shouldHandleCompletableFutureExceptionsGracefully() {
            // Given
            ValidationResult validationResult = ValidationResult.success("retry", "TXN-123");
            when(retryValidationService.validateRetryOperation(validRetryInput, authentication))
                    .thenReturn(validationResult);
            when(retryService.initiateRetry(eq("TXN-123"), any()))
                    .thenThrow(new RuntimeException("Unexpected error"));

            // When
            CompletableFuture<RetryExceptionResult> future = mutationResolver.retryException(validRetryInput, authentication);

            // Then
            assertThatThrownBy(() -> future.get())
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should handle null validation result gracefully")
        void shouldHandleNullValidationResultGracefully() throws ExecutionException, InterruptedException {
            // Given
            when(retryValidationService.validateRetryOperation(validRetryInput, authentication))
                    .thenReturn(null);

            // When
            CompletableFuture<RetryExceptionResult> future = mutationResolver.retryException(validRetryInput, authentication);
            RetryExceptionResult result = future.get();

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.DATABASE_ERROR.getCode());
        }

        @Test
        @DisplayName("Should handle validation service exceptions gracefully")
        void shouldHandleValidationServiceExceptionsGracefully() throws ExecutionException, InterruptedException {
            // Given
            when(retryValidationService.validateRetryOperation(validRetryInput, authentication))
                    .thenThrow(new RuntimeException("Validation service error"));

            // When
            CompletableFuture<RetryExceptionResult> future = mutationResolver.retryException(validRetryInput, authentication);
            RetryExceptionResult result = future.get();

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getMessage()).contains("Validation service error");
        }
    }
}
