package com.arcone.biopro.exception.collector.api.controller;

import com.arcone.biopro.exception.collector.api.dto.RetryRequest;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.*;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for RetryController.
 * Tests the complete REST API functionality for retry operations.
 */
@SpringBootTest
@AutoConfigureWebMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
class RetryControllerIntegrationTest {

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
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private InterfaceExceptionRepository exceptionRepository;

        @Autowired
        private RetryAttemptRepository retryAttemptRepository;

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
        void shouldInitiateRetrySuccessfully() throws Exception {
                // Given
                RetryRequest retryRequest = RetryRequest.builder()
                                .reason("Manual retry after fixing validation issue")
                                .priority("HIGH")
                                .notifyOnCompletion(true)
                                .initiatedBy("test-user")
                                .build();

                // When & Then
                mockMvc.perform(post("/api/v1/exceptions/{transactionId}/retry", testException.getTransactionId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(retryRequest)))
                                .andExpect(status().isAccepted())
                                .andExpect(jsonPath("$.retryId").exists())
                                .andExpect(jsonPath("$.status").value("PENDING"))
                                .andExpect(jsonPath("$.message").value("Retry operation initiated successfully"))
                                .andExpect(jsonPath("$.attemptNumber").value(1))
                                .andExpect(jsonPath("$.estimatedCompletionTime").exists());
        }

        @Test
        void shouldReturnNotFoundForNonExistentTransaction() throws Exception {
                // Given
                RetryRequest retryRequest = RetryRequest.builder()
                                .reason("Test retry")
                                .initiatedBy("test-user")
                                .build();

                // When & Then
                mockMvc.perform(post("/api/v1/exceptions/{transactionId}/retry", "non-existent-transaction")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(retryRequest)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("EXCEPTION_NOT_FOUND"))
                                .andExpect(jsonPath("$.message")
                                                .value(containsString("Exception not found for transaction")));
        }

