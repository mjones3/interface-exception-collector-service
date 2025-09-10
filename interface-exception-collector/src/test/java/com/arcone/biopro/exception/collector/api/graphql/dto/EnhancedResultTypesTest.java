package com.arcone.biopro.exception.collector.api.graphql.dto;

import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput.RetryPriority;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ResolutionMethod;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for enhanced mutation result types with operation metadata.
 */
class EnhancedResultTypesTest {

    @Test
    void retryExceptionResult_WithEnhancedMetadata_ShouldCreateSuccessfully() {
        // Given
        InterfaceException exception = new InterfaceException();
        exception.setTransactionId("TXN-123");
        
        RetryAttempt retryAttempt = new RetryAttempt();
        retryAttempt.setAttemptNumber(2);
        
        String operationId = UUID.randomUUID().toString();
        String performedBy = "test-user";
        RetryPriority priority = RetryPriority.HIGH;
        String reason = "System error resolved";

        // When
        RetryExceptionResult result = RetryExceptionResult.success(
            exception, retryAttempt, operationId, performedBy, priority, reason
        );

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getException()).isEqualTo(exception);
        assertThat(result.getRetryAttempt()).isEqualTo(retryAttempt);
        assertThat(result.getOperationId()).isEqualTo(operationId);
        assertThat(result.getPerformedBy()).isEqualTo(performedBy);
        assertThat(result.getRetryPriority()).isEqualTo(priority);
        assertThat(result.getRetryReason()).isEqualTo(reason);
        assertThat(result.getAttemptNumber()).isEqualTo(2);
        assertThat(result.getTimestamp()).isNotNull();
        assertThat(result.hasOperationMetadata()).isTrue();
        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void retryExceptionResult_WithBasicMetadata_ShouldCreateSuccessfully() {
        // Given
        InterfaceException exception = new InterfaceException();
        RetryAttempt retryAttempt = new RetryAttempt();
        retryAttempt.setAttemptNumber(1);

        // When
        RetryExceptionResult result = RetryExceptionResult.success(exception, retryAttempt);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getException()).isEqualTo(exception);
        assertThat(result.getRetryAttempt()).isEqualTo(retryAttempt);
        assertThat(result.getAttemptNumber()).isEqualTo(1);
        assertThat(result.getTimestamp()).isNotNull();
        assertThat(result.hasOperationMetadata()).isFalse();
    }

    @Test
    void retryExceptionResult_WithFailure_ShouldCreateWithErrorDetails() {
        // Given
        GraphQLError error = GraphQLError.builder()
            .message("Retry failed")
            .code(ErrorCode.BUSINESS_RULE_ERROR)
            .build();
        String operationId = UUID.randomUUID().toString();
        String performedBy = "test-user";

        // When
        RetryExceptionResult result = RetryExceptionResult.failure(error, operationId, performedBy);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getException()).isNull();
        assertThat(result.getRetryAttempt()).isNull();
        assertThat(result.getOperationId()).isEqualTo(operationId);
        assertThat(result.getPerformedBy()).isEqualTo(performedBy);
        assertThat(result.getTimestamp()).isNotNull();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrorCount()).isEqualTo(1);
    }

    @Test
    void acknowledgeExceptionResult_WithEnhancedMetadata_ShouldCreateSuccessfully() {
        // Given
        InterfaceException exception = new InterfaceException();
        exception.setTransactionId("TXN-456");
        
        String operationId = UUID.randomUUID().toString();
        String performedBy = "test-user";
        String reason = "Acknowledged for investigation";
        String notes = "Customer reported issue";

        // When
        AcknowledgeExceptionResult result = AcknowledgeExceptionResult.success(
            exception, operationId, performedBy, reason, notes
        );

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getException()).isEqualTo(exception);
        assertThat(result.getOperationId()).isEqualTo(operationId);
        assertThat(result.getPerformedBy()).isEqualTo(performedBy);
        assertThat(result.getAcknowledgmentReason()).isEqualTo(reason);
        assertThat(result.getAcknowledgmentNotes()).isEqualTo(notes);
        assertThat(result.getTimestamp()).isNotNull();
        assertThat(result.hasOperationMetadata()).isTrue();
        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void acknowledgeExceptionResult_WithBasicMetadata_ShouldCreateSuccessfully() {
        // Given
        InterfaceException exception = new InterfaceException();

        // When
        AcknowledgeExceptionResult result = AcknowledgeExceptionResult.success(exception);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getException()).isEqualTo(exception);
        assertThat(result.getTimestamp()).isNotNull();
        assertThat(result.hasOperationMetadata()).isFalse();
    }

    @Test
    void acknowledgeExceptionResult_WithFailure_ShouldCreateWithErrorDetails() {
        // Given
        List<GraphQLError> errors = List.of(
            GraphQLError.builder()
                .message("Validation failed")
                .code(ErrorCode.VALIDATION_ERROR)
                .build(),
            GraphQLError.builder()
                .message("Business rule violation")
                .code(ErrorCode.BUSINESS_RULE_ERROR)
                .build()
        );
        String operationId = UUID.randomUUID().toString();
        String performedBy = "test-user";

        // When
        AcknowledgeExceptionResult result = AcknowledgeExceptionResult.failure(errors, operationId, performedBy);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getException()).isNull();
        assertThat(result.getOperationId()).isEqualTo(operationId);
        assertThat(result.getPerformedBy()).isEqualTo(performedBy);
        assertThat(result.getTimestamp()).isNotNull();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrorCount()).isEqualTo(2);
    }

    @Test
    void resolveExceptionResult_WithEnhancedMetadata_ShouldCreateSuccessfully() {
        // Given
        InterfaceException exception = new InterfaceException();
        exception.setTransactionId("TXN-789");
        
        String operationId = UUID.randomUUID().toString();
        String performedBy = "test-user";
        ResolutionMethod method = ResolutionMethod.MANUAL_RESOLUTION;
        String notes = "Issue resolved by manual intervention";

        // When
        ResolveExceptionResult result = ResolveExceptionResult.success(
            exception, operationId, performedBy, method, notes
        );

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getException()).isEqualTo(exception);
        assertThat(result.getOperationId()).isEqualTo(operationId);
        assertThat(result.getPerformedBy()).isEqualTo(performedBy);
        assertThat(result.getResolutionMethod()).isEqualTo(method);
        assertThat(result.getResolutionNotes()).isEqualTo(notes);
        assertThat(result.getTimestamp()).isNotNull();
        assertThat(result.hasOperationMetadata()).isTrue();
        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void cancelRetryResult_WithEnhancedMetadata_ShouldCreateSuccessfully() {
        // Given
        InterfaceException exception = new InterfaceException();
        exception.setTransactionId("TXN-999");
        
        RetryAttempt cancelledAttempt = new RetryAttempt();
        cancelledAttempt.setAttemptNumber(3);
        
        String operationId = UUID.randomUUID().toString();
        String performedBy = "test-user";
        String reason = "Retry no longer needed";

        // When
        CancelRetryResult result = CancelRetryResult.success(
            exception, cancelledAttempt, operationId, performedBy, reason
        );

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getException()).isEqualTo(exception);
        assertThat(result.getCancelledRetryAttempt()).isEqualTo(cancelledAttempt);
        assertThat(result.getOperationId()).isEqualTo(operationId);
        assertThat(result.getPerformedBy()).isEqualTo(performedBy);
        assertThat(result.getCancellationReason()).isEqualTo(reason);
        assertThat(result.getCancelledAttemptNumber()).isEqualTo(3);
        assertThat(result.getTimestamp()).isNotNull();
    }

    @Test
    void allResultTypes_ShouldHaveConsistentFactoryMethods() {
        // Given
        GraphQLError error = GraphQLError.builder()
            .message("Test error")
            .code(ErrorCode.INTERNAL_ERROR)
            .build();

        // When & Then - Test that all result types have consistent factory methods
        RetryExceptionResult retryFailure = RetryExceptionResult.failure(error);
        assertThat(retryFailure.isSuccess()).isFalse();
        assertThat(retryFailure.hasErrors()).isTrue();

        AcknowledgeExceptionResult ackFailure = AcknowledgeExceptionResult.failure(error);
        assertThat(ackFailure.isSuccess()).isFalse();
        assertThat(ackFailure.hasErrors()).isTrue();

        ResolveExceptionResult resolveFailure = ResolveExceptionResult.failure(error);
        assertThat(resolveFailure.isSuccess()).isFalse();
        assertThat(resolveFailure.hasErrors()).isTrue();

        CancelRetryResult cancelFailure = CancelRetryResult.failure(error, "op-123", "user");
        assertThat(cancelFailure.isSuccess()).isFalse();
        assertThat(cancelFailure.getErrors()).hasSize(1);
    }
}