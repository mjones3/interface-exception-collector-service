package com.arcone.biopro.exception.collector.infrastructure.repository;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Custom repository interface for complex filtering operations.
 * Provides type-safe filtering methods using Criteria API.
 */
public interface InterfaceExceptionRepositoryCustom {

    /**
     * Find exceptions with multiple optional filters using type-safe Criteria API.
     * This method ensures proper parameter type handling and avoids parameter
     * binding issues.
     * 
     * @param interfaceType optional interface type filter
     * @param status        optional status filter
     * @param severity      optional severity filter
     * @param customerId    optional customer ID filter
     * @param fromDate      optional start date filter
     * @param toDate        optional end date filter
     * @param pageable      pagination and sorting parameters
     * @return Page of exceptions matching the specified filters
     */
    Page<InterfaceException> findWithFiltersTypeSafe(
            InterfaceType interfaceType,
            ExceptionStatus status,
            ExceptionSeverity severity,
            String customerId,
            OffsetDateTime fromDate,
            OffsetDateTime toDate,
            Pageable pageable);

    /**
     * Find exceptions with multiple optional filters using type-safe Criteria API.
     * Returns all matching results without pagination.
     * 
     * @param interfaceType optional interface type filter
     * @param status        optional status filter
     * @param severity      optional severity filter
     * @param customerId    optional customer ID filter
     * @param fromDate      optional start date filter
     * @param toDate        optional end date filter
     * @param sort          sorting parameters
     * @return List of exceptions matching the specified filters
     */
    List<InterfaceException> findWithFiltersTypeSafe(
            InterfaceType interfaceType,
            ExceptionStatus status,
            ExceptionSeverity severity,
            String customerId,
            OffsetDateTime fromDate,
            OffsetDateTime toDate,
            Sort sort);

    /**
     * Search exceptions in specified fields without pagination.
     * 
     * @param searchQuery  the search query string
     * @param searchFields list of fields to search in
     * @param sort         sorting parameters
     * @return List of exceptions matching the search query
     */
    List<InterfaceException> searchInFields(
            String searchQuery,
            List<String> searchFields,
            Sort sort);

    /**
     * Find related exceptions for a customer without pagination.
     * 
     * @param customerId           the customer ID
     * @param excludeTransactionId transaction ID to exclude from results
     * @param sort                 sorting parameters
     * @param limit                maximum number of results to return
     * @return List of related exceptions for the customer
     */
    List<InterfaceException> findRelatedExceptionsByCustomer(
            String customerId,
            String excludeTransactionId,
            Sort sort,
            int limit);
}