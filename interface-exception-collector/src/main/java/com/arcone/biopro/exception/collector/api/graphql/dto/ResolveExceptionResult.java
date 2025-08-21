package com.arcone.biopro.exception.collector.api.graphql.dto;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.ArrayList;

/**
 * Result DTO for exception resolution operations via GraphQL.
 * Contains the resolution outcome, updated exception data, and any errors.
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
     * Creates a successful resolution result.
     *
     * @param exception the updated exception entity
     * @return ResolveExceptionResult indicating success
     */
    public static ResolveExceptionResult success(InterfaceException exception) {
        return ResolveExceptionResult.builder()
                .success(true)
                .exception(exception)
                .errors(new ArrayList<>())
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
                .errors(errors)
                .build();
    }
}