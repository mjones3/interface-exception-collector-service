package com.arcone.biopro.exception.collector.infrastructure.repository;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

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
}