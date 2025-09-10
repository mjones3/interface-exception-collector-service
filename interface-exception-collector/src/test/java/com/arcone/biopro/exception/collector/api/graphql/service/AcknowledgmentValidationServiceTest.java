package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionInput;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AcknowledgmentValidationService.
 * Tests the simplified validation logic focusing on essential business rules.
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
    private InterfaceException validException;

    @BeforeEach
    void setUp() {
        validInput = AcknowledgeExceptionInput.builder()
                .transactionId("TXN-123")
                .reason("Valid acknowledgment reason")
                .notes("Optional notes")
                .build();

        validException = new InterfaceException();
        validException.setTransactionId("TXN-123");
        validException.setStatus(ExceptionStatus.FAILED);
        validException.setCreatedAt(OffsetDateTime.now().minusDays(1));

        // Setup mock authentication
        when(authentication.getName()).thenReturn("test.user");
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS"))
        );
    }

    @Test
    void validateAcknowledgmentRequest_WithValidInput_ShouldPass() {
        // Given
        when(exceptionRepository.findByTransactionId("TXN-123"))
                .thenReturn(Optional.of(validException));

        // When & Then
        assertDoesNotThrow(() -> 
                validationService.validateAcknowledgmentRequest(validInput, authentication));
    }

    @Test
    void validateAcknowledgmentRequest_WithNullInput_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateAcknowledgmentRequest(null, authentication));
        
        assertEquals("Acknowledgment input cannot be null", exception.getMessage());
    }

    @Test
    void validateAcknowledgmentRequest_WithEmptyTransactionId_ShouldThrowException() {
        // Given
        validInput.setTransactionId("");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateAcknowledgmentRequest(validInput, authentication));
        
        assertEquals("Transaction ID is required", exception.getMessage());
    }

    @Test
    void validateAcknowledgmentRequest_WithEmptyReason_ShouldThrowException() {
        // Given
        validInput.setReason("");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateAcknowledgmentRequest(validInput, authentication));
        
        assertEquals("Acknowledgment reason is required", exception.getMessage());
    }

    @Test
    void validateAcknowledgmentRequest_WithTooLongReason_ShouldThrowException() {
        // Given
        validInput.setReason("A".repeat(1001));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateAcknowledgmentRequest(validInput, authentication));
        
        assertEquals("Acknowledgment reason exceeds maximum length (1000 characters)", exception.getMessage());
    }

    @Test
    void validateAcknowledgmentRequest_WithTooLongNotes_ShouldThrowException() {
        // Given
        validInput.setNotes("A".repeat(2001));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateAcknowledgmentRequest(validInput, authentication));
        
        assertEquals("Acknowledgment notes exceed maximum length (2000 characters)", exception.getMessage());
    }

    @Test
    void validateAcknowledgmentRequest_WithNonExistentException_ShouldThrowException() {
        // Given
        when(exceptionRepository.findByTransactionId("TXN-123"))
                .thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateAcknowledgmentRequest(validInput, authentication));
        
        assertEquals("Exception not found with transaction ID: TXN-123", exception.getMessage());
    }

    @Test
    void validateAcknowledgmentRequest_WithResolvedStatus_ShouldThrowException() {
        // Given
        validException.setStatus(ExceptionStatus.RESOLVED);
        when(exceptionRepository.findByTransactionId("TXN-123"))
                .thenReturn(Optional.of(validException));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateAcknowledgmentRequest(validInput, authentication));
        
        assertEquals("Cannot acknowledge exception - it has already been resolved", exception.getMessage());
    }

    @Test
    void validateAcknowledgmentRequest_WithClosedStatus_ShouldThrowException() {
        // Given
        validException.setStatus(ExceptionStatus.CLOSED);
        when(exceptionRepository.findByTransactionId("TXN-123"))
                .thenReturn(Optional.of(validException));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateAcknowledgmentRequest(validInput, authentication));
        
        assertEquals("Cannot acknowledge exception - it has been closed", exception.getMessage());
    }

    @Test
    void validateAcknowledgmentRequest_WithRetryingStatus_ShouldPassWithWarning() {
        // Given
        validException.setStatus(ExceptionStatus.RETRYING);
        when(exceptionRepository.findByTransactionId("TXN-123"))
                .thenReturn(Optional.of(validException));

        // When & Then - Should not throw exception, just log warning
        assertDoesNotThrow(() -> 
                validationService.validateAcknowledgmentRequest(validInput, authentication));
    }

    @Test
    void validateAcknowledgmentRequest_WithAlreadyAcknowledged_ShouldPassWithInfo() {
        // Given
        validException.setStatus(ExceptionStatus.ACKNOWLEDGED);
        validException.setAcknowledgedBy("previous.user");
        when(exceptionRepository.findByTransactionId("TXN-123"))
                .thenReturn(Optional.of(validException));

        // When & Then - Should allow re-acknowledgment
        assertDoesNotThrow(() -> 
                validationService.validateAcknowledgmentRequest(validInput, authentication));
    }

    @Test
    void validateAcknowledgmentRequest_WithInsufficientPermissions_ShouldThrowException() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateAcknowledgmentRequest(validInput, authentication));
        
        assertTrue(exception.getMessage().contains("does not have permission to acknowledge exceptions"));
    }

    @Test
    void validateBulkAcknowledgmentRequest_WithValidInput_ShouldPass() {
        // Given
        List<String> transactionIds = List.of("TXN-1", "TXN-2", "TXN-3");

        // When & Then
        assertDoesNotThrow(() -> 
                validationService.validateBulkAcknowledgmentRequest(transactionIds, authentication));
    }

    @Test
    void validateBulkAcknowledgmentRequest_WithEmptyList_ShouldThrowException() {
        // Given
        List<String> transactionIds = List.of();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateBulkAcknowledgmentRequest(transactionIds, authentication));
        
        assertEquals("Transaction IDs list cannot be empty", exception.getMessage());
    }

    @Test
    void validateBulkAcknowledgmentRequest_WithDuplicates_ShouldThrowException() {
        // Given
        List<String> transactionIds = List.of("TXN-1", "TXN-2", "TXN-1");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateBulkAcknowledgmentRequest(transactionIds, authentication));
        
        assertEquals("Duplicate transaction IDs found in bulk acknowledgment request", exception.getMessage());
    }

    @Test
    void validateBulkOperationSize_WithValidSize_ShouldPass() {
        // When & Then
        assertDoesNotThrow(() -> 
                validationService.validateBulkOperationSize(50, authentication));
    }

    @Test
    void validateBulkOperationSize_WithExcessiveSize_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateBulkOperationSize(101, authentication));
        
        assertTrue(exception.getMessage().contains("exceeds maximum allowed"));
    }
}