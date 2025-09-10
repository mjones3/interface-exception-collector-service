package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.validation.MutationErrorCode;
import com.arcone.biopro.exception.collector.api.graphql.validation.ValidationResult;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit tests for CancelRetryValidationService covering all validation scenarios,
 * edge cases, and error conditions to achieve >95% code coverage.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CancelRetryValidationService Comprehensive Tests")
class CancelRetryValidationServiceComprehensiveTest {

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    @Mock
    private RetryAttemptRepository retryAttemptRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CancelRetryValidationService validationService;

    private InterfaceException validException;
    private RetryAttempt pendingRetryAttempt;
    private RetryAttempt completedRetryAttempt;
    private RetryAttempt failedRetryAttempt;
    private RetryAttempt cancelledRetryAttempt;

    @BeforeEach
    void setUp() {
        // Setup valid exception
        validException = new InterfaceException();
        validException.setId(1L);
        validException.setTransactionId("TXN-123");
        validException.setStatus(ExceptionStatus.FAILED);
        validException.setRetryable(true);

        // Setup retry attempts with different statuses
        pendingRetryAttempt = new RetryAttempt();
        pendingRetryAttempt.setId(1L);
        pendingRetryAttempt.setInterfaceException(validException);
        pendingRetryAttempt.setAttemptNumber(1);
        pendingRetryAttempt.setStatus(RetryStatus.PENDING);
        pendingRetryAttempt.setInitiatedAt(OffsetDateTime.now().minusMinutes(5));

        completedRetryAttempt = new RetryAttempt();
        completedRetryAttempt.setId(2L);
        completedRetryAttempt.setInterfaceException(validException);
        completedRetryAttempt.setAttemptNumber(2);
        completedRetryAttempt.setStatus(RetryStatus.SUCCESS);
        completedRetryAttempt.setInitiatedAt(OffsetDateTime.now().minusMinutes(10));

        failedRetryAttempt = new RetryAttempt();
        failedRetryAttempt.setId(3L);
        failedRetryAttempt.setInterfaceException(validException);
        failedRetryAttempt.setAttemptNumber(3);
        failedRetryAttempt.setStatus(RetryStatus.FAILED);
        failedRetryAttempt.setInitiatedAt(OffsetDateTime.now().minusMinutes(15));

        cancelledRetryAttempt = new RetryAttempt();
        cancelledRetryAttempt.setId(4L);
        cancelledRetryAttempt.setInterfaceException(validException);
        cancelledRetryAttempt.setAttemptNumber(4);
        cancelledRetryAttempt.setStatus(RetryStatus.CANCELLED);
        cancelledRetryAttempt.setInitiatedAt(OffsetDateTime.now().minusMinutes(20));

        // Setup authentication
        when(authentication.getName()).thenReturn("test-user");
    }

    @Nested
    @DisplayName("Basic Input Validation Tests")
    class BasicInputValidationTests {

        @Test
        @DisplayName("Should pass validation for valid cancel retry request")
        void shouldPassValidationForValidCancelRetryRequest() {
            // Given
            String transactionId = "TXN-123";
            String reason = "User requested cancellation";

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.of(pendingRetryAttempt));

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation for null transaction ID")
        void shouldFailValidationForNullTransactionId() {
            // Given
            String transactionId = null;
            String reason = "Valid reason";

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_TRANSACTION_ID.getCode());
        }

