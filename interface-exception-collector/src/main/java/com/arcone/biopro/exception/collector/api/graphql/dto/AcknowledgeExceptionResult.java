package com.arcone.biopro.exception.collector.api.graphql.dto;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

/**
 * Enhanced result DTO for exception acknowledgment operations via GraphQL.
 * Contains the acknowledgment outcome, updated exception data, operation metadata, and any errors.
 * Includes enhanced metadata for better tracking and audit purposes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcknowledgeExceptionResult {

    /**
     * Indicates whether the acknowledgment operation was successful.
     */
    private boolean success;

    /**
     * The updated exception entity after acknowledgment.
     * Only populated if the operation was successful.
     */
    private InterfaceException exception;

    /**
     * List of errors that occurred during the acknowledgment operation.
     * Empty if the operation was successful.
     */
    @Builder.Default
    private List<GraphQLError> errors = new ArrayList<>();

    /**
     * Enhanced metadata: Unique operation ID for tracking and audit purposes.
     */
    private String operationId;

    /**
     * Enhanced metadata: Timestamp when the acknowledgment operation was performed.
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Enhanced metadata: User who performed the acknowledgment operation.
     */
    private String performedBy;

    /**
     * Enhanced metadata: Acknowledgment reason provided during the operation.
     */
    private String acknowledgmentReason;

    /**
     * Enhanced metadata: Additional notes provided during acknowledgment.
     */
    private String acknowledgmentNotes;



    /**
     * Creates a successful acknowledgment result with enhanced metadata.
     *
     * @param exception            the updated exception entity
     * @param operationId          unique operation identifier
     * @param performedBy          user who performed the operation
     * @param acknowledgmentReason the acknowledgment reason provided
     * @param acknowledgmentNotes  additional notes provided
     * @return AcknowledgeExceptionResult indicating success
     */
    public static AcknowledgeExceptionResult success(InterfaceException exception, 
                                                   String operationId,
                                                   String performedBy,
                                                   String acknowledgmentReason,
                                                   String acknowledgmentNotes) {
        return AcknowledgeExceptionResult.builder()
                .success(true)
                .exception(exception)
                .errors(new ArrayList<>())
                .operationId(operationId)
                .timestamp(Instant.now())
                .performedBy(performedBy)
                .acknowledgmentReason(acknowledgmentReason)
                .acknowledgmentNotes(acknowledgmentNotes)
                .build();
    }

    /**
     * Creates a successful acknowledgment result with basic metadata.
     *
     * @param exception the updated exception entity
     * @return AcknowledgeExceptionResult indicating success
     */
    public static AcknowledgeExceptionResult success(InterfaceException exception) {
        return AcknowledgeExceptionResult.builder()
                .success(true)
                .exception(exception)
                .errors(new ArrayList<>())
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates a failed acknowledgment result with error details and operation context.
     *
     * @param error       the error that occurred
     * @param operationId unique operation identifier
     * @param performedBy user who attempted the operation
     * @return AcknowledgeExceptionResult indicating failure
     */
    public static AcknowledgeExceptionResult failure(GraphQLError error, String operationId, String performedBy) {
        return AcknowledgeExceptionResult.builder()
                .success(false)
                .exception(null)
                .errors(List.of(error))
                .operationId(operationId)
                .timestamp(Instant.now())
                .performedBy(performedBy)
                .build();
    }

    /**
     * Creates a failed acknowledgment result with a single error.
     *
     * @param error the error that occurred
     * @return AcknowledgeExceptionResult indicating failure
     */
    public static AcknowledgeExceptionResult failure(GraphQLError error) {
        return AcknowledgeExceptionResult.builder()
                .success(false)
                .exception(null)
                .errors(List.of(error))
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates a failed acknowledgment result with multiple errors and operation context.
     *
     * @param errors      the list of errors that occurred
     * @param operationId unique operation identifier
     * @param performedBy user who attempted the operation
     * @return AcknowledgeExceptionResult indicating failure
     */
    public static AcknowledgeExceptionResult failure(List<GraphQLError> errors, String operationId, String performedBy) {
        return AcknowledgeExceptionResult.builder()
                .success(false)
                .exception(null)
                .errors(new ArrayList<>(errors))
                .operationId(operationId)
                .timestamp(Instant.now())
                .performedBy(performedBy)
                .build();
    }

    /**
     * Creates a failed acknowledgment result with multiple errors.
     *
     * @param errors the list of errors that occurred
     * @return AcknowledgeExceptionResult indicating failure
     */
    public static AcknowledgeExceptionResult failure(List<GraphQLError> errors) {
        return AcknowledgeExceptionResult.builder()
                .success(false)
                .exception(null)
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