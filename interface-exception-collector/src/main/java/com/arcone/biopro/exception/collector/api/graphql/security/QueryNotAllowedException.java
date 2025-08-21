package com.arcone.biopro.exception.collector.api.graphql.security;

import graphql.ErrorClassification;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.List;
import java.util.Map;

/**
 * Exception thrown when a GraphQL query is not in the production allowlist.
 * Implements GraphQLError to provide structured error responses.
 */
public class QueryNotAllowedException extends RuntimeException implements GraphQLError {

    private static final String ERROR_CODE = "QUERY_NOT_ALLOWED";

    public QueryNotAllowedException(String message) {
        super(message);
    }

    public QueryNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorClassification getErrorType() {
        return ErrorType.ValidationError;
    }

    @Override
    public Map<String, Object> getExtensions() {
        return Map.of(
                "errorCode", ERROR_CODE,
                "classification", "SECURITY",
                "description", "Query not approved for production use");
    }
}