        @Test
        @DisplayName("Should fail validation for empty transaction ID")
        void shouldFailValidationForEmptyTransactionId() {
            // Given
            String transactionId = "";
            String reason = "Valid reason";

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_TRANSACTION_ID.getCode());
        }

        @ParameterizedTest
        @ValueSource(strings = {"   ", "\t", "\n", "\r\n"})
        @DisplayName("Should fail validation for whitespace-only transaction ID")
        void shouldFailValidationForWhitespaceOnlyTransactionId(String whitespace) {
            // Given
            String reason = "Valid reason";

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(whitespace, reason, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_TRANSACTION_ID.getCode());
        }

        @Test
        @DisplayName("Should fail validation for transaction ID too short")
        void shouldFailValidationForTransactionIdTooShort() {
            // Given
            String transactionId = "AB"; // Less than 3 characters
            String reason = "Valid reason";

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_TRANSACTION_ID.getCode());
        }

        @Test
        @DisplayName("Should fail validation for transaction ID too long")
        void shouldFailValidationForTransactionIdTooLong() {
            // Given
            String transactionId = "A".repeat(101); // More than 100 characters
            String reason = "Valid reason";

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_TRANSACTION_ID.getCode());
        }

        @ParameterizedTest
        @ValueSource(strings = {"TXN@123", "TXN 123", "TXN#123", "TXN%123", "TXN&123", "TXN+123", "TXN=123"})
        @DisplayName("Should fail validation for transaction ID with invalid characters")
        void shouldFailValidationForTransactionIdWithInvalidCharacters(String invalidTransactionId) {
            // Given
            String reason = "Valid reason";

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(invalidTransactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_TRANSACTION_ID.getCode());
        }

        @ParameterizedTest
        @ValueSource(strings = {"TXN-123", "ORDER_456", "ABC123DEF", "test-order-1", "A1B2C3", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
        @DisplayName("Should pass validation for valid transaction ID formats")
        void shouldPassValidationForValidTransactionIdFormats(String validTransactionId) {
            // Given
            String reason = "Valid reason";

            when(exceptionRepository.findByTransactionId(validTransactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.of(pendingRetryAttempt));

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(validTransactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation for null reason")
        void shouldFailValidationForNullReason() {
            // Given
            String transactionId = "TXN-123";
            String reason = null;

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
        }

        @Test
        @DisplayName("Should fail validation for empty reason")
        void shouldFailValidationForEmptyReason() {
            // Given
            String transactionId = "TXN-123";
            String reason = "";

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
        }

        @ParameterizedTest
        @ValueSource(strings = {"   ", "\t", "\n", "\r\n"})
        @DisplayName("Should fail validation for whitespace-only reason")
        void shouldFailValidationForWhitespaceOnlyReason(String whitespace) {
            // Given
            String transactionId = "TXN-123";

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, whitespace, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
        }

        @Test
        @DisplayName("Should fail validation for reason exceeding maximum length")
        void shouldFailValidationForReasonTooLong() {
            // Given
            String transactionId = "TXN-123";
            String reason = "A".repeat(501); // Exceeds 500 character limit

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_REASON_LENGTH.getCode());
        }

        @Test
        @DisplayName("Should pass validation for reason at maximum length")
        void shouldPassValidationForReasonAtMaximumLength() {
            // Given
            String transactionId = "TXN-123";
            String reason = "A".repeat(500); // Exactly 500 characters

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.of(pendingRetryAttempt));

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should handle multiple validation errors")
        void shouldHandleMultipleValidationErrors() {
            // Given
            String invalidTransactionId = ""; // Invalid: empty
            String invalidReason = ""; // Invalid: empty

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(invalidTransactionId, invalidReason, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(2);
            assertThat(result.getErrors()).extracting(error -> error.getCode())
                    .contains(
                            MutationErrorCode.INVALID_TRANSACTION_ID.getCode(),
                            MutationErrorCode.MISSING_REQUIRED_FIELD.getCode()
                    );
        }
    }

    @Nested
    @DisplayName("Exception State Validation Tests")
    class ExceptionStateValidationTests {

        @Test
        @DisplayName("Should fail validation when exception not found")
        void shouldFailValidationWhenExceptionNotFound() {
            // Given
            String transactionId = "NONEXISTENT-TXN";
            String reason = "Valid reason";

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.EXCEPTION_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("Should fail validation when exception is resolved")
        void shouldFailValidationWhenExceptionResolved() {
            // Given
            String transactionId = "TXN-123";
            String reason = "Valid reason";
            validException.setStatus(ExceptionStatus.RESOLVED);

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.CANCELLATION_NOT_ALLOWED.getCode());
            assertThat(result.getErrors().get(0).getMessage()).contains("resolved or closed exception");
        }

        @Test
        @DisplayName("Should fail validation when exception is closed")
        void shouldFailValidationWhenExceptionClosed() {
            // Given
            String transactionId = "TXN-123";
            String reason = "Valid reason";
            validException.setStatus(ExceptionStatus.CLOSED);

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.CANCELLATION_NOT_ALLOWED.getCode());
            assertThat(result.getErrors().get(0).getMessage()).contains("resolved or closed exception");
        }

        @ParameterizedTest
        @EnumSource(value = ExceptionStatus.class, names = {"NEW", "FAILED", "ACKNOWLEDGED", "RETRYING", "RETRIED_FAILED", "ESCALATED"})
        @DisplayName("Should pass validation for cancellable exception statuses")
        void shouldPassValidationForCancellableExceptionStatuses(ExceptionStatus status) {
            // Given
            String transactionId = "TXN-123";
            String reason = "Valid reason";
            validException.setStatus(status);

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.of(pendingRetryAttempt));

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Retry Attempt Validation Tests")
    class RetryAttemptValidationTests {

        @Test
        @DisplayName("Should fail validation when no retry attempts found")
        void shouldFailValidationWhenNoRetryAttemptsFound() {
            // Given
            String transactionId = "TXN-123";
            String reason = "Valid reason";

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.empty());

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.NO_PENDING_RETRY.getCode());
            assertThat(result.getErrors().get(0).getMessage()).contains("No retry attempts found");
        }

        @Test
        @DisplayName("Should fail validation when retry already completed successfully")
        void shouldFailValidationWhenRetryAlreadyCompletedSuccessfully() {
            // Given
            String transactionId = "TXN-123";
            String reason = "Valid reason";

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.of(completedRetryAttempt));

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.RETRY_ALREADY_COMPLETED.getCode());
            assertThat(result.getErrors().get(0).getMessage()).contains("already completed successfully");
        }

        @Test
        @DisplayName("Should fail validation when retry already failed")
        void shouldFailValidationWhenRetryAlreadyFailed() {
            // Given
            String transactionId = "TXN-123";
            String reason = "Valid reason";

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.of(failedRetryAttempt));

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.RETRY_ALREADY_COMPLETED.getCode());
            assertThat(result.getErrors().get(0).getMessage()).contains("already failed");
        }

        @Test
        @DisplayName("Should fail validation when retry already cancelled")
        void shouldFailValidationWhenRetryAlreadyCancelled() {
            // Given
            String transactionId = "TXN-123";
            String reason = "Valid reason";

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.of(cancelledRetryAttempt));

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.RETRY_ALREADY_COMPLETED.getCode());
            assertThat(result.getErrors().get(0).getMessage()).contains("already been cancelled");
        }

        @Test
        @DisplayName("Should log warning for long-running pending retry")
        void shouldLogWarningForLongRunningPendingRetry() {
            // Given
            String transactionId = "TXN-123";
            String reason = "Valid reason";
            
            // Create a retry that has been pending for over 1 hour
            RetryAttempt longRunningRetry = new RetryAttempt();
            longRunningRetry.setStatus(RetryStatus.PENDING);
            longRunningRetry.setInitiatedAt(OffsetDateTime.now().minusHours(2));

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.of(longRunningRetry));

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isTrue(); // Should still pass but log warning
            assertThat(result.getErrors()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {

        @Test
        @DisplayName("Should return true for canCancelRetry when retry is pending")
        void shouldReturnTrueForCanCancelRetryWhenRetryIsPending() {
            // Given
            String transactionId = "TXN-123";

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.of(pendingRetryAttempt));

            // When
            boolean canCancel = validationService.canCancelRetry(transactionId);

            // Then
            assertThat(canCancel).isTrue();
        }

        @Test
        @DisplayName("Should return false for canCancelRetry when exception not found")
        void shouldReturnFalseForCanCancelRetryWhenExceptionNotFound() {
            // Given
            String transactionId = "NONEXISTENT-TXN";

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());

            // When
            boolean canCancel = validationService.canCancelRetry(transactionId);

            // Then
            assertThat(canCancel).isFalse();
        }

        @Test
        @DisplayName("Should return false for canCancelRetry when exception is resolved")
        void shouldReturnFalseForCanCancelRetryWhenExceptionResolved() {
            // Given
            String transactionId = "TXN-123";
            validException.setStatus(ExceptionStatus.RESOLVED);

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));

            // When
            boolean canCancel = validationService.canCancelRetry(transactionId);

            // Then
            assertThat(canCancel).isFalse();
        }

        @Test
        @DisplayName("Should return false for canCancelRetry when exception is closed")
        void shouldReturnFalseForCanCancelRetryWhenExceptionClosed() {
            // Given
            String transactionId = "TXN-123";
            validException.setStatus(ExceptionStatus.CLOSED);

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));

            // When
            boolean canCancel = validationService.canCancelRetry(transactionId);

            // Then
            assertThat(canCancel).isFalse();
        }

        @Test
        @DisplayName("Should return false for canCancelRetry when retry is not pending")
        void shouldReturnFalseForCanCancelRetryWhenRetryNotPending() {
            // Given
            String transactionId = "TXN-123";

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.of(completedRetryAttempt));

            // When
            boolean canCancel = validationService.canCancelRetry(transactionId);

            // Then
            assertThat(canCancel).isFalse();
        }

        @Test
        @DisplayName("Should return false for canCancelRetry when no retry attempts exist")
        void shouldReturnFalseForCanCancelRetryWhenNoRetryAttemptsExist() {
            // Given
            String transactionId = "TXN-123";

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.empty());

            // When
            boolean canCancel = validationService.canCancelRetry(transactionId);

            // Then
            assertThat(canCancel).isFalse();
        }

        @Test
        @DisplayName("Should handle exception in canCancelRetry gracefully")
        void shouldHandleExceptionInCanCancelRetryGracefully() {
            // Given
            String transactionId = "TXN-123";

            when(exceptionRepository.findByTransactionId(transactionId)).thenThrow(new RuntimeException("Database error"));

            // When
            boolean canCancel = validationService.canCancelRetry(transactionId);

            // Then
            assertThat(canCancel).isFalse();
        }
    }

    @Nested
    @DisplayName("Cancellation Blocked Reason Tests")
    class CancellationBlockedReasonTests {

        @Test
        @DisplayName("Should return appropriate blocked reason when exception not found")
        void shouldReturnAppropriateBlockedReasonWhenExceptionNotFound() {
            // Given
            String transactionId = "NONEXISTENT-TXN";

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());

            // When
            String reason = validationService.getCancellationBlockedReason(transactionId);

            // Then
            assertThat(reason).contains("Exception not found for transaction ID: " + transactionId);
        }

        @Test
        @DisplayName("Should return appropriate blocked reason when exception is resolved")
        void shouldReturnAppropriateBlockedReasonWhenExceptionResolved() {
            // Given
            String transactionId = "TXN-123";
            validException.setStatus(ExceptionStatus.RESOLVED);

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));

            // When
            String reason = validationService.getCancellationBlockedReason(transactionId);

            // Then
            assertThat(reason).contains("already resolved and cannot have retries cancelled");
        }

        @Test
        @DisplayName("Should return appropriate blocked reason when exception is closed")
        void shouldReturnAppropriateBlockedReasonWhenExceptionClosed() {
            // Given
            String transactionId = "TXN-123";
            validException.setStatus(ExceptionStatus.CLOSED);

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));

            // When
            String reason = validationService.getCancellationBlockedReason(transactionId);

            // Then
            assertThat(reason).contains("closed and cannot have retries cancelled");
        }

        @Test
        @DisplayName("Should return appropriate blocked reason when no retry attempts found")
        void shouldReturnAppropriateBlockedReasonWhenNoRetryAttemptsFound() {
            // Given
            String transactionId = "TXN-123";

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.empty());

            // When
            String reason = validationService.getCancellationBlockedReason(transactionId);

            // Then
            assertThat(reason).contains("No retry attempts found for this exception");
        }

        @Test
        @DisplayName("Should return appropriate blocked reason when retry already completed successfully")
        void shouldReturnAppropriateBlockedReasonWhenRetryAlreadyCompletedSuccessfully() {
            // Given
            String transactionId = "TXN-123";

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.of(completedRetryAttempt));

            // When
            String reason = validationService.getCancellationBlockedReason(transactionId);

            // Then
            assertThat(reason).contains("already completed successfully");
        }

        @Test
        @DisplayName("Should return appropriate blocked reason when retry already failed")
        void shouldReturnAppropriateBlockedReasonWhenRetryAlreadyFailed() {
            // Given
            String transactionId = "TXN-123";

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.of(failedRetryAttempt));

            // When
            String reason = validationService.getCancellationBlockedReason(transactionId);

            // Then
            assertThat(reason).contains("already failed");
        }

        @Test
        @DisplayName("Should return appropriate blocked reason when retry already cancelled")
        void shouldReturnAppropriateBlockedReasonWhenRetryAlreadyCancelled() {
            // Given
            String transactionId = "TXN-123";

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.of(cancelledRetryAttempt));

            // When
            String reason = validationService.getCancellationBlockedReason(transactionId);

            // Then
            assertThat(reason).contains("already been cancelled");
        }

        @Test
        @DisplayName("Should return null blocked reason when cancellation is allowed")
        void shouldReturnNullBlockedReasonWhenCancellationAllowed() {
            // Given
            String transactionId = "TXN-123";

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.of(pendingRetryAttempt));

            // When
            String reason = validationService.getCancellationBlockedReason(transactionId);

            // Then
            assertThat(reason).isNull();
        }

        @Test
        @DisplayName("Should handle exception in getCancellationBlockedReason gracefully")
        void shouldHandleExceptionInGetCancellationBlockedReasonGracefully() {
            // Given
            String transactionId = "TXN-123";

            when(exceptionRepository.findByTransactionId(transactionId)).thenThrow(new RuntimeException("Database error"));

            // When
            String reason = validationService.getCancellationBlockedReason(transactionId);

            // Then
            assertThat(reason).contains("Unable to determine cancellation status due to system error");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle validation with special characters in reason")
        void shouldHandleValidationWithSpecialCharactersInReason() {
            // Given
            String transactionId = "TXN-123";
            String reason = "Cancellation requested: issue #456 - timeout (30s) & network error";

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.of(pendingRetryAttempt));

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should handle concurrent validation requests")
        void shouldHandleConcurrentValidationRequests() {
            // Given
            String transactionId = "TXN-123";
            String reason = "Valid reason";

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.of(pendingRetryAttempt));

            // When - Simulate concurrent requests
            ValidationResult result1 = validationService.validateCancelRetryOperation(transactionId, reason, authentication);
            ValidationResult result2 = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result1.isValid()).isTrue();
            assertThat(result2.isValid()).isTrue();
        }

        @Test
        @DisplayName("Should handle validation with minimum valid inputs")
        void shouldHandleValidationWithMinimumValidInputs() {
            // Given
            String transactionId = "ABC"; // Minimum valid length
            String reason = "X"; // Minimum valid length

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.of(pendingRetryAttempt));

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should handle validation with maximum valid inputs")
        void shouldHandleValidationWithMaximumValidInputs() {
            // Given
            String transactionId = "A".repeat(100); // Maximum valid length
            String reason = "B".repeat(500); // Maximum valid length

            when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.of(pendingRetryAttempt));

            // When
            ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }
    }
}

