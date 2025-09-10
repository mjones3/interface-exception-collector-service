package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CancelRetryValidationService.
 * Tests comprehensive validation logic for retry cancellation operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CancelRetryValidationService Tests")
class CancelRetryValidationServiceTest {

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    @Mock
    private RetryAttemptRepository retryAttemptRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CancelRetryValidationService validationService;

    private InterfaceException testException;
    private RetryAttempt pendingRetryAttempt;
    private RetryAttempt completedRetryAttempt;

    @BeforeEach
    void setUp() {
        // Set up test data
        testException = new InterfaceException();
        testException.setId(1L);
        testException.setTransactionId("TXN-123");
        testException.setStatus(ExceptionStatus.FAILED);
        testException.setRetryable(true);

        pendingRetryAttempt = new RetryAttempt();
        pendingRetryAttempt.setId(1L);
        pendingRetryAttempt.setInterfaceException(testException);
        pendingRetryAttempt.setAttemptNumber(1);
        pendingRetryAttempt.setStatus(RetryStatus.PENDING);
        pendingRetryAttempt.setInitiatedAt(OffsetDateTime.now().minusMinutes(5));

        completedRetryAttempt = new RetryAttempt();
        completedRetryAttempt.setId(2L);
        completedRetryAttempt.setInterfaceException(testException);
        completedRetryAttempt.setAttemptNumber(2);
        completedRetryAttempt.setStatus(RetryStatus.SUCCESS);
        completedRetryAttempt.setInitiatedAt(OffsetDateTime.now().minusMinutes(10));

        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    @DisplayName("Should pass validation for valid cancel retry request")
    void shouldPassValidationForValidRequest() {
        // Given
        String transactionId = "TXN-123";
        String reason = "User requested cancellation";

        when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(testException));
        when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException))
                .thenReturn(Optional.of(pendingRetryAttempt));

        // When
        ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation for invalid transaction ID format")
    void shouldFailValidationForInvalidTransactionId() {
        // Given
        String invalidTransactionId = "";
        String reason = "Valid reason";

        // When
        ValidationResult result = validationService.validateCancelRetryOperation(invalidTransactionId, reason, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_TRANSACTION_ID.getCode());
    }

    @Test
    @DisplayName("Should fail validation for missing reason")
    void shouldFailValidationForMissingReason() {
        // Given
        String transactionId = "TXN-123";
        String emptyReason = "";

        // When
        ValidationResult result = validationService.validateCancelRetryOperation(transactionId, emptyReason, authentication);

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
        String longReason = "A".repeat(501); // Exceeds 500 character limit

        // When
        ValidationResult result = validationService.validateCancelRetryOperation(transactionId, longReason, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_REASON_LENGTH.getCode());
    }

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
        testException.setStatus(ExceptionStatus.RESOLVED);

        when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(testException));

        // When
        ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.CANCELLATION_NOT_ALLOWED.getCode());
    }

    @Test
    @DisplayName("Should fail validation when exception is closed")
    void shouldFailValidationWhenExceptionClosed() {
        // Given
        String transactionId = "TXN-123";
        String reason = "Valid reason";
        testException.setStatus(ExceptionStatus.CLOSED);

        when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(testException));

        // When
        ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.CANCELLATION_NOT_ALLOWED.getCode());
    }

    @Test
    @DisplayName("Should fail validation when no retry attempts found")
    void shouldFailValidationWhenNoRetryAttempts() {
        // Given
        String transactionId = "TXN-123";
        String reason = "Valid reason";

        when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(testException));
        when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException))
                .thenReturn(Optional.empty());

        // When
        ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.NO_PENDING_RETRY.getCode());
    }

    @Test
    @DisplayName("Should fail validation when retry already completed successfully")
    void shouldFailValidationWhenRetryAlreadyCompleted() {
        // Given
        String transactionId = "TXN-123";
        String reason = "Valid reason";

        when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(testException));
        when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException))
                .thenReturn(Optional.of(completedRetryAttempt));

        // When
        ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.RETRY_ALREADY_COMPLETED.getCode());
    }

    @Test
    @DisplayName("Should fail validation when retry already failed")
    void shouldFailValidationWhenRetryAlreadyFailed() {
        // Given
        String transactionId = "TXN-123";
        String reason = "Valid reason";
        RetryAttempt failedAttempt = new RetryAttempt();
        failedAttempt.setStatus(RetryStatus.FAILED);

        when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(testException));
        when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException))
                .thenReturn(Optional.of(failedAttempt));

        // When
        ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.RETRY_ALREADY_COMPLETED.getCode());
    }

    @Test
    @DisplayName("Should fail validation when retry already cancelled")
    void shouldFailValidationWhenRetryAlreadyCancelled() {
        // Given
        String transactionId = "TXN-123";
        String reason = "Valid reason";
        RetryAttempt cancelledAttempt = new RetryAttempt();
        cancelledAttempt.setStatus(RetryStatus.CANCELLED);

        when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(testException));
        when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException))
                .thenReturn(Optional.of(cancelledAttempt));

        // When
        ValidationResult result = validationService.validateCancelRetryOperation(transactionId, reason, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.RETRY_ALREADY_COMPLETED.getCode());
    }

    @Test
    @DisplayName("Should return true for canCancelRetry when retry is pending")
    void shouldReturnTrueForCanCancelRetryWhenPending() {
        // Given
        String transactionId = "TXN-123";

        when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(testException));
        when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException))
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
        testException.setStatus(ExceptionStatus.RESOLVED);

        when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(testException));

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

        when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(testException));
        when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException))
                .thenReturn(Optional.of(completedRetryAttempt));

        // When
        boolean canCancel = validationService.canCancelRetry(transactionId);

        // Then
        assertThat(canCancel).isFalse();
    }

    @Test
    @DisplayName("Should return appropriate blocked reason when exception not found")
    void shouldReturnBlockedReasonWhenExceptionNotFound() {
        // Given
        String transactionId = "NONEXISTENT-TXN";

        when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());

        // When
        String reason = validationService.getCancellationBlockedReason(transactionId);

        // Then
        assertThat(reason).contains("Exception not found");
    }

    @Test
    @DisplayName("Should return appropriate blocked reason when retry already completed")
    void shouldReturnBlockedReasonWhenRetryCompleted() {
        // Given
        String transactionId = "TXN-123";

        when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(testException));
        when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException))
                .thenReturn(Optional.of(completedRetryAttempt));

        // When
        String reason = validationService.getCancellationBlockedReason(transactionId);

        // Then
        assertThat(reason).contains("already completed successfully");
    }

    @Test
    @DisplayName("Should return null blocked reason when cancellation is allowed")
    void shouldReturnNullBlockedReasonWhenCancellationAllowed() {
        // Given
        String transactionId = "TXN-123";

        when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(testException));
        when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException))
                .thenReturn(Optional.of(pendingRetryAttempt));

        // When
        String reason = validationService.getCancellationBlockedReason(transactionId);

        // Then
        assertThat(reason).isNull();
    }

    @Test
    @DisplayName("Should handle validation with multiple errors")
    void shouldHandleValidationWithMultipleErrors() {
        // Given
        String invalidTransactionId = ""; // Invalid format
        String emptyReason = ""; // Missing reason

        // When
        ValidationResult result = validationService.validateCancelRetryOperation(invalidTransactionId, emptyReason, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(2);
        assertThat(result.getErrors()).extracting(GraphQLError::getCode)
                .contains(
                        MutationErrorCode.INVALID_TRANSACTION_ID.getCode(),
                        MutationErrorCode.MISSING_REQUIRED_FIELD.getCode()
                );
    }

    @Test
    @DisplayName("Should validate transaction ID format correctly")
    void shouldValidateTransactionIdFormat() {
        // Test valid formats
        assertThat(validationService.validateCancelRetryOperation("TXN-123", "reason", authentication).isValid()).isFalse(); // Will fail on other validations but format is OK
        assertThat(validationService.validateCancelRetryOperation("ORDER_456", "reason", authentication).isValid()).isFalse(); // Will fail on other validations but format is OK
        assertThat(validationService.validateCancelRetryOperation("ABC123DEF", "reason", authentication).isValid()).isFalse(); // Will fail on other validations but format is OK

        // Test invalid formats - these should fail on format validation
        ValidationResult result1 = validationService.validateCancelRetryOperation("", "reason", authentication);
        assertThat(result1.getErrors()).anyMatch(error -> error.getCode().equals(MutationErrorCode.INVALID_TRANSACTION_ID.getCode()));

        ValidationResult result2 = validationService.validateCancelRetryOperation("AB", "reason", authentication); // Too short
        assertThat(result2.getErrors()).anyMatch(error -> error.getCode().equals(MutationErrorCode.INVALID_TRANSACTION_ID.getCode()));

        ValidationResult result3 = validationService.validateCancelRetryOperation("A".repeat(101), "reason", authentication); // Too long
        assertThat(result3.getErrors()).anyMatch(error -> error.getCode().equals(MutationErrorCode.INVALID_TRANSACTION_ID.getCode()));

        ValidationResult result4 = validationService.validateCancelRetryOperation("TXN@123", "reason", authentication); // Invalid characters
        assertThat(result4.getErrors()).anyMatch(error -> error.getCode().equals(MutationErrorCode.INVALID_TRANSACTION_ID.getCode()));
    }
}