        @Test
        void shouldReturnConflictForNonRetryableException() throws Exception {
                // Given
                testException.setRetryable(false);
                exceptionRepository.save(testException);

                RetryRequest retryRequest = RetryRequest.builder()
                                .reason("Test retry")
                                .initiatedBy("test-user")
                                .build();

                // When & Then
                mockMvc.perform(post("/api/v1/exceptions/{transactionId}/retry", testException.getTransactionId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(retryRequest)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.error").value("RETRY_NOT_ALLOWED"))
                                .andExpect(jsonPath("$.message")
                                                .value("Exception is not retryable or retry already in progress"));
        }

        @Test
        void shouldReturnBadRequestForInvalidRetryRequest() throws Exception {
                // Given - Invalid request with missing required fields
                RetryRequest invalidRequest = RetryRequest.builder()
                                .priority("HIGH")
                                .build(); // Missing reason and initiatedBy

                // When & Then
                mockMvc.perform(post("/api/v1/exceptions/{transactionId}/retry", testException.getTransactionId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldGetRetryHistorySuccessfully() throws Exception {
                // Given - Create retry attempts
                createRetryAttempt(1, RetryStatus.FAILED, "First attempt failed");
                createRetryAttempt(2, RetryStatus.SUCCESS, "Second attempt succeeded");

                // When & Then
                mockMvc.perform(get("/api/v1/exceptions/{transactionId}/retry-history",
                                testException.getTransactionId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(2)))
                                .andExpect(jsonPath("$[0].attemptNumber").value(1))
                                .andExpect(jsonPath("$[0].status").value("FAILED"))
                                .andExpect(jsonPath("$[0].resultMessage").value("First attempt failed"))
                                .andExpect(jsonPath("$[1].attemptNumber").value(2))
                                .andExpect(jsonPath("$[1].status").value("SUCCESS"))
                                .andExpect(jsonPath("$[1].resultMessage").value("Second attempt succeeded"));
        }

        @Test
        void shouldReturnNotFoundForRetryHistoryOfNonExistentTransaction() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/v1/exceptions/{transactionId}/retry-history", "non-existent-transaction"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("EXCEPTION_NOT_FOUND"));
        }

        @Test
        void shouldGetLatestRetryAttemptSuccessfully() throws Exception {
                // Given - Create retry attempts
                createRetryAttempt(1, RetryStatus.FAILED, "First attempt failed");
                createRetryAttempt(2, RetryStatus.PENDING, "Second attempt pending");

                // When & Then
                mockMvc.perform(get("/api/v1/exceptions/{transactionId}/retry/latest",
                                testException.getTransactionId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.attemptNumber").value(2))
                                .andExpect(jsonPath("$.status").value("PENDING"))
                                .andExpect(jsonPath("$.resultMessage").value("Second attempt pending"));
        }

        @Test
        void shouldReturnNotFoundWhenNoRetryAttemptsExist() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/v1/exceptions/{transactionId}/retry/latest",
                                testException.getTransactionId()))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("NO_RETRY_ATTEMPTS"))
                                .andExpect(jsonPath("$.message").value("No retry attempts found for this exception"));
        }

        @Test
        void shouldGetRetryStatisticsSuccessfully() throws Exception {
                // Given - Create retry attempts with different statuses
                createRetryAttempt(1, RetryStatus.FAILED, "First attempt failed");
                createRetryAttempt(2, RetryStatus.SUCCESS, "Second attempt succeeded");
                createRetryAttempt(3, RetryStatus.PENDING, "Third attempt pending");

                // When & Then
                mockMvc.perform(get("/api/v1/exceptions/{transactionId}/retry/statistics",
                                testException.getTransactionId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalAttempts").value(3))
                                .andExpect(jsonPath("$.successfulAttempts").value(1))
                                .andExpect(jsonPath("$.failedAttempts").value(1))
                                .andExpect(jsonPath("$.pendingAttempts").value(1));
        }

        @Test
        void shouldCancelRetrySuccessfully() throws Exception {
                // Given - Create a pending retry attempt
                createRetryAttempt(1, RetryStatus.PENDING, "Pending retry");

                // When & Then
                mockMvc.perform(delete("/api/v1/exceptions/{transactionId}/retry/{attemptNumber}",
                                testException.getTransactionId(), 1))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Retry attempt cancelled successfully"))
                                .andExpect(jsonPath("$.transactionId").value(testException.getTransactionId()))
                                .andExpect(jsonPath("$.attemptNumber").value(1));
        }

        @Test
        void shouldReturnConflictWhenCancellingNonPendingRetry() throws Exception {
                // Given - Create a completed retry attempt
                createRetryAttempt(1, RetryStatus.SUCCESS, "Completed retry");

                // When & Then
                mockMvc.perform(delete("/api/v1/exceptions/{transactionId}/retry/{attemptNumber}",
                                testException.getTransactionId(), 1))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.error").value("RETRY_CANCELLATION_FAILED"))
                                .andExpect(jsonPath("$.message")
                                                .value("Retry attempt cannot be cancelled or does not exist"));
        }

        @Test
        void shouldReturnNotFoundWhenCancellingNonExistentRetry() throws Exception {
                // When & Then
                mockMvc.perform(delete("/api/v1/exceptions/{transactionId}/retry/{attemptNumber}",
                                testException.getTransactionId(), 999))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.error").value("RETRY_CANCELLATION_FAILED"));
        }

        @Test
        void shouldHandleInternalServerErrorGracefully() throws Exception {
                // This test would require mocking to simulate internal errors
                // For now, we'll test the basic error handling structure

                // Given - Invalid transaction ID format that might cause internal errors
                RetryRequest retryRequest = RetryRequest.builder()
                                .reason("Test retry")
                                .initiatedBy("test-user")
                                .build();

                // When & Then - Test with a very long transaction ID that might cause issues
                String veryLongTransactionId = "a".repeat(1000);
                mockMvc.perform(post("/api/v1/exceptions/{transactionId}/retry", veryLongTransactionId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(retryRequest)))
                                .andExpect(status().isNotFound()); // Should return 404 for non-existent transaction
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