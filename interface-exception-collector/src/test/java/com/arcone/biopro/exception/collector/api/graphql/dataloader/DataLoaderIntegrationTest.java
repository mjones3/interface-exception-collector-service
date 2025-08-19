package com.arcone.biopro.exception.collector.api.graphql.dataloader;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.api.graphql.config.DataLoaderConfig;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for DataLoader implementations.
 * Tests the batching behavior and caching functionality of all DataLoaders.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataLoaderIntegrationTest {

    @Autowired
    private DataLoaderConfig dataLoaderConfig;

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    @Autowired
    private RetryAttemptRepository retryAttemptRepository;

    @Autowired
    private ExceptionDataLoader exceptionDataLoader;

    @Autowired
    private RetryHistoryDataLoader retryHistoryDataLoader;

    private DataLoaderRegistry registry;
    private InterfaceException testException1;
    private InterfaceException testException2;
    private RetryAttempt retryAttempt1;
    private RetryAttempt retryAttempt2;

    @BeforeEach
    void setUp() {
        registry = dataLoaderConfig.dataLoaderRegistry();

        // Create test data
        testException1 = InterfaceException.builder()
                .transactionId("TEST-001")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Test exception 1")
                .operation("CREATE_ORDER")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.MEDIUM)
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .build();

        testException2 = InterfaceException.builder()
                .transactionId("TEST-002")
                .interfaceType(InterfaceType.COLLECTION)
                .exceptionReason("Test exception 2")
                .operation("COLLECT_SAMPLE")
                .status(ExceptionStatus.ACKNOWLEDGED)
                .severity(ExceptionSeverity.HIGH)
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .build();

        exceptionRepository.save(testException1);
        exceptionRepository.save(testException2);

        // Create retry attempts
        retryAttempt1 = RetryAttempt.builder()
                .interfaceException(testException1)
                .attemptNumber(1)
                .status(RetryStatus.SUCCESS)
                .initiatedBy("test-user")
                .initiatedAt(OffsetDateTime.now())
                .resultSuccess(true)
                .build();

        retryAttempt2 = RetryAttempt.builder()
                .interfaceException(testException1)
                .attemptNumber(2)
                .status(RetryStatus.FAILED)
                .initiatedBy("test-user")
                .initiatedAt(OffsetDateTime.now())
                .resultSuccess(false)
                .build();

        retryAttemptRepository.save(retryAttempt1);
        retryAttemptRepository.save(retryAttempt2);
    }

    @Test
    void testExceptionDataLoaderBatching() throws Exception {
        // Given
        DataLoader<String, InterfaceException> loader = registry.getDataLoader(DataLoaderConfig.EXCEPTION_LOADER);
        assertThat(loader).isNotNull();

        // When - Load multiple exceptions
        CompletableFuture<InterfaceException> future1 = loader.load("TEST-001");
        CompletableFuture<InterfaceException> future2 = loader.load("TEST-002");
        CompletableFuture<InterfaceException> future3 = loader.load("NON-EXISTENT");

        // Dispatch the batch
        loader.dispatch();

        // Then
        InterfaceException result1 = future1.get();
        InterfaceException result2 = future2.get();
        InterfaceException result3 = future3.get();

        assertThat(result1).isNotNull();
        assertThat(result1.getTransactionId()).isEqualTo("TEST-001");
        assertThat(result1.getInterfaceType()).isEqualTo(InterfaceType.ORDER);

        assertThat(result2).isNotNull();
        assertThat(result2.getTransactionId()).isEqualTo("TEST-002");
        assertThat(result2.getInterfaceType()).isEqualTo(InterfaceType.COLLECTION);

        assertThat(result3).isNull(); // Non-existent exception
    }

    @Test
    void testExceptionDataLoaderCaching() throws Exception {
        // Given
        DataLoader<String, InterfaceException> loader = registry.getDataLoader(DataLoaderConfig.EXCEPTION_LOADER);

        // When - Load the same exception twice
        CompletableFuture<InterfaceException> future1 = loader.load("TEST-001");
        loader.dispatch();
        InterfaceException result1 = future1.get();

        // Load again (should come from cache)
        CompletableFuture<InterfaceException> future2 = loader.load("TEST-001");
        loader.dispatch();
        InterfaceException result2 = future2.get();

        // Then
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1).isSameAs(result2); // Should be the same instance from cache
    }

    @Test
    void testRetryHistoryDataLoaderBatching() throws Exception {
        // Given
        DataLoader<String, List<RetryAttempt>> loader = registry.getDataLoader(DataLoaderConfig.RETRY_HISTORY_LOADER);
        assertThat(loader).isNotNull();

        // When - Load retry history for multiple exceptions
        CompletableFuture<List<RetryAttempt>> future1 = loader.load("TEST-001");
        CompletableFuture<List<RetryAttempt>> future2 = loader.load("TEST-002");
        CompletableFuture<List<RetryAttempt>> future3 = loader.load("NON-EXISTENT");

        // Dispatch the batch
        loader.dispatch();

        // Then
        List<RetryAttempt> result1 = future1.get();
        List<RetryAttempt> result2 = future2.get();
        List<RetryAttempt> result3 = future3.get();

        assertThat(result1).hasSize(2); // testException1 has 2 retry attempts
        assertThat(result1.get(0).getAttemptNumber()).isEqualTo(1);
        assertThat(result1.get(1).getAttemptNumber()).isEqualTo(2);

        assertThat(result2).isEmpty(); // testException2 has no retry attempts

        assertThat(result3).isEmpty(); // Non-existent exception
    }

    @Test
    void testRetryHistoryDataLoaderSorting() throws Exception {
        // Given
        DataLoader<String, List<RetryAttempt>> loader = registry.getDataLoader(DataLoaderConfig.RETRY_HISTORY_LOADER);

        // When
        CompletableFuture<List<RetryAttempt>> future = loader.load("TEST-001");
        loader.dispatch();
        List<RetryAttempt> result = future.get();

        // Then - Retry attempts should be sorted by attempt number
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAttemptNumber()).isEqualTo(1);
        assertThat(result.get(0).getStatus()).isEqualTo(RetryStatus.SUCCESS);
        assertThat(result.get(1).getAttemptNumber()).isEqualTo(2);
        assertThat(result.get(1).getStatus()).isEqualTo(RetryStatus.FAILED);
    }

    @Test
    void testDataLoaderRegistryConfiguration() {
        // Given & When
        DataLoaderRegistry registry = dataLoaderConfig.dataLoaderRegistry();

        // Then
        assertThat(registry).isNotNull();
        assertThat(registry.getKeys()).containsExactlyInAnyOrder(
                DataLoaderConfig.EXCEPTION_LOADER,
                DataLoaderConfig.PAYLOAD_LOADER,
                DataLoaderConfig.RETRY_HISTORY_LOADER);

        // Verify each DataLoader is properly configured
        DataLoader<String, InterfaceException> exceptionLoader = registry
                .getDataLoader(DataLoaderConfig.EXCEPTION_LOADER);
        assertThat(exceptionLoader).isNotNull();

        DataLoader<String, PayloadResponse> payloadLoader = registry.getDataLoader(DataLoaderConfig.PAYLOAD_LOADER);
        assertThat(payloadLoader).isNotNull();

        DataLoader<String, List<RetryAttempt>> retryLoader = registry
                .getDataLoader(DataLoaderConfig.RETRY_HISTORY_LOADER);
        assertThat(retryLoader).isNotNull();
    }

    @Test
    void testExceptionDataLoaderDirectBatchLoad() throws Exception {
        // Given
        Set<String> transactionIds = Set.of("TEST-001", "TEST-002", "NON-EXISTENT");

        // When
        CompletableFuture<Map<String, InterfaceException>> future = exceptionDataLoader.load(transactionIds)
                .toCompletableFuture();
        Map<String, InterfaceException> result = future.get();

        // Then
        assertThat(result).hasSize(2); // Only existing exceptions
        assertThat(result).containsKey("TEST-001");
        assertThat(result).containsKey("TEST-002");
        assertThat(result).doesNotContainKey("NON-EXISTENT");

        assertThat(result.get("TEST-001").getInterfaceType()).isEqualTo(InterfaceType.ORDER);
        assertThat(result.get("TEST-002").getInterfaceType()).isEqualTo(InterfaceType.COLLECTION);
    }

    @Test
    void testRetryHistoryDataLoaderDirectBatchLoad() throws Exception {
        // Given
        Set<String> transactionIds = Set.of("TEST-001", "TEST-002", "NON-EXISTENT");

        // When
        CompletableFuture<Map<String, List<RetryAttempt>>> future = retryHistoryDataLoader.load(transactionIds)
                .toCompletableFuture();
        Map<String, List<RetryAttempt>> result = future.get();

        // Then
        assertThat(result).hasSize(3); // All requested IDs should have entries
        assertThat(result.get("TEST-001")).hasSize(2);
        assertThat(result.get("TEST-002")).isEmpty();
        assertThat(result.get("NON-EXISTENT")).isEmpty();

        // Verify sorting
        List<RetryAttempt> retries = result.get("TEST-001");
        assertThat(retries.get(0).getAttemptNumber()).isEqualTo(1);
        assertThat(retries.get(1).getAttemptNumber()).isEqualTo(2);
    }
}