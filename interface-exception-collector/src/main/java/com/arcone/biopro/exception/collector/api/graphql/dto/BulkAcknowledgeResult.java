package com.arcone.biopro.exception.collector.api.graphql.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.ArrayList;

/**
 * Result DTO for bulk exception acknowledgment operations via GraphQL.
 * Contains aggregated results and individual operation outcomes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkAcknowledgeResult {

    /**
     * Number of exceptions successfully acknowledged.
     */
    private int successCount;

    /**
     * Number of exceptions that failed to be acknowledged.
     */
    private int failureCount;

    /**
     * Individual results for each acknowledgment operation.
     * Contains both successful and failed operations.
     */
    @Builder.Default
    private List<AcknowledgeExceptionResult> results = new ArrayList<>();

    /**
     * List of general errors that affected the entire bulk operation.
     * Individual operation errors are contained in the results list.
     */
    @Builder.Default
    private List<GraphQLError> errors = new ArrayList<>();

    /**
     * Creates a successful bulk acknowledgment result.
     *
     * @param results the list of individual acknowledgment results
     * @return BulkAcknowledgeResult with aggregated success/failure counts
     */
    public static BulkAcknowledgeResult fromResults(List<AcknowledgeExceptionResult> results) {
        int successCount = (int) results.stream().filter(AcknowledgeExceptionResult::isSuccess).count();
        int failureCount = results.size() - successCount;

        return BulkAcknowledgeResult.builder()
                .successCount(successCount)
                .failureCount(failureCount)
                .results(results)
                .errors(new ArrayList<>())
                .build();
    }

    /**
     * Creates a failed bulk acknowledgment result with a general error.
     *
     * @param error      the error that prevented the bulk operation
     * @param totalCount the total number of exceptions that were attempted
     * @return BulkAcknowledgeResult indicating complete failure
     */
    public static BulkAcknowledgeResult failure(GraphQLError error, int totalCount) {
        return BulkAcknowledgeResult.builder()
                .successCount(0)
                .failureCount(totalCount)
                .results(new ArrayList<>())
                .errors(List.of(error))
                .build();
    }

    /**
     * Gets the total number of operations attempted.
     *
     * @return sum of successful and failed operations
     */
    public int getTotalCount() {
        return successCount + failureCount;
    }

    /**
     * Checks if the bulk operation was completely successful.
     *
     * @return true if all operations succeeded, false otherwise
     */
    public boolean isCompleteSuccess() {
        return failureCount == 0 && errors.isEmpty();
    }

    /**
     * Checks if the bulk operation was completely failed.
     *
     * @return true if all operations failed, false otherwise
     */
    public boolean isCompleteFailure() {
        return successCount == 0;
    }
}