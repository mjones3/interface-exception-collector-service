package com.arcone.biopro.exception.collector.infrastructure.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * GraphQL Authentication Provider for JWT token validation.
 * This provider handles JWT authentication specifically for GraphQL endpoints.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GraphQLAuthenticationProvider implements AuthenticationProvider {

    private final JwtService jwtService;

    /**
     * Authenticates JWT tokens for GraphQL requests.
     *
     * @param authentication the authentication request containing JWT token
     * @return authenticated token with user details and authorities
     * @throws AuthenticationException if authentication fails
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof JwtAuthenticationToken)) {
            return null;
        }

        JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
        String token = (String) jwtToken.getCredentials();

        try {
            // Validate JWT token and extract claims
            var claims = jwtService.validateToken(token);

            // Check if token is expired
            if (jwtService.isTokenExpired(claims)) {
                log.debug("JWT token is expired");
                throw new BadCredentialsException("JWT token is expired");
            }

            // Extract user information
            String username = jwtService.extractUsername(claims);
            Collection<GrantedAuthority> authorities = jwtService.extractAuthorities(claims);

            log.debug("Successfully authenticated user: {} with authorities: {}", username, authorities);

            // Return authenticated token
            return new JwtAuthenticationToken(username, token, authorities);

        } catch (JwtService.InvalidJwtTokenException e) {
            log.debug("JWT token validation failed: {}", e.getMessage());
            throw new BadCredentialsException("Invalid JWT token", e);
        } catch (Exception e) {
            log.error("Unexpected error during JWT authentication", e);
            throw new BadCredentialsException("Authentication failed", e);
        }
    }

    /**
     * Indicates whether this provider supports the given authentication type.
     *
     * @param authentication the authentication type
     * @return true if this provider supports JWT authentication tokens
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}