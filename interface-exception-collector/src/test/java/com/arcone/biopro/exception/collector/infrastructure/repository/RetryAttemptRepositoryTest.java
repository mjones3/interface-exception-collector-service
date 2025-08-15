package com.arcone.biopro.exception.collector.infrastructure.repository;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RetryAttemptRepository using TestContainers for PostgreSQL
 * integration.
 * Tests all custom query methods, pagination, sorting, and retry history
 * management.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.kafka.enabled=false"
})
class RetryAttemptRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test_exception_collector")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private RetryAttemptRepository retryAttemptRepository;

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    private InterfaceException exception1;
    private InterfaceException exception2;
    private RetryAttempt attempt1;
    private RetryAttempt attempt2;
    private RetryAttempt attempt3;
    private OffsetDateTime baseTime;

    @BeforeEach
    void setUp() {
        retryAttemptRepository.deleteAll();
        exceptionRepository.deleteAll();
        baseTime = OffsetDateTime.now().minusDays(1);

        // Create test exceptions
        exception1 = InterfaceException.builder()
                .transactionId("TXN-001")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Order validation failed")
                .operation("CREATE_ORDER")
                .externalId("ORDER-001")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.HIGH)
                .category(ExceptionCategory.VALIDATION)
                .retryable(true)
                .customerId("CUST-001")
                .timestamp(baseTime)
                .processedAt(baseTime.plusMinutes(1))
                .retryCount(2)
                .build();

        exception2 = InterfaceException.builder()
                .transactionId("TXN-002")
                .interfaceType(InterfaceType.COLLECTION)
                .exceptionReason("Collection processing error")
                .operation("CREATE_COLLECTION")
                .externalId("COLLECTION-001")
                .status(ExceptionStatus.ACKNOWLEDGED)
                .severity(ExceptionSeverity.MEDIUM)
                .category(ExceptionCategory.BUSINESS_RULE)
                .retryable(true)
                .customerId("CUST-002")
                .timestamp(baseTime.plusHours(1))
                .processedAt(baseTime.plusHours(1).plusMinutes(1))
                .retryCount(1)
                .build();

        exceptionRepository.saveAll(List.of(exception1, exception2));

        // Create test retry attempts
        attempt1 = RetryAttempt.builder()
                .interfaceException(exception1)
                .attemptNumber(1)
                .status(RetryStatus.SUCCESS)
                .initiatedBy("user1")
                .initiatedAt(baseTime.plusMinutes(10))
                .completedAt(baseTime.plusMinutes(11))
                .resultSuccess(true)
                .resultMessage("Retry successful")
                .resultResponseCode(200)
                .build();

        attempt2 = RetryAttempt.builder()
                .interfaceException(exception1)
                .attemptNumber(2)
                .status(RetryStatus.FAILED)
                .initiatedBy("user2")
                .initiatedAt(baseTime.plusMinutes(20))
                .completedAt(baseTime.plusMinutes(21))
                .resultSuccess(false)
                .resultMessage("Retry failed")
                .resultResponseCode(500)
                .resultErrorDetails("{\"error\": \"Internal server error\"}")
                .build();

        attempt3 = RetryAttempt.builder()
                .interfaceException(exception2)
                .attemptNumber(1)
                .status(RetryStatus.PENDING)
                .initiatedBy("user1")
                .initiatedAt(baseTime.plusMinutes(30))
                .build();

        retryAttemptRepository.saveAll(List.of(attempt1, attempt2, attempt3));
    }

    @Test
    void findByInterfaceExceptionOrderByAttemptNumberAsc_ShouldReturnOrderedAttempts() {
        // When
        List<RetryAttempt> result = retryAttemptRepository
                .findByInterfaceExceptionOrderByAttemptNumberAsc(exception1);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAttemptNumber()).isEqualTo(1);
        assertThat(result.get(1).getAttemptNumber()).isEqualTo(2);
    }

    @Test
    void findByInterfaceException_ShouldReturnPagedAttempts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<RetryAttempt> result = retryAttemptRepository.findByInterfaceException(exception1, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findByStatus_ShouldReturnFilteredAttempts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<RetryAttempt> result = retryAttemptRepository.findByStatus(RetryStatus.SUCCESS, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(RetryStatus.SUCCESS);
    }

    @Test
    void findByInitiatedBy_ShouldReturnFilteredAttempts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<RetryAttempt> result = retryAttemptRepository.findByInitiatedBy("user1", pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(attempt -> "user1".equals(attempt.getInitiatedBy()));
    }

    @Test
    void findByInitiatedAtBetween_ShouldReturnFilteredAttempts() {
        // Given
        OffsetDateTime fromDate = baseTime.plusMinutes(5);
        OffsetDateTime toDate = baseTime.plusMinutes(25);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<RetryAttempt> result = retryAttemptRepository
                .findByInitiatedAtBetween(fromDate, toDate, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findTopByInterfaceExceptionOrderByAttemptNumberDesc_ShouldReturnLatestAttempt() {
        // When
        Optional<RetryAttempt> result = retryAttemptRepository
                .findTopByInterfaceExceptionOrderByAttemptNumberDesc(exception1);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getAttemptNumber()).isEqualTo(2);
        assertThat(result.get().getStatus()).isEqualTo(RetryStatus.FAILED);
    }

    @Test
    void findByInterfaceExceptionAndAttemptNumber_ShouldReturnSpecificAttempt() {
        // When
        Optional<RetryAttempt> result = retryAttemptRepository
                .findByInterfaceExceptionAndAttemptNumber(exception1, 1);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getAttemptNumber()).isEqualTo(1);
        assertThat(result.get().getStatus()).isEqualTo(RetryStatus.SUCCESS);
    }

    @Test
    void countByInterfaceException_ShouldReturnCorrectCount() {
        // When
        long count = retryAttemptRepository.countByInterfaceException(exception1);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByInterfaceExceptionAndStatus_ShouldReturnCorrectCount() {
        // When
        long count = retryAttemptRepository
                .countByInterfaceExceptionAndStatus(exception1, RetryStatus.SUCCESS);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void countByStatus_ShouldReturnCorrectCount() {
        // When
        long count = retryAttemptRepository.countByStatus(RetryStatus.PENDING);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void findByStatusOrderByInitiatedAtAsc_ShouldReturnOrderedPendingAttempts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<RetryAttempt> result = retryAttemptRepository
                .findByStatusOrderByInitiatedAtAsc(RetryStatus.PENDING, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(RetryStatus.PENDING);
    }

    @Test
    void findStaleRetryAttempts_ShouldReturnStaleAttempts() {
        // Given
        OffsetDateTime cutoffTime = baseTime.plusMinutes(35); // After all attempts
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<RetryAttempt> result = retryAttemptRepository
                .findStaleRetryAttempts(RetryStatus.PENDING, cutoffTime, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(RetryStatus.PENDING);
    }

    @Test
    void getRetryStatistics_ShouldReturnCorrectStatistics() {
        // When
        Object[] result = retryAttemptRepository.getRetryStatistics(exception1);

        // Then
        assertThat(result).hasSize(4);
        assertThat(result[0]).isEqualTo(2L); // total attempts
        assertThat(result[1]).isEqualTo(1L); // successful attempts
        assertThat(result[2]).isEqualTo(1L); // failed attempts
        assertThat(result[3]).isEqualTo(0L); // pending attempts
    }

    @Test
    void findByResultSuccess_ShouldReturnFilteredAttempts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<RetryAttempt> result = retryAttemptRepository.findByResultSuccess(true, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getResultSuccess()).isTrue();
    }

    @Test
    void findByCompletedAtBetween_ShouldReturnFilteredAttempts() {
        // Given
        OffsetDateTime fromDate = baseTime.plusMinutes(10);
        OffsetDateTime toDate = baseTime.plusMinutes(22);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<RetryAttempt> result = retryAttemptRepository
                .findByCompletedAtBetween(fromDate, toDate, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(attempt -> attempt.getCompletedAt() != null);
    }

    @Test
    void getDailyRetryCounts_ShouldReturnDailyStatistics() {
        // Given
        OffsetDateTime fromDate = baseTime.minusDays(1);
        OffsetDateTime toDate = baseTime.plusDays(1);

        // When
        List<Object[]> result = retryAttemptRepository.getDailyRetryCounts(fromDate, toDate);

        // Then
        assertThat(result).isNotEmpty();
        // Note: Exact assertions depend on the date grouping behavior
    }

    @Test
    void findByErrorDetailsContaining_ShouldReturnMatchingAttempts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<RetryAttempt> result = retryAttemptRepository
                .findByErrorDetailsContaining("server error", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getResultErrorDetails())
                .containsIgnoringCase("server error");
    }

    @Test
    void getNextAttemptNumber_ShouldReturnCorrectNextNumber() {
        // When
        Integer nextAttemptNumber = retryAttemptRepository.getNextAttemptNumber(exception1);

        // Then
        assertThat(nextAttemptNumber).isEqualTo(3);
    }

    @Test
    void getNextAttemptNumber_ShouldReturnOne_WhenNoAttemptsExist() {
        // Given
        InterfaceException newException = InterfaceException.builder()
                .transactionId("TXN-NEW")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("New exception")
                .operation("CREATE_ORDER")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.LOW)
                .category(ExceptionCategory.VALIDATION)
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .build();
        exceptionRepository.save(newException);

        // When
        Integer nextAttemptNumber = retryAttemptRepository.getNextAttemptNumber(newException);

        // Then
        assertThat(nextAttemptNumber).isEqualTo(1);
    }

    @Test
    void deleteByInterfaceException_ShouldDeleteAllAttempts() {
        // Given
        long initialCount = retryAttemptRepository.countByInterfaceException(exception1);
        assertThat(initialCount).isEqualTo(2);

        // When
        retryAttemptRepository.deleteByInterfaceException(exception1);

        // Then
        long finalCount = retryAttemptRepository.countByInterfaceException(exception1);
        assertThat(finalCount).isEqualTo(0);
    }

    @Test
    void findByResultResponseCode_ShouldReturnFilteredAttempts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<RetryAttempt> result = retryAttemptRepository.findByResultResponseCode(200, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getResultResponseCode()).isEqualTo(200);
    }

    @Test
    void existsByInterfaceExceptionAndStatus_ShouldReturnTrue_WhenExists() {
        // When
        boolean exists = retryAttemptRepository
                .existsByInterfaceExceptionAndStatus(exception1, RetryStatus.SUCCESS);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByInterfaceExceptionAndStatus_ShouldReturnFalse_WhenNotExists() {
        // When
        boolean exists = retryAttemptRepository
                .existsByInterfaceExceptionAndStatus(exception2, RetryStatus.SUCCESS);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void retryAttemptConvenienceMethods_ShouldWorkCorrectly() {
        // Given
        RetryAttempt newAttempt = RetryAttempt.builder()
                .interfaceException(exception2)
                .attemptNumber(2)
                .status(RetryStatus.PENDING)
                .initiatedBy("user3")
                .initiatedAt(OffsetDateTime.now())
                .build();

        // When - mark as success
        newAttempt.markAsSuccess("Operation completed successfully", 201);

        // Then
        assertThat(newAttempt.getStatus()).isEqualTo(RetryStatus.SUCCESS);
        assertThat(newAttempt.getResultSuccess()).isTrue();
        assertThat(newAttempt.getResultMessage()).isEqualTo("Operation completed successfully");
        assertThat(newAttempt.getResultResponseCode()).isEqualTo(201);
        assertThat(newAttempt.getCompletedAt()).isNotNull();

        // When - mark as failed
        newAttempt.markAsFailed("Operation failed", 400, "{\"error\": \"Bad request\"}");

        // Then
        assertThat(newAttempt.getStatus()).isEqualTo(RetryStatus.FAILED);
        assertThat(newAttempt.getResultSuccess()).isFalse();
        assertThat(newAttempt.getResultMessage()).isEqualTo("Operation failed");
        assertThat(newAttempt.getResultResponseCode()).isEqualTo(400);
        assertThat(newAttempt.getResultErrorDetails()).isEqualTo("{\"error\": \"Bad request\"}");
    }
}