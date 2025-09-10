package com.arcone.biopro.exception.collector.api.graphql.security;

/**
 * Exception thrown when a GraphQL query or mutation is not allowed.
 * Used for security enforcement and operation restrictions.
 * 
 * Requirements: 5.3, 5.5
 */
public class QueryNotAllowedException extends RuntimeException {

    private final String operationType;
    private final String reason;

    public QueryNotAllowedException(String operationType, String reason) {
        super(String.format("Operation %s not allowed: %s", operationType, reason));
        this.operationType = operationType;
        this.reason = reason;
    }

    public String getOperationType() {
        return operationType;
    }

    public String getReason() {
        return reason;
    }
}