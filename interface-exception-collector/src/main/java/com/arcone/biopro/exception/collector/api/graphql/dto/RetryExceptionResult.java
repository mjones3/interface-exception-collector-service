package com.arcone.biopro.exception.collector.api.graphql.dto;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput.RetryPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

/**
 * Enhanced result type for retry exception GraphQL mutation.
 * Includes operation metadata for better tracking and audit purposes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryExceptionResult {

    /**
     * Indicates whether the retry operation was successful.
     */
    private boolean success;

    /**
     * The updated exception entity after retry initiation.
     * Only populated if the operation was successful.
     */
    private InterfaceException exception;

    /**
     * The retry attempt that was created.
     * Only populated if the operation was successful.
     */
    private RetryAttempt retryAttempt;

    /**
     * List of errors that occurred during the retry operation.
     * Empty if the operation was successful.
     */
    @Builder.Default
    private List<GraphQLError> errors = new ArrayList<>();

    /**
     * Enhanced metadata: Unique operation ID for tracking and audit purposes.
     */
    private String operationId;

    /**
     * Enhanced metadata: Timestamp when the retry operation was performed.
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Enhanced metadata: User who performed the retry operation.
     */
    private String performedBy;

    /**
     * Enhanced metadata: The retry priority that was used.
     */
    private RetryPriority retryPriority;

    /**
     * Enhanced metadata: Retry reason provided during the operation.
     */
    private String retryReason;

    /**
     * Enhanced metadata: The attempt number for this retry.
     */
    private Integer attemptNumber;

    /**
     * Creates a successful retry result with enhanced metadata.
     *
     * @param exception      the updated exception entity
     * @param retryAttempt   the created retry attempt
     * @param operationId    unique operation identifier
     * @param performedBy    user who performed the operation
     * @param retryPriority  the retry priority used
     * @param retryReason    the retry reason provided
     * @return RetryExceptionResult indicating success
     */
    public static RetryExceptionResult success(InterfaceException exception, 
                                             RetryAttempt retryAttempt,
                                             String operationId,
                                             String performedBy,
                                             RetryPriority retryPriority,
                                             String retryReason) {
        return RetryExceptionResult.builder()
                .success(true)
                .exception(exception)
                .retryAttempt(retryAttempt)
                .errors(new ArrayList<>())
                .operationId(operationId)
                .timestamp(Instant.now())
                .performedBy(performedBy)
                .retryPriority(retryPriority)
                .retryReason(retryReason)
                .attemptNumber(retryAttempt != null ? retryAttempt.getAttemptNumber() : null)
                .build();
    }

    /**
     * Creates a successful retry result with basic metadata.
     *
     * @param exception    the updated exception entity
     * @param retryAttempt the created retry attempt
     * @return RetryExceptionResult indicating success
     */
    public static RetryExceptionResult success(InterfaceException exception, RetryAttempt retryAttempt) {
        return RetryExceptionResult.builder()
                .success(true)
                .exception(exception)
                .retryAttempt(retryAttempt)
                .errors(new ArrayList<>())
                .timestamp(Instant.now())
                .attemptNumber(retryAttempt != null ? retryAttempt.getAttemptNumber() : null)
                .build();
    }

    /**
     * Creates a failed retry result with error details and operation context.
     *
     * @param error       the error that occurred
     * @param operationId unique operation identifier
     * @param performedBy user who attempted the operation
     * @return RetryExceptionResult indicating failure
     */
    public static RetryExceptionResult failure(GraphQLError error, String operationId, String performedBy) {
        return RetryExceptionResult.builder()
                .success(false)
                .exception(null)
                .retryAttempt(null)
                .errors(List.of(error))
                .operationId(operationId)
                .timestamp(Instant.now())
                .performedBy(performedBy)
                .build();
    }

    /**
     * Creates a failed retry result with a single error.
     *
     * @param error the error that occurred
     * @return RetryExceptionResult indicating failure
     */
    public static RetryExceptionResult failure(GraphQLError error) {
        return RetryExceptionResult.builder()
                .success(false)
                .exception(null)
                .retryAttempt(null)
                .errors(List.of(error))
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates a failed retry result with multiple errors and operation context.
     *
     * @param errors      the list of errors that occurred
     * @param operationId unique operation identifier
     * @param performedBy user who attempted the operation
     * @return RetryExceptionResult indicating failure
     */
    public static RetryExceptionResult failure(List<GraphQLError> errors, String operationId, String performedBy) {
        return RetryExceptionResult.builder()
                .success(false)
                .exception(null)
                .retryAttempt(null)
                .errors(new ArrayList<>(errors))
                .operationId(operationId)
                .timestamp(Instant.now())
                .performedBy(performedBy)
                .build();
    }

    /**
     * Creates a failed retry result with multiple errors.
     *
     * @param errors the list of errors that occurred
     * @return RetryExceptionResult indicating failure
     */
    public static RetryExceptionResult failure(List<GraphQLError> errors) {
        return RetryExceptionResult.builder()
                .success(false)
                .exception(null)
                .retryAttempt(null)
                .errors(new ArrayList<>(errors))
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Checks if the result has any errors.
     *
     * @return true if there are errors
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Gets the number of errors.
     *
     * @return the error count
     */
    public int getErrorCount() {
        return errors != null ? errors.size() : 0;
    }

    /**
     * Checks if operation metadata is available.
     *
     * @return true if operation metadata is present
     */
    public boolean hasOperationMetadata() {
        return operationId != null && performedBy != null;
    }
}