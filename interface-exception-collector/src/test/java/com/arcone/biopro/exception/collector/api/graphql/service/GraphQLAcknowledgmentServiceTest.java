package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.dto.AcknowledgeRequest;
import com.arcone.biopro.exception.collector.api.dto.AcknowledgeResponse;
import com.arcone.biopro.exception.collector.api.graphql.dto.*;
import com.arcone.biopro.exception.collector.application.service.ExceptionManagementService;
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
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GraphQLAcknowledgmentService.
 */
@ExtendWith(MockitoExtension.class)
class GraphQLAcknowledgmentServiceTest {

    @Mock
    private ExceptionManagementService exceptionManagementService;

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    @Mock
    private AcknowledgmentValidationService validationService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private GraphQLAcknowledgmentService acknowledgmentService;

    private AcknowledgeExceptionInput acknowledgeInput;
    private BulkAcknowledgeInput bulkAcknowledgeInput;
    private InterfaceException testException;
    private AcknowledgeResponse acknowledgeResponse;

    @BeforeEach
    void setUp() {
        acknowledgeInput = AcknowledgeExceptionInput.builder()
                .transactionId("test-transaction-123")
                .reason("Manual acknowledgment requested")
                .notes("Test acknowledgment")
                .assignedTo("test-user")
                .build();

        bulkAcknowledgeInput = BulkAcknowledgeInput.builder()
                .transactionIds(List.of("test-transaction-123", "test-transaction-456"))
                .reason("Bulk acknowledgment requested")
                .notes("Test bulk acknowledgment")
                .assignedTo("test-user")
                .build();

        testException = InterfaceException.builder()
                .id(1L)
                .transactionId("test-transaction-123")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Test exception")
                .operation("CREATE_ORDER")
                .status(ExceptionStatus.ACKNOWLEDGED)
                .acknowledgedBy("test-user")
                .acknowledgedAt(OffsetDateTime.now())
                .retryable(true)
                .retryCount(0)
                .maxRetries(3)
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .build();

        acknowledgeResponse = AcknowledgeResponse.builder()
                .transactionId("test-transaction-123")
                .status("ACKNOWLEDGED")
                .acknowledgedBy("test-user")
                .acknowledgedAt(OffsetDateTime.now())
                .notes("Reason: Manual acknowledgment requested\nNotes: Test acknowledgment\nAssigned to: test-user")
                .build();

        // Setup authentication mock
        when(authentication.getName()).thenReturn("test-user");
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS")));
    }

    @Test
    void testAcknowledgeException_Success() {
        // Arrange
        doNothing().when(validationService).validateAcknowledgmentRequest(any(), any());
        when(exceptionManagementService.canAcknowledge(anyString())).thenReturn(true);
        when(exceptionManagementService.acknowledgeException(anyString(), any(AcknowledgeRequest.class)))
                .thenReturn(acknowledgeResponse);
        when(exceptionRepository.findByTransactionId(anyString())).thenReturn(Optional.of(testException));

        // Act
        CompletableFuture<AcknowledgeExceptionResult> result = acknowledgmentService
                .acknowledgeException(acknowledgeInput, authentication);

        // Assert
        assertNotNull(result);
        AcknowledgeExceptionResult actualResult = result.join();
        assertTrue(actualResult.isSuccess());
        assertEquals(testException, actualResult.getException());
        assertTrue(actualResult.getErrors().isEmpty());

        verify(validationService).validateAcknowledgmentRequest(eq(acknowledgeInput), eq(authentication));
        verify(exceptionManagementService).canAcknowledge(eq("test-transaction-123"));
        verify(exceptionManagementService).acknowledgeException(eq("test-transaction-123"),
                any(AcknowledgeRequest.class));
        verify(exceptionRepository).findByTransactionId(eq("test-transaction-123"));
    }

    @Test
    void testAcknowledgeException_ValidationFailure() {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid transaction ID"))
                .when(validationService).validateAcknowledgmentRequest(any(), any());

        // Act
        CompletableFuture<AcknowledgeExceptionResult> result = acknowledgmentService
                .acknowledgeException(acknowledgeInput, authentication);

        // Assert
        assertNotNull(result);
        AcknowledgeExceptionResult actualResult = result.join();
        assertFalse(actualResult.isSuccess());
        assertNull(actualResult.getException());
        assertFalse(actualResult.getErrors().isEmpty());
        assertEquals("VALIDATION_ERROR", actualResult.getErrors().get(0).getCode());
        assertEquals("Invalid transaction ID", actualResult.getErrors().get(0).getMessage());
    }

    @Test
    void testAcknowledgeException_CannotAcknowledge() {
        // Arrange
        doNothing().when(validationService).validateAcknowledgmentRequest(any(), any());
        when(exceptionManagementService.canAcknowledge(anyString())).thenReturn(false);

        // Act
        CompletableFuture<AcknowledgeExceptionResult> result = acknowledgmentService
                .acknowledgeException(acknowledgeInput, authentication);

        // Assert
        assertNotNull(result);
        AcknowledgeExceptionResult actualResult = result.join();
        assertFalse(actualResult.isSuccess());
        assertNull(actualResult.getException());
        assertFalse(actualResult.getErrors().isEmpty());
        assertEquals("ACKNOWLEDGMENT_NOT_ALLOWED", actualResult.getErrors().get(0).getCode());
    }

