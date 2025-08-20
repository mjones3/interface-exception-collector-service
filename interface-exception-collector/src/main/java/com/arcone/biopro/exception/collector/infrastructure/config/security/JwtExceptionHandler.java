package com.arcone.biopro.exception.collector.infrastructure.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

/**
 * Global exception handler for JWT authentication and authorization errors
 * Provides consistent error responses across the application
 */
@RestControllerAdvice
@Slf4j
public class JwtExceptionHandler {

    /**
     * Handle JWT service validation exceptions
     */
    @ExceptionHandler(JwtService.InvalidJwtTokenException.class)
    public ResponseEntity<JwtErrorResponse> handleInvalidJwtToken(
            JwtService.InvalidJwtTokenException ex, WebRequest request) {

        String path = request.getDescription(false).replace("uri=", "");

        log.warn("[JWT-AUTH] JWT validation exception: {} - {}", ex.getErrorType(), ex.getMessage());

        JwtErrorResponse errorResponse = createErrorResponseForValidationError(ex.getErrorType(), ex.getMessage(),
                path);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Create error response for specific JWT validation errors
     */
    private JwtErrorResponse createErrorResponseForValidationError(JwtService.JwtValidationError validationError,
            String errorMessage, String path) {
        String sanitizedDetails = sanitizeErrorDetails(errorMessage);

        return switch (validationError) {
            case TOKEN_MISSING -> JwtErrorResponse.tokenMissing(path);
            case TOKEN_MALFORMED -> JwtErrorResponse.tokenMalformed(path, sanitizedDetails);
            case TOKEN_EXPIRED -> JwtErrorResponse.tokenExpired(path, sanitizedDetails);
            case INVALID_SIGNATURE -> JwtErrorResponse.invalidSignature(path);
            case TOKEN_INVALID -> JwtErrorResponse.tokenInvalid(path, sanitizedDetails);
            case VALIDATION_ERROR -> JwtErrorResponse.validationError(path, sanitizedDetails);
        };
    }

    /**
     * Sanitize error details to avoid exposing sensitive information
     */
    private String sanitizeErrorDetails(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }

        // Remove any potential token content or sensitive data
        String sanitized = errorMessage
                .replaceAll("JWT signature does not match locally computed signature.*",
                        "JWT signature validation failed")
                .replaceAll("eyJ[A-Za-z0-9+/=]+", "[JWT_TOKEN]") // Replace JWT tokens with placeholder
                .replaceAll("Bearer [A-Za-z0-9+/=]+", "Bearer [JWT_TOKEN]"); // Replace Bearer tokens

        return sanitized.length() > 200 ? sanitized.substring(0, 200) + "..." : sanitized;
    }

    /**
     * Handle general authentication exceptions
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<JwtErrorResponse> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {

        String path = request.getDescription(false).replace("uri=", "");

        log.warn("[JWT-AUTH] Authentication exception: {}", ex.getMessage());

        String details = null;

        // Provide more specific messages based on exception type
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Full authentication is required")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(JwtErrorResponse.tokenMissing(path));
            } else if (ex.getMessage().contains("Access is denied")) {
                details = sanitizeErrorDetails(ex.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(JwtErrorResponse.authenticationFailed(path, details));
            } else {
                details = sanitizeErrorDetails(ex.getMessage());
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(JwtErrorResponse.authenticationFailed(path, details));
    }

    /**
     * Handle bad credentials exceptions
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<JwtErrorResponse> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {

        String path = request.getDescription(false).replace("uri=", "");

        log.warn("[JWT-AUTH] Bad credentials: {}", ex.getMessage());

        String details = sanitizeErrorDetails(ex.getMessage());

        // Provide more context based on the specific error
        if (ex.getMessage() != null && ex.getMessage().contains("JWT")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(JwtErrorResponse.tokenInvalid(path, details));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(JwtErrorResponse.authenticationFailed(path, details));
    }

    /**
     * Handle access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<JwtErrorResponse> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {

        String path = request.getDescription(false).replace("uri=", "");

        log.warn("[JWT-AUTH] Access denied: {}", ex.getMessage());

        String requiredRoles;
        String currentRoles = "unknown";

        // Determine required roles based on the path
        if (path.contains("/api/v1/exceptions")) {
            requiredRoles = "ADMIN or OPERATOR";
        } else if (path.contains("/api/v1/management")) {
            requiredRoles = "ADMIN";
        } else if (path.contains("/api/v1/retry")) {
            requiredRoles = "ADMIN or OPERATOR";
        } else {
            requiredRoles = "appropriate role";
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(JwtErrorResponse.insufficientPrivileges(path, requiredRoles, currentRoles));
    }

}