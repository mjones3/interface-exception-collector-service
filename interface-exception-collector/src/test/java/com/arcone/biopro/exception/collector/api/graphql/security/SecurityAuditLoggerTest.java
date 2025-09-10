package com.arcone.biopro.exception.collector.api.graphql.security;

import com.arcone.biopro.exception.collector.domain.entity.MutationAuditLog;
import com.arcone.biopro.exception.collector.infrastructure.repository.MutationAuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SecurityAuditLogger mutation audit logging functionality.
 * Tests the comprehensive audit logging for all mutation operations.
 * 
 * Requirements: 5.3, 5.5, 6.4
 */
@ExtendWith(MockitoExtension.class)
class SecurityAuditLoggerTest {

    @Mock
    private MutationAuditLogRepository auditLogRepository;

    @Mock
    private ObjectMapper objectMapper;

    private SecurityAuditLogger auditLogger;

    @BeforeEach
    void setUp() {
        auditLogger = new SecurityAuditLogger(objectMapper, auditLogRepository);
    }

    @Test
    void logMutationAttempt_ShouldCreateAuditLogEntry() {
        // Given
        String transactionId = "TXN-123";
        String performedBy = "test-user";
        Map<String, Object> inputData = Map.of("reason", "Test retry");
        String operationId = "RETRY_TXN123_123456789";
        String correlationId = "corr-123";

        when(auditLogRepository.save(any(MutationAuditLog.class))).thenReturn(new MutationAuditLog());

        // When
        auditLogger.logMutationAttempt(
                MutationAuditLog.OperationType.RETRY,
                transactionId,
                performedBy,
                inputData,
                operationId,
                correlationId
        );

        // Then
        ArgumentCaptor<MutationAuditLog> captor = ArgumentCaptor.forClass(MutationAuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        MutationAuditLog savedLog = captor.getValue();
        assertThat(savedLog.getOperationType()).isEqualTo(MutationAuditLog.OperationType.RETRY);
        assertThat(savedLog.getTransactionId()).isEqualTo(transactionId);
        assertThat(savedLog.getPerformedBy()).isEqualTo(performedBy);
        assertThat(savedLog.getOperationId()).isEqualTo(operationId);
        assertThat(savedLog.getCorrelationId()).isEqualTo(correlationId);
        assertThat(savedLog.getResultStatus()).isEqualTo(MutationAuditLog.ResultStatus.SUCCESS);
    }

    @Test
    void logMutationResult_ShouldUpdateExistingAuditLog() {
        // Given
        String operationId = "RETRY_TXN123_123456789";
        boolean success = true;
        List<Object> errors = List.of();
        long executionTimeMs = 150L;

        MutationAuditLog existingLog = MutationAuditLog.builder()
                .operationId(operationId)
                .operationType(MutationAuditLog.OperationType.RETRY)
                .transactionId("TXN-123")
                .performedBy("test-user")
                .resultStatus(MutationAuditLog.ResultStatus.SUCCESS)
                .build();

        when(auditLogRepository.findByOperationIdOrderByPerformedAtAsc(operationId))
                .thenReturn(List.of(existingLog));
        when(auditLogRepository.save(any(MutationAuditLog.class))).thenReturn(existingLog);

        // When
        auditLogger.logMutationResult(operationId, success, errors, executionTimeMs);

        // Then
        ArgumentCaptor<MutationAuditLog> captor = ArgumentCaptor.forClass(MutationAuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        MutationAuditLog updatedLog = captor.getValue();
        assertThat(updatedLog.getResultStatus()).isEqualTo(MutationAuditLog.ResultStatus.SUCCESS);
        assertThat(updatedLog.getExecutionTimeMs()).isEqualTo(150);
    }

    @Test
    void logBulkMutationResult_ShouldHandlePartialSuccess() {
        // Given
        String operationId = "BULK_RETRY_MULTIPLE_123456789";
        int successCount = 3;
        int failureCount = 2;
        List<Object> errors = List.of("Error 1", "Error 2");
        long executionTimeMs = 500L;

        MutationAuditLog existingLog = MutationAuditLog.builder()
                .operationId(operationId)
                .operationType(MutationAuditLog.OperationType.BULK_RETRY)
                .transactionId("BULK_5_TRANSACTIONS")
                .performedBy("test-user")
                .resultStatus(MutationAuditLog.ResultStatus.SUCCESS)
                .build();

        when(auditLogRepository.findByOperationIdOrderByPerformedAtAsc(operationId))
                .thenReturn(List.of(existingLog));
        when(auditLogRepository.save(any(MutationAuditLog.class))).thenReturn(existingLog);

        // When
        auditLogger.logBulkMutationResult(operationId, successCount, failureCount, errors, executionTimeMs);

        // Then
        ArgumentCaptor<MutationAuditLog> captor = ArgumentCaptor.forClass(MutationAuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        MutationAuditLog updatedLog = captor.getValue();
        assertThat(updatedLog.getResultStatus()).isEqualTo(MutationAuditLog.ResultStatus.PARTIAL_SUCCESS);
        assertThat(updatedLog.getExecutionTimeMs()).isEqualTo(500);
    }

    @Test
    void generateOperationId_ShouldCreateUniqueId() {
        // When
        String operationId1 = auditLogger.generateOperationId("retry", "TXN-123");
        String operationId2 = auditLogger.generateOperationId("retry", "TXN-123");

        // Then
        assertThat(operationId1).startsWith("RETRY_TXN123_");
        assertThat(operationId2).startsWith("RETRY_TXN123_");
        assertThat(operationId1).isNotEqualTo(operationId2); // Should be unique due to timestamp
    }

    @Test
    void generateCorrelationId_ShouldCreateUniqueUUID() {
        // When
        String correlationId1 = auditLogger.generateCorrelationId();
        String correlationId2 = auditLogger.generateCorrelationId();

        // Then
        assertThat(correlationId1).isNotNull();
        assertThat(correlationId2).isNotNull();
        assertThat(correlationId1).isNotEqualTo(correlationId2);
        assertThat(correlationId1).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    void logMutationAttempt_ShouldHandleNullInputData() {
        // Given
        String transactionId = "TXN-123";
        String performedBy = "test-user";
        String operationId = "RETRY_TXN123_123456789";
        String correlationId = "corr-123";

        when(auditLogRepository.save(any(MutationAuditLog.class))).thenReturn(new MutationAuditLog());

        // When
        auditLogger.logMutationAttempt(
                MutationAuditLog.OperationType.RETRY,
                transactionId,
                performedBy,
                null, // null input data
                operationId,
                correlationId
        );

        // Then
        ArgumentCaptor<MutationAuditLog> captor = ArgumentCaptor.forClass(MutationAuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        MutationAuditLog savedLog = captor.getValue();
        assertThat(savedLog.getInputData()).isNull();
    }

    @Test
    void logMutationResult_ShouldHandleFailureCase() {
        // Given
        String operationId = "RETRY_TXN123_123456789";
        boolean success = false;
        List<Object> errors = List.of("Validation failed", "Transaction not found");
        long executionTimeMs = 50L;

        MutationAuditLog existingLog = MutationAuditLog.builder()
                .operationId(operationId)
                .operationType(MutationAuditLog.OperationType.RETRY)
                .transactionId("TXN-123")
                .performedBy("test-user")
                .resultStatus(MutationAuditLog.ResultStatus.SUCCESS)
                .build();

        when(auditLogRepository.findByOperationIdOrderByPerformedAtAsc(operationId))
                .thenReturn(List.of(existingLog));
        when(auditLogRepository.save(any(MutationAuditLog.class))).thenReturn(existingLog);

        // When
        auditLogger.logMutationResult(operationId, success, errors, executionTimeMs);

        // Then
        ArgumentCaptor<MutationAuditLog> captor = ArgumentCaptor.forClass(MutationAuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        MutationAuditLog updatedLog = captor.getValue();
        assertThat(updatedLog.getResultStatus()).isEqualTo(MutationAuditLog.ResultStatus.FAILURE);
        assertThat(updatedLog.getExecutionTimeMs()).isEqualTo(50);
    }
}