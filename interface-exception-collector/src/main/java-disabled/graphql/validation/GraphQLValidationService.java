package com.arcone.biopro.exception.collector.api.graphql.validation;

import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionFilters;
import com.arcone.biopro.exception.collector.api.graphql.dto.PaginationInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.exception.GraphQLErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;

/**
 * Service for performing comprehensive validation of GraphQL inputs.
 * Combines Bean Validation with custom business rule validation.
 */
@Service
@Slf4j
public class GraphQLValidationService {

    @Autowired
    private Validator validator;

    /**
     * Validates exception filters input.
     */
    public void validateExceptionFilters(ExceptionFilters filters) {
        if (filters == null) {
            return; // Null filters are allowed
        }

        Set<ConstraintViolation<ExceptionFilters>> violations = validator.validate(filters);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("Exception filters validation failed", violations);
        }

        // Additional business rule validation
        validateFilterCombinations(filters);
    }

    /**
     * Validates pagination input.
     */
    public void validatePaginationInput(PaginationInput pagination) {
        if (pagination == null) {
            return; // Null pagination is allowed (will use defaults)
        }

        Set<ConstraintViolation<PaginationInput>> violations = validator.validate(pagination);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("Pagination validation failed", violations);
        }
    }

    /**
     * Validates retry exception input.
     */
    public void validateRetryExceptionInput(RetryExceptionInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Retry input cannot be null");
        }

        Set<ConstraintViolation<RetryExceptionInput>> violations = validator.validate(input);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("Retry input validation failed", violations);
        }

        // Additional business rule validation
        validateRetryBusinessRules(input);
    }

    /**
     * Validates transaction ID format and business rules.
     */
    public void validateTransactionId(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID is required");
        }

        if (transactionId.length() < 8 || transactionId.length() > 64) {
            throw new IllegalArgumentException("Transaction ID must be between 8 and 64 characters");
        }

        if (!transactionId.matches("^[A-Za-z0-9\\-_]+$")) {
            throw new IllegalArgumentException("Transaction ID contains invalid characters");
        }
    }

    /**
     * Validates search term for security and performance.
     */
    public void validateSearchTerm(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return; // Empty search terms are allowed
        }

        if (searchTerm.length() > 100) {
            throw new IllegalArgumentException("Search term cannot exceed 100 characters");
        }

        // Check for potentially dangerous patterns
        if (containsSqlInjectionPatterns(searchTerm)) {
            throw new IllegalArgumentException("Search term contains invalid patterns");
        }

        // Check for excessive wildcard usage
        if (countWildcards(searchTerm) > 5) {
            throw new IllegalArgumentException("Search term contains too many wildcards");
        }
    }

    /**
     * Validates filter combinations for business rules.
     */
    private void validateFilterCombinations(ExceptionFilters filters) {
        // Validate date range if present
        if (filters.getDateRange() != null) {
            ExceptionFilters.DateRangeInput dateRange = filters.getDateRange();
            if (dateRange.getFrom() != null && dateRange.getTo() != null) {
                if (dateRange.getDurationInDays() > 90) {
                    throw new IllegalArgumentException("Date range cannot exceed 90 days for performance reasons");
                }
            }
        }

        // Validate filter complexity (prevent overly complex queries)
        int filterComplexity = calculateFilterComplexity(filters);
        if (filterComplexity > 50) {
            throw new IllegalArgumentException("Filter combination is too complex. Please simplify your query.");
        }
    }

    /**
     * Validates retry operation business rules.
     */
    private void validateRetryBusinessRules(RetryExceptionInput input) {
        // Validate reason content
        String reason = input.getReason();
        if (reason != null) {
            if (reason.trim().length() < 5) {
                throw new IllegalArgumentException("Retry reason must be at least 5 characters long");
            }

            // Check for meaningful content (not just repeated characters)
            if (isRepeatedCharacters(reason)) {
                throw new IllegalArgumentException("Retry reason must contain meaningful content");
            }
        }

        // Validate priority is reasonable
        if (input.getPriority() == RetryExceptionInput.RetryPriority.URGENT) {
            // In a real system, you might check user permissions for urgent retries
            log.info("Urgent retry requested for transaction: {}", input.getTransactionId());
        }
    }

    /**
     * Calculates the complexity score of filter combinations.
     */
    private int calculateFilterComplexity(ExceptionFilters filters) {
        int complexity = 0;

        if (filters.getInterfaceTypes() != null) {
            complexity += filters.getInterfaceTypes().size();
        }
        if (filters.getStatuses() != null) {
            complexity += filters.getStatuses().size();
        }
        if (filters.getSeverities() != null) {
            complexity += filters.getSeverities().size();
        }
        if (filters.getCategories() != null) {
            complexity += filters.getCategories().size();
        }
        if (filters.getCustomerIds() != null) {
            complexity += filters.getCustomerIds().size() * 2; // Customer filters are more expensive
        }
        if (filters.getLocationCodes() != null) {
            complexity += filters.getLocationCodes().size() * 2; // Location filters are more expensive
        }
        if (filters.getSearchTerm() != null && !filters.getSearchTerm().trim().isEmpty()) {
            complexity += 10; // Text search is expensive
        }
        if (filters.getDateRange() != null) {
            complexity += 5; // Date range filtering
        }

        return complexity;
    }

    /**
     * Checks for SQL injection patterns in search terms.
     */
    private boolean containsSqlInjectionPatterns(String input) {
        String lowerInput = input.toLowerCase();
        String[] dangerousPatterns = {
                "union", "select", "insert", "update", "delete", "drop", "create", "alter",
                "--", "/*", "*/", "xp_", "sp_", "exec", "execute", "script", "javascript"
        };

        for (String pattern : dangerousPatterns) {
            if (lowerInput.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Counts wildcard characters in search terms.
     */
    private int countWildcards(String input) {
        int count = 0;
        for (char c : input.toCharArray()) {
            if (c == '*' || c == '%' || c == '?') {
                count++;
            }
        }
        return count;
    }

    /**
     * Checks if a string consists mostly of repeated characters.
     */
    private boolean isRepeatedCharacters(String input) {
        if (input.length() < 3) {
            return false;
        }

        char firstChar = input.charAt(0);
        int sameCharCount = 0;

        for (char c : input.toCharArray()) {
            if (c == firstChar) {
                sameCharCount++;
            }
        }

        // If more than 80% of characters are the same, consider it repeated
        return (double) sameCharCount / input.length() > 0.8;
    }
}