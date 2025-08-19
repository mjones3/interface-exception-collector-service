package com.arcone.biopro.exception.collector.api.graphql.dto;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.ArrayList;

/**
 * Result DTO for exception acknowledgment operations via GraphQL.
 * Contains the acknowledgment outcome, updated exception data, and any errors.
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
     * Creates a successful acknowledgment result.
     *
     * @param exception the updated exception entity
     * @return AcknowledgeExceptionResult indicating success
     */
    public static AcknowledgeExceptionResult success(InterfaceException exception) {
        return AcknowledgeExceptionResult.builder()
                .success(true)
                .exception(exception)
                .errors(new ArrayList<>())
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
                .errors(errors)
                .build();
    }
}