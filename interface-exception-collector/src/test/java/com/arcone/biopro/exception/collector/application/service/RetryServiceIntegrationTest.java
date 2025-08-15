package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.api.dto.RetryRequest;
import com.arcone.biopro.exception.collector.api.dto.RetryResponse;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.*;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Integration tests for RetryService.
 * Tests the complete retry workflow including database operations and event
 * publishing.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
class RetryServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("exception_collector_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private RetryService retryService;

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    @Autowired
    private RetryAttemptRepository retryAttemptRepository;

    @MockBean
    private PayloadRetrievalService payloadRetrievalService;

    @Autowired
    private ObjectMapper objectMapper;

    private InterfaceException testException;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        retryAttemptRepository.deleteAll();
        exceptionRepository.deleteAll();

        // Create test exception
        testException = InterfaceException.builder()
                .transactionId("test-transaction-123")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Order validation failed")
                .operation("CREATE_ORDER")
                .externalId("ORDER-123")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.MEDIUM)
                .category(ExceptionCategory.VALIDATION)
                .retryable(true)
                .customerId("CUST-001")
                .locationCode("LOC-001")
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .retryCount(0)
                .build();

        testException = exceptionRepository.save(testException);
    }

    @Test
    void shouldInitiateRetrySuccessfully() {
        // Given
        RetryRequest retryRequest = RetryRequest.builder()
                .reason("Manual retry after fixing validation issue")
                .priority("HIGH")
                .notifyOnCompletion(true)
                .initiatedBy("test-user")
                .build();

        // When
        RetryResponse response = retryService.initiateRetry(testException.getTransactionId(), retryRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRetryId()).isNotNull();
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getMessage()).isEqualTo("Retry operation initiated successfully");
        assertThat(response.getAttemptNumber()).isEqualTo(1);
        assertThat(response.getEstimatedCompletionTime()).isAfter(OffsetDateTime.now());

        // Verify database updates
        InterfaceException updatedException = exceptionRepository.findByTransactionId(testException.getTransactionId())
                .orElseThrow();
        assertThat(updatedException.getRetryCount()).isEqualTo(1);
        assertThat(updatedException.getLastRetryAt()).isNotNull();

        // Verify retry attempt creation
        List<RetryAttempt> retryAttempts = retryAttemptRepository
                .findByInterfaceExceptionOrderByAttemptNumberAsc(testException);
        assertThat(retryAttempts).hasSize(1);

        RetryAttempt retryAttempt = retryAttempts.get(0);
        assertThat(retryAttempt.getAttemptNumber()).isEqualTo(1);
        assertThat(retryAttempt.getStatus()).isEqualTo(RetryStatus.PENDING);
        assertThat(retryAttempt.getInitiatedBy()).isEqualTo("test-user");
        assertThat(retryAttempt.getInitiatedAt()).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenRetryingNonExistentTransaction() {
        // Given
        String nonExistentTransactionId = "non-existent-transaction";
        RetryRequest retryRequest = RetryRequest.builder()
                .reason("Test retry")
                .initiatedBy("test-user")
                .build();

        // When & Then
        assertThatThrownBy(() -> retryService.initiateRetry(nonExistentTransactionId, retryRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Exception not found for transaction: " + nonExistentTransactionId);
    }

    @Test
    void shouldThrowExceptionWhenRetryingNonRetryableException() {
        // Given
        testException.setRetryable(false);
        exceptionRepository.save(testException);

        RetryRequest retryRequest = RetryRequest.builder()
                .reason("Test retry")
                .initiatedBy("test-user")
                .build();

        // When & Then
        assertThatThrownBy(() -> retryService.initiateRetry(testException.getTransactionId(), retryRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "Exception is not retryable for transaction: " + testException.getTransactionId());
    }

    @Test
    void shouldGetRetryHistorySuccessfully() {
        // Given - Create multiple retry attempts
        createRetryAttempt(1, RetryStatus.FAILED, "First attempt failed");
        createRetryAttempt(2, RetryStatus.SUCCESS, "Second attempt succeeded");

        // When
        List<RetryAttempt> retryHistory = retryService.getRetryHistory(testException.getTransactionId());

        // Then
        assertThat(retryHistory).hasSize(2);
        assertThat(retryHistory.get(0).getAttemptNumber()).isEqualTo(1);
        assertThat(retryHistory.get(0).getStatus()).isEqualTo(RetryStatus.FAILED);
        assertThat(retryHistory.get(1).getAttemptNumber()).isEqualTo(2);
        assertThat(retryHistory.get(1).getStatus()).isEqualTo(RetryStatus.SUCCESS);
    }

    @Test
    void shouldGetLatestRetryAttemptSuccessfully() {
        // Given - Create multiple retry attempts
        createRetryAttempt(1, RetryStatus.FAILED, "First attempt failed");
        RetryAttempt latestAttempt = createRetryAttempt(2, RetryStatus.PENDING, "Second attempt pending");

        // When
        Optional<RetryAttempt> result = retryService.getLatestRetryAttempt(testException.getTransactionId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getAttemptNumber()).isEqualTo(2);
        assertThat(result.get().getStatus()).isEqualTo(RetryStatus.PENDING);
        assertThat(result.get().getId()).isEqualTo(latestAttempt.getId());
    }

    @Test
    void shouldGetRetryStatisticsSuccessfully() {
        // Given - Create retry attempts with different statuses
        createRetryAttempt(1, RetryStatus.FAILED, "First attempt failed");
        createRetryAttempt(2, RetryStatus.SUCCESS, "Second attempt succeeded");
        createRetryAttempt(3, RetryStatus.PENDING, "Third attempt pending");

        // When
        Object[] stats = retryService.getRetryStatistics(testException.getTransactionId());

        // Then
        assertThat(stats).hasSize(4);
        assertThat(((Number) stats[0]).longValue()).isEqualTo(3L); // totalAttempts
        assertThat(((Number) stats[1]).longValue()).isEqualTo(1L); // successfulAttempts
        assertThat(((Number) stats[2]).longValue()).isEqualTo(1L); // failedAttempts
        assertThat(((Number) stats[3]).longValue()).isEqualTo(1L); // pendingAttempts
    }

    @Test
    void shouldValidateCanRetryCorrectly() {
        // Test retryable exception
        assertThat(retryService.canRetry(testException.getTransactionId())).isTrue();

        // Test non-retryable exception
        testException.setRetryable(false);
        exceptionRepository.save(testException);
        assertThat(retryService.canRetry(testException.getTransactionId())).isFalse();

        // Reset to retryable
        testException.setRetryable(true);
        exceptionRepository.save(testException);

        // Test resolved exception
        testException.setStatus(ExceptionStatus.RESOLVED);
        exceptionRepository.save(testException);
        assertThat(retryService.canRetry(testException.getTransactionId())).isFalse();

        // Reset status
        testException.setStatus(ExceptionStatus.NEW);
        exceptionRepository.save(testException);

        // Test with pending retry
        createRetryAttempt(1, RetryStatus.PENDING, "Pending retry");
        assertThat(retryService.canRetry(testException.getTransactionId())).isFalse();

        // Test non-existent transaction
        assertThat(retryService.canRetry("non-existent")).isFalse();
    }

    @Test
    void shouldCancelRetrySuccessfully() {
        // Given - Create a pending retry attempt
        RetryAttempt pendingAttempt = createRetryAttempt(1, RetryStatus.PENDING, "Pending retry");

        // When
        boolean cancelled = retryService.cancelRetry(testException.getTransactionId(), 1);

        // Then
        assertThat(cancelled).isTrue();

        // Verify the retry attempt is marked as failed
        RetryAttempt updatedAttempt = retryAttemptRepository.findById(pendingAttempt.getId()).orElseThrow();
        assertThat(updatedAttempt.getStatus()).isEqualTo(RetryStatus.FAILED);
        assertThat(updatedAttempt.getResultMessage()).isEqualTo("Retry cancelled by user");
        assertThat(updatedAttempt.getResultErrorDetails()).isEqualTo("User cancelled retry operation");
    }

    @Test
    void shouldNotCancelNonPendingRetry() {
        // Given - Create a completed retry attempt
        createRetryAttempt(1, RetryStatus.SUCCESS, "Completed retry");

        // When
        boolean cancelled = retryService.cancelRetry(testException.getTransactionId(), 1);

        // Then
        assertThat(cancelled).isFalse();
    }

    @Test
    void shouldHandleRetrySuccessCorrectly() {
        // Given
        RetryAttempt retryAttempt = createRetryAttempt(1, RetryStatus.PENDING, "Pending retry");

        // When
        retryService.handleRetrySuccess(testException, retryAttempt, "Retry completed successfully", 200);

        // Then
        // Verify retry attempt is updated
        RetryAttempt updatedAttempt = retryAttemptRepository.findById(retryAttempt.getId()).orElseThrow();
        assertThat(updatedAttempt.getStatus()).isEqualTo(RetryStatus.SUCCESS);
        assertThat(updatedAttempt.getResultSuccess()).isTrue();
        assertThat(updatedAttempt.getResultMessage()).isEqualTo("Retry completed successfully");
        assertThat(updatedAttempt.getResultResponseCode()).isEqualTo(200);
        assertThat(updatedAttempt.getCompletedAt()).isNotNull();

        // Verify exception status is updated
        InterfaceException updatedException = exceptionRepository.findById(testException.getId()).orElseThrow();
        assertThat(updatedException.getStatus()).isEqualTo(ExceptionStatus.RETRIED_SUCCESS);
        assertThat(updatedException.getResolvedAt()).isNotNull();
    }

    @Test
    void shouldHandleRetryFailureCorrectly() {
        // Given
        RetryAttempt retryAttempt = createRetryAttempt(1, RetryStatus.PENDING, "Pending retry");

        // When
        retryService.handleRetryFailure(testException, retryAttempt, "Retry failed", 500, "Internal server error");

        // Then
        // Verify retry attempt is updated
        RetryAttempt updatedAttempt = retryAttemptRepository.findById(retryAttempt.getId()).orElseThrow();
        assertThat(updatedAttempt.getStatus()).isEqualTo(RetryStatus.FAILED);
        assertThat(updatedAttempt.getResultSuccess()).isFalse();
        assertThat(updatedAttempt.getResultMessage()).isEqualTo("Retry failed");
        assertThat(updatedAttempt.getResultResponseCode()).isEqualTo(500);
        assertThat(updatedAttempt.getResultErrorDetails()).isEqualTo("Internal server error");
        assertThat(updatedAttempt.getCompletedAt()).isNotNull();

        // Verify exception status is updated
        InterfaceException updatedException = exceptionRepository.findById(testException.getId()).orElseThrow();
        assertThat(updatedException.getStatus()).isEqualTo(ExceptionStatus.RETRIED_FAILED);
    }

    private RetryAttempt createRetryAttempt(Integer attemptNumber, RetryStatus status, String message) {
        RetryAttempt retryAttempt = RetryAttempt.builder()
                .interfaceException(testException)
                .attemptNumber(attemptNumber)
                .status(status)
                .initiatedBy("test-user")
                .initiatedAt(OffsetDateTime.now())
                .build();

        if (status != RetryStatus.PENDING) {
            retryAttempt.setCompletedAt(OffsetDateTime.now());
            retryAttempt.setResultMessage(message);
            retryAttempt.setResultSuccess(status == RetryStatus.SUCCESS);
        }

        return retryAttemptRepository.save(retryAttempt);
    }
}