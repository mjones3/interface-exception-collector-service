package com.arcone.biopro.exception.collector.infrastructure.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * GraphQL Security Service providing permission checking utilities for
 * field-level access control.
 * This service provides methods to check user permissions for GraphQL
 * operations and field access.
 */
@Service("graphqlSecurityService")
@Slf4j
public class GraphQLSecurityService {

    /**
     * Security roles used in the application
     */
    public enum Role {
        ADMIN("ROLE_ADMIN"),
        OPERATIONS("ROLE_OPERATIONS"),
        VIEWER("ROLE_VIEWER");

        private final String authority;

        Role(String authority) {
            this.authority = authority;
        }

        public String getAuthority() {
            return authority;
        }
    }

    /**
     * Checks if the current user has any of the specified roles.
     *
     * @param roles the roles to check
     * @return true if user has any of the specified roles
     */
    public boolean hasAnyRole(Role... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Set<String> userAuthorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        for (Role role : roles) {
            if (userAuthorities.contains(role.getAuthority())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the current user has admin role.
     *
     * @return true if user has admin role
     */
    public boolean isAdmin() {
        return hasAnyRole(Role.ADMIN);
    }

    /**
     * Checks if the current user has operations role or higher.
     *
     * @return true if user has operations or admin role
     */
    public boolean canPerformOperations() {
        return hasAnyRole(Role.ADMIN, Role.OPERATIONS);
    }

    /**
     * Checks if the current user can view exception data.
     *
     * @return true if user has viewer, operations, or admin role
     */
    public boolean canViewExceptions() {
        return hasAnyRole(Role.ADMIN, Role.OPERATIONS, Role.VIEWER);
    }

    /**
     * Checks if the current user can view sensitive payload data.
     * Only operations and admin users can view original payloads.
     *
     * @param exceptionId    the exception ID (for future customer-specific access
     *                       control)
     * @param authentication the current authentication context
     * @return true if user can view payload data
     */
    public boolean canViewPayload(String exceptionId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Check if user has operations or admin role
        Set<String> userAuthorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        boolean hasPermission = userAuthorities.contains(Role.ADMIN.getAuthority()) ||
                userAuthorities.contains(Role.OPERATIONS.getAuthority());

        if (hasPermission) {
            log.debug("User {} granted access to payload for exception {}",
                    authentication.getName(), exceptionId);
        } else {
            log.debug("User {} denied access to payload for exception {}",
                    authentication.getName(), exceptionId);
        }

        return hasPermission;
    }

    /**
     * Checks if the current user can retry exceptions.
     *
     * @return true if user has operations or admin role
     */
    public boolean canRetryExceptions() {
        return canPerformOperations();
    }

    /**
     * Checks if the current user can acknowledge exceptions.
     *
     * @return true if user has operations or admin role
     */
    public boolean canAcknowledgeExceptions() {
        return canPerformOperations();
    }

    /**
     * Checks if the current user can resolve exceptions.
     *
     * @return true if user has operations or admin role
     */
    public boolean canResolveExceptions() {
        return canPerformOperations();
    }

    /**
     * Gets the current authenticated user's username.
     *
     * @return the username or null if not authenticated
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * Gets the current authenticated user's roles.
     *
     * @return collection of user authorities
     */
    public Collection<? extends GrantedAuthority> getCurrentUserAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities();
        }
        return Set.of();
    }

    /**
     * Checks if the current user can access customer-specific data.
     * This method can be extended to implement customer-specific access control.
     *
     * @param customerId the customer ID to check access for
     * @return true if user can access customer data
     */
    public boolean canAccessCustomerData(String customerId) {
        // For now, all authenticated users with viewer role or higher can access all
        // customer data
        // This can be extended to implement customer-specific access control based on
        // user attributes
        return canViewExceptions();
    }

    /**
     * Checks if the current user can access location-specific data.
     * This method can be extended to implement location-specific access control.
     *
     * @param locationCode the location code to check access for
     * @return true if user can access location data
     */
    public boolean canAccessLocationData(String locationCode) {
        // For now, all authenticated users with viewer role or higher can access all
        // location data
        // This can be extended to implement location-specific access control based on
        // user attributes
        return canViewExceptions();
    }

    /**
     * Validates that the current user has the required role for an operation.
     *
     * @param requiredRole the minimum required role
     * @throws SecurityException if user doesn't have required role
     */
    public void requireRole(Role requiredRole) {
        if (!hasAnyRole(requiredRole)) {
            String username = getCurrentUsername();
            log.warn("User {} attempted to access operation requiring {} role", username, requiredRole);
            throw new SecurityException("Insufficient permissions for this operation");
        }
    }

    /**
     * Validates that the current user can perform operations.
     *
     * @throws SecurityException if user cannot perform operations
     */
    public void requireOperationsRole() {
        if (!canPerformOperations()) {
            String username = getCurrentUsername();
            log.warn("User {} attempted to perform operation without sufficient permissions", username);
            throw new SecurityException("Operations role required for this action");
        }
    }
}