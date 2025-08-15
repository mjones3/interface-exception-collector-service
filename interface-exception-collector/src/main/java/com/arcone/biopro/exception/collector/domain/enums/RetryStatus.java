package com.arcone.biopro.exception.collector.domain.enums;

/**
 * Enumeration representing the status of a retry attempt.
 * Tracks the lifecycle of individual retry operations.
 */
public enum RetryStatus {
    PENDING,
    SUCCESS,
    FAILED
}