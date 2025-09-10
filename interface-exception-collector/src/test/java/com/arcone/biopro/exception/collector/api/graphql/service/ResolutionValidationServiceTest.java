package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.dto.ResolveExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.validation.ValidationResult;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.ResolutionMethod;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * Comprehensive unit tests for ResolutionValidationService.
 * Tests enhanced validation logic, state transitions, and error handling.
 */
@ExtendWith(MockitoExtension.class)
class ResolutionValidationServiceTest {

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ResolutionValidationService resolutionValidationService;

    private InterfaceException testException;
    private ResolveExceptionInput validInput;

    @BeforeEach
    void setUp() {
        testException = new InterfaceException();
        testException.setTransactionId("TXN-123");
        testException.setStatus(ExceptionStatus.ACKNOWLEDGED);

        validInput = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("Test resolution notes")
                .build();

        when(authentication.getName()).thenReturn("test-user");
    }

    @Test
    void validateResolutionOperation_WithValidInput_ShouldReturnSuccess() {
        // Given
        when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(testException));

        // When
        ValidationResult result = resolutionValidationService.validateResolutionOperation(validInput, authentication);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getOperation()).isEqualTo("resolve");
        assertThat(result.getTransactionId()).isEqualTo("TXN-123");
    }

    @Test
    void validateResolutionOperation_WithMissingTransactionId_ShouldReturnValidationError() {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId(null)
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .build();

        // When
        ValidationResult result = resolutionValidationService.validateResolutionOperation(input, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("VALIDATION_002");
        assertThat(result.getErrors().get(0).getMessage()).contains("Transaction ID is required");
    }

    @Test
    void validateResolutionOperation_WithInvalidTransactionIdLength_ShouldReturnValidationError() {
        // Given
        String longTransactionId = "A".repeat(256); // Exceeds 255 character limit
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId(longTransactionId)
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .build();

        // When
        ValidationResult result = resolutionValidationService.validateResolutionOperation(input, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("VALIDATION_003");
        assertThat(result.getErrors().get(0).getMessage()).contains("must not exceed 255 characters");
    }

    @Test
    void validateResolutionOperation_WithMissingResolutionMethod_ShouldReturnValidationError() {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(null)
                .build();

        // When
        ValidationResult result = resolutionValidationService.validateResolutionOperation(input, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("VALIDATION_002");
        assertThat(result.getErrors().get(0).getMessage()).contains("Resolution method is required");
    }

    @Test
    void validateResolutionOperation_WithExcessiveResolutionNotes_ShouldReturnValidationError() {
        // Given
        String longNotes = "A".repeat(2001); // Exceeds 2000 character limit
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes(longNotes)
                .build();

        // When
        ValidationResult result = resolutionValidationService.validateResolutionOperation(input, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("VALIDATION_005");
        assertThat(result.getErrors().get(0).getMessage()).contains("must not exceed 2000 characters");
    }

    @Test
    void validateResolutionOperation_WithNonExistentException_ShouldReturnNotFoundError() {
        // Given
        when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.empty());

        // When
        ValidationResult result = resolutionValidationService.validateResolutionOperation(validInput, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("BUSINESS_001");
        assertThat(result.getErrors().get(0).getMessage()).contains("Exception not found");
    }

    @Test
    void validateResolutionOperation_WithAlreadyResolvedStatus_ShouldReturnBusinessRuleError() {
        // Given
        testException.setStatus(ExceptionStatus.RESOLVED);
        when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(testException));

        // When
        ValidationResult result = resolutionValidationService.validateResolutionOperation(validInput, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("BUSINESS_006");
        assertThat(result.getErrors().get(0).getMessage()).contains("already resolved");
    }

    @Test
    void validateResolutionOperation_WithClosedStatus_ShouldReturnBusinessRuleError() {
        // Given
        testException.setStatus(ExceptionStatus.CLOSED);
        when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(testException));

        // When
        ValidationResult result = resolutionValidationService.validateResolutionOperation(validInput, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("BUSINESS_010");
        assertThat(result.getErrors().get(0).getMessage()).contains("closed and cannot be resolved");
    }

    @Test
    void validateResolutionOperation_WithInvalidStatusTransition_ShouldReturnBusinessRuleError() {
        // Given
        testException.setStatus(ExceptionStatus.RETRIED_SUCCESS);
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION) // Invalid for RETRIED_SUCCESS
                .build();
        when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(testException));

        // When
        ValidationResult result = resolutionValidationService.validateResolutionOperation(input, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("RESOLVE_002");
        assertThat(result.getErrors().get(0).getMessage()).contains("MANUAL_RESOLUTION cannot be used for exceptions with RETRIED_SUCCESS status");
    }

    @Test
    void validateResolutionOperation_WithRetriedSuccessAndCorrectMethod_ShouldReturnSuccess() {
        // Given
        testException.setStatus(ExceptionStatus.RETRIED_SUCCESS);
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(ResolutionMethod.RETRY_SUCCESS) // Correct for RETRIED_SUCCESS
                .build();
        when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(testException));

        // When
        ValidationResult result = resolutionValidationService.validateResolutionOperation(input, authentication);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void validateResolutionOperation_WithCustomerResolvedForNewStatus_ShouldReturnBusinessRuleError() {
        // Given
        testException.setStatus(ExceptionStatus.NEW);
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(ResolutionMethod.CUSTOMER_RESOLVED) // Invalid for NEW status
                .build();
        when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(testException));

        // When
        ValidationResult result = resolutionValidationService.validateResolutionOperation(input, authentication);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("RESOLVE_002");
        assertThat(result.getErrors().get(0).getMessage()).contains("CUSTOMER_RESOLVED can only be used for acknowledged or escalated exceptions");
    }

    @Test
    void validateResolutionOperation_WithCustomerResolvedForEscalatedStatus_ShouldReturnSuccess() {
        // Given
        testException.setStatus(ExceptionStatus.ESCALATED);
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(ResolutionMethod.CUSTOMER_RESOLVED) // Valid for ESCALATED status
                .build();
        when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(testException));

        // When
        ValidationResult result = resolutionValidationService.validateResolutionOperation(input, authentication);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void canResolve_WithResolvableStatus_ShouldReturnTrue() {
        // Given
        testException.setStatus(ExceptionStatus.ACKNOWLEDGED);
        when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(testException));

        // When
        boolean canResolve = resolutionValidationService.canResolve("TXN-123");

        // Then
        assertThat(canResolve).isTrue();
    }

    @Test
    void canResolve_WithNonResolvableStatus_ShouldReturnFalse() {
        // Given
        testException.setStatus(ExceptionStatus.RESOLVED);
        when(exceptionRepository.findByTransactionId("TXN-123")).thenReturn(Optional.of(testException));

        // When
        boolean canResolve = resolutionValidationService.canResolve("TXN-123");

        // Then
        assertThat(canResolve).isFalse();
    }

    @Test
    void canResolve_WithNonExistentException_ShouldReturnFalse() {
        // Given
        when(exceptionRepository.findByTransactionId(anyString())).thenReturn(Optional.empty());

        // When
        boolean canResolve = resolutionValidationService.canResolve("TXN-NONEXISTENT");

        // Then
        assertThat(canResolve).isFalse();
    }

    @Test
    void getValidResolutionMethods_ForRetriedSuccessStatus_ShouldReturnOnlyRetrySuccess() {
        // When
        Set<ResolutionMethod> methods = resolutionValidationService.getValidResolutionMethods(ExceptionStatus.RETRIED_SUCCESS);

        // Then
        assertThat(methods).containsExactly(ResolutionMethod.RETRY_SUCCESS);
    }

    @Test
    void getValidResolutionMethods_ForAcknowledgedStatus_ShouldReturnManualAndCustomer() {
        // When
        Set<ResolutionMethod> methods = resolutionValidationService.getValidResolutionMethods(ExceptionStatus.ACKNOWLEDGED);

        // Then
        assertThat(methods).containsExactlyInAnyOrder(
            ResolutionMethod.MANUAL_RESOLUTION, 
            ResolutionMethod.CUSTOMER_RESOLVED
        );
    }

    @Test
    void getValidResolutionMethods_ForNewStatus_ShouldReturnOnlyManual() {
        // When
        Set<ResolutionMethod> methods = resolutionValidationService.getValidResolutionMethods(ExceptionStatus.NEW);

        // Then
        assertThat(methods).containsExactly(ResolutionMethod.MANUAL_RESOLUTION);
    }

    @Test
    void getValidResolutionMethods_ForResolvedStatus_ShouldReturnEmpty() {
        // When
        Set<ResolutionMethod> methods = resolutionValidationService.getValidResolutionMethods(ExceptionStatus.RESOLVED);

        // Then
        assertThat(methods).isEmpty();
    }
}