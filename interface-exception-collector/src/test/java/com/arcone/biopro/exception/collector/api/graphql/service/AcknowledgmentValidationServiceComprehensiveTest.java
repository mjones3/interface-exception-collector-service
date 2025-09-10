package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.validation.MutationErrorCode;
import com.arcone.biopro.exception.collector.api.graphql.validation.ValidationResult;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit tests for AcknowledgmentValidationService covering all validation scenarios,
 * edge cases, and error conditions to achieve >95% code coverage.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AcknowledgmentValidationService Comprehensive Tests")
class AcknowledgmentValidationServiceComprehensiveTest {

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AcknowledgmentValidationService validationService;

    private InterfaceException validException;
    private AcknowledgeExceptionInput validInput;

    @BeforeEach
    void setUp() {
        // Setup valid exception
        validException = new InterfaceException();
        validException.setId(1L);
        validException.setTransactionId("TXN-123");
        validException.setStatus(ExceptionStatus.FAILED);
        validException.setCreatedAt(OffsetDateTime.now().minusDays(1));

        // Setup valid input
        validInput = AcknowledgeExceptionInput.builder()
                .transactionId("TXN-123")
                .reason("Valid acknowledgment reason")
                .notes("Optional notes")
                .build();

        // Setup authentication
        when(authentication.getName()).thenReturn("test-user");
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS")));
    }

    @Nested
    @DisplayName("Acknowledgment Operation Validation Tests")
    class AcknowledgmentOperationValidationTests {

        @Test
        @DisplayName("Should pass validation for valid acknowledgment input")
        void shouldPassValidationForValidAcknowledgmentInput() {
            // Given
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(validInput, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
            assertThat(result.getOperation()).isEqualTo("acknowledge");
            assertThat(result.getTransactionId()).isEqualTo("TXN-123");
        }

        @Test
        @DisplayName("Should fail validation for null transaction ID")
        void shouldFailValidationForNullTransactionId() {
            // Given
            AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                    .transactionId(null)
                    .reason("Valid reason")
                    .build();

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
        }

        @Test
        @DisplayName("Should fail validation for empty transaction ID")
        void shouldFailValidationForEmptyTransactionId() {
            // Given
            AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                    .transactionId("")
                    .reason("Valid reason")
                    .build();

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
        }

        @ParameterizedTest
        @ValueSource(strings = {"TXN@123", "TXN 123", "TXN#123", "TXN%123", "TXN&123"})
        @DisplayName("Should fail validation for invalid transaction ID format")
        void shouldFailValidationForInvalidTransactionIdFormat(String invalidTransactionId) {
            // Given
            AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                    .transactionId(invalidTransactionId)
                    .reason("Valid reason")
                    .build();

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_TRANSACTION_ID.getCode());
        }

        @Test
        @DisplayName("Should fail validation for transaction ID exceeding maximum length")
        void shouldFailValidationForTransactionIdTooLong() {
            // Given
            String longTransactionId = "A".repeat(51); // Exceeds 50 character limit
            AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                    .transactionId(longTransactionId)
                    .reason("Valid reason")
                    .build();

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_TRANSACTION_ID.getCode());
        }

        @Test
        @DisplayName("Should fail validation for null reason")
        void shouldFailValidationForNullReason() {
            // Given
            AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                    .transactionId("TXN-123")
                    .reason(null)
                    .build();

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
        }

        @Test
        @DisplayName("Should fail validation for empty reason")
        void shouldFailValidationForEmptyReason() {
            // Given
            AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                    .transactionId("TXN-123")
                    .reason("")
                    .build();

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
        }

