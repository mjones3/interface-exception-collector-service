package com.arcone.biopro.exception.collector.domain.enums;

/**
 * Enumeration representing the method used to resolve an exception.
 * Used to track how exceptions were resolved for reporting and analysis.
 */
public enum ResolutionMethod {
    RETRY_SUCCESS,
    MANUAL_RESOLUTION,
    CUSTOMER_RESOLVED,
    AUTOMATED
}