package com.arcone.biopro.exception.collector.config.security;

import com.arcone.biopro.exception.collector.infrastructure.config.security.JwtAuthenticationFilter;
import com.arcone.biopro.exception.collector.infrastructure.config.security.JwtAuthenticationToken;
import com.arcone.biopro.exception.collector.infrastructure.config.security.JwtService;
import com.arcone.biopro.exception.collector.infrastructure.config.security.JwtService.InvalidJwtTokenException;
import com.arcone.biopro.exception.collector.infrastructure.config.security.JwtService.JwtValidationError;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Comprehensive tests for JWT Authentication Filter covering all filter
 * behavior scenarios
 * Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Authentication Filter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Claims claims;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService);
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("Public Endpoint Tests")
    class PublicEndpointTests {

        @Test
        @DisplayName("Should skip JWT validation for health endpoint")
        void shouldSkipJwtValidationForHealthEndpoint() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/actuator/health");
            when(request.getMethod()).thenReturn("GET");

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtService);
            verify(securityContext, never()).setAuthentication(any());
        }

        @Test
        @DisplayName("Should skip JWT validation for info endpoint")
        void shouldSkipJwtValidationForInfoEndpoint() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/actuator/info");
            when(request.getMethod()).thenReturn("GET");

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtService);
            verify(securityContext, never()).setAuthentication(any());
        }

        @Test
        @DisplayName("Should skip JWT validation for swagger endpoints")
        void shouldSkipJwtValidationForSwaggerEndpoints() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");
            when(request.getMethod()).thenReturn("GET");

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtService);
            verify(securityContext, never()).setAuthentication(any());
        }

        @Test
        @DisplayName("Should skip JWT validation for API docs endpoints")
        void shouldSkipJwtValidationForApiDocsEndpoints() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/v3/api-docs");
            when(request.getMethod()).thenReturn("GET");

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtService);
            verify(securityContext, never()).setAuthentication(any());
        }
    }

    @Nested
    @DisplayName("Token Extraction Tests")
    class TokenExtractionTests {

        @Test
        @DisplayName("Should continue without authentication when no Authorization header")
        void shouldContinueWithoutAuthenticationWhenNoAuthorizationHeader() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/api/v1/exceptions");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn(null);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtService);
            verify(securityContext, never()).setAuthentication(any());
        }

        @Test
        @DisplayName("Should continue without authentication when Authorization header is empty")
        void shouldContinueWithoutAuthenticationWhenAuthorizationHeaderIsEmpty() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/api/v1/exceptions");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("");

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtService);
            verify(securityContext, never()).setAuthentication(any());
        }

        @Test
        @DisplayName("Should continue without authentication when Authorization header doesn't start with Bearer")
        void shouldContinueWithoutAuthenticationWhenNotBearerToken() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/api/v1/exceptions");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtService);
            verify(securityContext, never()).setAuthentication(any());
        }

        @Test
        @DisplayName("Should continue without authentication when Bearer token is empty")
        void shouldContinueWithoutAuthenticationWhenBearerTokenIsEmpty() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/api/v1/exceptions");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer ");

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtService);
            verify(securityContext, never()).setAuthentication(any());
        }

        @Test
        @DisplayName("Should continue without authentication when Bearer token is whitespace only")
        void shouldContinueWithoutAuthenticationWhenBearerTokenIsWhitespace() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/api/v1/exceptions");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer    ");

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtService);
            verify(securityContext, never()).setAuthentication(any());
        }
    }

    @Nested
    @DisplayName("Successful Authentication Tests")
    class SuccessfulAuthenticationTests {

        @Test
        @DisplayName("Should authenticate successfully with valid token")
        void shouldAuthenticateSuccessfullyWithValidToken() throws ServletException, IOException {
            String token = "valid.jwt.token";
            String username = "testuser";
            Collection<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_OPERATOR"),
                    new SimpleGrantedAuthority("ROLE_VIEWER"));

            when(request.getRequestURI()).thenReturn("/api/v1/exceptions");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.validateToken(token)).thenReturn(claims);
            when(jwtService.isTokenExpired(claims)).thenReturn(false);
            when(jwtService.extractUsername(claims)).thenReturn(username);
            when(jwtService.extractAuthorities(claims)).thenReturn(authorities);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(jwtService).validateToken(token);
            verify(jwtService).isTokenExpired(claims);
            verify(jwtService).extractUsername(claims);
            verify(jwtService).extractAuthorities(claims);
            verify(securityContext).setAuthentication(any(JwtAuthenticationToken.class));
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should set correct authentication details")
        void shouldSetCorrectAuthenticationDetails() throws ServletException, IOException {
            String token = "valid.jwt.token";
            String username = "testuser";
            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_OPERATOR"));

            when(request.getRequestURI()).thenReturn("/api/v1/exceptions");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.validateToken(token)).thenReturn(claims);
            when(jwtService.isTokenExpired(claims)).thenReturn(false);
            when(jwtService.extractUsername(claims)).thenReturn(username);
            when(jwtService.extractAuthorities(claims)).thenReturn(authorities);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(securityContext).setAuthentication(any(Authentication.class));
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle token with whitespace")
        void shouldHandleTokenWithWhitespace() throws ServletException, IOException {
            String token = "valid.jwt.token";
            String username = "testuser";
            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_OPERATOR"));

            when(request.getRequestURI()).thenReturn("/api/v1/exceptions");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer  " + token + "  ");
            when(jwtService.validateToken(token)).thenReturn(claims);
            when(jwtService.isTokenExpired(claims)).thenReturn(false);
            when(jwtService.extractUsername(claims)).thenReturn(username);
            when(jwtService.extractAuthorities(claims)).thenReturn(authorities);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(jwtService).validateToken(token);
            verify(securityContext).setAuthentication(any(JwtAuthenticationToken.class));
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Authentication Failure Tests")
    class AuthenticationFailureTests {

        @Test
        @DisplayName("Should handle expired token gracefully")
        void shouldHandleExpiredTokenGracefully() throws ServletException, IOException {
            String token = "expired.jwt.token";

            when(request.getRequestURI()).thenReturn("/api/v1/exceptions");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.validateToken(token)).thenReturn(claims);
            when(jwtService.isTokenExpired(claims)).thenReturn(true);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(jwtService).validateToken(token);
            verify(jwtService).isTokenExpired(claims);
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle null username gracefully")
        void shouldHandleNullUsernameGracefully() throws ServletException, IOException {
            String token = "valid.jwt.token";
            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_OPERATOR"));

            when(request.getRequestURI()).thenReturn("/api/v1/exceptions");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.validateToken(token)).thenReturn(claims);
            when(jwtService.isTokenExpired(claims)).thenReturn(false);
            when(jwtService.extractUsername(claims)).thenReturn(null);
            when(jwtService.extractAuthorities(claims)).thenReturn(authorities);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(jwtService).validateToken(token);
            verify(jwtService).isTokenExpired(claims);
            verify(jwtService).extractUsername(claims);
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle JWT validation exception gracefully")
        void shouldHandleJwtValidationExceptionGracefully() throws ServletException, IOException {
            String token = "invalid.jwt.token";
            InvalidJwtTokenException exception = new InvalidJwtTokenException(
                    "Token is invalid", JwtValidationError.TOKEN_INVALID);

            when(request.getRequestURI()).thenReturn("/api/v1/exceptions");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.validateToken(token)).thenThrow(exception);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(jwtService).validateToken(token);
            verify(request).setAttribute("jwt.error.type", JwtValidationError.TOKEN_INVALID);
            verify(request).setAttribute("jwt.error.message", "Token is invalid");
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle unexpected exception gracefully")
        void shouldHandleUnexpectedExceptionGracefully() throws ServletException, IOException {
            String token = "problematic.jwt.token";
            RuntimeException exception = new RuntimeException("Unexpected error");

            when(request.getRequestURI()).thenReturn("/api/v1/exceptions");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.validateToken(token)).thenThrow(exception);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(jwtService).validateToken(token);
            verify(request).setAttribute("jwt.error.type", JwtValidationError.VALIDATION_ERROR);
            verify(request).setAttribute("jwt.error.message", "Unexpected authentication error");
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should store error information for expired token exception")
        void shouldStoreErrorInformationForExpiredTokenException() throws ServletException, IOException {
            String token = "expired.jwt.token";
            InvalidJwtTokenException exception = new InvalidJwtTokenException(
                    "Token has expired", JwtValidationError.TOKEN_EXPIRED);

            when(request.getRequestURI()).thenReturn("/api/v1/exceptions");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.validateToken(token)).thenThrow(exception);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(request).setAttribute("jwt.error.type", JwtValidationError.TOKEN_EXPIRED);
            verify(request).setAttribute("jwt.error.message", "Token has expired");
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should store error information for malformed token exception")
        void shouldStoreErrorInformationForMalformedTokenException() throws ServletException, IOException {
            String token = "malformed.token";
            InvalidJwtTokenException exception = new InvalidJwtTokenException(
                    "Token is malformed", JwtValidationError.TOKEN_MALFORMED);

            when(request.getRequestURI()).thenReturn("/api/v1/exceptions");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.validateToken(token)).thenThrow(exception);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(request).setAttribute("jwt.error.type", JwtValidationError.TOKEN_MALFORMED);
            verify(request).setAttribute("jwt.error.message", "Token is malformed");
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should store error information for invalid signature exception")
        void shouldStoreErrorInformationForInvalidSignatureException() throws ServletException, IOException {
            String token = "invalid.signature.token";
            InvalidJwtTokenException exception = new InvalidJwtTokenException(
                    "Invalid signature", JwtValidationError.INVALID_SIGNATURE);

            when(request.getRequestURI()).thenReturn("/api/v1/exceptions");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.validateToken(token)).thenThrow(exception);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(request).setAttribute("jwt.error.type", JwtValidationError.INVALID_SIGNATURE);
            verify(request).setAttribute("jwt.error.message", "Invalid signature");
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Filter Chain Continuation Tests")
    class FilterChainContinuationTests {

        @Test
        @DisplayName("Should always continue filter chain regardless of authentication result")
        void shouldAlwaysContinueFilterChain() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/api/v1/exceptions");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn(null);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should continue filter chain after successful authentication")
        void shouldContinueFilterChainAfterSuccessfulAuthentication() throws ServletException, IOException {
            String token = "valid.jwt.token";
            String username = "testuser";
            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_OPERATOR"));

            when(request.getRequestURI()).thenReturn("/api/v1/exceptions");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.validateToken(token)).thenReturn(claims);
            when(jwtService.isTokenExpired(claims)).thenReturn(false);
            when(jwtService.extractUsername(claims)).thenReturn(username);
            when(jwtService.extractAuthorities(claims)).thenReturn(authorities);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should continue filter chain after authentication failure")
        void shouldContinueFilterChainAfterAuthenticationFailure() throws ServletException, IOException {
            String token = "invalid.jwt.token";
            InvalidJwtTokenException exception = new InvalidJwtTokenException(
                    "Token is invalid", JwtValidationError.TOKEN_INVALID);

            when(request.getRequestURI()).thenReturn("/api/v1/exceptions");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.validateToken(token)).thenThrow(exception);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }
    }
}