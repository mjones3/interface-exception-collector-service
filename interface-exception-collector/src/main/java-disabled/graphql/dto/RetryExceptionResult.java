package com.arcone.biopro.exception.collector.api.graphql.dto;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Result type for retry exception GraphQL mutation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryExceptionResult {

    private boolean success;
    private InterfaceException exception;
    private RetryAttempt retryAttempt;

    @Builder.Default
    private List<GraphQLError> errors = List.of();
}