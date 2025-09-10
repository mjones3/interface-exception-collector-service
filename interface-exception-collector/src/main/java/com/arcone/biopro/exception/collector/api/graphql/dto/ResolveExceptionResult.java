package com.arcone.biopro.exception.collector.api.graphql.dto;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ResolutionMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

/**
 * Enhanced result DTO for exception resolution operations via GraphQL.
 * Contains the resolution outcome, updated exception data, operation metadata, and any errors.
 * Includes enhanced metadata for better tracking and audit purposes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveExceptionResult {

    /**
     * Indicates whether the resolution operation was successful.
     */
    private boolean success;

    /**
     * The updated exception entity after resolution.
     * Only populated if the operation was successful.
     */
    private InterfaceException exception;

    /**
     * List of errors that occurred during the resolution operation.
     * Empty if the operation was successful.
     */
    @Builder.Default
    private List<GraphQLError> errors = new ArrayList<>();

    /**
     * Enhanced metadata: Unique operation ID for tracking and audit purposes.
     */
    private String operationId;

    /**
     * Enhanced metadata: Timestamp when the resolution operation was performed.
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Enhanced metadata: User who performed the resolution operation.
     */
    private String performedBy;

    /**
     * Enhanced metadata: The resolution method that was used.
     */
    private ResolutionMethod resolutionMethod;

    /**
     * Enhanced metadata: Resolution notes provided during the operation.
     */
    private String resolutionNotes;

    /**
     * Creates a successful resolution result with enhanced metadata.
     *
     * @param exception        the updated exception entity
     * @param operationId      unique operation identifier
     * @param performedBy      user who performed the operation
     * @param resolutionMethod the resolution method used
     * @param resolutionNotes  the resolution notes provided
     * @return ResolveExceptionResult indicating success
     */
    public static ResolveExceptionResult success(InterfaceException exception, 
                                               String operationId,
                                               String performedBy,
                                               ResolutionMethod resolutionMethod,
                                               String resolutionNotes) {
        return ResolveExceptionResult.builder()
                .success(true)
                .exception(exception)
                .errors(new ArrayList<>())
                .operationId(operationId)
                .timestamp(Instant.now())
                .performedBy(performedBy)
                .resolutionMethod(resolutionMethod)
                .resolutionNotes(resolutionNotes)
                .build();
    }

    /**
     * Creates a successful resolution result with basic metadata.
     *
     * @param exception the updated exception entity
     * @return ResolveExceptionResult indicating success
     */
    public static ResolveExceptionResult success(InterfaceException exception) {
        return ResolveExceptionResult.builder()
                .success(true)
                .exception(exception)
                .errors(new ArrayList<>())
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates a failed resolution result with a single error and operation context.
     *
     * @param error       the error that occurred
     * @param operationId unique operation identifier
     * @param performedBy user who attempted the operation
     * @return ResolveExceptionResult indicating failure
     */
    public static ResolveExceptionResult failure(GraphQLError error, String operationId, String performedBy) {
        return ResolveExceptionResult.builder()
                .success(false)
                .exception(null)
                .errors(List.of(error))
                .operationId(operationId)
                .timestamp(Instant.now())
                .performedBy(performedBy)
                .build();
    }

    /**
     * Creates a failed resolution result with a single error.
     *
     * @param error the error that occurred
     * @return ResolveExceptionResult indicating failure
     */
    public static ResolveExceptionResult failure(GraphQLError error) {
        return ResolveExceptionResult.builder()
                .success(false)
                .exception(null)
                .errors(List.of(error))
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates a failed resolution result with multiple errors and operation context.
     *
     * @param errors      the list of errors that occurred
     * @param operationId unique operation identifier
     * @param performedBy user who attempted the operation
     * @return ResolveExceptionResult indicating failure
     */
    public static ResolveExceptionResult failure(List<GraphQLError> errors, String operationId, String performedBy) {
        return ResolveExceptionResult.builder()
                .success(false)
                .exception(null)
                .errors(new ArrayList<>(errors))
                .operationId(operationId)
                .timestamp(Instant.now())
                .performedBy(performedBy)
                .build();
    }

    /**
     * Creates a failed resolution result with multiple errors.
     *
     * @param errors the list of errors that occurred
     * @return ResolveExceptionResult indicating failure
     */
    public static ResolveExceptionResult failure(List<GraphQLError> errors) {
        return ResolveExceptionResult.builder()
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