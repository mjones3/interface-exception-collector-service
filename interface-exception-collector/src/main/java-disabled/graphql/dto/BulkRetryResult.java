package com.arcone.biopro.exception.collector.api.graphql.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Result type for bulk retry exceptions GraphQL mutation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkRetryResult {

    private int successCount;
    private int failureCount;

    @Builder.Default
    private List<RetryExceptionResult> results = List.of();

    @Builder.Default
    private List<GraphQLError> errors = List.of();
}