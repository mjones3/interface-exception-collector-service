package com.arcone.biopro.exception.collector.config.security;

import com.arcone.biopro.exception.collector.infrastructure.config.security.JwtService;
import com.arcone.biopro.exception.collector.infrastructure.config.security.JwtService.InvalidJwtTokenException;
import com.arcone.biopro.exception.collector.infrastructure.config.security.JwtService.JwtValidationError;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive tests for JWT service covering all validation scenarios and
 * edge cases
 * Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3
 */
@DisplayName("JWT Service Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "mySecretKey1234567890123456789012345678901234567890";
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret);
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate valid token successfully")
        void shouldValidateValidToken() {
            String token = createValidToken("testuser", List.of("OPERATOR", "VIEWER"));

            Claims claims = jwtService.validateToken(token);

            assertThat(claims).isNotNull();
            assertThat(claims.getSubject()).isEqualTo("testuser");
            assertThat(claims.get("roles")).isEqualTo(List.of("OPERATOR", "VIEWER"));
        }

        @Test
        @DisplayName("Should throw exception for null token")
        void shouldThrowExceptionForNullToken() {
            assertThatThrownBy(() -> jwtService.validateToken(null))
                    .isInstanceOf(InvalidJwtTokenException.class)
                    .hasMessageContaining("JWT token is null")
                    .extracting("errorType")
                    .isEqualTo(JwtValidationError.TOKEN_MISSING);
        }

        @Test
        @DisplayName("Should throw exception for empty token")
        void shouldThrowExceptionForEmptyToken() {
            assertThatThrownBy(() -> jwtService.validateToken(""))
                    .isInstanceOf(InvalidJwtTokenException.class)
                    .hasMessageContaining("JWT token is empty")
                    .extracting("errorType")
                    .isEqualTo(JwtValidationError.TOKEN_MISSING);
        }

        @Test
        @DisplayName("Should throw exception for whitespace-only token")
        void shouldThrowExceptionForWhitespaceToken() {
            assertThatThrownBy(() -> jwtService.validateToken("   "))
                    .isInstanceOf(InvalidJwtTokenException.class)
                    .hasMessageContaining("JWT token is empty")
                    .extracting("errorType")
                    .isEqualTo(JwtValidationError.TOKEN_MISSING);
        }

        @Test
        @DisplayName("Should throw exception for malformed token - invalid format")
        void shouldThrowExceptionForMalformedToken() {
            String invalidToken = "invalid.jwt";

            assertThatThrownBy(() -> jwtService.validateToken(invalidToken))
                    .isInstanceOf(InvalidJwtTokenException.class)
                    .hasMessageContaining("JWT token has invalid format")
                    .extracting("errorType")
                    .isEqualTo(JwtValidationError.TOKEN_MALFORMED);
        }

        @Test
        @DisplayName("Should throw exception for token too short")
        void shouldThrowExceptionForTokenTooShort() {
            String shortToken = "a.b.c";

            assertThatThrownBy(() -> jwtService.validateToken(shortToken))
                    .isInstanceOf(InvalidJwtTokenException.class)
                    .hasMessageContaining("JWT token is too short")
                    .extracting("errorType")
                    .isEqualTo(JwtValidationError.TOKEN_MALFORMED);
        }

        @Test
        @DisplayName("Should throw exception for token too long")
        void shouldThrowExceptionForTokenTooLong() {
            String longToken = "a".repeat(4100);

            assertThatThrownBy(() -> jwtService.validateToken(longToken))
                    .isInstanceOf(InvalidJwtTokenException.class)
                    .hasMessageContaining("JWT token is too long")
                    .extracting("errorType")
                    .isEqualTo(JwtValidationError.TOKEN_MALFORMED);
        }

        @Test
        @DisplayName("Should throw exception for expired token")
        void shouldThrowExceptionForExpiredToken() {
            String expiredToken = createExpiredToken("testuser", List.of("OPERATOR"));

            assertThatThrownBy(() -> jwtService.validateToken(expiredToken))
                    .isInstanceOf(InvalidJwtTokenException.class)
                    .hasMessageContaining("JWT token has expired")
                    .extracting("errorType")
                    .isEqualTo(JwtValidationError.TOKEN_EXPIRED);
        }

        @Test
        @DisplayName("Should throw exception for token with invalid signature")
        void shouldThrowExceptionForInvalidSignature() {
            String tokenWithWrongSignature = createTokenWithWrongSignature("testuser", List.of("OPERATOR"));

            assertThatThrownBy(() -> jwtService.validateToken(tokenWithWrongSignature))
                    .isInstanceOf(InvalidJwtTokenException.class)
                    .hasMessageContaining("JWT token signature is invalid")
                    .extracting("errorType")
                    .isEqualTo(JwtValidationError.INVALID_SIGNATURE);
        }

        @Test
        @DisplayName("Should handle token with trimmed whitespace")
        void shouldHandleTokenWithWhitespace() {
            String token = createValidToken("testuser", List.of("OPERATOR"));
            String tokenWithWhitespace = "  " + token + "  ";

            Claims claims = jwtService.validateToken(tokenWithWhitespace);

            assertThat(claims).isNotNull();
            assertThat(claims.getSubject()).isEqualTo("testuser");
        }
    }

    @Nested
    @DisplayName("Username Extraction Tests")
    class UsernameExtractionTests {

        @Test
        @DisplayName("Should extract username from valid claims")
        void shouldExtractUsernameFromClaims() {
            String token = createValidToken("testuser", List.of("OPERATOR"));
            Claims claims = jwtService.validateToken(token);

            String username = jwtService.extractUsername(claims);

            assertThat(username).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should return null for null claims")
        void shouldReturnNullForNullClaims() {
            String username = jwtService.extractUsername(null);

            assertThat(username).isNull();
        }

        @Test
        @DisplayName("Should return null for claims with null subject")
        void shouldReturnNullForNullSubject() {
            Claims claims = Jwts.claims().build();

            String username = jwtService.extractUsername(claims);

            assertThat(username).isNull();
        }

        @Test
        @DisplayName("Should return null for claims with empty subject")
        void shouldReturnNullForEmptySubject() {
            Claims claims = Jwts.claims().subject("").build();

            String username = jwtService.extractUsername(claims);

            assertThat(username).isNull();
        }

        @Test
        @DisplayName("Should return null for claims with whitespace-only subject")
        void shouldReturnNullForWhitespaceSubject() {
            Claims claims = Jwts.claims().subject("   ").build();

            String username = jwtService.extractUsername(claims);

            assertThat(username).isNull();
        }

        @Test
        @DisplayName("Should return null for subject that is too long")
        void shouldReturnNullForTooLongSubject() {
            String longSubject = "a".repeat(300);
            Claims claims = Jwts.claims().subject(longSubject).build();

            String username = jwtService.extractUsername(claims);

            assertThat(username).isNull();
        }

        @Test
        @DisplayName("Should trim whitespace from subject")
        void shouldTrimWhitespaceFromSubject() {
            Claims claims = Jwts.claims().subject("  testuser  ").build();

            String username = jwtService.extractUsername(claims);

            assertThat(username).isEqualTo("testuser");
        }
    }

    @Nested
    @DisplayName("Authorities Extraction Tests")
    class AuthoritiesExtractionTests {

        @Test
        @DisplayName("Should extract authorities from claims with roles list")
        void shouldExtractAuthoritiesFromClaims() {
            String token = createValidToken("testuser", List.of("OPERATOR", "VIEWER"));
            Claims claims = jwtService.validateToken(token);

            Collection<GrantedAuthority> authorities = jwtService.extractAuthorities(claims);

            assertThat(authorities).hasSize(2);
            assertThat(authorities.stream().map(GrantedAuthority::getAuthority))
                    .containsExactlyInAnyOrder("ROLE_OPERATOR", "ROLE_VIEWER");
        }

        @Test
        @DisplayName("Should handle empty roles list")
        void shouldHandleEmptyRoles() {
            String token = createValidToken("testuser", List.of());
            Claims claims = jwtService.validateToken(token);

            Collection<GrantedAuthority> authorities = jwtService.extractAuthorities(claims);

            assertThat(authorities).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for null claims")
        void shouldReturnEmptyListForNullClaims() {
            Collection<GrantedAuthority> authorities = jwtService.extractAuthorities(null);

            assertThat(authorities).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for claims without roles")
        void shouldReturnEmptyListForClaimsWithoutRoles() {
            Claims claims = Jwts.claims().subject("testuser").build();

            Collection<GrantedAuthority> authorities = jwtService.extractAuthorities(claims);

            assertThat(authorities).isEmpty();
        }

        @Test
        @DisplayName("Should handle single role as string")
        void shouldHandleSingleRoleAsString() {
            Claims claims = Jwts.claims().subject("testuser").add("roles", "OPERATOR").build();

            Collection<GrantedAuthority> authorities = jwtService.extractAuthorities(claims);

            assertThat(authorities).hasSize(1);
            assertThat(authorities.stream().map(GrantedAuthority::getAuthority))
                    .containsExactly("ROLE_OPERATOR");
        }

        @Test
        @DisplayName("Should filter out null and empty roles")
        void shouldFilterOutNullAndEmptyRoles() {
            Claims claims = Jwts.claims().subject("testuser")
                    .add("roles", List.of("OPERATOR", null, "", "VIEWER", "   "))
                    .build();

            Collection<GrantedAuthority> authorities = jwtService.extractAuthorities(claims);

            assertThat(authorities).hasSize(2);
            assertThat(authorities.stream().map(GrantedAuthority::getAuthority))
                    .containsExactlyInAnyOrder("ROLE_OPERATOR", "ROLE_VIEWER");
        }

        @Test
        @DisplayName("Should convert roles to uppercase and sanitize")
        void shouldConvertRolesToUppercaseAndSanitize() {
            Claims claims = Jwts.claims().subject("testuser")
                    .add("roles", List.of("operator", "viewer-admin", "test@role"))
                    .build();

            Collection<GrantedAuthority> authorities = jwtService.extractAuthorities(claims);

            assertThat(authorities).hasSize(3);
            assertThat(authorities.stream().map(GrantedAuthority::getAuthority))
                    .containsExactlyInAnyOrder("ROLE_OPERATOR", "ROLE_VIEWER_ADMIN", "ROLE_TEST_ROLE");
        }

        @Test
        @DisplayName("Should truncate long role names")
        void shouldTruncateLongRoleNames() {
            String longRole = "A".repeat(60);
            Claims claims = Jwts.claims().subject("testuser")
                    .add("roles", List.of(longRole))
                    .build();

            Collection<GrantedAuthority> authorities = jwtService.extractAuthorities(claims);

            assertThat(authorities).hasSize(1);
            String authority = authorities.iterator().next().getAuthority();
            assertThat(authority).startsWith("ROLE_");
            assertThat(authority.length()).isEqualTo(55); // "ROLE_" + 50 chars
        }
    }

    @Nested
    @DisplayName("Token Expiration Tests")
    class TokenExpirationTests {

        @Test
        @DisplayName("Should detect expired token")
        void shouldDetectExpiredToken() {
            String expiredToken = createExpiredToken("testuser", List.of("OPERATOR"));
            Claims claims = jwtService.validateToken(expiredToken);

            boolean isExpired = jwtService.isTokenExpired(claims);

            assertThat(isExpired).isTrue();
        }

        @Test
        @DisplayName("Should detect valid token")
        void shouldDetectValidToken() {
            String validToken = createValidToken("testuser", List.of("OPERATOR"));
            Claims claims = jwtService.validateToken(validToken);

            boolean isExpired = jwtService.isTokenExpired(claims);

            assertThat(isExpired).isFalse();
        }

        @Test
        @DisplayName("Should return true for null claims")
        void shouldReturnTrueForNullClaims() {
            boolean isExpired = jwtService.isTokenExpired(null);

            assertThat(isExpired).isTrue();
        }

        @Test
        @DisplayName("Should return true for claims without expiration")
        void shouldReturnTrueForClaimsWithoutExpiration() {
            Claims claims = Jwts.claims().subject("testuser").build();

            boolean isExpired = jwtService.isTokenExpired(claims);

            assertThat(isExpired).isTrue();
        }
    }

    @Nested
    @DisplayName("Complete Token Validation Tests")
    class CompleteTokenValidationTests {

        @Test
        @DisplayName("Should validate complete token successfully")
        void shouldValidateCompleteTokenSuccessfully() {
            String token = createValidToken("testuser", List.of("OPERATOR"));

            boolean isValid = jwtService.isTokenValid(token);

            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should reject null token")
        void shouldRejectNullToken() {
            boolean isValid = jwtService.isTokenValid(null);

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject empty token")
        void shouldRejectEmptyToken() {
            boolean isValid = jwtService.isTokenValid("");

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject expired token")
        void shouldRejectExpiredToken() {
            String expiredToken = createExpiredToken("testuser", List.of("OPERATOR"));

            boolean isValid = jwtService.isTokenValid(expiredToken);

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject token with invalid signature")
        void shouldRejectTokenWithInvalidSignature() {
            String tokenWithWrongSignature = createTokenWithWrongSignature("testuser", List.of("OPERATOR"));

            boolean isValid = jwtService.isTokenValid(tokenWithWrongSignature);

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject malformed token")
        void shouldRejectMalformedToken() {
            String malformedToken = "invalid.jwt.token";

            boolean isValid = jwtService.isTokenValid(malformedToken);

            assertThat(isValid).isFalse();
        }
    }

    // Helper methods for creating test tokens
    private String createValidToken(String username, List<String> roles) {
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(secretKey)
                .compact();
    }

    private String createExpiredToken(String username, List<String> roles) {
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(Date.from(Instant.now().minus(2, ChronoUnit.HOURS)))
                .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
                .signWith(secretKey)
                .compact();
    }

    private String createTokenWithWrongSignature(String username, List<String> roles) {
        SecretKey wrongKey = Keys
                .hmacShaKeyFor("wrongSecretKey123456789012345678901234567890".getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(wrongKey)
                .compact();
    }
}