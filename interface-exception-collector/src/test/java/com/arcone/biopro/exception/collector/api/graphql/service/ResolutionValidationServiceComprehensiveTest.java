package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.dto.ResolveExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.validation.MutationErrorCode;
import com.arcone.biopro.exception.collector.api.graphql.validation.ValidationResult;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.ResolutionMethod;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;

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

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit tests for ResolutionValidationService covering all validation scenarios,
 * edge cases, and error conditions to achieve >95% code coverage.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResolutionValidationService Comprehensive Tests")
class ResolutionValidationServiceComprehensiveTest {

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ResolutionValidationService validationService;

    private InterfaceException validException;
    private ResolveExceptionInput validInput;

    @BeforeEach
    void setUp() {
        // Setup valid exception
        validException = new InterfaceException();
        validException.setId(1L);
        validException.setTransactionId("TXN-123");
        validException.setStatus(ExceptionStatus.ACKNOWLEDGED);

        // Setup valid input
        validInput = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("Test resolution notes")
                .build();

        // Setup authentication
        when(authentication.getName()).thenReturn("test-user");
    }

    @Nested
    @DisplayName("Basic Input Validation Tests")
    class BasicInputValidationTests {

        @Test
        @DisplayName("Should pass validation for valid resolution input")
        void shouldPassValidationForValidResolutionInput() {
            // Given
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateResolutionOperation(validInput, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
            assertThat(result.getOperation()).isEqualTo("resolve");
            assertThat(result.getTransactionId()).isEqualTo("TXN-123");
        }

        @Test
        @DisplayName("Should fail validation for null transaction ID")
        void shouldFailValidationForNullTransactionId() {
            // Given
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId(null)
                    .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                    .build();

            // When
            ValidationResult result = validationService.validateResolutionOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
            assertThat(result.getErrors().get(0).getMessage()).contains("Transaction ID is required");
        }

        @Test
        @DisplayName("Should fail validation for empty transaction ID")
        void shouldFailValidationForEmptyTransactionId() {
            // Given
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId("")
                    .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                    .build();

            // When
            ValidationResult result = validationService.validateResolutionOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
        }

        @Test
        @DisplayName("Should fail validation for whitespace-only transaction ID")
        void shouldFailValidationForWhitespaceOnlyTransactionId() {
            // Given
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId("   ")
                    .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                    .build();

            // When
            ValidationResult result = validationService.validateResolutionOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
        }

        @Test
        @DisplayName("Should fail validation for transaction ID exceeding maximum length")
        void shouldFailValidationForTransactionIdTooLong() {
            // Given
            String longTransactionId = "A".repeat(256); // Exceeds 255 character limit
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId(longTransactionId)
                    .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                    .build();

            // When
            ValidationResult result = validationService.validateResolutionOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_FIELD_VALUE.getCode());
            assertThat(result.getErrors().get(0).getMessage()).contains("must not exceed 255 characters");
        }

        @Test
        @DisplayName("Should fail validation for null resolution method")
        void shouldFailValidationForNullResolutionMethod() {
            // Given
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId("TXN-123")
                    .resolutionMethod(null)
                    .build();

            // When
            ValidationResult result = validationService.validateResolutionOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
            assertThat(result.getErrors().get(0).getMessage()).contains("Resolution method is required");
        }

        @Test
        @DisplayName("Should fail validation for resolution notes exceeding maximum length")
        void shouldFailValidationForResolutionNotesTooLong() {
            // Given
            String longNotes = "A".repeat(2001); // Exceeds 2000 character limit
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId("TXN-123")
                    .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                    .resolutionNotes(longNotes)
                    .build();

            // When
            ValidationResult result = validationService.validateResolutionOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_NOTES_LENGTH.getCode());
            assertThat(result.getErrors().get(0).getMessage()).contains("must not exceed 2000 characters");
        }

