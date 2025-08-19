package com.arcone.biopro.exception.collector.infrastructure.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Simple unit tests for GraphQLSecurityService without complex dependencies.
 */
class GraphQLSecurityServiceSimpleTest {

    private GraphQLSecurityService securityService;

    @BeforeEach
    void setUp() {
        securityService = new GraphQLSecurityService();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnFalseWhenNoAuthentication() {
        // When & Then
        assertFalse(securityService.hasAnyRole(GraphQLSecurityService.Role.VIEWER));
        assertFalse(securityService.isAdmin());
        assertFalse(securityService.canPerformOperations());
        assertFalse(securityService.canViewExceptions());
        assertNull(securityService.getCurrentUsername());
    }

    @Test
    void shouldValidateRoleEnumValues() {
        // Test that role enum has correct authority strings
        assertEquals("ROLE_ADMIN", GraphQLSecurityService.Role.ADMIN.getAuthority());
        assertEquals("ROLE_OPERATIONS", GraphQLSecurityService.Role.OPERATIONS.getAuthority());
        assertEquals("ROLE_VIEWER", GraphQLSecurityService.Role.VIEWER.getAuthority());
    }

    @Test
    void shouldThrowSecurityExceptionWhenRequiredRoleNotPresent() {
        // Given - no authentication context

        // When & Then
        assertThrows(SecurityException.class, () -> securityService.requireRole(GraphQLSecurityService.Role.ADMIN));

        assertThrows(SecurityException.class, () -> securityService.requireOperationsRole());
    }

    @Test
    void shouldAllowAccessToCustomerAndLocationDataWhenAuthenticated() {
        // Given
        setupMockAuthentication("test-user", List.of("ROLE_VIEWER"));

        // When & Then
        assertTrue(securityService.canAccessCustomerData("customer-123"));
        assertTrue(securityService.canAccessLocationData("location-456"));
    }

    @Test
    void shouldDenyPayloadAccessForUnauthenticatedUser() {
        // Given - no authentication

        // When & Then
        assertFalse(securityService.canViewPayload("exception-123", null));
    }

    private void setupMockAuthentication(String username, List<String> roles) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(authentication.getAuthorities()).thenReturn((Collection<GrantedAuthority>) authorities);

        SecurityContextHolder.setContext(securityContext);
    }
}