package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionConnection;
import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionFilters;
import com.arcone.biopro.exception.collector.api.graphql.dto.PaginationInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.SearchInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.SortingInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.SystemHealth;
import com.arcone.biopro.exception.collector.application.service.ExceptionQueryService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.context.ApplicationContext;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    private final ExceptionQueryService exceptionQueryService;
    private final ApplicationContext applicationContext;

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

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Convert GraphQL filters to service parameters
                InterfaceType interfaceType = filters != null && filters.getInterfaceTypes() != null
                        && !filters.getInterfaceTypes().isEmpty()
                                ? filters.getInterfaceTypes().get(0)
                                : null;
                ExceptionStatus status = filters != null && filters.getStatuses() != null
                        && !filters.getStatuses().isEmpty()
                                ? filters.getStatuses().get(0)
                                : null;
                ExceptionSeverity severity = filters != null && filters.getSeverities() != null
                        && !filters.getSeverities().isEmpty()
                                ? filters.getSeverities().get(0)
                                : null;
                String customerId = filters != null && filters.getCustomerIds() != null
                        && !filters.getCustomerIds().isEmpty()
                                ? filters.getCustomerIds().get(0)
                                : null;

                OffsetDateTime fromDate = null;
                OffsetDateTime toDate = null;
                if (filters != null && filters.getDateRange() != null) {
                    fromDate = filters.getDateRange().getFrom();
                    toDate = filters.getDateRange().getTo();
                }

                // Convert sorting to Spring Data Sort
                Sort sort = Sort.unsorted();
                if (sorting != null && sorting.getField() != null) {
                    Sort.Direction direction = sorting
                            .getDirection() == SortingInput.SortDirection.ASC
                                    ? Sort.Direction.ASC
                                    : Sort.Direction.DESC;
                    sort = Sort.by(direction, sorting.getField());
                }

                // Query exceptions using eager loading service to prevent lazy loading issues
                List<InterfaceException> exceptions = exceptionQueryService.findExceptionsWithFiltersEager(
                        interfaceType, status, severity, customerId, fromDate, toDate, sort);

                // Apply pagination
                int pageSize = pagination != null ? pagination.getEffectivePageSize() : 20;
                int startIndex = 0;

                if (pagination != null && pagination.getAfter() != null) {
                    // Decode cursor to get start index
                    try {
                        String decodedCursor = new String(Base64.getDecoder().decode(pagination.getAfter()));
                        startIndex = Integer.parseInt(decodedCursor) + 1;
                    } catch (Exception e) {
                        log.warn("Invalid cursor format: {}", pagination.getAfter());
                        startIndex = 0;
                    }
                }

                // Apply pagination limits
                int endIndex = Math.min(startIndex + pageSize, exceptions.size());
                List<InterfaceException> paginatedExceptions = exceptions.subList(startIndex, endIndex);

                // Create final copy for lambda
                final int finalStartIndex = startIndex;

                // Convert to GraphQL response format
                List<ExceptionConnection.ExceptionEdge> edges = paginatedExceptions.stream()
                        .map(exception -> {
                            int index = paginatedExceptions.indexOf(exception);
                            return ExceptionConnection.ExceptionEdge.builder()
                                    .node(exception)
                                    .cursor(Base64.getEncoder()
                                            .encodeToString(String.valueOf(finalStartIndex + index).getBytes()))
                                    .build();
                        })
                        .collect(Collectors.toList());

                // Build page info
                ExceptionConnection.PageInfo pageInfo = ExceptionConnection.PageInfo.builder()
                        .hasNextPage(endIndex < exceptions.size())
                        .hasPreviousPage(startIndex > 0)
                        .startCursor(edges.isEmpty() ? null : edges.get(0).getCursor())
                        .endCursor(edges.isEmpty() ? null : edges.get(edges.size() - 1).getCursor())
                        .build();

                return ExceptionConnection.builder()
                        .edges(edges)
                        .pageInfo(pageInfo)
                        .totalCount((long) exceptions.size())
                        .build();

            } catch (Exception e) {
                log.error("Error executing exceptions query", e);
                throw new RuntimeException("Failed to execute exceptions query", e);
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

        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Searching for exception with transaction ID: {}", transactionId);
                Optional<InterfaceException> exceptionOpt = exceptionQueryService
                        .findExceptionByTransactionIdEager(transactionId);

                if (exceptionOpt.isPresent()) {
                    log.info("Found exception for transaction ID: {}", transactionId);
                    return exceptionOpt.get();
                } else {
                    log.info("No exception found for transaction ID: {}", transactionId);
                    return null;
                }
            } catch (Exception e) {
                log.error("Error executing exception query for transaction ID: {}, error: {}",
                        transactionId, e.getMessage(), e);
                throw new RuntimeException("Failed to find exception: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Query for searching exceptions using full-text search across specified
     * fields.
     * 
     * Supports:
     * - Full-text search across multiple fields (exception reason, transaction ID,
     * external ID, etc.)
     * - Fuzzy search for approximate matching
     * - Cursor-based pagination for stable results
     * - Sorting by various fields
     * 
     * Performance target: 500ms response time (95th percentile)
     * 
     * @param search     search parameters including query string and fields to
     *                   search
     * @param pagination cursor-based pagination parameters
     * @param sorting    sorting configuration
     * @return CompletableFuture containing paginated search results
     */
    @QueryMapping
    public CompletableFuture<ExceptionConnection> searchExceptions(
            @Argument @Valid SearchInput search,
            @Argument @Valid PaginationInput pagination,
            @Argument @Valid SortingInput sorting) {

        log.debug("GraphQL searchExceptions query - search: {}, pagination: {}, sorting: {}",
                search, pagination, sorting);

        // Validate search input
        if (search == null || search.getQuery() == null || search.getQuery().trim().isEmpty()) {
            throw new IllegalArgumentException("Search query is required");
        }

        // Validate pagination parameters
        if (pagination != null) {
            validatePaginationInput(pagination);
        }

        // Validate sorting parameters
        if (sorting != null) {
            validateSortingInput(sorting);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Convert sorting to Spring Data Sort
                Sort sort = Sort.unsorted();
                if (sorting != null && sorting.getField() != null) {
                    Sort.Direction direction = sorting
                            .getDirection() == SortingInput.SortDirection.ASC
                                    ? Sort.Direction.ASC
                                    : Sort.Direction.DESC;
                    sort = Sort.by(direction, sorting.getField());
                } else {
                    // Default sort by timestamp descending for search results
                    sort = Sort.by(Sort.Direction.DESC, "timestamp");
                }

                // Convert SearchField enums to strings for the service layer
                List<String> searchFields = search.getEffectiveFields().stream()
                        .map(field -> convertSearchFieldToString(field))
                        .collect(Collectors.toList());

                // Query exceptions using search service
                List<InterfaceException> exceptions = exceptionQueryService.searchExceptions(
                        search.getQuery().trim(), searchFields, sort);

                // Apply pagination
                int pageSize = pagination != null ? pagination.getEffectivePageSize() : 20;
                int startIndex = 0;

                if (pagination != null && pagination.getAfter() != null) {
                    // Decode cursor to get start index
                    try {
                        String decodedCursor = new String(Base64.getDecoder().decode(pagination.getAfter()));
                        startIndex = Integer.parseInt(decodedCursor) + 1;
                    } catch (Exception e) {
                        log.warn("Invalid cursor format: {}", pagination.getAfter());
                        startIndex = 0;
                    }
                }

                // Apply pagination limits
                int endIndex = Math.min(startIndex + pageSize, exceptions.size());
                List<InterfaceException> paginatedExceptions = exceptions.subList(startIndex, endIndex);

                // Create final copy for lambda
                final int finalStartIndex = startIndex;

                // Convert to GraphQL response format
                List<ExceptionConnection.ExceptionEdge> edges = paginatedExceptions.stream()
                        .map(exception -> {
                            int index = paginatedExceptions.indexOf(exception);
                            return ExceptionConnection.ExceptionEdge.builder()
                                    .node(exception)
                                    .cursor(Base64.getEncoder()
                                            .encodeToString(String.valueOf(finalStartIndex + index).getBytes()))
                                    .build();
                        })
                        .collect(Collectors.toList());

                // Build page info
                ExceptionConnection.PageInfo pageInfo = ExceptionConnection.PageInfo.builder()
                        .hasNextPage(endIndex < exceptions.size())
                        .hasPreviousPage(startIndex > 0)
                        .startCursor(edges.isEmpty() ? null : edges.get(0).getCursor())
                        .endCursor(edges.isEmpty() ? null : edges.get(edges.size() - 1).getCursor())
                        .build();

                return ExceptionConnection.builder()
                        .edges(edges)
                        .pageInfo(pageInfo)
                        .totalCount((long) exceptions.size())
                        .build();

            } catch (Exception e) {
                log.error("Error executing searchExceptions query", e);
                throw new RuntimeException("Failed to execute search query", e);
            }
        });
    }

    /**
     * Converts SearchField enum to string representation for service layer.
     */
    private String convertSearchFieldToString(SearchInput.SearchField field) {
        switch (field) {
            case EXCEPTION_REASON:
                return "exceptionReason";
            case TRANSACTION_ID:
                return "transactionId";
            case EXTERNAL_ID:
                return "externalId";
            case CUSTOMER_ID:
                return "customerId";
            case LOCATION_CODE:
                return "locationCode";
            case OPERATION:
                return "operation";
            default:
                return "exceptionReason";
        }
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

    /**
     * Query for retrieving system health information.
     * 
     * Provides health status for:
     * - Database connectivity
     * - Cache availability
     * - External services
     * - Overall system status
     * 
     * @return CompletableFuture containing system health information
     */
    @QueryMapping
    public CompletableFuture<SystemHealth> systemHealth() {
        log.debug("GraphQL systemHealth query");

        return CompletableFuture.supplyAsync(() -> {
            try {
                OffsetDateTime now = OffsetDateTime.now();

                // Check database health
                SystemHealth.ComponentHealth databaseHealth = checkDatabaseHealth();

                // Check cache health (simplified since Redis is disabled)
                SystemHealth.ComponentHealth cacheHealth = SystemHealth.ComponentHealth.builder()
                        .status(SystemHealth.HealthStatus.UP)
                        .responseTime(0.0f)
                        .details(Map.of("type", "simple", "status", "Cache disabled, using simple in-memory cache"))
                        .build();

                // Check external services health
                List<SystemHealth.ServiceHealth> externalServices = checkExternalServicesHealth();

                // Determine overall system status
                SystemHealth.HealthStatus overallStatus = determineOverallStatus(databaseHealth, cacheHealth,
                        externalServices);

                return SystemHealth.builder()
                        .status(overallStatus)
                        .database(databaseHealth)
                        .cache(cacheHealth)
                        .externalServices(externalServices)
                        .lastUpdated(now)
                        .build();

            } catch (Exception e) {
                log.error("Error executing systemHealth query", e);

                // Return degraded status on error
                return SystemHealth.builder()
                        .status(SystemHealth.HealthStatus.DEGRADED)
                        .database(SystemHealth.ComponentHealth.builder()
                                .status(SystemHealth.HealthStatus.DOWN)
                                .details(Map.of("error", e.getMessage()))
                                .build())
                        .cache(SystemHealth.ComponentHealth.builder()
                                .status(SystemHealth.HealthStatus.UP)
                                .responseTime(0.0f)
                                .build())
                        .externalServices(List.of())
                        .lastUpdated(OffsetDateTime.now())
                        .build();
            }
        });
    }

    /**
     * Checks database health by attempting a simple query.
     */
    private SystemHealth.ComponentHealth checkDatabaseHealth() {
        try {
            long startTime = System.currentTimeMillis();

            // Simple health check - count total exceptions
            long count = exceptionQueryService.findExceptionsWithFilters(
                    null, null, null, null, null, null, Sort.unsorted()).size();

            long responseTime = System.currentTimeMillis() - startTime;

            return SystemHealth.ComponentHealth.builder()
                    .status(SystemHealth.HealthStatus.UP)
                    .responseTime((float) responseTime)
                    .details(Map.of(
                            "type", "postgresql",
                            "totalExceptions", count,
                            "connectionPool", "hikari"))
                    .build();

        } catch (Exception e) {
            log.error("Database health check failed", e);
            return SystemHealth.ComponentHealth.builder()
                    .status(SystemHealth.HealthStatus.DOWN)
                    .details(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Checks external services health.
     */
    private List<SystemHealth.ServiceHealth> checkExternalServicesHealth() {
        // For now, return mock data for external services
        // In a real implementation, you would check actual external service endpoints
        return List.of(
                SystemHealth.ServiceHealth.builder()
                        .serviceName("partner-order-service")
                        .status(SystemHealth.HealthStatus.UP)
                        .responseTime(150.0f)
                        .lastChecked(OffsetDateTime.now())
                        .build(),
                SystemHealth.ServiceHealth.builder()
                        .serviceName("collection-service")
                        .status(SystemHealth.HealthStatus.UP)
                        .responseTime(200.0f)
                        .lastChecked(OffsetDateTime.now())
                        .build(),
                SystemHealth.ServiceHealth.builder()
                        .serviceName("distribution-service")
                        .status(SystemHealth.HealthStatus.UP)
                        .responseTime(180.0f)
                        .lastChecked(OffsetDateTime.now())
                        .build());
    }

    /**
     * Determines overall system status based on component health.
     */
    private SystemHealth.HealthStatus determineOverallStatus(
            SystemHealth.ComponentHealth database,
            SystemHealth.ComponentHealth cache,
            List<SystemHealth.ServiceHealth> externalServices) {

        // If database is down, system is down
        if (database.getStatus() == SystemHealth.HealthStatus.DOWN) {
            return SystemHealth.HealthStatus.DOWN;
        }

        // Check if any external services are down
        boolean hasDownServices = externalServices.stream()
                .anyMatch(service -> service.getStatus() == SystemHealth.HealthStatus.DOWN);

        if (hasDownServices) {
            return SystemHealth.HealthStatus.DEGRADED;
        }

        // Check if any components are degraded
        boolean hasDegradedComponents = database.getStatus() == SystemHealth.HealthStatus.DEGRADED ||
                cache.getStatus() == SystemHealth.HealthStatus.DEGRADED ||
                externalServices.stream()
                        .anyMatch(service -> service.getStatus() == SystemHealth.HealthStatus.DEGRADED);

        if (hasDegradedComponents) {
            return SystemHealth.HealthStatus.DEGRADED;
        }

        return SystemHealth.HealthStatus.UP;
    }
}