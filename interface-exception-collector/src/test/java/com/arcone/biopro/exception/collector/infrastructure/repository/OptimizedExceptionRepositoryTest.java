package com.arcone.biopro.exception.collector.infrastructure.repository;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for OptimizedExceptionRepository.
 * Tests the optimized queries for mutation operations.
 */
@DataJpaTest
@ActiveProfiles("test")
class OptimizedExceptionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OptimizedExceptionRepository optimizedRepository;

    @Autowired
    private RetryAttemptRepository retryAttemptRepository;

    private InterfaceException testException;
    private InterfaceException nonRetryableException;
    private InterfaceException resolvedException;

    @BeforeEach
    void setUp() {
        // Create test exception that is retryable
        testException = InterfaceException.builder()
            .transactionId("TXN-001")
            .interfaceType(InterfaceType.ORDER)
            .exceptionReason("Test exception")
            .operation("CREATE_ORDER")
            .status(ExceptionStatus.NEW)
            .severity(ExceptionSeverity.MEDIUM)
            .category(ExceptionCategory.BUSINESS_RULE)
            .retryable(true)
            .retryCount(1)
            .maxRetries(3)
            .timestamp(OffsetDateTime.now())
            .processedAt(OffsetDateTime.now())
            .build();

        // Create non-retryable exception
        nonRetryableException = InterfaceException.builder()
            .transactionId("TXN-002")
            .interfaceType(InterfaceType.COLLECTION)
            .exceptionReason("Non-retryable exception")
            .operation("COLLECT_SAMPLE")
            .status(ExceptionStatus.NEW)
            .severity(ExceptionSeverity.HIGH)
            .category(ExceptionCategory.TECHNICAL)
            .retryable(false)
            .retryCount(0)
            .maxRetries(3)
            .timestamp(OffsetDateTime.now())
            .processedAt(OffsetDateTime.now())
            .build();

        // Create resolved exception
        resolvedException = InterfaceException.builder()
            .transactionId("TXN-003")
            .interfaceType(InterfaceType.DISTRIBUTION)
            .exceptionReason("Resolved exception")
            .operation("DISTRIBUTE_SAMPLE")
            .status(ExceptionStatus.RESOLVED)
            .severity(ExceptionSeverity.LOW)
            .category(ExceptionCategory.BUSINESS_RULE)
            .retryable(true)
            .retryCount(0)
            .maxRetries(3)
            .timestamp(OffsetDateTime.now())
            .processedAt(OffsetDateTime.now())
            .resolvedAt(OffsetDateTime.now())
            .resolvedBy("test-user")
            .build();

        entityManager.persistAndFlush(testException);
        entityManager.persistAndFlush(nonRetryableException);
        entityManager.persistAndFlush(resolvedException);
    }

    @Test
    void findByTransactionIdOptimized_ShouldReturnException_WhenExists() {
        // When
        Optional<InterfaceException> result = optimizedRepository.findByTransactionIdOptimized("TXN-001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTransactionId()).isEqualTo("TXN-001");
        assertThat(result.get().getExceptionReason()).isEqualTo("Test exception");
    }

    @Test
    void findByTransactionIdOptimized_ShouldReturnEmpty_WhenNotExists() {
        // When
        Optional<InterfaceException> result = optimizedRepository.findByTransactionIdOptimized("TXN-999");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findRetryableExceptionByTransactionId_ShouldReturnException_WhenRetryable() {
        // Given
        List<ExceptionStatus> retryableStatuses = Arrays.asList(
            ExceptionStatus.NEW, ExceptionStatus.RETRIED_FAILED, ExceptionStatus.ESCALATED
        );

        // When
        Optional<InterfaceException> result = optimizedRepository.findRetryableExceptionByTransactionId(
            "TXN-001", retryableStatuses);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTransactionId()).isEqualTo("TXN-001");
        assertThat(result.get().getRetryable()).isTrue();
    }

    @Test
    void findRetryableExceptionByTransactionId_ShouldReturnEmpty_WhenNotRetryable() {
        // Given
        List<ExceptionStatus> retryableStatuses = Arrays.asList(
            ExceptionStatus.NEW, ExceptionStatus.RETRIED_FAILED, ExceptionStatus.ESCALATED
        );

        // When
        Optional<InterfaceException> result = optimizedRepository.findRetryableExceptionByTransactionId(
            "TXN-002", retryableStatuses);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findRetryableExceptionByTransactionId_ShouldReturnEmpty_WhenStatusNotRetryable() {
        // Given
        List<ExceptionStatus> retryableStatuses = Arrays.asList(
            ExceptionStatus.NEW, ExceptionStatus.RETRIED_FAILED, ExceptionStatus.ESCALATED
        );

        // When
        Optional<InterfaceException> result = optimizedRepository.findRetryableExceptionByTransactionId(
            "TXN-003", retryableStatuses);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getRetryLimits_ShouldReturnLimits_WhenExists() {
        // When
        Object[] result = optimizedRepository.getRetryLimits("TXN-001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result[0]).isEqualTo(1); // retryCount
        assertThat(result[1]).isEqualTo(3); // maxRetries
    }

    @Test
    void getRetryLimits_ShouldReturnNull_WhenNotExists() {
        // When
        Object[] result = optimizedRepository.getRetryLimits("TXN-999");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void countPendingRetries_ShouldReturnZero_WhenNoPendingRetries() {
        // When
        long result = optimizedRepository.countPendingRetries("TXN-001");

        // Then
        assertThat(result).isEqualTo(0);
    }

    @Test
    void countPendingRetries_ShouldReturnCount_WhenPendingRetriesExist() {
        // Given
        RetryAttempt pendingRetry = RetryAttempt.builder()
            .interfaceException(testException)
            .attemptNumber(2)
            .status(RetryStatus.PENDING)
            .initiatedAt(OffsetDateTime.now())
            .initiatedBy("test-user")
            .reason("Test retry")
            .build();
        
        retryAttemptRepository.saveAndFlush(pendingRetry);

        // When
        long result = optimizedRepository.countPendingRetries("TXN-001");

        // Then
        assertThat(result).isEqualTo(1);
    }

    @Test
    void getValidationInfo_ShouldReturnInfo_WhenExists() {
        // When
        Object[] result = optimizedRepository.getValidationInfo("TXN-001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(5);
        assertThat(result[0]).isEqualTo(testException.getId()); // id
        assertThat(result[1]).isEqualTo(ExceptionStatus.NEW); // status
        assertThat(result[2]).isEqualTo(true); // retryable
        assertThat(result[3]).isEqualTo(1); // retryCount
        assertThat(result[4]).isEqualTo(3); // maxRetries
    }

    @Test
    void getValidationInfo_ShouldReturnNull_WhenNotExists() {
        // When
        Object[] result = optimizedRepository.getValidationInfo("TXN-999");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void hasCancellableRetries_ShouldReturnFalse_WhenNoActiveRetries() {
        // When
        boolean result = optimizedRepository.hasCancellableRetries("TXN-001");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void hasCancellableRetries_ShouldReturnTrue_WhenActiveRetriesExist() {
        // Given
        RetryAttempt pendingRetry = RetryAttempt.builder()
            .interfaceException(testException)
            .attemptNumber(2)
            .status(RetryStatus.PENDING)
            .initiatedAt(OffsetDateTime.now())
            .initiatedBy("test-user")
            .reason("Test retry")
            .build();
        
        retryAttemptRepository.saveAndFlush(pendingRetry);

        // When
        boolean result = optimizedRepository.hasCancellableRetries("TXN-001");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void getStatusByTransactionId_ShouldReturnStatus_WhenExists() {
        // When
        ExceptionStatus result = optimizedRepository.getStatusByTransactionId("TXN-001");

        // Then
        assertThat(result).isEqualTo(ExceptionStatus.NEW);
    }

    @Test
    void getStatusByTransactionId_ShouldReturnNull_WhenNotExists() {
        // When
        ExceptionStatus result = optimizedRepository.getStatusByTransactionId("TXN-999");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void existsByTransactionIdOptimized_ShouldReturnTrue_WhenExists() {
        // When
        boolean result = optimizedRepository.existsByTransactionIdOptimized("TXN-001");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void existsByTransactionIdOptimized_ShouldReturnFalse_WhenNotExists() {
        // When
        boolean result = optimizedRepository.existsByTransactionIdOptimized("TXN-999");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getBatchValidationInfo_ShouldReturnInfoForAllExistingIds() {
        // Given
        List<String> transactionIds = Arrays.asList("TXN-001", "TXN-002", "TXN-999");

        // When
        List<Object[]> result = optimizedRepository.getBatchValidationInfo(transactionIds);

        // Then
        assertThat(result).hasSize(2); // Only TXN-001 and TXN-002 exist
        
        // Verify first result (TXN-001)
        Object[] firstResult = result.stream()
            .filter(r -> "TXN-001".equals(r[0]))
            .findFirst()
            .orElse(null);
        assertThat(firstResult).isNotNull();
        assertThat(firstResult[1]).isEqualTo(ExceptionStatus.NEW);
        assertThat(firstResult[2]).isEqualTo(true);

        // Verify second result (TXN-002)
        Object[] secondResult = result.stream()
            .filter(r -> "TXN-002".equals(r[0]))
            .findFirst()
            .orElse(null);
        assertThat(secondResult).isNotNull();
        assertThat(secondResult[1]).isEqualTo(ExceptionStatus.NEW);
        assertThat(secondResult[2]).isEqualTo(false);
    }

    @Test
    void findAcknowledgeableExceptionByTransactionId_ShouldReturnException_WhenAcknowledgeable() {
        // Given
        List<ExceptionStatus> acknowledgeableStatuses = Arrays.asList(
            ExceptionStatus.NEW, ExceptionStatus.RETRIED_FAILED, ExceptionStatus.ESCALATED
        );

        // When
        Optional<InterfaceException> result = optimizedRepository.findAcknowledgeableExceptionByTransactionId(
            "TXN-001", acknowledgeableStatuses);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTransactionId()).isEqualTo("TXN-001");
    }

    @Test
    void findResolvableExceptionByTransactionId_ShouldReturnException_WhenResolvable() {
        // Given
        List<ExceptionStatus> resolvableStatuses = Arrays.asList(
            ExceptionStatus.NEW, ExceptionStatus.ACKNOWLEDGED, ExceptionStatus.RETRIED_FAILED, ExceptionStatus.ESCALATED
        );

        // When
        Optional<InterfaceException> result = optimizedRepository.findResolvableExceptionByTransactionId(
            "TXN-001", resolvableStatuses);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTransactionId()).isEqualTo("TXN-001");
    }

    @Test
    void findExceptionWithActiveRetries_ShouldReturnEmpty_WhenNoActiveRetries() {
        // When
        Optional<InterfaceException> result = optimizedRepository.findExceptionWithActiveRetries("TXN-001");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findExceptionWithActiveRetries_ShouldReturnException_WhenActiveRetriesExist() {
        // Given
        RetryAttempt pendingRetry = RetryAttempt.builder()
            .interfaceException(testException)
            .attemptNumber(2)
            .status(RetryStatus.PENDING)
            .initiatedAt(OffsetDateTime.now())
            .initiatedBy("test-user")
            .reason("Test retry")
            .build();
        
        retryAttemptRepository.saveAndFlush(pendingRetry);

        // When
        Optional<InterfaceException> result = optimizedRepository.findExceptionWithActiveRetries("TXN-001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTransactionId()).isEqualTo("TXN-001");
        assertThat(result.get().getRetryAttempts()).isNotEmpty();
    }

    @Test
    void findByTransactionIdForUpdate_ShouldReturnException_WhenExists() {
        // When
        Optional<InterfaceException> result = optimizedRepository.findByTransactionIdForUpdate("TXN-001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTransactionId()).isEqualTo("TXN-001");
    }
}