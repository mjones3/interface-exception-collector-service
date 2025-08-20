package com.arcone.biopro.exception.collector.infrastructure.config.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

/**
 * JWT Authentication Filter for processing Bearer tokens
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    static {
        System.out.println("JwtAuthenticationFilter class loaded");
    }

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
        System.out.println("JwtAuthenticationFilter bean created");
        log.info("JwtAuthenticationFilter bean created with JwtService: {}", jwtService);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        log.debug("[JWT-AUTH] Processing request: {} {}", method, requestPath);

        // Skip JWT processing for public endpoints
        if (isPublicEndpoint(requestPath)) {
            log.debug("[JWT-AUTH] Skipping JWT validation for public endpoint: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractTokenFromRequest(request);

        if (token == null) {
            log.debug("[JWT-AUTH] No JWT token found in request to: {}", requestPath);
            // Continue without authentication - let Spring Security handle unauthorized
            // access
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("[JWT-AUTH] JWT token found for request to: {} (token length: {})", requestPath, token.length());

        try {
            Claims claims = jwtService.validateToken(token);

            if (!jwtService.isTokenExpired(claims)) {
                String username = jwtService.extractUsername(claims);
                Collection<GrantedAuthority> authorities = jwtService.extractAuthorities(claims);

                if (username != null) {
                    Authentication authentication = new JwtAuthenticationToken(username, token, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.info("[JWT-AUTH] Authentication successful for user: {} accessing: {} {} with authorities: {}",
                            username, method, requestPath, authorities.stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .toList());
                } else {
                    log.warn("[JWT-AUTH] Authentication failed: Unable to extract username from valid token for: {} {}",
                            method, requestPath);
                }
            } else {
                log.warn("[JWT-AUTH] Authentication failed: Token expired for request to: {} {}", method, requestPath);
            }
        } catch (JwtService.InvalidJwtTokenException e) {
            log.warn("[JWT-AUTH] Authentication failed for: {} {} - Error: {} ({})",
                    method, requestPath, e.getErrorType().getDescription(), e.getMessage());

            // Store error information in request attributes for the authentication entry
            // point
            request.setAttribute("jwt.error.type", e.getErrorType());
            request.setAttribute("jwt.error.message", e.getMessage());

            // Continue without authentication - let Spring Security handle unauthorized
            // access
        } catch (Exception e) {
            log.error("[JWT-AUTH] Unexpected error during authentication for: {} {} - {}: {}",
                    method, requestPath, e.getClass().getSimpleName(), e.getMessage());

            // Store generic error information
            request.setAttribute("jwt.error.type", JwtService.JwtValidationError.VALIDATION_ERROR);
            request.setAttribute("jwt.error.message", "Unexpected authentication error");

            // Continue without authentication - let Spring Security handle unauthorized
            // access
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (!StringUtils.hasText(bearerToken)) {
            log.debug("[JWT-AUTH] No Authorization header found");
            return null;
        }

        if (!bearerToken.startsWith(BEARER_PREFIX)) {
            log.debug("[JWT-AUTH] Authorization header does not start with 'Bearer '");
            return null;
        }

        String token = bearerToken.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            log.debug("[JWT-AUTH] Empty token after 'Bearer ' prefix");
            return null;
        }

        return token;
    }

    /**
     * Check if the endpoint is public and doesn't require JWT authentication
     */
    private boolean isPublicEndpoint(String requestPath) {
        return requestPath.startsWith("/actuator/health") ||
                requestPath.startsWith("/actuator/info") ||
                requestPath.startsWith("/swagger-ui") ||
                requestPath.startsWith("/v3/api-docs");
    }
}