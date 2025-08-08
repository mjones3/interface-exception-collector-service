package com.arcone.biopro.exception.collector.domain.enums;

/**
 * Enumeration representing the reason for generating a critical exception
 * alert.
 * Used to categorize alerts based on their triggering conditions.
 */
public enum AlertReason {
    CRITICAL_SEVERITY,
    MULTIPLE_RETRIES_FAILED,
    SYSTEM_ERROR,
    CUSTOMER_IMPACT
}