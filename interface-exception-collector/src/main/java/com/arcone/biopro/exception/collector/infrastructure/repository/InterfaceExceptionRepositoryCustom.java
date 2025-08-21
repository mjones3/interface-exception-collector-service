package com.arcone.biopro.exception.collector.infrastructure.repository;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import org.springframework.data.domain.Sort;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Custom repository interface for complex InterfaceException queries.
 * Provides type-safe filtering and search operations.
 */
public interface InterfaceExceptionRepositoryCustom {

        /**
         * Find exceptions with filters using type-safe parameters.
         * This method handles null parameters properly and provides efficient querying.
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
        List<InterfaceException> findWithFiltersTypeSafe(
                        InterfaceType interfaceType,
                        ExceptionStatus status,
                        ExceptionSeverity severity,
                        String customerId,
                        OffsetDateTime fromDate,
                        OffsetDateTime toDate,
                        Sort sort);

        /**
         * Search exceptions in specified fields with type-safe parameters.
         *
         * @param searchQuery  the search query string
         * @param searchFields list of fields to search in
         * @param sort         sorting parameters
         * @return list of exceptions matching the search query
         */
        List<InterfaceException> searchInFields(
                        String searchQuery,
                        List<String> searchFields,
                        Sort sort);

        /**
         * Find related exceptions by customer with limit.
         *
         * @param customerId           the customer ID
         * @param excludeTransactionId transaction ID to exclude from results
         * @param sort                 sorting parameters
         * @param limit                maximum number of results to return
         * @return list of related exceptions
         */
        List<InterfaceException> findRelatedExceptionsByCustomer(
                        String customerId,
                        String excludeTransactionId,
                        Sort sort,
                        int limit);
}