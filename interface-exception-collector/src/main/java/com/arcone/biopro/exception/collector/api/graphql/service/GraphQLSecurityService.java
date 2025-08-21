package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

/**
 * Service for handling GraphQL field-level security and access control.
 * Provides methods to check permissions for sensitive data access.
 */
@Service
@Slf4j
public class GraphQLSecurityService {

    // Roles that have full access to all data
    private static final Set<String> ADMIN_ROLES = Set.of("ROLE_ADMIN", "ADMIN");

    // Roles that can view payloads for operational purposes
    private static final Set<String> OPERATIONS_ROLES = Set.of("ROLE_OPERATIONS", "OPERATIONS");

    // Roles that have read-only access
    private static final Set<String> VIEWER_ROLES = Set.of("ROLE_VIEWER", "VIEWER");

    /**
     * Checks if the authenticated user can view the original payload for an
     * exception.
     * Access rules:
     * - ADMIN: Full access to all payloads
     * - OPERATIONS: Access to payloads for exceptions they can manage
     * - VIEWER: No access to payloads (sensitive data)
     *
     * @param exception      the exception to check access for
     * @param authentication the current user's authentication
     * @return true if the user can view the payload, false otherwise
     */
    public boolean canViewPayload(InterfaceException exception, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("Access denied to payload for transaction {}: No authentication",
                    exception.getTransactionId());
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Set<String> userRoles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());

        // Admin users have full access
        if (hasAnyRole(userRoles, ADMIN_ROLES)) {
            log.debug("Payload access granted for transaction {}: Admin user {}",
                    exception.getTransactionId(), authentication.getName());
            return true;
        }

        // Operations users can view payloads for exceptions they can manage
        if (hasAnyRole(userRoles, OPERATIONS_ROLES)) {
            boolean canManage = canManageException(exception, authentication);
            log.debug("Payload access {} for transaction {}: Operations user {} (can manage: {})",
                    canManage ? "granted" : "denied",
                    exception.getTransactionId(),
                    authentication.getName(),
                    canManage);
            return canManage;
        }

        // Viewer users cannot access payloads (sensitive data)
        log.debug("Payload access denied for transaction {}: Viewer user {} (insufficient privileges)",
                exception.getTransactionId(), authentication.getName());
        return false;
    }

    /**
     * Checks if the authenticated user can manage (retry, acknowledge, resolve) an
     * exception.
     * Access rules:
     * - ADMIN: Can manage all exceptions
     * - OPERATIONS: Can manage exceptions based on severity and business rules
     * - VIEWER: Cannot manage exceptions
     *
     * @param exception      the exception to check access for
     * @param authentication the current user's authentication
     * @return true if the user can manage the exception, false otherwise
     */
    public boolean canManageException(InterfaceException exception, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Set<String> userRoles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());

        // Admin users can manage all exceptions
        if (hasAnyRole(userRoles, ADMIN_ROLES)) {
            return true;
        }

        // Operations users can manage exceptions based on business rules
        if (hasAnyRole(userRoles, OPERATIONS_ROLES)) {
            return canOperationsUserManageException(exception, authentication);
        }

        // Viewer users cannot manage exceptions
        return false;
    }

    /**
     * Checks if the authenticated user can view status history for an exception.
     * Most users can view status history as it's audit information.
     *
     * @param exception      the exception to check access for
     * @param authentication the current user's authentication
     * @return true if the user can view status history, false otherwise
     */
    public boolean canViewStatusHistory(InterfaceException exception, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // All authenticated users can view status history (audit trail)
        return true;
    }

    /**
     * Checks if the authenticated user can view retry history for an exception.
     *
     * @param exception      the exception to check access for
     * @param authentication the current user's authentication
     * @return true if the user can view retry history, false otherwise
     */
    public boolean canViewRetryHistory(InterfaceException exception, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // All authenticated users can view retry history
        return true;
    }

    /**
     * Determines if an operations user can manage a specific exception based on
     * business rules.
     */
    private boolean canOperationsUserManageException(InterfaceException exception, Authentication authentication) {
        // Operations users can manage exceptions that are:
        // 1. Not resolved or cancelled
        // 2. Not critical severity (requires admin approval)
        // 3. Retryable exceptions

        if (exception.getStatus() == com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus.RESOLVED ||
                exception.getStatus() == com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus.CLOSED) {
            return false;
        }

        // Critical exceptions require admin approval
        if (exception.getSeverity() == ExceptionSeverity.CRITICAL) {
            log.debug("Operations user {} cannot manage critical exception {}",
                    authentication.getName(), exception.getTransactionId());
            return false;
        }

        return true;
    }

    /**
     * Checks if the user has any of the specified roles.
     */
    private boolean hasAnyRole(Set<String> userRoles, Set<String> requiredRoles) {
        return userRoles.stream().anyMatch(requiredRoles::contains);
    }

    /**
     * Checks if the authenticated user can view an exception for subscription
     * updates.
     * 
     * @param authentication the current user's authentication
     * @param exception      the exception to check access for
     * @return true if the user can view the exception, false otherwise
     */
    public boolean canViewException(Authentication authentication,
            com.arcone.biopro.exception.collector.api.graphql.resolver.ExceptionSubscriptionResolver.Exception exception) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // All authenticated users can view basic exception information
        // Additional filtering based on customer/location restrictions could be added
        // here
        return true;
    }

    /**
     * Checks if the authenticated user can view retry status updates.
     * 
     * @param authentication the current user's authentication
     * @param retryEvent     the retry status event to check access for
     * @return true if the user can view the retry status, false otherwise
     */
    public boolean canViewRetryStatus(Authentication authentication,
            com.arcone.biopro.exception.collector.api.graphql.resolver.ExceptionSubscriptionResolver.RetryStatusEvent retryEvent) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Set<String> userRoles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());

        // Admin and Operations users can view retry status
        return hasAnyRole(userRoles, ADMIN_ROLES) || hasAnyRole(userRoles, OPERATIONS_ROLES);
    }

    /**
     * Gets the user's highest privilege level for logging and audit purposes.
     */
    public String getUserPrivilegeLevel(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "ANONYMOUS";
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Set<String> userRoles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());

        if (hasAnyRole(userRoles, ADMIN_ROLES)) {
            return "ADMIN";
        } else if (hasAnyRole(userRoles, OPERATIONS_ROLES)) {
            return "OPERATIONS";
        } else if (hasAnyRole(userRoles, VIEWER_ROLES)) {
            return "VIEWER";
        } else {
            return "UNKNOWN";
        }
    }
}