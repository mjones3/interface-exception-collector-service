package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.graphql.dto.CancelRetryResult;
import com.arcone.biopro.exception.collector.api.graphql.validation.MutationErrorCode;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for enhanced cancel retry GraphQL mutation.
 * Tests the complete flow from GraphQL input to database persistence.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Cancel Retry Mutation Integration Tests")
class CancelRetryMutationIntegrationTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    @Autowired
    private RetryAttemptRepository retryAttemptRepository;

    private InterfaceException testException;
    private RetryAttempt pendingRetryAttempt;

    private static final String CANCEL_RETRY_MUTATION = """
            mutation CancelRetry($transactionId: String!, $reason: String!) {
                cancelRetry(transactionId: $transactionId, reason: $reason) {
                    success
                    operationId
                    timestamp
                    performedBy
                    cancellationReason
                    cancelledAttemptNumber
                    exception {
                        transactionId
                        status
                    }
                    cancelledRetryAttempt {
                        attemptNumber
                        status
                    }
                    errors {
                        message
                        code
                        category
                    }
                }
            }
            """;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        retryAttemptRepository.deleteAll();
        exceptionRepository.deleteAll();

        // Create test exception
        testException = new InterfaceException();
        testException.setTransactionId("TEST-TXN-001");
        testException.setExternalId("EXT-001");
        testException.setInterfaceType("ORDER_PROCESSING");
        testException.setOperation("CREATE_ORDER");
        testException.setStatus(ExceptionStatus.FAILED);
        testException.setExceptionReason("Network timeout");
        testException.setSeverity("HIGH");
        testException.setCategory("TECHNICAL");
        testException.setCustomerId("CUST-001");
        testException.setLocationCode("LOC-001");
        testException.setTimestamp(OffsetDateTime.now().minusHours(1));
        testException.setRetryable(true);
        testException.setRetryCount(1);
        testException.setMaxRetries(3);
        testException.setLastRetryAt(OffsetDateTime.now().minusMinutes(5));

        testException = exceptionRepository.save(testException);

        // Create pending retry attempt
        pendingRetryAttempt = new RetryAttempt();
        pendingRetryAttempt.setInterfaceException(testException);
        pendingRetryAttempt.setAttemptNumber(1);
        pendingRetryAttempt.setStatus(RetryStatus.PENDING);
        pendingRetryAttempt.setInitiatedBy("testuser");
        pendingRetryAttempt.setInitiatedAt(OffsetDateTime.now().minusMinutes(5));

        pendingRetryAttempt = retryAttemptRepository.save(pendingRetryAttempt);
    }

    @Test
    @WithMockUser(roles = "OPERATIONS")
    @DisplayName("Should successfully cancel pending retry with enhanced details")
    void shouldSuccessfullyCancelPendingRetry() {
        // Given
        String transactionId = testException.getTransactionId();
        String reason = "User requested cancellation due to system maintenance";

        // When
        CancelRetryResult result = graphQlTester.document(CANCEL_RETRY_MUTATION)
                .variable("transactionId", transactionId)
                .variable("reason", reason)
                .execute()
                .path("cancelRetry")
                .entity(CancelRetryResult.class)
                .get();

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getOperationId()).isNotNull();
        assertThat(result.getOperationId()).startsWith("CANCEL-");
        assertThat(result.getTimestamp()).isNotNull();
        assertThat(result.getPerformedBy()).isEqualTo("user");
        assertThat(result.getCancellationReason()).isEqualTo(reason);
        assertThat(result.getCancelledAttemptNumber()).isEqualTo(1);
        assertThat(result.getErrors()).isEmpty();

        // Verify exception data
        assertThat(result.getException()).isNotNull();
        assertThat(result.getException().getTransactionId()).isEqualTo(transactionId);

        // Verify cancelled retry attempt data
        assertThat(result.getCancelledRetryAttempt()).isNotNull();
        assertThat(result.getCancelledRetryAttempt().getAttemptNumber()).isEqualTo(1);

        // Verify database state
        RetryAttempt updatedAttempt = retryAttemptRepository.findById(pendingRetryAttempt.getId()).orElseThrow();
        assertThat(updatedAttempt.getStatus()).isEqualTo(RetryStatus.CANCELLED);
        assertThat(updatedAttempt.getResultMessage()).contains("Retry cancelled by user");
        assertThat(updatedAttempt.getResultMessage()).contains(reason);
    }

    @Test
    @WithMockUser(roles = "OPERATIONS")
    @DisplayName("Should fail to cancel retry when transaction ID not found")
    void shouldFailToCancelRetryWhenTransactionNotFound() {
        // Given
        String nonExistentTransactionId = "NONEXISTENT-TXN";
        String reason = "Valid reason";

        // When & Then
        graphQlTester.document(CANCEL_RETRY_MUTATION)
                .variable("transactionId", nonExistentTransactionId)
                .variable("reason", reason)
                .execute()
                .path("cancelRetry.success")
                .entity(Boolean.class)
                .isEqualTo(false)
                .path("cancelRetry.errors[0].code")
                .entity(String.class)
                .isEqualTo(MutationErrorCode.EXCEPTION_NOT_FOUND.getCode())
                .path("cancelRetry.errors[0].message")
                .entity(String.class)
                .contains("Exception not found");
    }

    @Test
    @WithMockUser(roles = "OPERATIONS")
    @DisplayName("Should fail to cancel retry when no pending retry exists")
    void shouldFailToCancelRetryWhenNoPendingRetry() {
        // Given - Update retry attempt to completed status
        pendingRetryAttempt.setStatus(RetryStatus.SUCCESS);
        pendingRetryAttempt.setCompletedAt(OffsetDateTime.now());
        retryAttemptRepository.save(pendingRetryAttempt);

        String transactionId = testException.getTransactionId();
        String reason = "Valid reason";

        // When & Then
        graphQlTester.document(CANCEL_RETRY_MUTATION)
                .variable("transactionId", transactionId)
                .variable("reason", reason)
                .execute()
                .path("cancelRetry.success")
                .entity(Boolean.class)
                .isEqualTo(false)
                .path("cancelRetry.errors[0].code")
                .entity(String.class)
                .isEqualTo(MutationErrorCode.RETRY_ALREADY_COMPLETED.getCode())
                .path("cancelRetry.errors[0].message")
                .entity(String.class)
                .contains("already completed successfully");
    }

    @Test
    @WithMockUser(roles = "OPERATIONS")
    @DisplayName("Should fail to cancel retry when exception is resolved")
    void shouldFailToCancelRetryWhenExceptionResolved() {
        // Given - Update exception to resolved status
        testException.setStatus(ExceptionStatus.RESOLVED);
        testException.setResolvedAt(OffsetDateTime.now());
        exceptionRepository.save(testException);

        String transactionId = testException.getTransactionId();
        String reason = "Valid reason";

        // When & Then
        graphQlTester.document(CANCEL_RETRY_MUTATION)
                .variable("transactionId", transactionId)
                .variable("reason", reason)
                .execute()
                .path("cancelRetry.success")
                .entity(Boolean.class)
                .isEqualTo(false)
                .path("cancelRetry.errors[0].code")
                .entity(String.class)
                .isEqualTo(MutationErrorCode.CANCELLATION_NOT_ALLOWED.getCode())
                .path("cancelRetry.errors[0].message")
                .entity(String.class)
                .contains("resolved or closed exception");
    }

    @Test
    @WithMockUser(roles = "OPERATIONS")
    @DisplayName("Should fail validation for invalid transaction ID format")
    void shouldFailValidationForInvalidTransactionId() {
        // Given
        String invalidTransactionId = ""; // Empty transaction ID
        String reason = "Valid reason";

        // When & Then
        graphQlTester.document(CANCEL_RETRY_MUTATION)
                .variable("transactionId", invalidTransactionId)
                .variable("reason", reason)
                .execute()
                .path("cancelRetry.success")
                .entity(Boolean.class)
                .isEqualTo(false)
                .path("cancelRetry.errors[0].code")
                .entity(String.class)
                .isEqualTo(MutationErrorCode.INVALID_TRANSACTION_ID.getCode());
    }

    @Test
    @WithMockUser(roles = "OPERATIONS")
    @DisplayName("Should fail validation for missing cancellation reason")
    void shouldFailValidationForMissingReason() {
        // Given
        String transactionId = testException.getTransactionId();
        String emptyReason = "";

        // When & Then
        graphQlTester.document(CANCEL_RETRY_MUTATION)
                .variable("transactionId", transactionId)
                .variable("reason", emptyReason)
                .execute()
                .path("cancelRetry.success")
                .entity(Boolean.class)
                .isEqualTo(false)
                .path("cancelRetry.errors[0].code")
                .entity(String.class)
                .isEqualTo(MutationErrorCode.MISSING_REQUIRED_FIELD.getCode())
                .path("cancelRetry.errors[0].message")
                .entity(String.class)
                .contains("reason is required");
    }

    @Test
    @WithMockUser(roles = "OPERATIONS")
    @DisplayName("Should fail validation for reason exceeding maximum length")
    void shouldFailValidationForReasonTooLong() {
        // Given
        String transactionId = testException.getTransactionId();
        String longReason = "A".repeat(501); // Exceeds 500 character limit

        // When & Then
        graphQlTester.document(CANCEL_RETRY_MUTATION)
                .variable("transactionId", transactionId)
                .variable("reason", longReason)
                .execute()
                .path("cancelRetry.success")
                .entity(Boolean.class)
                .isEqualTo(false)
                .path("cancelRetry.errors[0].code")
                .entity(String.class)
                .isEqualTo(MutationErrorCode.INVALID_REASON_LENGTH.getCode())
                .path("cancelRetry.errors[0].message")
                .entity(String.class)
                .contains("exceeds maximum length");
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    @DisplayName("Should fail authorization for insufficient permissions")
    void shouldFailAuthorizationForInsufficientPermissions() {
        // Given
        String transactionId = testException.getTransactionId();
        String reason = "Valid reason";

        // When & Then
        graphQlTester.document(CANCEL_RETRY_MUTATION)
                .variable("transactionId", transactionId)
                .variable("reason", reason)
                .execute()
                .errors()
                .expect(error -> error.getMessage().contains("Access Denied"));
    }

    @Test
    @WithMockUser(roles = "OPERATIONS")
    @DisplayName("Should handle concurrent cancellation attempts gracefully")
    void shouldHandleConcurrentCancellationAttempts() {
        // Given - First, cancel the retry
        String transactionId = testException.getTransactionId();
        String reason1 = "First cancellation attempt";

        // First cancellation
        CancelRetryResult firstResult = graphQlTester.document(CANCEL_RETRY_MUTATION)
                .variable("transactionId", transactionId)
                .variable("reason", reason1)
                .execute()
                .path("cancelRetry")
                .entity(CancelRetryResult.class)
                .get();

        assertThat(firstResult.isSuccess()).isTrue();

        // Second cancellation attempt should fail
        String reason2 = "Second cancellation attempt";

        graphQlTester.document(CANCEL_RETRY_MUTATION)
                .variable("transactionId", transactionId)
                .variable("reason", reason2)
                .execute()
                .path("cancelRetry.success")
                .entity(Boolean.class)
                .isEqualTo(false)
                .path("cancelRetry.errors[0].code")
                .entity(String.class)
                .isEqualTo(MutationErrorCode.RETRY_ALREADY_COMPLETED.getCode())
                .path("cancelRetry.errors[0].message")
                .entity(String.class)
                .contains("already been cancelled");
    }

    @Test
    @WithMockUser(roles = "OPERATIONS")
    @DisplayName("Should include operation metadata in successful response")
    void shouldIncludeOperationMetadataInSuccessfulResponse() {
        // Given
        String transactionId = testException.getTransactionId();
        String reason = "Cancellation for testing metadata";

        // When
        Map<String, Object> response = graphQlTester.document(CANCEL_RETRY_MUTATION)
                .variable("transactionId", transactionId)
                .variable("reason", reason)
                .execute()
                .path("cancelRetry")
                .entity(Map.class)
                .get();

        // Then - Verify all metadata fields are present
        assertThat(response.get("success")).isEqualTo(true);
        assertThat(response.get("operationId")).isNotNull();
        assertThat(response.get("timestamp")).isNotNull();
        assertThat(response.get("performedBy")).isEqualTo("user");
        assertThat(response.get("cancellationReason")).isEqualTo(reason);
        assertThat(response.get("cancelledAttemptNumber")).isEqualTo(1);

        // Verify operation ID format
        String operationId = (String) response.get("operationId");
        assertThat(operationId).matches("CANCEL-TEST-TXN-001-\\d+");
    }

    @Test
    @WithMockUser(roles = "OPERATIONS")
    @DisplayName("Should include operation metadata in failed response")
    void shouldIncludeOperationMetadataInFailedResponse() {
        // Given
        String nonExistentTransactionId = "NONEXISTENT-TXN";
        String reason = "Valid reason";

        // When
        Map<String, Object> response = graphQlTester.document(CANCEL_RETRY_MUTATION)
                .variable("transactionId", nonExistentTransactionId)
                .variable("reason", reason)
                .execute()
                .path("cancelRetry")
                .entity(Map.class)
                .get();

        // Then - Verify metadata is included even in failure cases
        assertThat(response.get("success")).isEqualTo(false);
        assertThat(response.get("operationId")).isNotNull();
        assertThat(response.get("timestamp")).isNotNull();
        assertThat(response.get("performedBy")).isEqualTo("user");
        assertThat(response.get("errors")).isNotNull();

        // Verify operation ID format for failed operations
        String operationId = (String) response.get("operationId");
        assertThat(operationId).matches("CANCEL-NONEXISTENT-TXN-\\d+");
    }
}