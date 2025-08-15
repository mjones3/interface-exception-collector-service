package com.arcone.biopro.exception.collector.domain.enums;

/**
 * Enumeration representing the category of an interface exception.
 * Used to classify exceptions based on their root cause type.
 */
public enum ExceptionCategory {
    BUSINESS_RULE,
    VALIDATION,
    SYSTEM_ERROR,
    NETWORK_ERROR,
    TIMEOUT,
    AUTHENTICATION,
    AUTHORIZATION,
    DATA_INTEGRITY,
    EXTERNAL_SERVICE
}