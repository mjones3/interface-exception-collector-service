package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.api.dto.ExceptionSummaryResponse;
import com.arcone.biopro.exception.collector.config.CacheConfig;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for querying exception data for API endpoints.
 * Implements requirements US-007, US-008, US-009, US-010 for exception
 * retrieval and search.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExceptionQueryService {

    private final InterfaceExceptionRepository exceptionRepository;

    /**
     * Retrieves exceptions with filtering support.
     * Implements requirement US-007 for exception listing with filters.
     *
     * @param interfaceType optional interface type filter
     * @param status        optional status filter
     * @param severity      optional severity filter
     * @param customerId    optional customer ID filter
     * @param fromDate      optional start date filter
     * @param toDate        optional end date filter
     * @param sort          sorting parameters
     * @return list of exceptions matching the filters
     */
    public List<InterfaceException> findExceptionsWithFilters(
            InterfaceType interfaceType,
            ExceptionStatus status,
            ExceptionSeverity severity,
            String customerId,
            OffsetDateTime fromDate,
            OffsetDateTime toDate,
            Sort sort) {

        log.debug(
                "Finding exceptions with filters: interfaceType={}, status={}, severity={}, customerId={}, fromDate={}, toDate={}",
                interfaceType, status, severity, customerId, fromDate, toDate);

        return exceptionRepository.findWithFiltersTypeSafe(
                interfaceType,
                status,
                severity,
                customerId, fromDate, toDate, sort);
    }

    /**
     * Retrieves detailed exception information by transaction ID.
     * Implements requirement US-008 for detailed exception retrieval.
     * Results are cached for 10 minutes to improve performance.
     *
     * @param transactionId the unique transaction identifier
     * @return optional containing the exception if found
     */
    @Cacheable(value = CacheConfig.EXCEPTION_DETAILS_CACHE, key = "#transactionId", condition = "#transactionId != null")
    public Optional<InterfaceException> findExceptionByTransactionId(String transactionId) {
        log.debug("Finding exception by transaction ID: {}", transactionId);
        return exceptionRepository.findByTransactionId(transactionId);
    }

    /**
     * Retrieves related exceptions for the same customer.
     * Used to provide context in detailed exception responses.
     * Results are cached for 15 minutes to improve performance.
     *
     * @param customerId           the customer ID
     * @param excludeTransactionId transaction ID to exclude from results
     * @param sort                 sorting parameters
     * @param limit                maximum number of results to return
     * @return list of related exceptions
     */
    @Cacheable(value = CacheConfig.RELATED_EXCEPTIONS_CACHE, key = "#customerId + ':' + #excludeTransactionId + ':' + #limit", condition = "#customerId != null")
    public List<InterfaceException> findRelatedExceptionsByCustomer(
            String customerId, String excludeTransactionId, Sort sort, int limit) {

        log.debug("Finding related exceptions for customer: {}, excluding: {}", customerId, excludeTransactionId);
        return exceptionRepository.findRelatedExceptionsByCustomer(customerId, excludeTransactionId, sort, limit);
    }

    /**
     * Performs full-text search across exception fields.
     * Implements requirement US-009 for text-based exception search.
     * Results are cached for 5 minutes to improve performance.
     *
     * @param searchQuery  the search query string
     * @param searchFields list of fields to search in (exceptionReason, externalId,
     *                     operation)
     * @param sort         sorting parameters
     * @return list of exceptions matching the search query
     */
    @Cacheable(value = CacheConfig.SEARCH_RESULTS_CACHE, key = "#searchQuery + ':' + #searchFields.toString()", condition = "#searchQuery != null and !#searchQuery.isEmpty()")
    public List<InterfaceException> searchExceptions(String searchQuery, List<String> searchFields, Sort sort) {
        log.debug("Searching exceptions with query: '{}' in fields: {}", searchQuery, searchFields);

        // Validate search fields
        List<String> validFields = Arrays.asList("exceptionReason", "externalId", "operation");
        List<String> fieldsToSearch = searchFields.stream()
                .filter(validFields::contains)
                .collect(Collectors.toList());

        if (fieldsToSearch.isEmpty()) {
            fieldsToSearch = List.of("exceptionReason"); // Default to searching exception reason
        }

        return exceptionRepository.searchInFields(searchQuery, fieldsToSearch, sort);
    }

    /**
     * Generates aggregated exception statistics for the specified time range.
     * Implements requirement US-010 for exception summary statistics.
     * Results are cached for 2 minutes to balance freshness and performance.
     *
     * @param timeRange the time range for statistics (today, week, month, quarter)
     * @param groupBy   optional grouping parameter (interfaceType, severity,
     *                  status)
     * @return aggregated exception statistics
     */
    @Cacheable(value = CacheConfig.EXCEPTION_SUMMARY_CACHE, key = "#timeRange + ':' + (#groupBy != null ? #groupBy : 'none')", condition = "#timeRange != null")
    public ExceptionSummaryResponse getExceptionSummary(String timeRange, String groupBy) {
        log.debug("Generating exception summary for timeRange: {}, groupBy: {}", timeRange, groupBy);

        // Calculate date range based on timeRange parameter
        OffsetDateTime[] dateRange = calculateDateRange(timeRange);
        OffsetDateTime fromDate = dateRange[0];
        OffsetDateTime toDate = dateRange[1];

        // Get total count for the time range
        long totalExceptions = exceptionRepository.countByTimestampBetween(fromDate, toDate);

        // Build summary response
        ExceptionSummaryResponse.ExceptionSummaryResponseBuilder builder = ExceptionSummaryResponse.builder()
                .totalExceptions(totalExceptions);

        // Add grouping statistics
        builder.byInterfaceType(getCountsByInterfaceType(fromDate, toDate));
        builder.bySeverity(getCountsBySeverity(fromDate, toDate));
        builder.byStatus(getCountsByStatus(fromDate, toDate));

        // Add trend data (daily counts)
        builder.trends(getDailyTrends(fromDate, toDate));

        return builder.build();
    }

    /**
     * Calculates the date range based on the time range parameter.
     *
     * @param timeRange the time range string (today, week, month, quarter)
     * @return array containing [fromDate, toDate]
     */
    private OffsetDateTime[] calculateDateRange(String timeRange) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime fromDate;

        switch (timeRange.toLowerCase()) {
            case "today":
                fromDate = now.toLocalDate().atStartOfDay().atOffset(ZoneOffset.UTC);
                break;
            case "week":
                fromDate = now.minusDays(7);
                break;
            case "month":
                fromDate = now.minusDays(30);
                break;
            case "quarter":
                fromDate = now.minusDays(90);
                break;
            default:
                // Default to last 7 days
                fromDate = now.minusDays(7);
                break;
        }

        return new OffsetDateTime[] { fromDate, now };
    }

    /**
     * Gets exception counts grouped by interface type for the specified date range.
     *
     * @param fromDate start date
     * @param toDate   end date
     * @return map of interface type to count
     */
    private Map<String, Long> getCountsByInterfaceType(OffsetDateTime fromDate, OffsetDateTime toDate) {
        Map<String, Long> counts = new HashMap<>();

        for (InterfaceType type : InterfaceType.values()) {
            // This is a simplified implementation - in a real scenario, you'd want a more
            // efficient query
            List<InterfaceException> exceptions = exceptionRepository.findWithFiltersTypeSafe(
                    type, null, null, null, fromDate, toDate, Sort.unsorted());
            counts.put(type.name(), (long) exceptions.size());
        }

        return counts;
    }

    /**
     * Gets exception counts grouped by severity for the specified date range.
     *
     * @param fromDate start date
     * @param toDate   end date
     * @return map of severity to count
     */
    private Map<String, Long> getCountsBySeverity(OffsetDateTime fromDate, OffsetDateTime toDate) {
        Map<String, Long> counts = new HashMap<>();

        for (ExceptionSeverity severity : ExceptionSeverity.values()) {
            List<InterfaceException> exceptions = exceptionRepository.findWithFiltersTypeSafe(
                    null, null, severity, null, fromDate, toDate, Sort.unsorted());
            counts.put(severity.name(), (long) exceptions.size());
        }

        return counts;
    }

    /**
     * Gets exception counts grouped by status for the specified date range.
     *
     * @param fromDate start date
     * @param toDate   end date
     * @return map of status to count
     */
    private Map<String, Long> getCountsByStatus(OffsetDateTime fromDate, OffsetDateTime toDate) {
        Map<String, Long> counts = new HashMap<>();

        for (ExceptionStatus status : ExceptionStatus.values()) {
            List<InterfaceException> exceptions = exceptionRepository.findWithFiltersTypeSafe(
                    null, status, null, null, fromDate, toDate, Sort.unsorted());
            counts.put(status.name(), (long) exceptions.size());
        }

        return counts;
    }

    /**
     * Gets daily exception counts for trend analysis.
     *
     * @param fromDate start date
     * @param toDate   end date
     * @return list of daily trend data points
     */
    private List<ExceptionSummaryResponse.DailyTrendResponse> getDailyTrends(OffsetDateTime fromDate,
            OffsetDateTime toDate) {
        List<Object[]> dailyCounts = exceptionRepository.getDailyCounts(fromDate, toDate);

        return dailyCounts.stream()
                .map(row -> {
                    // Handle the date conversion properly - JPQL DATE() function can return
                    // different types
                    String dateStr;
                    if (row[0] instanceof java.sql.Date) {
                        dateStr = ((java.sql.Date) row[0]).toLocalDate().toString();
                    } else if (row[0] instanceof LocalDate) {
                        dateStr = ((LocalDate) row[0]).toString();
                    } else {
                        // Fallback - convert to string and parse
                        dateStr = row[0].toString();
                    }

                    return ExceptionSummaryResponse.DailyTrendResponse.builder()
                            .date(dateStr)
                            .count(((Number) row[1]).longValue())
                            .build();
                })
                .collect(Collectors.toList());
    }
}