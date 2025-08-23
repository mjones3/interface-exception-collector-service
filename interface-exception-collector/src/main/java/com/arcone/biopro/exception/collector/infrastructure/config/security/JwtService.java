package com.arcone.biopro.exception.collector.infrastructure.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for JWT token validation and parsing
 */
@Service
@Slf4j
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${app.security.jwt.secret}") String secret) {
        System.out.println("=== JWT SERVICE INITIALIZATION ===");
        System.out.println("JWT Secret being used: '" + secret + "'");
        System.out.println("JWT Secret length: " + secret.length());
        System.out.println(
                "JWT Secret first 10 chars: '" + (secret.length() >= 10 ? secret.substring(0, 10) : secret) + "...'");
        System.out.println("JWT Secret last 10 chars: '..."
                + (secret.length() >= 10 ? secret.substring(secret.length() - 10) : secret) + "'");

        log.info("=== JWT SERVICE INITIALIZATION ===");
        log.info("JWT Secret: '{}'", secret);
        log.info("JWT Secret length: {}", secret.length());
        log.info("JWT Secret first 10 chars: '{}'", secret.length() >= 10 ? secret.substring(0, 10) + "..." : secret);
        log.info("JWT Secret last 10 chars: '{}'",
                secret.length() >= 10 ? "..." + secret.substring(secret.length() - 10) : secret);

        // Use Keys.hmacShaKeyFor() with shorter secret to ensure HmacSHA256
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("Using secret key algorithm: {}", this.secretKey.getAlgorithm());
        System.out.println("Using secret key algorithm: " + this.secretKey.getAlgorithm());
        System.out.println("=====================================");
    }

    /**
     * Validates JWT token and extracts claims with comprehensive input validation
     */
    public Claims validateToken(String token) {
        // Enhanced input validation
        if (token == null) {
            log.warn("[JWT-AUTH] Token validation failed: Token is null");
            throw new InvalidJwtTokenException("JWT token is null", JwtValidationError.TOKEN_MISSING);
        }

        String trimmedToken = token.trim();
        if (trimmedToken.isEmpty()) {
            log.warn("[JWT-AUTH] Token validation failed: Token is empty");
            throw new InvalidJwtTokenException("JWT token is empty", JwtValidationError.TOKEN_MISSING);
        }

        // Validate token format - JWT should have 3 parts separated by dots
        String[] tokenParts = trimmedToken.split("\\.");
        if (tokenParts.length != 3) {
            log.warn("[JWT-AUTH] Token validation failed: Invalid JWT format - expected 3 parts, got {}",
                    tokenParts.length);
            throw new InvalidJwtTokenException("JWT token has invalid format", JwtValidationError.TOKEN_MALFORMED);
        }

        // Validate token length - reasonable bounds
        if (trimmedToken.length() < 50) {
            log.warn("[JWT-AUTH] Token validation failed: Token too short ({})", trimmedToken.length());
            throw new InvalidJwtTokenException("JWT token is too short", JwtValidationError.TOKEN_MALFORMED);
        }

        if (trimmedToken.length() > 4096) {
            log.warn("[JWT-AUTH] Token validation failed: Token too long ({})", trimmedToken.length());
            throw new InvalidJwtTokenException("JWT token is too long", JwtValidationError.TOKEN_MALFORMED);
        }

        try {
            log.debug("[JWT-AUTH] Validating JWT token with length: {}", trimmedToken.length());
            log.debug("[JWT-AUTH] Secret key algorithm: {}", secretKey.getAlgorithm());
            log.debug("[JWT-AUTH] Token parts: header={}, payload={}, signature={}",
                    tokenParts[0].length(), tokenParts[1].length(), tokenParts[2].length());

            // Log token structure for debugging
            try {
                String headerJson = new String(java.util.Base64.getUrlDecoder().decode(tokenParts[0]));
                log.debug("[JWT-AUTH] Token header: {}", headerJson);
            } catch (Exception e) {
                log.debug("[JWT-AUTH] Could not decode token header: {}", e.getMessage());
            }

            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(trimmedToken)
                    .getPayload();

            // Additional validation of claims
            if (claims == null) {
                log.warn("[JWT-AUTH] Token validation failed: Claims are null");
                throw new InvalidJwtTokenException("JWT token claims are null", JwtValidationError.TOKEN_INVALID);
            }

            log.info("[JWT-AUTH] Token validation successful for user: {}", claims.getSubject());
            return claims;

        } catch (ExpiredJwtException e) {
            String subject = e.getClaims() != null ? e.getClaims().getSubject() : "unknown";
            Date expiration = e.getClaims() != null ? e.getClaims().getExpiration() : null;
            log.warn("[JWT-AUTH] Token validation failed: Token expired at {} for user: {}", expiration, subject);
            throw new InvalidJwtTokenException("JWT token has expired", JwtValidationError.TOKEN_EXPIRED, e);

        } catch (SignatureException e) {
            log.error("[JWT-AUTH] ❌ SIGNATURE VALIDATION FAILED ❌");
            log.error("[JWT-AUTH] Token signature does not match expected signature");
            log.error("[JWT-AUTH] This usually means:");
            log.error("[JWT-AUTH]   1. Token was signed with a different secret");
            log.error("[JWT-AUTH]   2. Token was modified after signing");
            log.error("[JWT-AUTH]   3. Secret key mismatch between token generator and validator");
            log.error("[JWT-AUTH] Error details: {}", e.getMessage());
            log.error("[JWT-AUTH] Token length: {}", trimmedToken.length());
            log.error("[JWT-AUTH] Expected secret algorithm: {}", secretKey.getAlgorithm());
            throw new InvalidJwtTokenException("JWT token signature is invalid", JwtValidationError.INVALID_SIGNATURE,
                    e);

        } catch (MalformedJwtException e) {
            log.warn("[JWT-AUTH] Token validation failed: Malformed token - {}", e.getMessage());
            throw new InvalidJwtTokenException("JWT token is malformed", JwtValidationError.TOKEN_MALFORMED, e);

        } catch (JwtException e) {
            log.warn("[JWT-AUTH] Token validation failed: JWT processing error - {} - {}",
                    e.getClass().getSimpleName(), e.getMessage());
            throw new InvalidJwtTokenException("JWT token processing failed", JwtValidationError.TOKEN_INVALID, e);

        } catch (InvalidJwtTokenException e) {
            // Re-throw our custom exceptions
            throw e;

        } catch (Exception e) {
            log.error("[JWT-AUTH] Token validation failed: Unexpected error - {} - {}",
                    e.getClass().getSimpleName(), e.getMessage());
            throw new InvalidJwtTokenException("Unexpected error during JWT validation",
                    JwtValidationError.VALIDATION_ERROR, e);
        }
    }

    /**
     * Extracts username from JWT claims with enhanced null safety
     */
    public String extractUsername(Claims claims) {
        if (claims == null) {
            log.warn("[JWT-AUTH] Cannot extract username: Claims are null");
            return null;
        }

        try {
            String username = claims.getSubject();
            if (username == null) {
                log.warn("[JWT-AUTH] Cannot extract username: Subject claim is null");
                return null;
            }

            String trimmedUsername = username.trim();
            if (trimmedUsername.isEmpty()) {
                log.warn("[JWT-AUTH] Cannot extract username: Subject claim is empty");
                return null;
            }

            // Validate username format - basic sanity checks
            if (trimmedUsername.length() > 255) {
                log.warn("[JWT-AUTH] Cannot extract username: Subject claim too long ({})", trimmedUsername.length());
                return null;
            }

            log.debug("[JWT-AUTH] Extracted username: {}", trimmedUsername);
            return trimmedUsername;

        } catch (Exception e) {
            log.warn("[JWT-AUTH] Error extracting username from claims: {} - {}",
                    e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * Extracts authorities from JWT claims with enhanced validation and null safety
     */
    public Collection<GrantedAuthority> extractAuthorities(Claims claims) {
        if (claims == null) {
            log.warn("[JWT-AUTH] Cannot extract authorities: Claims are null");
            return List.of();
        }

        try {
            String subject = claims.getSubject();
            Object rolesObject = claims.get("roles");

            if (rolesObject == null) {
                log.debug("[JWT-AUTH] No roles claim found in token for user: {}", subject);
                return List.of();
            }

            // Handle different types of roles claim (List, String, etc.)
            List<String> roles;
            if (rolesObject instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> rolesList = (List<String>) rolesObject;
                roles = rolesList;
            } else if (rolesObject instanceof String) {
                // Handle single role as string
                roles = List.of((String) rolesObject);
            } else {
                log.warn("[JWT-AUTH] Invalid roles claim type: {} for user: {}",
                        rolesObject.getClass().getSimpleName(), subject);
                return List.of();
            }

            if (roles.isEmpty()) {
                log.debug("[JWT-AUTH] Empty roles list in token for user: {}", subject);
                return List.of();
            }

            Collection<GrantedAuthority> authorities = roles.stream()
                    .filter(role -> role != null && !role.trim().isEmpty())
                    .map(role -> {
                        String trimmedRole = role.trim();
                        // Validate role format
                        if (trimmedRole.length() > 50) {
                            log.warn("[JWT-AUTH] Role name too long, truncating: {}", trimmedRole);
                            trimmedRole = trimmedRole.substring(0, 50);
                        }
                        // Ensure role follows expected format
                        if (!trimmedRole.matches("^[A-Z_]+$")) {
                            log.debug("[JWT-AUTH] Converting role to uppercase: {}", trimmedRole);
                            trimmedRole = trimmedRole.toUpperCase().replaceAll("[^A-Z_]", "_");
                        }
                        return new SimpleGrantedAuthority("ROLE_" + trimmedRole);
                    })
                    .collect(Collectors.toList());

            log.info("[JWT-AUTH] Extracted {} authorities for user: {} - authorities: {}",
                    authorities.size(), subject, authorities.stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList()));
            return authorities;

        } catch (ClassCastException e) {
            log.warn("[JWT-AUTH] Error casting roles claim for user: {} - {}",
                    claims.getSubject(), e.getMessage());
            return List.of();
        } catch (Exception e) {
            log.warn("[JWT-AUTH] Error extracting authorities for user: {} - {} - {}",
                    claims.getSubject(), e.getClass().getSimpleName(), e.getMessage());
            return List.of();
        }
    }

    /**
     * Checks if token is expired with enhanced null safety and validation
     */
    public boolean isTokenExpired(Claims claims) {
        if (claims == null) {
            log.warn("[JWT-AUTH] Cannot check expiration: Claims are null");
            return true;
        }

        try {
            String subject = claims.getSubject();
            Date expiration = claims.getExpiration();

            if (expiration == null) {
                log.warn("[JWT-AUTH] Cannot check expiration: Expiration claim is null for user: {}", subject);
                return true;
            }

            Date now = new Date();
            boolean expired = expiration.before(now);

            if (expired) {
                log.warn("[JWT-AUTH] Token expired at {} for user: {} (current time: {})",
                        expiration, subject, now);
            } else {
                long timeUntilExpiry = expiration.getTime() - now.getTime();
                log.debug("[JWT-AUTH] Token valid until {} for user: {} ({} ms remaining)",
                        expiration, subject, timeUntilExpiry);
            }

            return expired;

        } catch (Exception e) {
            log.warn("[JWT-AUTH] Error checking token expiration for user: {} - {} - {}",
                    claims.getSubject(), e.getClass().getSimpleName(), e.getMessage());
            // If we can't determine expiration, assume expired for security
            return true;
        }
    }

    /**
     * Comprehensive token validation that combines all validation checks
     * This method provides a single entry point for complete token validation
     */
    public boolean isTokenValid(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.debug("[JWT-AUTH] Token validation failed: Token is null or empty");
            return false;
        }

        try {
            Claims claims = validateToken(token);

            // Check if token is expired
            if (isTokenExpired(claims)) {
                log.debug("[JWT-AUTH] Token validation failed: Token is expired");
                return false;
            }

            // Check if we can extract username
            String username = extractUsername(claims);
            if (username == null) {
                log.debug("[JWT-AUTH] Token validation failed: Cannot extract username");
                return false;
            }

            log.debug("[JWT-AUTH] Token validation successful for user: {}", username);
            return true;

        } catch (InvalidJwtTokenException e) {
            log.debug("[JWT-AUTH] Token validation failed: {} - {}", e.getErrorType(), e.getMessage());
            return false;
        } catch (Exception e) {
            log.debug("[JWT-AUTH] Token validation failed: Unexpected error - {} - {}",
                    e.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }

    /**
     * JWT validation error types for structured error handling
     */
    public enum JwtValidationError {
        TOKEN_MISSING("JWT token is missing"),
        TOKEN_MALFORMED("JWT token is malformed"),
        TOKEN_EXPIRED("JWT token has expired"),
        INVALID_SIGNATURE("JWT token signature is invalid"),
        TOKEN_INVALID("JWT token is invalid"),
        VALIDATION_ERROR("JWT validation error");

        private final String description;

        JwtValidationError(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Custom exception for invalid JWT tokens with structured error information
     */
    public static class InvalidJwtTokenException extends RuntimeException {
        private final JwtValidationError errorType;

        public InvalidJwtTokenException(String message, JwtValidationError errorType) {
            super(message);
            this.errorType = errorType;
        }

        public InvalidJwtTokenException(String message, JwtValidationError errorType, Throwable cause) {
            super(message, cause);
            this.errorType = errorType;
        }

        public JwtValidationError getErrorType() {
            return errorType;
        }
    }
}