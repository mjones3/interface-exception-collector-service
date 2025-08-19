package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GraphQLSecurityService.
 * Tests role-based access control and field-level security.
 */
@ExtendWith(MockitoExtension.class)
class GraphQLSecurityServiceTest {

    @Mock
    private Authentication authentication;

    private GraphQLSecurityService securityService;

    @BeforeEach
    void setUp() {
        securityService = new GraphQLSecurityService();
    }

    @Test
    void testCanViewPayload_AdminUser_ShouldReturnTrue() {
        // Given
        InterfaceException exception = createTestException();
        setupAuthentication("admin", List.of("ROLE_ADMIN"));

        // When
        boolean result = securityService.canViewPayload(exception, authentication);

        // Then
        assertTrue(result);
    }

    @Test
    void testCanViewPayload_OperationsUser_ShouldReturnTrue() {
        // Given
        InterfaceException exception = createTestException();
        setupAuthentication("operations", List.of("ROLE_OPERATIONS"));

        // When
        boolean result = securityService.canViewPayload(exception, authentication);

        // Then
        assertTrue(result);
    }

    @Test
    void testCanViewPayload_ViewerUser_ShouldReturnFalse() {
        // Given
        InterfaceException exception = createTestException();
        setupAuthentication("viewer", List.of("ROLE_VIEWER"));

        // When
        boolean result = securityService.canViewPayload(exception, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanViewPayload_UnauthenticatedUser_ShouldReturnFalse() {
        // Given
        InterfaceException exception = createTestException();

        // When
        boolean result = securityService.canViewPayload(exception, null);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanManageException_AdminUser_ShouldReturnTrue() {
        // Given
        InterfaceException exception = createTestException();
        setupAuthentication("admin", List.of("ROLE_ADMIN"));

        // When
        boolean result = securityService.canManageException(exception, authentication);

        // Then
        assertTrue(result);
    }

    @Test
    void testCanManageException_OperationsUserWithNonCritical_ShouldReturnTrue() {
        // Given
        InterfaceException exception = createTestException();
        exception.setSeverity(ExceptionSeverity.MEDIUM);
        setupAuthentication("operations", List.of("ROLE_OPERATIONS"));

        // When
        boolean result = securityService.canManageException(exception, authentication);

        // Then
        assertTrue(result);
    }

    @Test
    void testCanManageException_OperationsUserWithCritical_ShouldReturnFalse() {
        // Given
        InterfaceException exception = createTestException();
        exception.setSeverity(ExceptionSeverity.CRITICAL);
        setupAuthentication("operations", List.of("ROLE_OPERATIONS"));

        // When
        boolean result = securityService.canManageException(exception, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanManageException_OperationsUserWithResolvedStatus_ShouldReturnFalse() {
        // Given
        InterfaceException exception = createTestException();
        exception.setStatus(ExceptionStatus.RESOLVED);
        setupAuthentication("operations", List.of("ROLE_OPERATIONS"));

        // When
        boolean result = securityService.canManageException(exception, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanManageException_ViewerUser_ShouldReturnFalse() {
        // Given
        InterfaceException exception = createTestException();
        setupAuthentication("viewer", List.of("ROLE_VIEWER"));

        // When
        boolean result = securityService.canManageException(exception, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanViewStatusHistory_AuthenticatedUser_ShouldReturnTrue() {
        // Given
        InterfaceException exception = createTestException();
        setupAuthentication("user", List.of("ROLE_VIEWER"));

        // When
        boolean result = securityService.canViewStatusHistory(exception, authentication);

        // Then
        assertTrue(result);
    }

    @Test
    void testCanViewStatusHistory_UnauthenticatedUser_ShouldReturnFalse() {
        // Given
        InterfaceException exception = createTestException();

        // When
        boolean result = securityService.canViewStatusHistory(exception, null);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanViewRetryHistory_AuthenticatedUser_ShouldReturnTrue() {
        // Given
        InterfaceException exception = createTestException();
        setupAuthentication("user", List.of("ROLE_VIEWER"));

        // When
        boolean result = securityService.canViewRetryHistory(exception, authentication);

        // Then
        assertTrue(result);
    }

    @Test
    void testGetUserPrivilegeLevel_AdminUser_ShouldReturnAdmin() {
        // Given
        setupAuthentication("admin", List.of("ROLE_ADMIN"));

        // When
        String result = securityService.getUserPrivilegeLevel(authentication);

        // Then
        assertEquals("ADMIN", result);
    }

    @Test
    void testGetUserPrivilegeLevel_OperationsUser_ShouldReturnOperations() {
        // Given
        setupAuthentication("operations", List.of("ROLE_OPERATIONS"));

        // When
        String result = securityService.getUserPrivilegeLevel(authentication);

        // Then
        assertEquals("OPERATIONS", result);
    }

    @Test
    void testGetUserPrivilegeLevel_ViewerUser_ShouldReturnViewer() {
        // Given
        setupAuthentication("viewer", List.of("ROLE_VIEWER"));

        // When
        String result = securityService.getUserPrivilegeLevel(authentication);

        // Then
        assertEquals("VIEWER", result);
    }

    @Test
    void testGetUserPrivilegeLevel_UnauthenticatedUser_ShouldReturnAnonymous() {
        // When
        String result = securityService.getUserPrivilegeLevel(null);

        // Then
        assertEquals("ANONYMOUS", result);
    }

    @Test
    void testGetUserPrivilegeLevel_UnknownRole_ShouldReturnUnknown() {
        // Given
        setupAuthentication("user", List.of("ROLE_UNKNOWN"));

        // When
        String result = securityService.getUserPrivilegeLevel(authentication);

        // Then
        assertEquals("UNKNOWN", result);
    }

    private InterfaceException createTestException() {
        return InterfaceException.builder()
                .id(1L)
                .transactionId("TEST-123")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.MEDIUM)
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .build();
    }

    private void setupAuthentication(String username, List<String> roles) {
        when(authentication.getName()).thenReturn(username);
        when(authentication.isAuthenticated()).thenReturn(true);

        Collection<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();

        when(authentication.getAuthorities()).thenReturn(authorities);
    }
}