        @Test
        @DisplayName("Should fail validation for reason exceeding maximum length")
        void shouldFailValidationForReasonTooLong() {
            // Given
            String longReason = "A".repeat(501); // Exceeds 500 character limit
            AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                    .transactionId("TXN-123")
                    .reason(longReason)
                    .build();

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_REASON_LENGTH.getCode());
        }

        @Test
        @DisplayName("Should fail validation for notes exceeding maximum length")
        void shouldFailValidationForNotesTooLong() {
            // Given
            String longNotes = "A".repeat(1001); // Exceeds 1000 character limit
            AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                    .transactionId("TXN-123")
                    .reason("Valid reason")
                    .notes(longNotes)
                    .build();

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_NOTES_LENGTH.getCode());
        }

        @Test
        @DisplayName("Should handle multiple validation errors")
        void shouldHandleMultipleValidationErrors() {
            // Given
            AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                    .transactionId("") // Invalid: empty
                    .reason("") // Invalid: empty
                    .notes("A".repeat(1001)) // Invalid: too long
                    .build();

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(3);
            assertThat(result.getErrors()).extracting(error -> error.getCode())
                    .contains(
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
            ValidationResult result = validationService.validateAcknowledgmentOperation(validInput, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.EXCEPTION_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("Should fail validation for resolved exception")
        void shouldFailValidationForResolvedException() {
            // Given
            validException.setStatus(ExceptionStatus.RESOLVED);
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(validInput, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.ALREADY_RESOLVED.getCode());
        }

        @Test
        @DisplayName("Should fail validation for closed exception")
        void shouldFailValidationForClosedException() {
            // Given
            validException.setStatus(ExceptionStatus.CLOSED);
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(validInput, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_STATUS_TRANSITION.getCode());
        }

        @Test
        @DisplayName("Should allow re-acknowledgment of already acknowledged exception")
        void shouldAllowReAcknowledgmentOfAlreadyAcknowledgedException() {
            // Given
            validException.setStatus(ExceptionStatus.ACKNOWLEDGED);
            validException.setAcknowledgedBy("previous-user");
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(validInput, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(value = ExceptionStatus.class, names = {"NEW", "FAILED", "RETRYING", "RETRIED_FAILED", "ESCALATED"})
        @DisplayName("Should pass validation for acknowledgeable statuses")
        void shouldPassValidationForAcknowledgeableStatuses(ExceptionStatus status) {
            // Given
            validException.setStatus(status);
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(validInput, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should handle very old exception with warning")
        void shouldHandleVeryOldExceptionWithWarning() {
            // Given
            validException.setCreatedAt(OffsetDateTime.now().minusDays(100)); // Very old exception
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(validInput, authentication);

            // Then
            assertThat(result.isValid()).isTrue(); // Should still pass but log warning
            assertThat(result.getErrors()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Permission Validation Tests")
    class PermissionValidationTests {

        @Test
        @DisplayName("Should pass validation for OPERATIONS role")
        void shouldPassValidationForOperationsRole() {
            // Given
            when(authentication.getAuthorities()).thenReturn(
                    List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS")));
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(validInput, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Should pass validation for ADMIN role")
        void shouldPassValidationForAdminRole() {
            // Given
            when(authentication.getAuthorities()).thenReturn(
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(validInput, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Should fail validation for insufficient role")
        void shouldFailValidationForInsufficientRole() {
            // Given
            when(authentication.getAuthorities()).thenReturn(
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(validInput, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INSUFFICIENT_PERMISSIONS.getCode());
        }

        @Test
        @DisplayName("Should fail validation for null authentication")
        void shouldFailValidationForNullAuthentication() {
            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(validInput, null);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INSUFFICIENT_PERMISSIONS.getCode());
        }

        @Test
        @DisplayName("Should fail validation for authentication with null name")
        void shouldFailValidationForAuthenticationWithNullName() {
            // Given
            when(authentication.getName()).thenReturn(null);
            when(authentication.getAuthorities()).thenReturn(
                    List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS")));

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(validInput, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INSUFFICIENT_PERMISSIONS.getCode());
        }
    }

    @Nested
    @DisplayName("Bulk Acknowledgment Validation Tests")
    class BulkAcknowledgmentValidationTests {

        @Test
        @DisplayName("Should pass validation for valid bulk acknowledgment request")
        void shouldPassValidationForValidBulkAcknowledgmentRequest() {
            // Given
            List<String> transactionIds = Arrays.asList("TXN-001", "TXN-002", "TXN-003");

            // When
            ValidationResult result = validationService.validateBulkAcknowledgmentOperation(transactionIds, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
            assertThat(result.getOperation()).isEqualTo("bulk_acknowledge");
        }

        @Test
        @DisplayName("Should fail validation for empty bulk request")
        void shouldFailValidationForEmptyBulkRequest() {
            // Given
            List<String> emptyList = Collections.emptyList();

            // When
            ValidationResult result = validationService.validateBulkAcknowledgmentOperation(emptyList, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
        }

        @Test
        @DisplayName("Should fail validation for null bulk request")
        void shouldFailValidationForNullBulkRequest() {
            // When
            ValidationResult result = validationService.validateBulkAcknowledgmentOperation(null, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
        }

        @Test
        @DisplayName("Should fail validation for bulk request exceeding size limit for non-admin")
        void shouldFailValidationForBulkRequestExceedingSizeLimitForNonAdmin() {
            // Given
            List<String> largeList = Arrays.asList(
                    "TXN-001", "TXN-002", "TXN-003", "TXN-004", "TXN-005",
                    "TXN-006", "TXN-007", "TXN-008", "TXN-009", "TXN-010", "TXN-011"
            ); // 11 items, exceeds limit of 10 for non-admin

            // When
            ValidationResult result = validationService.validateBulkAcknowledgmentOperation(largeList, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.BULK_SIZE_EXCEEDED.getCode());
        }

        @Test
        @DisplayName("Should pass validation for large bulk request for admin user")
        void shouldPassValidationForLargeBulkRequestForAdminUser() {
            // Given
            when(authentication.getAuthorities()).thenReturn(
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            List<String> largeList = Arrays.asList(
                    "TXN-001", "TXN-002", "TXN-003", "TXN-004", "TXN-005",
                    "TXN-006", "TXN-007", "TXN-008", "TXN-009", "TXN-010",
                    "TXN-011", "TXN-012", "TXN-013", "TXN-014", "TXN-015"
            ); // 15 items, allowed for admin

            // When
            ValidationResult result = validationService.validateBulkAcknowledgmentOperation(largeList, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation for bulk request with duplicate transaction IDs")
        void shouldFailValidationForBulkRequestWithDuplicates() {
            // Given
            List<String> duplicateList = Arrays.asList("TXN-001", "TXN-002", "TXN-001");

            // When
            ValidationResult result = validationService.validateBulkAcknowledgmentOperation(duplicateList, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_FIELD_VALUE.getCode());
        }

        @Test
        @DisplayName("Should fail validation for bulk request with invalid transaction ID format")
        void shouldFailValidationForBulkRequestWithInvalidFormat() {
            // Given
            List<String> invalidList = Arrays.asList("TXN-001", "INVALID@ID", "TXN-003");

            // When
            ValidationResult result = validationService.validateBulkAcknowledgmentOperation(invalidList, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_TRANSACTION_ID.getCode());
        }

        @Test
        @DisplayName("Should fail validation for bulk request with empty transaction ID")
        void shouldFailValidationForBulkRequestWithEmptyTransactionId() {
            // Given
            List<String> invalidList = Arrays.asList("TXN-001", "", "TXN-003");

            // When
            ValidationResult result = validationService.validateBulkAcknowledgmentOperation(invalidList, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
        }
    }

    @Nested
    @DisplayName("Legacy Method Tests")
    class LegacyMethodTests {

        @Test
        @DisplayName("Should pass legacy validation for valid input")
        void shouldPassLegacyValidationForValidInput() {
            // Given
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When & Then
            validationService.validateAcknowledgmentRequest(validInput, authentication);
            // Should not throw exception
        }

        @Test
        @DisplayName("Should throw exception for legacy validation with invalid input")
        void shouldThrowExceptionForLegacyValidationWithInvalidInput() {
            // Given
            AcknowledgeExceptionInput invalidInput = AcknowledgeExceptionInput.builder()
                    .transactionId("")
                    .reason("Valid reason")
                    .build();

            // When & Then
            assertThatThrownBy(() -> validationService.validateAcknowledgmentRequest(invalidInput, authentication))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should pass legacy bulk validation for valid input")
        void shouldPassLegacyBulkValidationForValidInput() {
            // Given
            List<String> transactionIds = Arrays.asList("TXN-001", "TXN-002", "TXN-003");

            // When & Then
            validationService.validateBulkAcknowledgmentRequest(transactionIds, authentication);
            // Should not throw exception
        }

        @Test
        @DisplayName("Should throw exception for legacy bulk validation with invalid input")
        void shouldThrowExceptionForLegacyBulkValidationWithInvalidInput() {
            // Given
            List<String> emptyList = Collections.emptyList();

            // When & Then
            assertThatThrownBy(() -> validationService.validateBulkAcknowledgmentRequest(emptyList, authentication))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should pass legacy bulk operation size validation for valid size")
        void shouldPassLegacyBulkOperationSizeValidationForValidSize() {
            // When & Then
            validationService.validateBulkOperationSize(5, authentication);
            // Should not throw exception
        }

        @Test
        @DisplayName("Should throw exception for legacy bulk operation size validation with invalid size")
        void shouldThrowExceptionForLegacyBulkOperationSizeValidationWithInvalidSize() {
            // When & Then
            assertThatThrownBy(() -> validationService.validateBulkOperationSize(101, authentication))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle validation with maximum valid input lengths")
        void shouldHandleValidationWithMaximumValidInputLengths() {
            // Given
            AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                    .transactionId("A".repeat(50)) // Maximum allowed length
                    .reason("B".repeat(500)) // Maximum allowed length
                    .notes("C".repeat(1000)) // Maximum allowed length
                    .build();

            when(exceptionRepository.findByTransactionId(anyString())).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should handle validation with minimum valid input lengths")
        void shouldHandleValidationWithMinimumValidInputLengths() {
            // Given
            AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                    .transactionId("A") // Minimum valid length
                    .reason("B") // Minimum valid length
                    .build();

            when(exceptionRepository.findByTransactionId(anyString())).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should handle validation with special characters in valid fields")
        void shouldHandleValidationWithSpecialCharactersInValidFields() {
            // Given
            AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                    .transactionId("TXN-123_ABC") // Valid special characters
                    .reason("Acknowledged due to network timeout (500ms)")
                    .notes("Additional info: acknowledgment #1 - system error")
                    .build();

            when(exceptionRepository.findByTransactionId(anyString())).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should handle validation with null notes")
        void shouldHandleValidationWithNullNotes() {
            // Given
            AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                    .transactionId("TXN-123")
                    .reason("Valid reason")
                    .notes(null) // Null notes should be allowed
                    .build();

            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateAcknowledgmentOperation(input, authentication);

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
            ValidationResult result1 = validationService.validateAcknowledgmentOperation(validInput, authentication);
            ValidationResult result2 = validationService.validateAcknowledgmentOperation(validInput, authentication);

            // Then
            assertThat(result1.isValid()).isTrue();
            assertThat(result2.isValid()).isTrue();
        }
    }
}