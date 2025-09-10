package com.arcone.biopro.exception.collector.api.graphql.dto;

import com.arcone.biopro.exception.collector.domain.enums.ResolutionMethod;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the enhanced ResolveExceptionInput DTO.
 * Tests validation rules, helper methods, and enhanced functionality.
 */
class ResolveExceptionInputTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void validInput_ShouldPassValidation() {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("Test resolution notes")
                .build();

        // When
        Set<ConstraintViolation<ResolveExceptionInput>> violations = validator.validate(input);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void missingTransactionId_ShouldFailValidation() {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId(null)
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .build();

        // When
        Set<ConstraintViolation<ResolveExceptionInput>> violations = validator.validate(input);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Transaction ID is required");
    }

    @Test
    void emptyTransactionId_ShouldFailValidation() {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId("")
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .build();

        // When
        Set<ConstraintViolation<ResolveExceptionInput>> violations = validator.validate(input);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Transaction ID is required");
    }

    @Test
    void transactionIdTooLong_ShouldFailValidation() {
        // Given
        String longTransactionId = "A".repeat(256); // Exceeds 255 character limit
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId(longTransactionId)
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .build();

        // When
        Set<ConstraintViolation<ResolveExceptionInput>> violations = validator.validate(input);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Transaction ID must not exceed 255 characters");
    }

    @Test
    void missingResolutionMethod_ShouldFailValidation() {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(null)
                .build();

        // When
        Set<ConstraintViolation<ResolveExceptionInput>> violations = validator.validate(input);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Resolution method is required");
    }

    @Test
    void resolutionNotesTooLong_ShouldFailValidation() {
        // Given
        String longNotes = "A".repeat(2001); // Exceeds 2000 character limit
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes(longNotes)
                .build();

        // When
        Set<ConstraintViolation<ResolveExceptionInput>> violations = validator.validate(input);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Resolution notes must not exceed 2000 characters");
    }

    @Test
    void nullResolutionNotes_ShouldPassValidation() {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes(null)
                .build();

        // When
        Set<ConstraintViolation<ResolveExceptionInput>> violations = validator.validate(input);

        // Then
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"TXN-123", "ORDER-456", "TEST_TXN_789", "ABC123DEF456"})
    void hasValidTransactionIdFormat_WithValidFormats_ShouldReturnTrue(String transactionId) {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId(transactionId)
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .build();

        // When & Then
        assertThat(input.hasValidTransactionIdFormat()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "TXN@123", "TXN 123", "TXN#123", "TXN.123"})
    void hasValidTransactionIdFormat_WithInvalidFormats_ShouldReturnFalse(String transactionId) {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId(transactionId)
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .build();

        // When & Then
        assertThat(input.hasValidTransactionIdFormat()).isFalse();
    }

    @Test
    void hasValidTransactionIdFormat_WithNullTransactionId_ShouldReturnFalse() {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId(null)
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .build();

        // When & Then
        assertThat(input.hasValidTransactionIdFormat()).isFalse();
    }

    @Test
    void hasValidTransactionIdFormat_WithTooLongTransactionId_ShouldReturnFalse() {
        // Given
        String longTransactionId = "A".repeat(256);
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId(longTransactionId)
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .build();

        // When & Then
        assertThat(input.hasValidTransactionIdFormat()).isFalse();
    }

    @Test
    void hasResolutionNotes_WithNonEmptyNotes_ShouldReturnTrue() {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("Test resolution notes")
                .build();

        // When & Then
        assertThat(input.hasResolutionNotes()).isTrue();
    }

    @Test
    void hasResolutionNotes_WithEmptyNotes_ShouldReturnFalse() {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("")
                .build();

        // When & Then
        assertThat(input.hasResolutionNotes()).isFalse();
    }

    @Test
    void hasResolutionNotes_WithWhitespaceOnlyNotes_ShouldReturnFalse() {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("   ")
                .build();

        // When & Then
        assertThat(input.hasResolutionNotes()).isFalse();
    }

    @Test
    void hasResolutionNotes_WithNullNotes_ShouldReturnFalse() {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes(null)
                .build();

        // When & Then
        assertThat(input.hasResolutionNotes()).isFalse();
    }

    @Test
    void getTrimmedResolutionNotes_WithValidNotes_ShouldReturnTrimmedNotes() {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("  Test resolution notes  ")
                .build();

        // When & Then
        assertThat(input.getTrimmedResolutionNotes()).isEqualTo("Test resolution notes");
    }

    @Test
    void getTrimmedResolutionNotes_WithEmptyNotes_ShouldReturnNull() {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("")
                .build();

        // When & Then
        assertThat(input.getTrimmedResolutionNotes()).isNull();
    }

    @Test
    void getTrimmedResolutionNotes_WithWhitespaceOnlyNotes_ShouldReturnNull() {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("   ")
                .build();

        // When & Then
        assertThat(input.getTrimmedResolutionNotes()).isNull();
    }

    @Test
    void getTrimmedResolutionNotes_WithNullNotes_ShouldReturnNull() {
        // Given
        ResolveExceptionInput input = ResolveExceptionInput.builder()
                .transactionId("TXN-123")
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes(null)
                .build();

        // When & Then
        assertThat(input.getTrimmedResolutionNotes()).isNull();
    }

    @Test
    void allResolutionMethods_ShouldBeSupported() {
        // Test that all resolution methods can be used
        for (ResolutionMethod method : ResolutionMethod.values()) {
            ResolveExceptionInput input = ResolveExceptionInput.builder()
                    .transactionId("TXN-123")
                    .resolutionMethod(method)
                    .build();

            Set<ConstraintViolation<ResolveExceptionInput>> violations = validator.validate(input);
            assertThat(violations).isEmpty();
        }
    }
}