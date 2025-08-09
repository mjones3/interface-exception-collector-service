package com.arcone.biopro.exception.collector.api.controller;

import com.arcone.biopro.exception.collector.api.dto.ErrorResponse;
import com.arcone.biopro.exception.collector.api.dto.ExceptionDetailResponse;
import com.arcone.biopro.exception.collector.api.dto.ExceptionListResponse;
import com.arcone.biopro.exception.collector.api.dto.ExceptionSummaryResponse;
import com.arcone.biopro.exception.collector.api.mapper.ExceptionMapper;
import com.arcone.biopro.exception.collector.application.service.ExceptionQueryService;
import com.arcone.biopro.exception.collector.application.service.PayloadRetrievalService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;

/**
 * REST controller for exception management endpoints.
 * Implements requirements US-007, US-008, US-009, US-010 for exception
 * retrieval and search.
 */
@RestController
@RequestMapping("/api/v1/exceptions")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Exception Management", description = "APIs for retrieving and searching interface exceptions")
public class ExceptionController {

        private final ExceptionQueryService exceptionQueryService;
        private final PayloadRetrievalService payloadRetrievalService;
        private final ExceptionMapper exceptionMapper;

        /**
         * Retrieves exceptions with filtering support.
         * Implements requirement US-007 for exception listing with filters.
         */
        @GetMapping
        @Operation(summary = "List exceptions with filtering", description = "Retrieves a list of exceptions with optional filtering by interface type, status, severity, customer, and date range")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved exceptions", content = @Content(schema = @Schema(implementation = ExceptionListResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<List<ExceptionListResponse>> listExceptions(
                        @Parameter(description = "Filter by interface type") @RequestParam(name = "interfaceType", required = false) InterfaceType interfaceType,

                        @Parameter(description = "Filter by exception status") @RequestParam(name = "status", required = false) ExceptionStatus status,

                        @Parameter(description = "Filter by exception severity") @RequestParam(name = "severity", required = false) ExceptionSeverity severity,

                        @Parameter(description = "Filter by customer ID") @RequestParam(name = "customerId", required = false) String customerId,

                        @Parameter(description = "Filter by start date (ISO 8601 format)") @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fromDate,

                        @Parameter(description = "Filter by end date (ISO 8601 format)") @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime toDate,

