package com.arcone.biopro.exception.collector.api.graphql.exception;

import graphql.ErrorClassification;

/**
 * GraphQL error types for proper error classification and handling.
 * Provides structured error categories for different types of failures.
 */
public enum GraphQLErrorType implements ErrorClassification {

    /**
     * Input validation errors - invalid or malformed input data.
     */
    VALIDATION_ERROR("VALIDATION_ERROR", "Input validation failed"),

    /**
     * Authorization errors - insufficient permissions or access denied.
     */
    AUTHORIZATION_ERROR("AUTHORIZATION_ERROR", "Access denied or insufficient permissions"),

    /**
     * Resource not found errors - requested resource does not exist.
     */
    NOT_FOUND("NOT_FOUND", "Requested resource not found"),

    /**
     * Business rule violations - operation violates business logic.
     */
    BUSINESS_RULE_ERROR("BUSINESS_RULE_ERROR", "Business rule violation"),

    /**
     * External service errors - failures in external service calls.
     */
    EXTERNAL_SERVICE_ERROR("EXTERNAL_SERVICE_ERROR", "External service unavailable or failed"),

    /**
     * Rate limiting errors - too many requests from client.
     */
    RATE_LIMIT_ERROR("RATE_LIMIT_ERROR", "Rate limit exceeded"),

    /**
     * Query complexity errors - query too complex or deep.
     */
    QUERY_COMPLEXITY_ERROR("QUERY_COMPLEXITY_ERROR", "Query complexity limit exceeded"),

    /**
     * Timeout errors - operation took too long to complete.
     */
    TIMEOUT_ERROR("TIMEOUT_ERROR", "Operation timeout"),

    /**
     * Concurrent modification errors - optimistic locking failures.
     */
    CONCURRENT_MODIFICATION_ERROR("CONCURRENT_MODIFICATION_ERROR", "Resource was modified by another user"),

    /**
     * Internal server errors - unexpected system failures.
     */
    INTERNAL_ERROR("INTERNAL_ERROR", "Internal server error");

    private final String code;
    private final String description;

    GraphQLErrorType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Gets the error code for this error type.
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the description for this error type.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Determines if this error type represents a client error (4xx equivalent).
     */
    public boolean isClientError() {
        return this == VALIDATION_ERROR ||
                this == AUTHORIZATION_ERROR ||
                this == NOT_FOUND ||
                this == BUSINESS_RULE_ERROR ||
                this == RATE_LIMIT_ERROR ||
                this == QUERY_COMPLEXITY_ERROR;
    }

    /**
     * Determines if this error type represents a server error (5xx equivalent).
     */
    public boolean isServerError() {
        return this == EXTERNAL_SERVICE_ERROR ||
                this == TIMEOUT_ERROR ||
                this == INTERNAL_ERROR;
    }

    /**
     * Gets the HTTP status code equivalent for this error type.
     */
    public int getHttpStatusCode() {
        switch (this) {
            case VALIDATION_ERROR:
            case BUSINESS_RULE_ERROR:
            case QUERY_COMPLEXITY_ERROR:
                return 400; // Bad Request
            case AUTHORIZATION_ERROR:
                return 403; // Forbidden
            case NOT_FOUND:
                return 404; // Not Found
            case CONCURRENT_MODIFICATION_ERROR:
                return 409; // Conflict
            case RATE_LIMIT_ERROR:
                return 429; // Too Many Requests
            case EXTERNAL_SERVICE_ERROR:
            case INTERNAL_ERROR:
                return 500; // Internal Server Error
            case TIMEOUT_ERROR:
                return 504; // Gateway Timeout
            default:
                return 500;
        }
    }

    @Override
    public String toString() {
        return code;
    }
}