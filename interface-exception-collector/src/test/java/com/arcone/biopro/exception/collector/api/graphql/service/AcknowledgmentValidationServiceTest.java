package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionInput;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AcknowledgmentValidationService.
 */
@ExtendWith(MockitoExtension.class)
class AcknowledgmentValidationServiceTest {

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AcknowledgmentValidationService validationService;

    private AcknowledgeExceptionInput validInput;
    private InterfaceException testException;

    @BeforeEach
    void setUp() {
        validInput = AcknowledgeExceptionInput.builder()
                .transactionId("test-transaction-123")
                .reason("Manual acknowledgment requested")
                .notes("Test acknowledgment")
                .assignedTo("test-user")
                .build();

        testException = InterfaceException.builder()
                .id(1L)
                .transactionId("test-transaction-123")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Test exception")
                .operation("CREATE_ORDER")
                .status(ExceptionStatus.NEW)
                .retryable(true)
                .retryCount(0)
                .maxRetries(3)
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .build();

        // Setup authentication mock
        when(authentication.getName()).thenReturn("test-user");
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS")));
    }

    @Test
    void testValidateAcknowledgmentRequest_Success() {
        // Arrange
        when(exceptionRepository.findByTransactionId(anyString())).thenReturn(Optional.of(testException));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> validationService.validateAcknowledgmentRequest(validInput, authentication));
    }

    @Test
    void testValidateAcknowledgmentRequest_NullInput() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateAcknowledgmentRequest(null, authentication));
        assertEquals("Acknowledgment input cannot be null", exception.getMessage());
    }

@Test
    void testValidateAcknowledgmentRequest_EmptyTransactionId() {
        // Arrange
        AcknowledgeExceptionInput invalidInput = AcknowledgeExceptionInput.builder()
                .transactionId("")
                .reason("Test reason")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateAcknowledgmentRequest(invalidInput, authentication));
        assertEquals("Transaction ID is required", exception.getMessage());
    }

    @Test
    void testValidateAcknowledgmentRequest_EmptyReason() {
        // Arrange
        AcknowledgeExceptionInput invalidInput = AcknowledgeExceptionInput.builder()
                .transactionId("test-transaction-123")
                .reason("")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateAcknowledgmentRequest(invalidInput, authentication));
        assertEquals("Acknowledgment reason is required", exception.getMessage());
    }

    @Test
    void testValidateAcknowledgmentRequest_ReasonTooLong() {
        // Arrange
        String longReason = "a".repeat(1001);
        AcknowledgeExceptionInput invalidInput = AcknowledgeExceptionInput.builder()
                .transactionId("test-transaction-123")
                .reason(longReason)
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateAcknowledgmentRequest(invalidInput, authentication));
        assertEquals("Acknowledgment reason exceeds maximum length (1000 characters)", exception.getMessage());
    }

    @Test
    void testValidateAcknowledgmentRequest_NotesTooLong() {
        // Arrange
        String longNotes = "a".repeat(2001);
        AcknowledgeExceptionInput invalidInput = AcknowledgeExceptionInput.builder()
                .transactionId("test-transaction-123")
                .reason("Valid reason")
                .notes(longNotes)
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateAcknowledgmentRequest(invalidInput, authentication));
        assertEquals("Acknowledgment notes exceed maximum length (2000 characters)", exception.getMessage());
    }

    @Test
    void testValidateAcknowledgmentRequest_NoPermission() {
        // Arrange
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER")));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateAcknowledgmentRequest(validInput, authentication));
        assertTrue(exception.getMessage().contains("does not have permission"));
    }

    @Test
    void testValidateAcknowledgmentRequest_ExceptionNotFound() {
        // Arrange
        when(exceptionRepository.findByTransactionId(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateAcknowledgmentRequest(validInput, authentication));
        assertEquals("Exception not found with transaction ID: test-transaction-123", exception.getMessage());
    }

    @Test
    void testValidateAcknowledgmentRequest_ExceptionResolved() {
        // Arrange
        InterfaceException resolvedException = testException.toBuilder()
                .status(ExceptionStatus.RESOLVED)
                .build();
        when(exceptionRepository.findByTransactionId(anyString())).thenReturn(Optional.of(resolvedException));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateAcknowledgmentRequest(validInput, authentication));
        assertEquals("Cannot acknowledge exception - it has already been resolved", exception.getMessage());
    }

    @Test
    void testValidateAcknowledgmentRequest_ExceptionClosed() {
        // Arrange
        InterfaceException closedException = testException.toBuilder()
                .status(ExceptionStatus.CLOSED)
                .build();
        when(exceptionRepository.findByTransactionId(anyString())).thenReturn(Optional.of(closedException));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateAcknowledgmentRequest(validInput, authentication));
        assertEquals("Cannot acknowledge exception - it has been closed", exception.getMessage());
    }

    @Test
    void testValidateAcknowledgmentRequest_NonAdminAssignToOther() {
        // Arrange
        AcknowledgeExceptionInput inputWithAssignment = validInput.toBuilder()
                .assignedTo("other-user")
                .build();
        when(exceptionRepository.findByTransactionId(anyString())).thenReturn(Optional.of(testException));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateAcknowledgmentRequest(inputWithAssignment, authentication));
        assertEquals("Non-admin users can only assign exceptions to themselves", exception.getMessage());
    }

    @Test
    void testValidateAcknowledgmentRequest_AdminCanAssignToOther() {
        // Arrange
        AcknowledgeExceptionInput inputWithAssignment = validInput.toBuilder()
                .assignedTo("other-user")
                .build();
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(exceptionRepository.findByTransactionId(anyString())).thenReturn(Optional.of(testException));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> validationService.validateAcknowledgmentRequest(inputWithAssignment, authentication));
    }

    @Test
    void testValidateBulkAcknowledgmentRequest_Success() {
        // Arrange
        List<String> transactionIds = List.of("test-transaction-123", "test-transaction-456");

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> validationService.validateBulkAcknowledgmentRequest(transactionIds, authentication));
    }

    @Test
    void testValidateBulkAcknowledgmentRequest_EmptyList() {
        // Arrange
        List<String> emptyList = List.of();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateBulkAcknowledgmentRequest(emptyList, authentication));
        assertEquals("Transaction IDs list cannot be empty", exception.getMessage());
    }

    @Test
    void testValidateBulkAcknowledgmentRequest_DuplicateIds() {
        // Arrange
        List<String> duplicateIds = List.of("test-transaction-123", "test-transaction-123");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateBulkAcknowledgmentRequest(duplicateIds, authentication));
        assertEquals("Duplicate transaction IDs found in bulk acknowledgment request", exception.getMessage());
    }

    @Test
    void testValidateBulkOperationSize_Success() {
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> validationService.validateBulkOperationSize(10, authentication));
    }

    @Test
    void testValidateBulkOperationSize_ExceedsMaximum() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateBulkOperationSize(101, authentication));
        assertTrue(exception.getMessage().contains("exceeds maximum allowed"));
    }

    @Test
    void testValidateBulkOperationSize_NonAdminExceedsLimit() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateBulkOperationSize(51, authentication));
        assertEquals("Non-admin users cannot acknowledge more than 50 exceptions at once", exception.getMessage());
    }

    @Test
    void testValidateBulkOperationSize_AdminCanExceedNonAdminLimit() {
        // Arrange
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> validationService.validateBulkOperationSize(75, authentication));
    }
}