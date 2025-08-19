package com.arcone.biopro.exception.collector.api.graphql.dataloader;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.StatusChange;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.infrastructure.repository.StatusChangeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for StatusChangeDataLoader.
 * Tests batching and error handling for status change queries.
 */
@ExtendWith(MockitoExtension.class)
class StatusChangeDataLoaderTest {

    @Mock
    private StatusChangeRepository statusChangeRepository;

    private StatusChangeDataLoader dataLoader;

    @BeforeEach
    void setUp() {
        dataLoader = new StatusChangeDataLoader(statusChangeRepository);
    }

    @Test
    void testLoad_WithValidTransactionIds_ShouldReturnGroupedResults() throws ExecutionException, InterruptedException {
        // Given
        Set<String> transactionIds = Set.of("TXN-001", "TXN-002");

        InterfaceException exception1 = createTestException("TXN-001");
        InterfaceException exception2 = createTestException("TXN-002");

        List<StatusChange> statusChanges = List.of(
                createStatusChange(exception1, ExceptionStatus.NEW, ExceptionStatus.ACKNOWLEDGED),
                createStatusChange(exception1, ExceptionStatus.ACKNOWLEDGED, ExceptionStatus.RESOLVED),
                createStatusChange(exception2, ExceptionStatus.NEW, ExceptionStatus.IN_PROGRESS));

        when(statusChangeRepository.findByInterfaceExceptionTransactionIdInOrderByChangedAtDesc(transactionIds))
                .thenReturn(statusChanges);

        // When
        CompletionStage<Map<String, List<StatusChange>>> result = dataLoader.load(transactionIds);
        Map<String, List<StatusChange>> resultMap = result.toCompletableFuture().get();

        // Then
        assertNotNull(resultMap);
        assertEquals(2, resultMap.size());

        assertTrue(resultMap.containsKey("TXN-001"));
        assertTrue(resultMap.containsKey("TXN-002"));

        assertEquals(2, resultMap.get("TXN-001").size());
        assertEquals(1, resultMap.get("TXN-002").size());
    }

    @Test
    void testLoad_WithEmptyResults_ShouldReturnEmptyLists() throws ExecutionException, InterruptedException {
        // Given
        Set<String> transactionIds = Set.of("TXN-001", "TXN-002");

        when(statusChangeRepository.findByInterfaceExceptionTransactionIdInOrderByChangedAtDesc(transactionIds))
                .thenReturn(List.of());

        // When
        CompletionStage<Map<String, List<StatusChange>>> result = dataLoader.load(transactionIds);
        Map<String, List<StatusChange>> resultMap = result.toCompletableFuture().get();

        // Then
        assertNotNull(resultMap);
        assertEquals(2, resultMap.size());

        assertTrue(resultMap.containsKey("TXN-001"));
        assertTrue(resultMap.containsKey("TXN-002"));

        assertTrue(resultMap.get("TXN-001").isEmpty());
        assertTrue(resultMap.get("TXN-002").isEmpty());
    }

    @Test
    void testLoad_WithRepositoryException_ShouldReturnEmptyLists() throws ExecutionException, InterruptedException {
        // Given
        Set<String> transactionIds = Set.of("TXN-001", "TXN-002");

        when(statusChangeRepository.findByInterfaceExceptionTransactionIdInOrderByChangedAtDesc(any()))
                .thenThrow(new RuntimeException("Database error"));

        // When
        CompletionStage<Map<String, List<StatusChange>>> result = dataLoader.load(transactionIds);
        Map<String, List<StatusChange>> resultMap = result.toCompletableFuture().get();

        // Then
        assertNotNull(resultMap);
        assertEquals(2, resultMap.size());

        assertTrue(resultMap.containsKey("TXN-001"));
        assertTrue(resultMap.containsKey("TXN-002"));

        assertTrue(resultMap.get("TXN-001").isEmpty());
        assertTrue(resultMap.get("TXN-002").isEmpty());
    }

    @Test
    void testLoad_WithEmptyTransactionIds_ShouldReturnEmptyMap() throws ExecutionException, InterruptedException {
        // Given
        Set<String> transactionIds = Set.of();

        // When
        CompletionStage<Map<String, List<StatusChange>>> result = dataLoader.load(transactionIds);
        Map<String, List<StatusChange>> resultMap = result.toCompletableFuture().get();

        // Then
        assertNotNull(resultMap);
        assertTrue(resultMap.isEmpty());
    }

    private InterfaceException createTestException(String transactionId) {
        return InterfaceException.builder()
                .id(1L)
                .transactionId(transactionId)
                .status(ExceptionStatus.NEW)
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .build();
    }

    private StatusChange createStatusChange(InterfaceException exception, ExceptionStatus fromStatus,
            ExceptionStatus toStatus) {
        return StatusChange.builder()
                .id(1L)
                .interfaceException(exception)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedBy("test-user")
                .changedAt(OffsetDateTime.now())
                .reason("Test status change")
                .build();
    }
}