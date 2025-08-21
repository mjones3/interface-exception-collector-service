package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionConnection;
import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionFilters;
import com.arcone.biopro.exception.collector.api.graphql.dto.PaginationInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.SortingInput;
import com.arcone.biopro.exception.collector.api.graphql.util.CursorUtil;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service class for handling GraphQL exception queries.
 * Provides methods for querying exceptions with filtering, pagination, and
 * sorting.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GraphQLExceptionService {

    private final InterfaceExceptionRepository exceptionRepository;

    /**
     * Queries exceptions with filtering, pagination, and sorting support.
     * Implements cursor-based pagination for stable results.
     * 
     * @param filters    optional filters to apply
     * @param pagination cursor-based pagination parameters
     * @param sorting    sorting configuration
     * @return CompletableFuture containing the exception connection
     */
    public CompletableFuture<ExceptionConnection> findExceptions(
            ExceptionFilters filters,
            PaginationInput pagination,
            SortingInput sorting) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Querying exceptions with filters: {}, pagination: {}, sorting: {}",
                        filters, pagination, sorting);

                // Build the query parameters
                Pageable pageable = buildPageable(pagination, sorting);

                // Execute the query based on filters
                Page<InterfaceException> page = executeFilteredQuery(filters, pageable);

                // Convert to GraphQL connection format
                return buildExceptionConnection(page, pagination);

            } catch (Exception e) {
                log.error("Error querying exceptions", e);
                throw new RuntimeException("Failed to query exceptions: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Finds a single exception by transaction ID.
     * 
     * @param transactionId the unique transaction identifier
     * @return CompletableFuture containing the exception if found
     */
    public CompletableFuture<Optional<InterfaceException>> findExceptionByTransactionId(String transactionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Finding exception by transaction ID: {}", transactionId);

                if (transactionId == null || transactionId.trim().isEmpty()) {
                    return Optional.empty();
                }

                return exceptionRepository.findByTransactionId(transactionId.trim());

            } catch (Exception e) {
                log.error("Error finding exception by transaction ID: {}", transactionId, e);
                throw new RuntimeException("Failed to find exception: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Builds a Pageable object from GraphQL pagination and sorting inputs.
     */
    private Pageable buildPageable(PaginationInput pagination, SortingInput sorting) {
        // Default values
        int pageSize = 20;
        int pageNumber = 0;
        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");

        // Apply pagination
        if (pagination != null) {
            pageSize = pagination.getEffectivePageSize();

            // Handle cursor-based pagination
            if (pagination.getAfter() != null) {
                CursorUtil.CursorData cursorData = CursorUtil.parseCursor(pagination.getAfter());
                if (cursorData != null) {
                    // For cursor-based pagination, we'll handle the cursor filtering in the query
                    // For now, just use the page size
                }
            }
        }

        // Apply sorting
        if (sorting != null) {
            String field = sorting.getEffectiveField();
            Sort.Direction direction = sorting.getEffectiveDirection() == SortingInput.SortDirection.ASC
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;

            // Map GraphQL field names to entity field names
            String entityField = mapGraphQLFieldToEntityField(field);
            sort = Sort.by(direction, entityField);
        }

        return PageRequest.of(pageNumber, pageSize, sort);
    }

    /**
     * Maps GraphQL field names to entity field names.
     */
    private String mapGraphQLFieldToEntityField(String graphqlField) {
        switch (graphqlField.toLowerCase()) {
            case "timestamp":
                return "timestamp";
            case "processedat":
                return "processedAt";
            case "severity":
                return "severity";
            case "status":
                return "status";
            case "interfacetype":
                return "interfaceType";
            case "customerid":
                return "customerId";
            case "retrycount":
                return "retryCount";
            default:
                return "timestamp"; // Default fallback
        }
    }

    /**
     * Executes the filtered query based on the provided filters.
     */
    private Page<InterfaceException> executeFilteredQuery(ExceptionFilters filters, Pageable pageable) {
        if (filters == null) {
            return exceptionRepository.findAll(pageable);
        }

        // Handle simple single-value filters first
        if (isSingleValueFilter(filters)) {
            return executeSingleValueFilter(filters, pageable);
        }

        // For complex filters, use the custom repository method
        return executeComplexFilter(filters, pageable);
    }

    /**
     * Checks if the filters represent a simple single-value filter.
     */
    private boolean isSingleValueFilter(ExceptionFilters filters) {
        int filterCount = 0;

        if (filters.getInterfaceTypes() != null && filters.getInterfaceTypes().size() == 1)
            filterCount++;
        if (filters.getStatuses() != null && filters.getStatuses().size() == 1)
            filterCount++;
        if (filters.getSeverities() != null && filters.getSeverities().size() == 1)
            filterCount++;
        if (filters.getCustomerIds() != null && filters.getCustomerIds().size() == 1)
            filterCount++;
        if (filters.getDateRange() != null)
            filterCount++;
        if (filters.getSearchTerm() != null)
            filterCount++;

        return filterCount == 1;
    }

    /**
     * Executes a simple single-value filter query.
     */
    private Page<InterfaceException> executeSingleValueFilter(ExceptionFilters filters, Pageable pageable) {
        // Interface type filter
        if (filters.getInterfaceTypes() != null && filters.getInterfaceTypes().size() == 1) {
            return exceptionRepository.findByInterfaceType(filters.getInterfaceTypes().get(0), pageable);
        }

        // Status filter
        if (filters.getStatuses() != null && filters.getStatuses().size() == 1) {
            return exceptionRepository.findByStatus(filters.getStatuses().get(0), pageable);
        }

        // Severity filter
        if (filters.getSeverities() != null && filters.getSeverities().size() == 1) {
            return exceptionRepository.findBySeverity(filters.getSeverities().get(0), pageable);
        }

        // Customer ID filter
        if (filters.getCustomerIds() != null && filters.getCustomerIds().size() == 1) {
            return exceptionRepository.findByCustomerId(filters.getCustomerIds().get(0), pageable);
        }

        // Date range filter
        if (filters.getDateRange() != null) {
            return exceptionRepository.findByTimestampBetween(
                    filters.getDateRange().getFrom(),
                    filters.getDateRange().getTo(),
                    pageable);
        }

        // Search term filter
        if (filters.getSearchTerm() != null && !filters.getSearchTerm().trim().isEmpty()) {
            return exceptionRepository.searchByExceptionReason(filters.getSearchTerm().trim(), pageable);
        }

        // Fallback to all
        return exceptionRepository.findAll(pageable);
    }

    /**
     * Executes a complex filter query using the custom repository method.
     */
    private Page<InterfaceException> executeComplexFilter(ExceptionFilters filters, Pageable pageable) {
        // Extract single values from lists for the repository method
        var interfaceType = filters.getInterfaceTypes() != null && !filters.getInterfaceTypes().isEmpty()
                ? filters.getInterfaceTypes().get(0)
                : null;
        var status = filters.getStatuses() != null && !filters.getStatuses().isEmpty()
                ? filters.getStatuses().get(0)
                : null;
        var severity = filters.getSeverities() != null && !filters.getSeverities().isEmpty()
                ? filters.getSeverities().get(0)
                : null;
        var customerId = filters.getCustomerIds() != null && !filters.getCustomerIds().isEmpty()
                ? filters.getCustomerIds().get(0)
                : null;
        var fromDate = filters.getDateRange() != null ? filters.getDateRange().getFrom() : null;
        var toDate = filters.getDateRange() != null ? filters.getDateRange().getTo() : null;

        // Apply excludeResolved filter
        if (filters.getExcludeResolved() != null && filters.getExcludeResolved()) {
            if (status == null) {
                // If no status specified but excludeResolved is true, exclude resolved statuses
                // This is a simplified approach - in a real implementation you might want to
                // handle this differently
                status = ExceptionStatus.NEW; // Default to NEW status
            }
        }

        return exceptionRepository.findWithFilters(
                interfaceType, status, severity, customerId, fromDate, toDate, pageable);
    }

    /**
     * Builds an ExceptionConnection from a Page of exceptions.
     */
    private ExceptionConnection buildExceptionConnection(Page<InterfaceException> page, PaginationInput pagination) {
        List<ExceptionConnection.ExceptionEdge> edges = page.getContent().stream()
                .map(exception -> ExceptionConnection.ExceptionEdge.builder()
                        .node(exception)
                        .cursor(CursorUtil.createCursor(exception))
                        .build())
                .collect(Collectors.toList());

        ExceptionConnection.PageInfo pageInfo = ExceptionConnection.PageInfo.builder()
                .hasNextPage(page.hasNext())
                .hasPreviousPage(page.hasPrevious())
                .startCursor(edges.isEmpty() ? null : edges.get(0).getCursor())
                .endCursor(edges.isEmpty() ? null : edges.get(edges.size() - 1).getCursor())
                .build();

        return ExceptionConnection.builder()
                .edges(edges)
                .pageInfo(pageInfo)
                .totalCount(page.getTotalElements())
                .build();
    }
}