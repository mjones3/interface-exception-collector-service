package com.arcone.biopro.exception.collector.infrastructure.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
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
 * Unit tests for GraphQLSecurityService.
 * Tests role-based access control and permission checking utilities.
 */
@ExtendWith(MockitoExtension.class)
class GraphQLSecurityServiceTest {

    private GraphQLSecurityService securityService;
    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        securityService = new GraphQLSecurityService();
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void shouldReturnTrueForAdminRole() {
        // Given
        setupAuthenticationWithRoles("ROLE_ADMIN");

        // When & Then
        assertTrue(securityService.hasAnyRole(GraphQLSecurityService.Role.ADMIN));
        assertTrue(securityService.isAdmin());
        assertTrue(securityService.canPerformOperations());
        assertTrue(securityService.canViewExceptions());
    }

    @Test
    void shouldReturnTrueForOperationsRole() {
        // Given
        setupAuthenticationWithRoles("ROLE_OPERATIONS");

        // When & Then
        assertTrue(securityService.hasAnyRole(GraphQLSecurityService.Role.OPERATIONS));
        assertFalse(securityService.isAdmin());
        assertTrue(securityService.canPerformOperations());
        assertTrue(securityService.canViewExceptions());
        assertTrue(securityService.canRetryExceptions());
        assertTrue(securityService.canAcknowledgeExceptions());
        assertTrue(securityService.canResolveExceptions());
    }

    @Test
    void shouldReturnTrueForViewerRole() {
        // Given
        setupAuthenticationWithRoles("ROLE_VIEWER");

        // When & Then
        assertTrue(securityService.hasAnyRole(GraphQLSecurityService.Role.VIEWER));
        assertFalse(securityService.isAdmin());
        assertFalse(securityService.canPerformOperations());
        assertTrue(securityService.canViewExceptions());
        assertFalse(securityService.canRetryExceptions());
        assertFalse(securityService.canAcknowledgeExceptions());
        assertFalse(securityService.canResolveExceptions());
    }

    @Test
    void shouldReturnFalseWhenNotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When & Then
        assertFalse(securityService.hasAnyRole(GraphQLSecurityService.Role.VIEWER));
        assertFalse(securityService.isAdmin());
        assertFalse(securityService.canPerformOperations());
        assertFalse(securityService.canViewExceptions());
        assertNull(securityService.getCurrentUsername());
    }

    @Test
    void shouldReturnFalseWhenAuthenticationNotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then
        assertFalse(securityService.hasAnyRole(GraphQLSecurityService.Role.VIEWER));
        assertFalse(securityService.canViewExceptions());
    }

    @Test
    void shouldAllowPayloadAccessForOperationsRole() {
        // Given
        setupAuthenticationWithRoles("ROLE_OPERATIONS");
        when(authentication.getName()).thenReturn("operations-user");

        // When & Then
        assertTrue(securityService.canViewPayload("exception-123", authentication));
    }

    @Test
    void shouldAllowPayloadAccessForAdminRole() {
        // Given
        setupAuthenticationWithRoles("ROLE_ADMIN");
        when(authentication.getName()).thenReturn("admin-user");

        // When & Then
        assertTrue(securityService.canViewPayload("exception-123", authentication));
    }

    @Test
    void shouldDenyPayloadAccessForViewerRole() {
        // Given
        setupAuthenticationWithRoles("ROLE_VIEWER");
        when(authentication.getName()).thenReturn("viewer-user");

        // When & Then
        assertFalse(securityService.canViewPayload("exception-123", authentication));
    }

    @Test
    void shouldReturnCurrentUsername() {
        // Given
        setupAuthenticationWithRoles("ROLE_VIEWER");
        when(authentication.getName()).thenReturn("test-user");

        // When & Then
        assertEquals("test-user", securityService.getCurrentUsername());
    }

    @Test
    void shouldReturnCurrentUserAuthorities() {
        // Given
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_VIEWER"),
                new SimpleGrantedAuthority("ROLE_OPERATIONS"));
        setupAuthenticationWithAuthorities(authorities);

        // When
        Collection<? extends GrantedAuthority> result = securityService.getCurrentUserAuthorities();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(new SimpleGrantedAuthority("ROLE_VIEWER")));
        assertTrue(result.contains(new SimpleGrantedAuthority("ROLE_OPERATIONS")));
    }

    @Test
    void shouldAllowAccessToCustomerDataForAuthenticatedUsers() {
        // Given
        setupAuthenticationWithRoles("ROLE_VIEWER");

        // When & Then
        assertTrue(securityService.canAccessCustomerData("customer-123"));
    }

    @Test
    void shouldAllowAccessToLocationDataForAuthenticatedUsers() {
        // Given
        setupAuthenticationWithRoles("ROLE_VIEWER");

        // When & Then
        assertTrue(securityService.canAccessLocationData("location-456"));
    }

    @Test
    void shouldThrowExceptionWhenRequiredRoleNotPresent() {
        // Given
        setupAuthenticationWithRoles("ROLE_VIEWER");

        // When & Then
        assertThrows(SecurityException.class, () -> securityService.requireRole(GraphQLSecurityService.Role.ADMIN));
    }

    @Test
    void shouldNotThrowExceptionWhenRequiredRolePresent() {
        // Given
        setupAuthenticationWithRoles("ROLE_ADMIN");

        // When & Then
        assertDoesNotThrow(() -> securityService.requireRole(GraphQLSecurityService.Role.ADMIN));
    }

    @Test
    void shouldThrowExceptionWhenOperationsRoleNotPresent() {
        // Given
        setupAuthenticationWithRoles("ROLE_VIEWER");

        // When & Then
        assertThrows(SecurityException.class, () -> securityService.requireOperationsRole());
    }

    @Test
    void shouldNotThrowExceptionWhenOperationsRolePresent() {
        // Given
        setupAuthenticationWithRoles("ROLE_OPERATIONS");

        // When & Then
        assertDoesNotThrow(() -> securityService.requireOperationsRole());
    }

    private void setupAuthenticationWithRoles(String... roles) {
        List<GrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
        setupAuthenticationWithAuthorities(authorities);
    }

    private void setupAuthenticationWithAuthorities(Collection<? extends GrantedAuthority> authorities) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn((Collection<GrantedAuthority>) authorities);
    }
}