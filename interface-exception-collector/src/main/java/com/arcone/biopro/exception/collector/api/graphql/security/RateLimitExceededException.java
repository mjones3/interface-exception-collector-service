package com.arcone.biopro.exception.collector.api.graphql.security;

import graphql.ErrorClassification;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.List;
import java.util.Map;

/**
 * Exception thrown when GraphQL rate limits are exceeded.
 * Implements GraphQLError to provide structured error responses.
 */
public class RateLimitExceededException extends RuntimeException implements GraphQLError {

    private static final String ERROR_CODE = "RATE_LIMIT_EXCEEDED";

    public RateLimitExceededException(String message) {
        super(message);
    }

    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorClassification getErrorType() {
        return ErrorType.ExecutionAborted;
    }

    @Override
    public Map<String, Object> getExtensions() {
        return Map.of(
                "errorCode", ERROR_CODE,
                "classification", "RATE_LIMITING",
                "retryAfter", 60 // Suggest retry after 60 seconds
        );
    }
}