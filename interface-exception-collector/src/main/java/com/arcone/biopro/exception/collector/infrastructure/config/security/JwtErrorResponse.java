package com.arcone.biopro.exception.collector.infrastructure.config.security;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Standardized error response for JWT authentication and authorization
 * failures.
 * Provides consistent error information with appropriate detail levels.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "JWT authentication/authorization error response")
public class JwtErrorResponse {

    @Schema(description = "HTTP status code", example = "401")
    private Integer status;

    @Schema(description = "Error type or category", example = "JWT_TOKEN_EXPIRED")
    private String error;

    @Schema(description = "Human-readable error message", example = "JWT token has expired. Please obtain a new token and retry your request.")
    private String message;

    @Schema(description = "API path where the error occurred", example = "/api/v1/exceptions")
    private String path;

    @Schema(description = "When the error occurred", example = "2025-08-19T10:30:00.123Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private String timestamp;

    @Schema(description = "Additional technical details (sanitized)", example = "Token expired at 2025-08-19T09:30:00Z")
    private String details;

    @Schema(description = "Suggested action for the client", example = "Please obtain a new JWT token and retry your request")
    private String suggestion;

    /**
     * Create error response for missing JWT token
     */
    public static JwtErrorResponse tokenMissing(String path) {
        return JwtErrorResponse.builder()
                .status(401)
                .error("JWT_TOKEN_MISSING")
                .message(
                        "JWT token is required to access this endpoint. Please provide a valid Bearer token in the Authorization header.")
                .path(path)
                .timestamp(Instant.now().toString())
                .suggestion("Add 'Authorization: Bearer <your-jwt-token>' header to your request")
                .build();
    }

    /**
     * Create error response for malformed JWT token
     */
    public static JwtErrorResponse tokenMalformed(String path, String details) {
        return JwtErrorResponse.builder()
                .status(401)
                .error("JWT_TOKEN_MALFORMED")
                .message(
                        "JWT token format is invalid. Please ensure the token is properly formatted and not corrupted.")
                .path(path)
                .timestamp(Instant.now().toString())
                .details(details)
                .suggestion("Verify your JWT token format and ensure it hasn't been truncated or modified")
                .build();
    }

    /**
     * Create error response for expired JWT token
     */
    public static JwtErrorResponse tokenExpired(String path, String details) {
        return JwtErrorResponse.builder()
                .status(401)
                .error("JWT_TOKEN_EXPIRED")
                .message("JWT token has expired. Please obtain a new token and retry your request.")
                .path(path)
                .timestamp(Instant.now().toString())
                .details(details)
                .suggestion("Obtain a new JWT token from your authentication provider")
                .build();
    }

    /**
     * Create error response for invalid JWT signature
     */
    public static JwtErrorResponse invalidSignature(String path) {
        return JwtErrorResponse.builder()
                .status(401)
                .error("JWT_TOKEN_INVALID")
                .message(
                        "JWT token signature is invalid. The token may have been tampered with or signed with a different key.")
                .path(path)
                .timestamp(Instant.now().toString())
                .suggestion("Ensure you are using a valid JWT token from the correct authentication provider")
                .build();
    }

    /**
     * Create error response for invalid JWT token
     */
    public static JwtErrorResponse tokenInvalid(String path, String details) {
        return JwtErrorResponse.builder()
                .status(401)
                .error("JWT_TOKEN_INVALID")
                .message("JWT token is invalid. Please verify the token format and content.")
                .path(path)
                .timestamp(Instant.now().toString())
                .details(details)
                .suggestion("Verify your JWT token is valid and properly formatted")
                .build();
    }

    /**
     * Create error response for insufficient privileges
     */
    public static JwtErrorResponse insufficientPrivileges(String path, String requiredRoles, String currentRoles) {
        String message = String.format(
                "Insufficient privileges to access this resource. Required roles: %s. Current roles: %s.",
                requiredRoles, currentRoles);

        return JwtErrorResponse.builder()
                .status(403)
                .error("INSUFFICIENT_PRIVILEGES")
                .message(message)
                .path(path)
                .timestamp(Instant.now().toString())
                .suggestion("Contact your administrator to request the required permissions")
                .build();
    }

    /**
     * Create error response for general authentication failure
     */
    public static JwtErrorResponse authenticationFailed(String path, String details) {
        return JwtErrorResponse.builder()
                .status(401)
                .error("AUTHENTICATION_FAILED")
                .message("Authentication failed. Please ensure you have provided a valid JWT token.")
                .path(path)
                .timestamp(Instant.now().toString())
                .details(details)
                .suggestion("Verify your JWT token is valid and properly formatted")
                .build();
    }

    /**
     * Create error response for validation errors
     */
    public static JwtErrorResponse validationError(String path, String details) {
        return JwtErrorResponse.builder()
                .status(401)
                .error("JWT_VALIDATION_ERROR")
                .message("JWT token validation failed due to an unexpected error. Please try again or contact support.")
                .path(path)
                .timestamp(Instant.now().toString())
                .details(details)
                .suggestion("Try again with a fresh JWT token, or contact support if the problem persists")
                .build();
    }
}