                        @Parameter(description = "Sort criteria (format: field,direction)") @RequestParam(name = "sort", defaultValue = "timestamp,desc") String sort) {

                log.info(
                                "Listing exceptions with filters - interfaceType: {}, status: {}, severity: {}, customerId: {}, fromDate: {}, toDate: {}",
                                interfaceType, status, severity, customerId, fromDate, toDate);

                // Parse sort parameter
                Sort sortObj = createSort(sort);

                // Query exceptions with filters
                List<InterfaceException> exceptions = exceptionQueryService.findExceptionsWithFilters(
                                interfaceType, status, severity, customerId, fromDate, toDate, sortObj);

                // Map to response DTOs
                List<ExceptionListResponse> response = exceptionMapper.toListResponse(exceptions);

                log.info("Retrieved {} exceptions", response.size());

                return ResponseEntity.ok(response);
        }

        /**
         * Retrieves detailed exception information by transaction ID.
         * Implements requirement US-008 for detailed exception retrieval.
         */
        @GetMapping("/{transactionId}")
        @Operation(summary = "Get exception details", description = "Retrieves detailed information for a specific exception including retry history and related exceptions")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved exception details", content = @Content(schema = @Schema(implementation = ExceptionDetailResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Exception not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<ExceptionDetailResponse> getExceptionDetails(
                        @Parameter(description = "Unique transaction identifier", required = true) @PathVariable String transactionId,

                        @Parameter(description = "Whether to include original payload from source system") @RequestParam(defaultValue = "false") Boolean includePayload) {

                log.info("Getting exception details for transaction: {}, includePayload: {}", transactionId,
                                includePayload);

                // Find the exception
                Optional<InterfaceException> exceptionOpt = exceptionQueryService
                                .findExceptionByTransactionId(transactionId);

                if (exceptionOpt.isEmpty()) {
                        log.warn("Exception not found for transaction: {}", transactionId);
                        return ResponseEntity.notFound().build();
                }

                InterfaceException exception = exceptionOpt.get();

                // Map to detail response
                ExceptionDetailResponse response = exceptionMapper.toDetailResponse(exception);

                // Include original payload if requested
                if (includePayload) {
                        Object originalPayload = payloadRetrievalService.getOriginalPayload(
                                        transactionId, exception.getInterfaceType().name());
                        response.setOriginalPayload(originalPayload);
                }

                // Include related exceptions for the same customer
                if (exception.getCustomerId() != null) {
                        Sort relatedSort = Sort.by("timestamp").descending();
                        List<InterfaceException> relatedExceptions = exceptionQueryService
                                        .findRelatedExceptionsByCustomer(
                                                        exception.getCustomerId(), transactionId, relatedSort, 5);
                        response.setRelatedExceptions(exceptionMapper.toListResponse(relatedExceptions));
                }

                log.info("Retrieved exception details for transaction: {}", transactionId);
                return ResponseEntity.ok(response);
        }

        /**
         * Performs full-text search across exception fields.
         * Implements requirement US-009 for text-based exception search.
         */
        @GetMapping("/search")
        @Operation(summary = "Search exceptions by text", description = "Performs full-text search across exception fields (reason, external ID, operation)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully performed search", content = @Content(schema = @Schema(implementation = ExceptionListResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid search parameters", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<List<ExceptionListResponse>> searchExceptions(
                        @Parameter(description = "Search query string", required = true) @RequestParam @NotBlank String query,

                        @Parameter(description = "Fields to search in (exceptionReason, externalId, operation)") @RequestParam(defaultValue = "exceptionReason") List<String> fields,

                        @Parameter(description = "Sort criteria (format: field,direction)") @RequestParam(defaultValue = "timestamp,desc") String sort) {

                log.info("Searching exceptions with query: '{}', fields: {}", query, fields);

                // Validate search fields
                List<String> validFields = Arrays.asList("exceptionReason", "externalId", "operation");
                List<String> fieldsToSearch = fields.stream()
                                .filter(validFields::contains)
                                .toList();

                if (fieldsToSearch.isEmpty()) {
                        log.warn("No valid search fields provided, defaulting to exceptionReason");
                        fieldsToSearch = List.of("exceptionReason");
                }

                // Parse sort parameter
                Sort sortObj = createSort(sort);

                // Perform search
                List<InterfaceException> searchResults = exceptionQueryService.searchExceptions(
                                query, fieldsToSearch, sortObj);

                // Map to response DTOs
                List<ExceptionListResponse> response = exceptionMapper.toListResponse(searchResults);

                log.info("Search returned {} results for query: '{}'", response.size(), query);

                return ResponseEntity.ok(response);
        }

        /**
         * Retrieves aggregated exception statistics.
         * Implements requirement US-010 for exception summary statistics.
         */
        @GetMapping("/summary")
        @Operation(summary = "Get exception summary statistics", description = "Retrieves aggregated exception statistics for the specified time range with optional grouping")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved summary statistics", content = @Content(schema = @Schema(implementation = ExceptionSummaryResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid summary parameters", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<ExceptionSummaryResponse> getExceptionSummary(
                        @Parameter(description = "Time range for statistics (today, week, month, quarter)") @RequestParam(defaultValue = "week") String timeRange,

                        @Parameter(description = "Group statistics by (interfaceType, severity, status)") @RequestParam(required = false) String groupBy) {

                log.info("Getting exception summary for timeRange: {}, groupBy: {}", timeRange, groupBy);

                // Validate time range
                List<String> validTimeRanges = Arrays.asList("today", "week", "month", "quarter");
                if (!validTimeRanges.contains(timeRange.toLowerCase())) {
                        log.warn("Invalid time range: {}, defaulting to 'week'", timeRange);
                        timeRange = "week";
                }

                // Generate summary
                ExceptionSummaryResponse summary = exceptionQueryService.getExceptionSummary(timeRange, groupBy);

                log.info("Generated summary with {} total exceptions for timeRange: {}",
                                summary.getTotalExceptions(), timeRange);

                return ResponseEntity.ok(summary);
        }

        /**
         * Creates a Sort object from request parameters.
         *
         * @param sort the sort criteria
         * @return the Sort object
         */
        private Sort createSort(String sort) {
                // Parse sort parameter (format: field,direction)
                String[] sortParts = sort.split(",");
                String sortField = sortParts[0];
                Sort.Direction sortDirection = sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1])
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;

                return Sort.by(sortDirection, sortField);
        }
}