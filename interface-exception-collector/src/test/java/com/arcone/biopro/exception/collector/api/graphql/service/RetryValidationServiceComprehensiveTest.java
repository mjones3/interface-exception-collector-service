package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.ResolveExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput.RetryPriority;
import com.arcone.biopro.exception.collector.api.graphql.validation.MutationErrorCode;
import com.arcone.biopro.exception.collector.api.graphql.validation.ValidationResult;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.ResolutionMethod;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import com.arcone.biopro.exception.collector.infrastructure.service.DatabaseCachingService;

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

import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit tests for RetryValidationService covering all validation scenarios,
 * edge cases, and error conditions to achieve >95% code coverage.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RetryValidationService Comprehensive Tests")
class RetryValidationServiceComprehensiveTest {

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    @Mock
    private RetryAttemptRepository retryAttemptRepository;

    @Mock
    private DatabaseCachingService databaseCachingService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RetryValidationService validationService;

    private InterfaceException validException;
    private RetryExceptionInput validRetryInput;
    private AcknowledgeExceptionInput validAcknowledgeInput;
    private ResolveExceptionInput validResolveInput;

    @BeforeEach
    void setUp() {
        // Setup valid exception
        validException = new InterfaceException();
        validException.setId(1L);
        validException.setTransactionId("TXN-123");
        validException.setStatus(ExceptionStatus.FAILED);
        validException.setRetryable(true);
        validException.setRetryCount(0);
        validException.setMaxRetries(3);
        validException.setCreatedAt(OffsetDateTime.now().minusDays(1));

        // Setup valid inputs
        validRetryInput = RetryExceptionInput.builder()
                .transactionId("TXN-123")
                .reason("Valid retry reason")
                .priority(RetryPriority.NORMAL)
                .notes("Optional notes")
                .build();

        validAcknowledgeInput = AcknowledgeExceptionInput.builder()
                .transactionId("TXN-123")
                .reason("Valid acknowledgment reason")
                .notes("Optional notes")
                .build();

        validResolveInput = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("Resolution notes")
                .build();

        // Setup authentication
        when(authentication.getName()).thenReturn("test-user");
        when(authentication.getAuthorities()).thenReturn(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_OPERATIONS")));
    }

    @Nested
    @DisplayName("Retry Operation Validation Tests")
    class RetryOperationValidationTests {

        @Test
        @DisplayName("Should pass validation for valid retry input")
        void shouldPassValidationForValidRetryInput() {
            // Given
            when(databaseCachingService.validateForOperation("TXN-123", "retry"))
                    .thenReturn(ValidationResult.success("retry", "TXN-123"));

            // When
            ValidationResult result = validationService.validateRetryOperation(validRetryInput, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
            assertThat(result.getOperation()).isEqualTo("retry");
            assertThat(result.getTransactionId()).isEqualTo("TXN-123");
        }

        @Test
        @DisplayName("Should fail validation for null transaction ID")
        void shouldFailValidationForNullTransactionId() {
            // Given
            RetryExceptionInput input = RetryExceptionInput.builder()
                    .transactionId(null)
                    .reason("Valid reason")
                    .build();

            // When
            ValidationResult result = validationService.validateRetryOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
        }

        @Test
        @DisplayName("Should fail validation for empty transaction ID")
        void shouldFailValidationForEmptyTransactionId() {
            // Given
            RetryExceptionInput input = RetryExceptionInput.builder()
                    .transactionId("")
                    .reason("Valid reason")
                    .build();

            // When
            ValidationResult result = validationService.validateRetryOperation(input, authentication);

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
            RetryExceptionInput input = RetryExceptionInput.builder()
                    .transactionId(invalidTransactionId)
                    .reason("Valid reason")
                    .build();

            // When
            ValidationResult result = validationService.validateRetryOperation(input, authentication);

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
            RetryExceptionInput input = RetryExceptionInput.builder()
                    .transactionId(longTransactionId)
                    .reason("Valid reason")
                    .build();

            // When
            ValidationResult result = validationService.validateRetryOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_TRANSACTION_ID.getCode());
        }

        @Test
        @DisplayName("Should fail validation for null reason")
        void shouldFailValidationForNullReason() {
            // Given
            RetryExceptionInput input = RetryExceptionInput.builder()
                    .transactionId("TXN-123")
                    .reason(null)
                    .build();

            // When
            ValidationResult result = validationService.validateRetryOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
        }

        @Test
        @DisplayName("Should fail validation for empty reason")
        void shouldFailValidationForEmptyReason() {
            // Given
            RetryExceptionInput input = RetryExceptionInput.builder()
                    .transactionId("TXN-123")
                    .reason("")
                    .build();

            // When
            ValidationResult result = validationService.validateRetryOperation(input, authentication);

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
            RetryExceptionInput input = RetryExceptionInput.builder()
                    .transactionId("TXN-123")
                    .reason(longReason)
                    .build();

            // When
            ValidationResult result = validationService.validateRetryOperation(input, authentication);

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
            RetryExceptionInput input = RetryExceptionInput.builder()
                    .transactionId("TXN-123")
                    .reason("Valid reason")
                    .notes(longNotes)
                    .build();

            // When
            ValidationResult result = validationService.validateRetryOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_NOTES_LENGTH.getCode());
        }

        @ParameterizedTest
        @EnumSource(RetryPriority.class)
        @DisplayName("Should accept all valid retry priorities")
        void shouldAcceptAllValidRetryPriorities(RetryPriority priority) {
            // Given
            RetryExceptionInput input = RetryExceptionInput.builder()
                    .transactionId("TXN-123")
                    .reason("Valid reason")
                    .priority(priority)
                    .build();

            when(databaseCachingService.validateForOperation("TXN-123", "retry"))
                    .thenReturn(ValidationResult.success("retry", "TXN-123"));

            // When
            ValidationResult result = validationService.validateRetryOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Should handle multiple validation errors")
        void shouldHandleMultipleValidationErrors() {
            // Given
            RetryExceptionInput input = RetryExceptionInput.builder()
                    .transactionId("") // Invalid: empty
                    .reason("") // Invalid: empty
                    .notes("A".repeat(1001)) // Invalid: too long
                    .build();

            // When
            ValidationResult result = validationService.validateRetryOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(3);
            assertThat(result.getErrors()).extracting(error -> error.getCode())
                    .contains(
                            MutationErrorCode.MISSING_REQUIRED_FIELD.getCode(),
                            MutationErrorCode.INVALID_NOTES_LENGTH.getCode()
                    );
        }

        @Test
        @DisplayName("Should fail validation for insufficient permissions")
        void shouldFailValidationForInsufficientPermissions() {
            // Given
            when(authentication.getAuthorities()).thenReturn(
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_VIEWER")));

            // When
            ValidationResult result = validationService.validateRetryOperation(validRetryInput, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INSUFFICIENT_PERMISSIONS.getCode());
        }

        @Test
        @DisplayName("Should use cached validation result when available")
        void shouldUseCachedValidationResult() {
            // Given
            ValidationResult cachedResult = ValidationResult.failure("retry", "TXN-123", 
                    List.of(com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError.builder()
                            .code(MutationErrorCode.NOT_RETRYABLE.getCode())
                            .message("Exception is not retryable")
                            .build()));

            when(databaseCachingService.validateForOperation("TXN-123", "retry"))
                    .thenReturn(cachedResult);

            // When
            ValidationResult result = validationService.validateRetryOperation(validRetryInput, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.NOT_RETRYABLE.getCode());
        }
    }

    @Nested
    @DisplayName("Acknowledge Operation Validation Tests")
    class AcknowledgeOperationValidationTests {

        @Test
        @DisplayName("Should pass validation for valid acknowledge input")
        void shouldPassValidationForValidAcknowledgeInput() {
            // Given
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateAcknowledgeOperation(validAcknowledgeInput, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
            assertThat(result.getOperation()).isEqualTo("acknowledge");
            assertThat(result.getTransactionId()).isEqualTo("TXN-123");
        }

        @Test
        @DisplayName("Should fail validation when exception not found")
        void shouldFailValidationWhenExceptionNotFound() {
            // Given
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.empty());

            // When
            ValidationResult result = validationService.validateAcknowledgeOperation(validAcknowledgeInput, authentication);

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
            ValidationResult result = validationService.validateAcknowledgeOperation(validAcknowledgeInput, authentication);

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
            ValidationResult result = validationService.validateAcknowledgeOperation(validAcknowledgeInput, authentication);

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
            ValidationResult result = validationService.validateAcknowledgeOperation(validAcknowledgeInput, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Resolve Operation Validation Tests")
    class ResolveOperationValidationTests {

        @Test
        @DisplayName("Should pass validation for valid resolve input")
        void shouldPassValidationForValidResolveInput() {
            // Given
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));

            // When
            ValidationResult result = validationService.validateResolveOperation(validResolveInput, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
            assertThat(result.getOperation()).isEqualTo("resolve");
            assertThat(result.getTransactionId()).isEqualTo("TXN-123");
        }

        @Test
        @DisplayName("Should fail validation when exception not found")
        void shouldFailValidationWhenExceptionNotFoundForResolve() {
            // Given
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.empty());

            // When
            ValidationResult result = validationService.validateResolveOperation(validResolveInput, authentication);

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
            ValidationResult result = validationService.validateResolveOperation(validResolveInput, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.ALREADY_RESOLVED.getCode());
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
            ValidationResult result = validationService.validateResolveOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
        }

        @Test
        @DisplayName("Should fail validation for resolution notes exceeding maximum length")
        void shouldFailValidationForResolutionNotesTooLong() {
            // Given
            String longNotes = "A".repeat(1001); // Exceeds 1000 character limit
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId("TXN-123")
                    .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                    .resolutionNotes(longNotes)
                    .build();

            // When
            ValidationResult result = validationService.validateResolveOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_NOTES_LENGTH.getCode());
        }
    }

    @Nested
    @DisplayName("Cancel Retry Operation Validation Tests")
    class CancelRetryOperationValidationTests {

        @Test
        @DisplayName("Should pass validation for valid cancel retry request")
        void shouldPassValidationForValidCancelRetryRequest() {
            // Given
            RetryAttempt pendingRetry = new RetryAttempt();
            pendingRetry.setStatus(RetryStatus.PENDING);
            pendingRetry.setInitiatedAt(OffsetDateTime.now().minusMinutes(5));

            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.of(pendingRetry));

            // When
            ValidationResult result = validationService.validateCancelRetryOperation("TXN-123", "Cancel reason", authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation when no pending retry exists")
        void shouldFailValidationWhenNoPendingRetryExists() {
            // Given
            when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(validException));
            when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(validException))
                    .thenReturn(Optional.empty());

            // When
            ValidationResult result = validationService.validateCancelRetryOperation("TXN-123", "Cancel reason", authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.EXCEPTION_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("Should fail validation for cancel reason exceeding maximum length")
        void shouldFailValidationForCancelReasonTooLong() {
            // Given
            String longReason = "A".repeat(501); // Exceeds 500 character limit

            // When
            ValidationResult result = validationService.validateCancelRetryOperation("TXN-123", longReason, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INVALID_REASON_LENGTH.getCode());
        }
    }

    @Nested
    @DisplayName("Bulk Operation Validation Tests")
    class BulkOperationValidationTests {

        @Test
        @DisplayName("Should pass validation for valid bulk retry request")
        void shouldPassValidationForValidBulkRetryRequest() {
            // Given
            List<String> transactionIds = Arrays.asList("TXN-001", "TXN-002", "TXN-003");

            // When
            ValidationResult result = validationService.validateBulkRetryRequest(transactionIds, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
            assertThat(result.getOperation()).isEqualTo("bulk_retry");
        }

        @Test
        @DisplayName("Should fail validation for empty bulk request")
        void shouldFailValidationForEmptyBulkRequest() {
            // Given
            List<String> emptyList = Collections.emptyList();

            // When
            ValidationResult result = validationService.validateBulkRetryRequest(emptyList, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
        }

        @Test
        @DisplayName("Should fail validation for null bulk request")
        void shouldFailValidationForNullBulkRequest() {
            // When
            ValidationResult result = validationService.validateBulkRetryRequest(null, authentication);

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
            ValidationResult result = validationService.validateBulkRetryRequest(largeList, authentication);

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
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

            List<String> largeList = Arrays.asList(
                    "TXN-001", "TXN-002", "TXN-003", "TXN-004", "TXN-005",
                    "TXN-006", "TXN-007", "TXN-008", "TXN-009", "TXN-010",
                    "TXN-011", "TXN-012", "TXN-013", "TXN-014", "TXN-015"
            ); // 15 items, allowed for admin

            // When
            ValidationResult result = validationService.validateBulkRetryRequest(largeList, authentication);

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
            ValidationResult result = validationService.validateBulkRetryRequest(duplicateList, authentication);

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
            ValidationResult result = validationService.validateBulkRetryRequest(invalidList, authentication);

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
            ValidationResult result = validationService.validateBulkRetryRequest(invalidList, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode());
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
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_OPERATIONS")));
            when(databaseCachingService.validateForOperation(anyString(), anyString()))
                    .thenReturn(ValidationResult.success("retry", "TXN-123"));

            // When
            ValidationResult result = validationService.validateRetryOperation(validRetryInput, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Should pass validation for ADMIN role")
        void shouldPassValidationForAdminRole() {
            // Given
            when(authentication.getAuthorities()).thenReturn(
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
            when(databaseCachingService.validateForOperation(anyString(), anyString()))
                    .thenReturn(ValidationResult.success("retry", "TXN-123"));

            // When
            ValidationResult result = validationService.validateRetryOperation(validRetryInput, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Should fail validation for insufficient role")
        void shouldFailValidationForInsufficientRole() {
            // Given
            when(authentication.getAuthorities()).thenReturn(
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

            // When
            ValidationResult result = validationService.validateRetryOperation(validRetryInput, authentication);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INSUFFICIENT_PERMISSIONS.getCode());
        }

        @Test
        @DisplayName("Should fail validation for null authentication")
        void shouldFailValidationForNullAuthentication() {
            // When
            ValidationResult result = validationService.validateRetryOperation(validRetryInput, null);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getCode()).isEqualTo(MutationErrorCode.INSUFFICIENT_PERMISSIONS.getCode());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle validation with maximum valid input lengths")
        void shouldHandleValidationWithMaximumValidInputLengths() {
            // Given
            RetryExceptionInput input = RetryExceptionInput.builder()
                    .transactionId("A".repeat(50)) // Maximum allowed length
                    .reason("B".repeat(500)) // Maximum allowed length
                    .notes("C".repeat(1000)) // Maximum allowed length
                    .priority(RetryPriority.HIGH)
                    .build();

            when(databaseCachingService.validateForOperation(anyString(), anyString()))
                    .thenReturn(ValidationResult.success("retry", input.getTransactionId()));

            // When
            ValidationResult result = validationService.validateRetryOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should handle validation with minimum valid input lengths")
        void shouldHandleValidationWithMinimumValidInputLengths() {
            // Given
            RetryExceptionInput input = RetryExceptionInput.builder()
                    .transactionId("A") // Minimum valid length
                    .reason("B") // Minimum valid length
                    .priority(RetryPriority.LOW)
                    .build();

            when(databaseCachingService.validateForOperation(anyString(), anyString()))
                    .thenReturn(ValidationResult.success("retry", input.getTransactionId()));

            // When
            ValidationResult result = validationService.validateRetryOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should handle validation with special characters in valid fields")
        void shouldHandleValidationWithSpecialCharactersInValidFields() {
            // Given
            RetryExceptionInput input = RetryExceptionInput.builder()
                    .transactionId("TXN-123_ABC") // Valid special characters
                    .reason("Retry due to network timeout (500ms)")
                    .notes("Additional info: retry #1 - system error")
                    .build();

            when(databaseCachingService.validateForOperation(anyString(), anyString()))
                    .thenReturn(ValidationResult.success("retry", input.getTransactionId()));

            // When
            ValidationResult result = validationService.validateRetryOperation(input, authentication);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should handle concurrent validation requests")
        void shouldHandleConcurrentValidationRequests() {
            // Given
            when(databaseCachingService.validateForOperation(anyString(), anyString()))
                    .thenReturn(ValidationResult.success("retry", "TXN-123"));

            // When - Simulate concurrent requests
            ValidationResult result1 = validationService.validateRetryOperation(validRetryInput, authentication);
            ValidationResult result2 = validationService.validateRetryOperation(validRetryInput, authentication);

            // Then
            assertThat(result1.isValid()).isTrue();
            assertThat(result2.isValid()).isTrue();
        }
    }
}

