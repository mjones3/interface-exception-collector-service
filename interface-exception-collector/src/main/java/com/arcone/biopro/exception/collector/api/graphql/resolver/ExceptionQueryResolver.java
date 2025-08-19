package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionConnection;
import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionFilters;
import com.arcone.biopro.exception.collector.api.graphql.dto.PaginationInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.SortingInput;
import com.arcone.biopro.exception.collector.api.graphql.service.GraphQLExceptionService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.concurrent.CompletableFuture;

/**
 * GraphQL Query resolver for exception-related operations.
 * Handles queries for exception lists and individual exception details.
 * 
 * Implements requirements:
 * - 1.1: GraphQL endpoint for exception data queries
 * - 1.2: Filtering by interface type, status, severity
 * - 1.3: Cursor-based pagination support
 * - 1.5: Performance requirements (500ms for list queries, 1s for detail
 * queries)
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@Validated
public class ExceptionQueryResolver {

    private final GraphQLExceptionService graphQLExceptionService;

    /**
     * Query for retrieving a paginated list of exceptions with optional filtering
     * and sorting.
     * 
     * Supports:
     * - Filtering by interface type, status, severity, date range, customer ID,
     * location code, and search terms
     * - Cursor-based pagination for stable results
     * - Sorting by various fields
     * 
     * Performance target: 500ms response time (95th percentile)
     * 
     * @param filters    optional filters to apply to the query
     * @param pagination cursor-based pagination parameters
     * @param sorting    sorting configuration
     * @return CompletableFuture containing paginated exception results
     */
    @QueryMapping
    public CompletableFuture<ExceptionConnection> exceptions(
            @Argument @Valid ExceptionFilters filters,
            @Argument @Valid PaginationInput pagination,
            @Argument @Valid SortingInput sorting) {

        log.debug("GraphQL exceptions query - filters: {}, pagination: {}, sorting: {}",
                filters, pagination, sorting);

        // Validate pagination parameters
        if (pagination != null) {
            validatePaginationInput(pagination);
        }

        // Validate sorting parameters
        if (sorting != null) {
            validateSortingInput(sorting);
        }

        return graphQLExceptionService.findExceptions(filters, pagination, sorting)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Error executing exceptions query", throwable);
                    } else {
                        log.debug("Successfully executed exceptions query, returned {} results",
                                result.getEdges().size());
                    }
                });
    }

    /**
     * Query for retrieving a single exception by its transaction ID.
     * 
     * Performance target: 1s response time (95th percentile)
     * 
     * @param transactionId the unique transaction identifier
     * @return CompletableFuture containing the exception if found, null otherwise
     */
    @QueryMapping
    public CompletableFuture<InterfaceException> exception(
            @Argument @NotBlank(message = "Transaction ID is required") @Size(max = 255, message = "Transaction ID must not exceed 255 characters") String transactionId) {

        log.debug("GraphQL exception query - transactionId: {}", transactionId);

        return graphQLExceptionService.findExceptionByTransactionId(transactionId)
                .thenApply(optional -> {
                    if (optional.isPresent()) {
                        log.debug("Found exception for transaction ID: {}", transactionId);
                        return optional.get();
                    } else {
                        log.debug("No exception found for transaction ID: {}", transactionId);
                        return null;
                    }
                })
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Error executing exception query for transaction ID: {}", transactionId, throwable);
                    }
                });
    }

    /**
     * Validates pagination input parameters.
     * 
     * @param pagination the pagination input to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePaginationInput(PaginationInput pagination) {
        // Validate that both forward and backward pagination aren't used together
        if (pagination.isForwardPagination() && pagination.isBackwardPagination()) {
            throw new IllegalArgumentException(
                    "Cannot use both forward pagination (first/after) and backward pagination (last/before) together");
        }

        // Validate page size limits
        if (pagination.getFirst() != null && pagination.getFirst() <= 0) {
            throw new IllegalArgumentException("'first' parameter must be positive");
        }

        if (pagination.getLast() != null && pagination.getLast() <= 0) {
            throw new IllegalArgumentException("'last' parameter must be positive");
        }

        // Validate maximum page size
        int effectivePageSize = pagination.getEffectivePageSize();
        if (effectivePageSize > 100) {
            throw new IllegalArgumentException("Page size cannot exceed 100 items");
        }
    }

    /**
     * Validates sorting input parameters.
     * 
     * @param sorting the sorting input to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateSortingInput(SortingInput sorting) {
        if (sorting.getField() != null) {
            String field = sorting.getField().toLowerCase();

            // Validate allowed sort fields
            if (!isValidSortField(field)) {
                throw new IllegalArgumentException(
                        "Invalid sort field: " + sorting.getField() +
                                ". Allowed fields: timestamp, processedAt, severity, status, interfaceType, customerId, retryCount");
            }
        }
    }

    /**
     * Checks if the provided field is valid for sorting.
     * 
     * @param field the field name to validate
     * @return true if the field is valid for sorting
     */
    private boolean isValidSortField(String field) {
        switch (field) {
            case "timestamp":
            case "processedat":
            case "severity":
            case "status":
            case "interfacetype":
            case "customerid":
            case "retrycount":
            case "acknowledgedat":
            case "resolvedat":
                return true;
            default:
                return false;
        }
    }
}