package com.arcone.biopro.exception.collector.domain.enums;

/**
 * Enumeration representing the lifecycle status of an interface exception.
 * Tracks the progression of exception handling from initial capture to
 * resolution.
 */
public enum ExceptionStatus {
    NEW,
    ACKNOWLEDGED,
    RETRIED_SUCCESS,
    RETRIED_FAILED,
    ESCALATED,
    RESOLVED,
    CLOSED,
    FAILED,
    RETRY_IN_PROGRESS,
    RETRY_FAILED,
    OPEN
}