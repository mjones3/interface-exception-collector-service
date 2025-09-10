package com.arcone.biopro.exception.collector.api.graphql.dto;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Enhanced result type for cancel retry GraphQL mutation.
 * Includes detailed cancellation information and operation metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelRetryResult {

    private boolean success;
    private InterfaceException exception;
    private RetryAttempt cancelledRetryAttempt;
    
    // Enhanced metadata fields
    private String operationId;
    private Instant timestamp;
    private String performedBy;
    private String cancellationReason;
    private Integer cancelledAttemptNumber;

    @Builder.Default
    private List<GraphQLError> errors = List.of();

    /**
     * Creates a successful cancellation result with enhanced metadata.
     *
     * @param exception the updated exception
     * @param cancelledAttempt the cancelled retry attempt
     * @param operationId unique operation identifier
     * @param performedBy user who performed the cancellation
     * @param reason cancellation reason
     * @return successful CancelRetryResult
     */
    public static CancelRetryResult success(InterfaceException exception, 
                                          RetryAttempt cancelledAttempt,
                                          String operationId,
                                          String performedBy,
                                          String reason) {
        return CancelRetryResult.builder()
                .success(true)
                .exception(exception)
                .cancelledRetryAttempt(cancelledAttempt)
                .operationId(operationId)
                .timestamp(Instant.now())
                .performedBy(performedBy)
                .cancellationReason(reason)
                .cancelledAttemptNumber(cancelledAttempt != null ? cancelledAttempt.getAttemptNumber() : null)
                .errors(List.of())
                .build();
    }

    /**
     * Creates a failed cancellation result with error details.
     *
     * @param error the error that occurred
     * @param operationId unique operation identifier
     * @param performedBy user who attempted the cancellation
     * @return failed CancelRetryResult
     */
    public static CancelRetryResult failure(GraphQLError error, 
                                          String operationId,
                                          String performedBy) {
        return CancelRetryResult.builder()
                .success(false)
                .operationId(operationId)
                .timestamp(Instant.now())
                .performedBy(performedBy)
                .errors(List.of(error))
                .build();
    }

    /**
     * Creates a failed cancellation result with multiple errors.
     *
     * @param errors list of errors that occurred
     * @param operationId unique operation identifier
     * @param performedBy user who attempted the cancellation
     * @return failed CancelRetryResult
     */
    public static CancelRetryResult failure(List<GraphQLError> errors, 
                                          String operationId,
                                          String performedBy) {
        return CancelRetryResult.builder()
                .success(false)
                .operationId(operationId)
                .timestamp(Instant.now())
                .performedBy(performedBy)
                .errors(errors)
                .build();
    }
}