package com.arcone.biopro.exception.collector.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
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
 * Tests for JWT service
 */
class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "mySecretKey1234567890123456789012345678901234567890";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret);
    }

    @Test
    void shouldValidateValidToken() {
        String token = createValidToken("testuser", List.of("OPERATOR", "VIEWER"));

        Claims claims = jwtService.validateToken(token);

        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("testuser");
    }

    @Test
    void shouldThrowExceptionForInvalidToken() {
        String invalidToken = "invalid.jwt.token";

        assertThatThrownBy(() -> jwtService.validateToken(invalidToken))
                .isInstanceOf(JwtService.InvalidJwtTokenException.class);
    }

    @Test
    void shouldExtractUsernameFromClaims() {
        String token = createValidToken("testuser", List.of("OPERATOR"));
        Claims claims = jwtService.validateToken(token);

        String username = jwtService.extractUsername(claims);

        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void shouldExtractAuthoritiesFromClaims() {
        String token = createValidToken("testuser", List.of("OPERATOR", "VIEWER"));
        Claims claims = jwtService.validateToken(token);

        Collection<GrantedAuthority> authorities = jwtService.extractAuthorities(claims);

        assertThat(authorities).hasSize(2);
        assertThat(authorities.stream().map(GrantedAuthority::getAuthority))
                .containsExactlyInAnyOrder("ROLE_OPERATOR", "ROLE_VIEWER");
    }

    @Test
    void shouldHandleEmptyRoles() {
        String token = createValidToken("testuser", List.of());
        Claims claims = jwtService.validateToken(token);

        Collection<GrantedAuthority> authorities = jwtService.extractAuthorities(claims);

        assertThat(authorities).isEmpty();
    }

    @Test
    void shouldDetectExpiredToken() {
        String expiredToken = createExpiredToken("testuser", List.of("OPERATOR"));
        Claims claims = jwtService.validateToken(expiredToken);

        boolean isExpired = jwtService.isTokenExpired(claims);

        assertThat(isExpired).isTrue();
    }

    @Test
    void shouldDetectValidToken() {
        String validToken = createValidToken("testuser", List.of("OPERATOR"));
        Claims claims = jwtService.validateToken(validToken);

        boolean isExpired = jwtService.isTokenExpired(claims);

        assertThat(isExpired).isFalse();
    }

    private String createValidToken(String username, List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(key)
                .compact();
    }

    private String createExpiredToken(String username, List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(Date.from(Instant.now().minus(2, ChronoUnit.HOURS)))
                .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
                .signWith(key)
                .compact();
    }
}