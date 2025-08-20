package com.arcone.biopro.exception.collector.infrastructure.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * JWT Authentication Entry Point for handling authentication failures
 * Returns structured error responses for unauthorized access attempts
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        log.warn("[JWT-AUTH] Unauthorized access attempt to: {} {} - {}",
                method, requestPath, authException.getMessage());

        // Determine the specific error type based on the request context
        JwtErrorResponse errorResponse = createErrorResponse(request, authException);

        response.setStatus(errorResponse.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);

        log.debug("[JWT-AUTH] Sent error response: {} ({})", errorResponse.getError(), errorResponse.getStatus());
    }

    /**
     * Create structured error response based on request context
     */
    private JwtErrorResponse createErrorResponse(HttpServletRequest request, AuthenticationException authException) {
        String path = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        // Check if JWT filter stored specific error information
        JwtService.JwtValidationError jwtError = (JwtService.JwtValidationError) request.getAttribute("jwt.error.type");
        String jwtErrorMessage = (String) request.getAttribute("jwt.error.message");

        if (jwtError != null) {
            // Use specific error information from JWT validation
            return createErrorResponseForValidationError(jwtError, jwtErrorMessage, path);
        } else if (authHeader == null || authHeader.trim().isEmpty()) {
            return JwtErrorResponse.tokenMissing(path);
        } else if (!authHeader.startsWith("Bearer ")) {
            String details = String.format("Received header format: '%s'",
                    authHeader.length() > 20 ? authHeader.substring(0, 20) + "..." : authHeader);
            return JwtErrorResponse.tokenMalformed(path, details);
        } else {
            String token = authHeader.substring(7).trim();
            if (token.isEmpty()) {
                return JwtErrorResponse.tokenMissing(path);
            } else {
                return JwtErrorResponse.tokenInvalid(path, "Token validation failed during authentication");
            }
        }
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

}