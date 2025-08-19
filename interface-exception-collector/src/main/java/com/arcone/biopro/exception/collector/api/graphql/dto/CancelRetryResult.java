package com.arcone.biopro.exception.collector.api.graphql.dto;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Result type for cancel retry GraphQL mutation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelRetryResult {

    private boolean success;
    private InterfaceException exception;

    @Builder.Default
    private List<GraphQLError> errors = List.of();
}