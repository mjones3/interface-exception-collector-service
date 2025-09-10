package com.arcone.biopro.exception.collector.api.graphql.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for simplified AcknowledgeExceptionInput DTO.
 * Verifies validation rules and field constraints.
 */
class AcknowledgeExceptionInputTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validInput_ShouldPassValidation() {
        // Given
        AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                .transactionId("TXN-123")
                .reason("Valid acknowledgment reason")
                .notes("Optional notes")
                .build();

        // When
        Set<ConstraintViolation<AcknowledgeExceptionInput>> violations = validator.validate(input);

        // Then
        assertTrue(violations.isEmpty(), "Valid input should pass validation");
    }

    @Test
    void validInputWithoutNotes_ShouldPassValidation() {
        // Given
        AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                .transactionId("TXN-123")
                .reason("Valid acknowledgment reason")
                .build();

        // When
        Set<ConstraintViolation<AcknowledgeExceptionInput>> violations = validator.validate(input);

        // Then
        assertTrue(violations.isEmpty(), "Valid input without notes should pass validation");
    }

    @Test
    void emptyTransactionId_ShouldFailValidation() {
        // Given
        AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                .transactionId("")
                .reason("Valid acknowledgment reason")
                .build();

        // When
        Set<ConstraintViolation<AcknowledgeExceptionInput>> violations = validator.validate(input);

        // Then
        assertFalse(violations.isEmpty(), "Empty transaction ID should fail validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Transaction ID is required")));
    }

    @Test
    void nullTransactionId_ShouldFailValidation() {
        // Given
        AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                .transactionId(null)
                .reason("Valid acknowledgment reason")
                .build();

        // When
        Set<ConstraintViolation<AcknowledgeExceptionInput>> violations = validator.validate(input);

        // Then
        assertFalse(violations.isEmpty(), "Null transaction ID should fail validation");
    }

    @Test
    void emptyReason_ShouldFailValidation() {
        // Given
        AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                .transactionId("TXN-123")
                .reason("")
                .build();

        // When
        Set<ConstraintViolation<AcknowledgeExceptionInput>> violations = validator.validate(input);

        // Then
        assertFalse(violations.isEmpty(), "Empty reason should fail validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Acknowledgment reason is required")));
    }

    @Test
    void nullReason_ShouldFailValidation() {
        // Given
        AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                .transactionId("TXN-123")
                .reason(null)
                .build();

        // When
        Set<ConstraintViolation<AcknowledgeExceptionInput>> violations = validator.validate(input);

        // Then
        assertFalse(violations.isEmpty(), "Null reason should fail validation");
    }

    @Test
    void tooLongTransactionId_ShouldFailValidation() {
        // Given
        String longTransactionId = "A".repeat(256); // Exceeds 255 character limit
        AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                .transactionId(longTransactionId)
                .reason("Valid acknowledgment reason")
                .build();

        // When
        Set<ConstraintViolation<AcknowledgeExceptionInput>> violations = validator.validate(input);

        // Then
        assertFalse(violations.isEmpty(), "Too long transaction ID should fail validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("must not exceed 255 characters")));
    }

    @Test
    void tooLongReason_ShouldFailValidation() {
        // Given
        String longReason = "A".repeat(1001); // Exceeds 1000 character limit
        AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                .transactionId("TXN-123")
                .reason(longReason)
                .build();

        // When
        Set<ConstraintViolation<AcknowledgeExceptionInput>> violations = validator.validate(input);

        // Then
        assertFalse(violations.isEmpty(), "Too long reason should fail validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("must not exceed 1000 characters")));
    }

    @Test
    void tooLongNotes_ShouldFailValidation() {
        // Given
        String longNotes = "A".repeat(2001); // Exceeds 2000 character limit
        AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                .transactionId("TXN-123")
                .reason("Valid acknowledgment reason")
                .notes(longNotes)
                .build();

        // When
        Set<ConstraintViolation<AcknowledgeExceptionInput>> violations = validator.validate(input);

        // Then
        assertFalse(violations.isEmpty(), "Too long notes should fail validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("must not exceed 2000 characters")));
    }

    @Test
    void maxLengthFields_ShouldPassValidation() {
        // Given
        AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                .transactionId("A".repeat(255)) // Max allowed length
                .reason("A".repeat(1000)) // Max allowed length
                .notes("A".repeat(2000)) // Max allowed length
                .build();

        // When
        Set<ConstraintViolation<AcknowledgeExceptionInput>> violations = validator.validate(input);

        // Then
        assertTrue(violations.isEmpty(), "Max length fields should pass validation");
    }

    @Test
    void builderPattern_ShouldWork() {
        // Given & When
        AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                .transactionId("TXN-123")
                .reason("Test reason")
                .notes("Test notes")
                .build();

        // Then
        assertEquals("TXN-123", input.getTransactionId());
        assertEquals("Test reason", input.getReason());
        assertEquals("Test notes", input.getNotes());
    }

    @Test
    void noArgsConstructor_ShouldWork() {
        // Given & When
        AcknowledgeExceptionInput input = new AcknowledgeExceptionInput();
        input.setTransactionId("TXN-123");
        input.setReason("Test reason");
        input.setNotes("Test notes");

        // Then
        assertEquals("TXN-123", input.getTransactionId());
        assertEquals("Test reason", input.getReason());
        assertEquals("Test notes", input.getNotes());
    }
}