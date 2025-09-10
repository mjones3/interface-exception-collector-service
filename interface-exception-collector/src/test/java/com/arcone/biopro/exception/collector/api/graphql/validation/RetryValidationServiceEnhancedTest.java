package com.arcone.biopro.exception.collector.api.graphql.validation;

import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.service.RetryValidationService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput.RetryPriority;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for enhanced RetryValidationService functionality.
 * Tests the new validation methods with detailed error categorization.
 */
@ExtendWith(MockitoExtension.class)
class RetryValidationServiceEnhancedTest {

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    @Mock
    private RetryAttemptRepository retryAttemptRepository;

    @Mock
    private Authentication authentication;

    private RetryValidationService validationService;

    private InterfaceException testException;
    private RetryExceptionInput validInput;

    @BeforeEach
    void setUp() {
        validationService = new RetryValidationService(exceptionRepository, retryAttemptRepository);

        // Setup test exception
        testException = new InterfaceException();
        testException.setTransactionId("TXN-123");
        testException.setRetryable(true);
        testException.setStatus(ExceptionStatus.FAILED);
        testException.setRetryCount(0);
        testException.setMaxRetries(3);

        // Setup valid input
        validInput = RetryExceptionInput.builder()
                .transactionId("TXN-123")
                .reason("Test retry reason")
                .priority(RetryPriority.NORMAL)
                .build();

        // Setup authentication
        when(authentication.getName()).thenReturn("test-user");
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS")));
    }

    @Test
    void validateRetryOperation_WithValidInput_ShouldReturnSuccess() {
        // Given
        when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(testException));
        when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException))
                .thenReturn(Optional.empty());

        // When
        ValidationResult result = validationService.validateRetryOperation(validInput, authentication);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getOperation()).isEqualTo("retry");
        assertThat(result.getTransactionId()).isEqualTo("TXN-123");
    }

    @Test
    void validateRetryOperation_WithInvalidTransactionId_ShouldReturnValidationError() {
        // Given
        RetryExceptionInput invalidInput = RetryExceptionInput.builder()
                .transactionId("") // Invalid: empty
                .reason("Test retry reason")
                .build();

        // When
        ValidationResult result = validationService.validateRetryOperation(invalidInput, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("VALIDATION_002");
        assertThat(result.getErrors().get(0).getMessage()).contains("Transaction ID is required");
    }

    @Test
    void validateRetryOperation_WithExceptionNotFound_ShouldReturnNotFoundError() {
        // Given
        when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.empty());

        // When
        ValidationResult result = validationService.validateRetryOperation(validInput, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("BUSINESS_001");
        assertThat(result.getErrors().get(0).getMessage()).contains("Exception not found");
    }

    @Test
    void validateRetryOperation_WithNotRetryableException_ShouldReturnBusinessRuleError() {
        // Given
        testException.setRetryable(false);
        when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(testException));

        // When
        ValidationResult result = validationService.validateRetryOperation(validInput, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("BUSINESS_002");
        assertThat(result.getErrors().get(0).getMessage()).contains("not retryable");
    }

    @Test
    void validateRetryOperation_WithResolvedStatus_ShouldReturnBusinessRuleError() {
        // Given
        testException.setStatus(ExceptionStatus.RESOLVED);
        when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(testException));

        // When
        ValidationResult result = validationService.validateRetryOperation(validInput, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("BUSINESS_004");
        assertThat(result.getErrors().get(0).getMessage()).contains("status: RESOLVED");
    }

    @Test
    void validateRetryOperation_WithRetryLimitExceeded_ShouldReturnBusinessRuleError() {
        // Given
        testException.setRetryCount(3);
        testException.setMaxRetries(3);
        when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(testException));

        // When
        ValidationResult result = validationService.validateRetryOperation(validInput, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("BUSINESS_003");
        assertThat(result.getErrors().get(0).getMessage()).contains("Maximum retry count");
    }

    @Test
    void validateRetryOperation_WithPendingRetry_ShouldReturnBusinessRuleError() {
        // Given
        RetryAttempt pendingRetry = new RetryAttempt();
        pendingRetry.setStatus(RetryStatus.PENDING);
        
        when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(testException));
        when(retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(testException))
                .thenReturn(Optional.of(pendingRetry));

        // When
        ValidationResult result = validationService.validateRetryOperation(validInput, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("BUSINESS_005");
        assertThat(result.getErrors().get(0).getMessage()).contains("already pending");
    }

    @Test
    void validateRetryOperation_WithInsufficientPermissions_ShouldReturnSecurityError() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER")));

        // When
        ValidationResult result = validationService.validateRetryOperation(validInput, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("SECURITY_001");
        assertThat(result.getErrors().get(0).getMessage()).contains("Insufficient permissions");
    }

    @Test
    void validateRetryOperation_WithLongReason_ShouldReturnValidationError() {
        // Given
        String longReason = "A".repeat(501); // Exceeds MAX_REASON_LENGTH of 500
        RetryExceptionInput invalidInput = RetryExceptionInput.builder()
                .transactionId("TXN-123")
                .reason(longReason)
                .build();

        // When
        ValidationResult result = validationService.validateRetryOperation(invalidInput, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("VALIDATION_004");
        assertThat(result.getErrors().get(0).getMessage()).contains("exceeds maximum length");
    }

    @Test
    void validateRetryOperation_WithMultipleErrors_ShouldReturnAllErrors() {
        // Given
        RetryExceptionInput invalidInput = RetryExceptionInput.builder()
                .transactionId("") // Invalid: empty
                .reason("") // Invalid: empty
                .build();

        // When
        ValidationResult result = validationService.validateRetryOperation(invalidInput, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(2);
        
        // Should have both transaction ID and reason validation errors
        List<String> errorCodes = result.getErrors().stream()
                .map(error -> error.getCode())
                .toList();
        assertThat(errorCodes).contains("VALIDATION_002"); // Missing required field errors
    }

    @Test
    void validateBulkRetryRequest_WithValidInput_ShouldReturnSuccess() {
        // Given
        List<String> transactionIds = List.of("TXN-001", "TXN-002", "TXN-003");

        // When
        ValidationResult result = validationService.validateBulkRetryRequest(transactionIds, authentication);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getOperation()).isEqualTo("bulk_retry");
    }

    @Test
    void validateBulkRetryRequest_WithTooManyItems_ShouldReturnSecurityError() {
        // Given
        List<String> transactionIds = List.of("TXN-001", "TXN-002", "TXN-003", "TXN-004", "TXN-005",
                "TXN-006", "TXN-007", "TXN-008", "TXN-009", "TXN-010", "TXN-011"); // 11 items, exceeds limit for non-admin

        // When
        ValidationResult result = validationService.validateBulkRetryRequest(transactionIds, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("SECURITY_003");
        assertThat(result.getErrors().get(0).getMessage()).contains("exceeds maximum allowed limit");
    }

    @Test
    void validateBulkRetryRequest_WithDuplicateIds_ShouldReturnValidationError() {
        // Given
        List<String> transactionIds = List.of("TXN-001", "TXN-002", "TXN-001"); // Duplicate TXN-001

        // When
        ValidationResult result = validationService.validateBulkRetryRequest(transactionIds, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("VALIDATION_003");
        assertThat(result.getErrors().get(0).getMessage()).contains("Duplicate transaction IDs");
    }
}