    @Test
    void testAcknowledgeException_OptimisticLockingFailure() {
        // Arrange
        doNothing().when(validationService).validateAcknowledgmentRequest(any(), any());
        when(exceptionManagementService.canAcknowledge(anyString())).thenReturn(true);
        when(exceptionManagementService.acknowledgeException(anyString(), any(AcknowledgeRequest.class)))
                .thenThrow(new OptimisticLockingFailureException("Concurrent modification"));

        // Act
        CompletableFuture<AcknowledgeExceptionResult> result = acknowledgmentService
                .acknowledgeException(acknowledgeInput, authentication);

        // Assert
        assertNotNull(result);
        AcknowledgeExceptionResult actualResult = result.join();
        assertFalse(actualResult.isSuccess());
        assertNull(actualResult.getException());
        assertFalse(actualResult.getErrors().isEmpty());
        assertEquals("CONCURRENT_MODIFICATION", actualResult.getErrors().get(0).getCode());
        assertTrue(actualResult.getErrors().get(0).getMessage().contains("modified by another user"));
    }

    @Test
    void testBulkAcknowledgeExceptions_Success() {
        // Arrange
        doNothing().when(validationService).validateBulkAcknowledgmentRequest(any(), any());
        doNothing().when(validationService).validateBulkOperationSize(anyInt(), any());

        // Mock successful acknowledgment for both transactions
        when(exceptionManagementService.canAcknowledge("test-transaction-123")).thenReturn(true);
        when(exceptionManagementService.canAcknowledge("test-transaction-456")).thenReturn(true);
        when(exceptionManagementService.acknowledgeException(anyString(), any(AcknowledgeRequest.class)))
                .thenReturn(acknowledgeResponse);
        when(exceptionRepository.findByTransactionId(anyString())).thenReturn(Optional.of(testException));

        // Act
        CompletableFuture<BulkAcknowledgeResult> result = acknowledgmentService
                .bulkAcknowledgeExceptions(bulkAcknowledgeInput, authentication);

        // Assert
        assertNotNull(result);
        BulkAcknowledgeResult actualResult = result.join();
        assertEquals(2, actualResult.getSuccessCount());
        assertEquals(0, actualResult.getFailureCount());
        assertEquals(2, actualResult.getResults().size());
        assertTrue(actualResult.getErrors().isEmpty());

        verify(validationService).validateBulkAcknowledgmentRequest(eq(bulkAcknowledgeInput.getTransactionIds()),
                eq(authentication));
        verify(validationService).validateBulkOperationSize(eq(2), eq(authentication));
    }

    @Test
    void testBulkAcknowledgeExceptions_PartialFailure() {
        // Arrange
        doNothing().when(validationService).validateBulkAcknowledgmentRequest(any(), any());
        doNothing().when(validationService).validateBulkOperationSize(anyInt(), any());

        // Mock first transaction success, second transaction failure
        when(exceptionManagementService.canAcknowledge("test-transaction-123")).thenReturn(true);
        when(exceptionManagementService.canAcknowledge("test-transaction-456")).thenReturn(false);
        when(exceptionManagementService.acknowledgeException(eq("test-transaction-123"), any(AcknowledgeRequest.class)))
                .thenReturn(acknowledgeResponse);
        when(exceptionRepository.findByTransactionId("test-transaction-123")).thenReturn(Optional.of(testException));

        // Act
        CompletableFuture<BulkAcknowledgeResult> result = acknowledgmentService
                .bulkAcknowledgeExceptions(bulkAcknowledgeInput, authentication);

        // Assert
        assertNotNull(result);
        BulkAcknowledgeResult actualResult = result.join();
        assertEquals(1, actualResult.getSuccessCount());
        assertEquals(1, actualResult.getFailureCount());
        assertEquals(2, actualResult.getResults().size());
        assertTrue(actualResult.getErrors().isEmpty());

        // Check individual results
        assertTrue(actualResult.getResults().get(0).isSuccess());
        assertFalse(actualResult.getResults().get(1).isSuccess());
    }

    @Test
    void testBulkAcknowledgeExceptions_ValidationFailure() {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid bulk request"))
                .when(validationService).validateBulkAcknowledgmentRequest(any(), any());

        // Act
        CompletableFuture<BulkAcknowledgeResult> result = acknowledgmentService
                .bulkAcknowledgeExceptions(bulkAcknowledgeInput, authentication);

        // Assert
        assertNotNull(result);
        BulkAcknowledgeResult actualResult = result.join();
        assertEquals(0, actualResult.getSuccessCount());
        assertEquals(2, actualResult.getFailureCount());
        assertTrue(actualResult.getResults().isEmpty());
        assertFalse(actualResult.getErrors().isEmpty());
        assertEquals("SERVICE_UNAVAILABLE", actualResult.getErrors().get(0).getCode());
    }
}