        @Test
        @DisplayName("Should pass validation for null resolution notes")
        void shouldPassValidationForNullResolutionNotes() {
            // Given
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId("TXN-123")
                    .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                    .resolutionNotes(null)
                    .build();

            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateResolutionOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should handle multiple validation errors")
        void shouldHandleMultipleValidationErrors() {
            // Given
            String longTransactionId = "A".repeat(256);
            String longNotes = "B".repeat(2001);
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId(longTransactionId)
                    .resolutionMethod(null)
                    .resolutionNotes(longNotes)
                    .build();

            // When
            ValidationResult result = validationService.validateResolutionOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(3);
            assertThat(result.getErrors()).extracting(error -> error.getCode())
                    .contains(
                            MutationErrorCode.INVALID_FIELD_VALUE.getCode(),
                            MutationErrorCode.MISSING_REQUIRED_FIELD.getCode(),
                            MutationErrorCode.INVALID_NOTES_LENGTH.getCode()
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
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.empty());

            // When
            ValidationResult result = validationService.validateResolutionOperation(validInput, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.EXCEPTION_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("Should fail validation for already resolved exception")
        void shouldFailValidationForAlreadyResolvedException() {
            // Given
            validException.setStatus(ExceptionStatus.RESOLVED);
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateResolutionOperation(validInput, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.ALREADY_RESOLVED.getCode());
            assertThat(result.getErrors().get(0).getMessage()).contains("already resolved");
        }

        @Test
        @DisplayName("Should fail validation for closed exception")
        void shouldFailValidationForClosedException() {
            // Given
            validException.setStatus(ExceptionStatus.CLOSED);
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateResolutionOperation(validInput, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_EXCEPTION_STATE.getCode());
            assertThat(result.getErrors().get(0).getMessage()).contains("closed and cannot be resolved");
        }

        @ParameterizedTest
        @EnumSource(value = ExceptionStatus.class, names = {"NEW", "ACKNOWLEDGED", "RETRIED_FAILED", "ESCALATED"})
        @DisplayName("Should pass validation for resolvable statuses")
        void shouldPassValidationForResolvableStatuses(ExceptionStatus status) {
            // Given
            validException.setStatus(status);
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateResolutionOperation(validInput, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(value = ExceptionStatus.class, names = {"RESOLVED", "CLOSED"})
        @DisplayName("Should fail validation for non-resolvable statuses")
        void shouldFailValidationForNonResolvableStatuses(ExceptionStatus status) {
            // Given
            validException.setStatus(status);
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateResolutionOperation(validInput, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Resolution Method Validation Tests")
    class ResolutionMethodValidationTests {

        @ParameterizedTest
        @EnumSource(ResolutionMethod.class)
        @DisplayName("Should accept all valid resolution methods")
        void shouldAcceptAllValidResolutionMethods(ResolutionMethod method) {
            // Given
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId("TXN-123")
                    .resolutionMethod(method)
                    .build();

            // Set appropriate exception status for the resolution method
            ExceptionStatus status = getAppropriateStatusForMethod(method);
            validException.setStatus(status);
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateResolutionOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation for RETRY_SUCCESS method with wrong status")
        void shouldFailValidationForRetrySuccessMethodWithWrongStatus() {
            // Given
            validException.setStatus(ExceptionStatus.ACKNOWLEDGED); // Wrong status for RETRY_SUCCESS
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId("TXN-123")
                    .resolutionMethod(ResolutionMethod.RETRY_SUCCESS)
                    .build();

            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateResolutionOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_RESOLUTION_DATA.getCode());
            assertThat(result.getErrors().get(0).getMessage()).contains("RETRY_SUCCESS resolution method can only be used for exceptions with RETRIED_SUCCESS status");
        }

        @Test
        @DisplayName("Should pass validation for RETRY_SUCCESS method with correct status")
        void shouldPassValidationForRetrySuccessMethodWithCorrectStatus() {
            // Given
            validException.setStatus(ExceptionStatus.RETRIED_SUCCESS);
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId("TXN-123")
                    .resolutionMethod(ResolutionMethod.RETRY_SUCCESS)
                    .build();

            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateResolutionOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation for MANUAL_RESOLUTION with RETRIED_SUCCESS status")
        void shouldFailValidationForManualResolutionWithRetriedSuccessStatus() {
            // Given
            validException.setStatus(ExceptionStatus.RETRIED_SUCCESS);
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId("TXN-123")
                    .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                    .build();

            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateResolutionOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_RESOLUTION_DATA.getCode());
            assertThat(result.getErrors().get(0).getMessage()).contains("MANUAL_RESOLUTION cannot be used for exceptions with RETRIED_SUCCESS status");
        }

        @Test
        @DisplayName("Should fail validation for CUSTOMER_RESOLVED with NEW status")
        void shouldFailValidationForCustomerResolvedWithNewStatus() {
            // Given
            validException.setStatus(ExceptionStatus.NEW);
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId("TXN-123")
                    .resolutionMethod(ResolutionMethod.CUSTOMER_RESOLVED)
                    .build();

            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateResolutionOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_RESOLUTION_DATA.getCode());
            assertThat(result.getErrors().get(0).getMessage()).contains("CUSTOMER_RESOLVED can only be used for acknowledged or escalated exceptions");
        }

        @ParameterizedTest
        @EnumSource(value = ExceptionStatus.class, names = {"ACKNOWLEDGED", "ESCALATED"})
        @DisplayName("Should pass validation for CUSTOMER_RESOLVED with appropriate statuses")
        void shouldPassValidationForCustomerResolvedWithAppropriateStatuses(ExceptionStatus status) {
            // Given
            validException.setStatus(status);
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId("TXN-123")
                    .resolutionMethod(ResolutionMethod.CUSTOMER_RESOLVED)
                    .build();

            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateResolutionOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should log warning for AUTOMATED resolution method")
        void shouldLogWarningForAutomatedResolutionMethod() {
            // Given
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId("TXN-123")
                    .resolutionMethod(ResolutionMethod.AUTOMATED)
                    .build();

            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateResolutionOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isTrue(); // Should still pass but log warning
            assertThat(result.getErrors()).isEmpty();
        }

        private ExceptionStatus getAppropriateStatusForMethod(ResolutionMethod method) {
            switch (method) {
                case RETRY_SUCCESS:
                    return ExceptionStatus.RETRIED_SUCCESS;
                case CUSTOMER_RESOLVED:
                    return ExceptionStatus.ACKNOWLEDGED;
                case MANUAL_RESOLUTION:
                case AUTOMATED:
                default:
                    return ExceptionStatus.NEW;
            }
        }
    }

    @Nested
    @DisplayName("State Transition Validation Tests")
    class StateTransitionValidationTests {

        @Test
        @DisplayName("Should detect concurrent modification when exception becomes resolved")
        void shouldDetectConcurrentModificationWhenExceptionBecomesResolved() {
            // Given
            validException.setStatus(ExceptionStatus.RESOLVED); // Simulate concurrent modification
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateResolutionOperation(validInput, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.ALREADY_RESOLVED.getCode());
        }

        @Test
        @DisplayName("Should detect concurrent modification when exception becomes closed")
        void shouldDetectConcurrentModificationWhenExceptionBecomesClosed() {
            // Given
            validException.setStatus(ExceptionStatus.CLOSED); // Simulate concurrent modification
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateResolutionOperation(validInput, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_EXCEPTION_STATE.getCode());
        }
    }

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {

        @Test
        @DisplayName("Should return true for canResolve with resolvable status")
        void shouldReturnTrueForCanResolveWithResolvableStatus() {
            // Given
            validException.setStatus(ExceptionStatus.ACKNOWLEDGED);
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            boolean canResolve = validationService.canResolve("TXN-123");

            // Then
            assertThat(canResolve).isTrue();
        }

        @Test
        @DisplayName("Should return false for canResolve with non-resolvable status")
        void shouldReturnFalseForCanResolveWithNonResolvableStatus() {
            // Given
            validException.setStatus(ExceptionStatus.RESOLVED);
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            boolean canResolve = validationService.canResolve("TXN-123");

            // Then
            assertThat(canResolve).isFalse();
        }

        @Test
        @DisplayName("Should return false for canResolve with non-existent exception")
        void shouldReturnFalseForCanResolveWithNonExistentException() {
            // Given
            when(exceptionRepository.findByTransactionId(anyString())).thenReturn(Optional.empty());

            // When
            boolean canResolve = validationService.canResolve("TXN-NONEXISTENT");

            // Then
            assertThat(canResolve).isFalse();
        }

        @Test
        @DisplayName("Should return correct valid resolution methods for RETRIED_SUCCESS status")
        void shouldReturnCorrectValidResolutionMethodsForRetriedSuccessStatus() {
            // When
            Set<ResolutionMethod> methods = validationService.getValidResolutionMethods(ExceptionStatus.RETRIED_SUCCESS);

            // Then
            assertThat(methods).containsExactly(ResolutionMethod.RETRY_SUCCESS);
        }

        @Test
        @DisplayName("Should return correct valid resolution methods for ACKNOWLEDGED status")
        void shouldReturnCorrectValidResolutionMethodsForAcknowledgedStatus() {
            // When
            Set<ResolutionMethod> methods = validationService.getValidResolutionMethods(ExceptionStatus.ACKNOWLEDGED);

            // Then
            assertThat(methods).containsExactlyInAnyOrder(
                    ResolutionMethod.MANUAL_RESOLUTION,
                    ResolutionMethod.CUSTOMER_RESOLVED
            );
        }

        @Test
        @DisplayName("Should return correct valid resolution methods for ESCALATED status")
        void shouldReturnCorrectValidResolutionMethodsForEscalatedStatus() {
            // When
            Set<ResolutionMethod> methods = validationService.getValidResolutionMethods(ExceptionStatus.ESCALATED);

            // Then
            assertThat(methods).containsExactlyInAnyOrder(
                    ResolutionMethod.MANUAL_RESOLUTION,
                    ResolutionMethod.CUSTOMER_RESOLVED
            );
        }

        @Test
        @DisplayName("Should return correct valid resolution methods for NEW status")
        void shouldReturnCorrectValidResolutionMethodsForNewStatus() {
            // When
            Set<ResolutionMethod> methods = validationService.getValidResolutionMethods(ExceptionStatus.NEW);

            // Then
            assertThat(methods).containsExactly(ResolutionMethod.MANUAL_RESOLUTION);
        }

        @Test
        @DisplayName("Should return correct valid resolution methods for RETRIED_FAILED status")
        void shouldReturnCorrectValidResolutionMethodsForRetriedFailedStatus() {
            // When
            Set<ResolutionMethod> methods = validationService.getValidResolutionMethods(ExceptionStatus.RETRIED_FAILED);

            // Then
            assertThat(methods).containsExactly(ResolutionMethod.MANUAL_RESOLUTION);
        }

        @Test
        @DisplayName("Should return empty set for RESOLVED status")
        void shouldReturnEmptySetForResolvedStatus() {
            // When
            Set<ResolutionMethod> methods = validationService.getValidResolutionMethods(ExceptionStatus.RESOLVED);

            // Then
            assertThat(methods).isEmpty();
        }

        @Test
        @DisplayName("Should return empty set for CLOSED status")
        void shouldReturnEmptySetForClosedStatus() {
            // When
            Set<ResolutionMethod> methods = validationService.getValidResolutionMethods(ExceptionStatus.CLOSED);

            // Then
            assertThat(methods).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle validation with maximum valid input lengths")
        void shouldHandleValidationWithMaximumValidInputLengths() {
            // Given
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId("A".repeat(255)) // Maximum allowed length
                    .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                    .resolutionNotes("B".repeat(2000)) // Maximum allowed length
                    .build();

            when(exceptionRepository.findByTransactionId(anyString())).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateResolutionOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should handle validation with minimum valid input lengths")
        void shouldHandleValidationWithMinimumValidInputLengths() {
            // Given
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId("A") // Minimum valid length
                    .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                    .resolutionNotes("B") // Minimum valid length
                    .build();

            when(exceptionRepository.findByTransactionId(anyString())).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateResolutionOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should handle validation with special characters in valid fields")
        void shouldHandleValidationWithSpecialCharactersInValidFields() {
            // Given
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId("TXN-123_ABC")
                    .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                    .resolutionNotes("Resolution completed: issue #123 - network timeout (500ms)")
                    .build();

            when(exceptionRepository.findByTransactionId(anyString())).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateResolutionOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should handle concurrent validation requests")
        void shouldHandleConcurrentValidationRequests() {
            // Given
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When - Simulate concurrent requests
            ValidationResult result1 = validationService.validateResolutionOperation(validInput, authentication);
            ValidationResult result2 = validationService.validateResolutionOperation(validInput, authentication);

            // Then
            assertThat(result1.isValid()).isTrue();
            assertThat(result2.isValid()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n", "\r\n"})
        @DisplayName("Should handle various whitespace patterns in transaction ID")
        void shouldHandleVariousWhitespacePatternsInTransactionId(String whitespace) {
            // Given
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId(whitespace)
                    .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                    .build();

            // When
            ValidationResult result = validationService.validateResolutionOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
        }
    }
}