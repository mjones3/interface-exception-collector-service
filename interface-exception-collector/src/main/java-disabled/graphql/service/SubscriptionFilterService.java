package com.arcone.biopro.exception.collector.api.graphql.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for filtering GraphQL subscription events based on user permissions
 * and filters.
 * Ensures users only receive subscription updates they are authorized to see.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionFilterService {

    /**
     * Checks if a user has permission to receive a specific exception update.
     * 
     * @param authentication the user's authentication context
     * @param exceptionEvent the exception event to check
     * @return true if the user can receive this event
     */
    public boolean canReceiveExceptionUpdate(Authentication authentication, Object exceptionEvent) {
        if (authentication == null) {
            log.debug("No authentication context - denying subscription update");
            return false;
        }

        String username = authentication.getName();
        Set<String> userRoles = getUserRoles(authentication);

        log.debug("Checking subscription permissions for user: {} with roles: {}", username, userRoles);

        // Admin users can see all updates
        if (userRoles.contains("ROLE_ADMIN")) {
            log.debug("Admin user - allowing all subscription updates");
            return true;
        }

        // Operations users can see most updates
        if (userRoles.contains("ROLE_OPERATIONS")) {
            return canOperationsUserReceiveUpdate(exceptionEvent);
        }

        // Viewer users have limited access
        if (userRoles.contains("ROLE_VIEWER")) {
            return canViewerReceiveUpdate(exceptionEvent);
        }

        log.debug("User has no valid roles for subscription updates");
        return false;
    }

    /**
     * Applies filters to determine if an exception event matches user-specified
     * criteria.
     * 
     * @param exceptionEvent the exception event
     * @param filters        the subscription filters
     * @return true if the event matches the filters
     */
    public boolean matchesSubscriptionFilters(Object exceptionEvent, ExceptionSubscriptionFilters filters) {
        if (filters == null) {
            return true; // No filters means accept all
        }

        log.debug("Applying subscription filters: {}", filters);

        // TODO: Implement actual filter matching logic based on the exception event
        // structure
        // This will need to be updated when the actual exception event DTOs are
        // available

        // For now, return true as a placeholder
        // In the actual implementation, this would check:
        // - Interface types
        // - Exception statuses
        // - Severity levels
        // - Customer IDs
        // - Location codes
        // - Date ranges

        return true;
    }

    /**
     * Applies filters to determine if an exception update event matches
     * user-specified criteria.
     * 
     * @param event   The exception update event
     * @param filters The exception filters to apply
     * @return true if the event matches the filters
     */
    public boolean matchesExceptionFilters(
            com.arcone.biopro.exception.collector.api.graphql.resolver.ExceptionSubscriptionResolver.ExceptionUpdateEvent event,
            com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionFilters filters) {
        if (filters == null) {
            return true; // No filters means accept all
        }

        log.debug("Applying exception filters to subscription event: {}", filters);

        // TODO: Implement actual filter matching logic
        // This would need to extract exception details from the event and match
        // against:
        // - Interface types (filters.getInterfaceTypes())
        // - Exception statuses (filters.getStatuses())
        // - Severity levels (filters.getSeverities())
        // - Customer IDs (filters.getCustomerIds())
        // - Location codes (filters.getLocationCodes())
        // - Search terms (filters.getSearchTerm())
        // - Date ranges (filters.getDateRange())
        // - Exclude resolved flag (filters.getExcludeResolved())
        // - Retryable flag (filters.getRetryable())

        // For now, return true as a placeholder
        return true;
    }

    /**
     * Checks if an operations user can receive a specific update.
     */
    private boolean canOperationsUserReceiveUpdate(Object exceptionEvent) {
        // Operations users can see all exception updates except sensitive payload data
        // The actual payload filtering should be done at the field level
        log.debug("Operations user - allowing exception update");
        return true;
    }

    /**
     * Checks if a viewer can receive a specific update.
     */
    private boolean canViewerReceiveUpdate(Object exceptionEvent) {
        // Viewers can see basic exception information but not sensitive details
        // Additional filtering may be needed based on customer/location restrictions
        log.debug("Viewer user - allowing basic exception update");
        return true;
    }

    /**
     * Extracts user roles from authentication context.
     */
    private Set<String> getUserRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    /**
     * Filters for GraphQL exception subscriptions.
     */
    public static class ExceptionSubscriptionFilters {
        private List<String> interfaceTypes;
        private List<String> statuses;
        private List<String> severities;
        private List<String> customerIds;
        private List<String> locationCodes;
        private String searchTerm;
        private Boolean excludeResolved;

        // Getters and setters
        public List<String> getInterfaceTypes() {
            return interfaceTypes;
        }

        public void setInterfaceTypes(List<String> interfaceTypes) {
            this.interfaceTypes = interfaceTypes;
        }

        public List<String> getStatuses() {
            return statuses;
        }

        public void setStatuses(List<String> statuses) {
            this.statuses = statuses;
        }

        public List<String> getSeverities() {
            return severities;
        }

        public void setSeverities(List<String> severities) {
            this.severities = severities;
        }

        public List<String> getCustomerIds() {
            return customerIds;
        }

        public void setCustomerIds(List<String> customerIds) {
            this.customerIds = customerIds;
        }

        public List<String> getLocationCodes() {
            return locationCodes;
        }

        public void setLocationCodes(List<String> locationCodes) {
            this.locationCodes = locationCodes;
        }

        public String getSearchTerm() {
            return searchTerm;
        }

        public void setSearchTerm(String searchTerm) {
            this.searchTerm = searchTerm;
        }

        public Boolean getExcludeResolved() {
            return excludeResolved;
        }

        public void setExcludeResolved(Boolean excludeResolved) {
            this.excludeResolved = excludeResolved;
        }

        @Override
        public String toString() {
            return "ExceptionSubscriptionFilters{" +
                    "interfaceTypes=" + interfaceTypes +
                    ", statuses=" + statuses +
                    ", severities=" + severities +
                    ", customerIds=" + customerIds +
                    ", locationCodes=" + locationCodes +
                    ", searchTerm='" + searchTerm + '\'' +
                    ", excludeResolved=" + excludeResolved +
                    '}';
        }
    }
}