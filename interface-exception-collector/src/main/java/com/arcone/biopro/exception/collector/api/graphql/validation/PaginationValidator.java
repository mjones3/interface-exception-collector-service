package com.arcone.biopro.exception.collector.api.graphql.validation;

import com.arcone.biopro.exception.collector.api.graphql.dto.PaginationInput;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.Base64;

/**
 * Validator for pagination parameters and business rules.
 */
public class PaginationValidator implements ConstraintValidator<ValidPagination, PaginationInput> {

    private int maxPageSize;
    private int defaultPageSize;

    @Override
    public void initialize(ValidPagination constraintAnnotation) {
        this.maxPageSize = constraintAnnotation.maxPageSize();
        this.defaultPageSize = constraintAnnotation.defaultPageSize();
    }

    @Override
    public boolean isValid(PaginationInput pagination, ConstraintValidatorContext context) {
        if (pagination == null) {
            return true; // Let @NotNull handle null validation
        }

        boolean hasForwardPagination = pagination.getFirst() != null || pagination.getAfter() != null;
        boolean hasBackwardPagination = pagination.getLast() != null || pagination.getBefore() != null;

        // Cannot use both forward and backward pagination
        if (hasForwardPagination && hasBackwardPagination) {
            addConstraintViolation(context,
                    "Cannot use both forward pagination (first/after) and backward pagination (last/before)");
            return false;
        }

        // Validate page size limits
        Integer pageSize = pagination.getFirst() != null ? pagination.getFirst() : pagination.getLast();
        if (pageSize != null) {
            if (pageSize <= 0) {
                addConstraintViolation(context, "Page size must be greater than 0");
                return false;
            }
            if (pageSize > maxPageSize) {
                addConstraintViolation(context,
                        String.format("Page size cannot exceed %d (requested: %d)", maxPageSize, pageSize));
                return false;
            }
        }

        // Validate cursor format
        if (!isValidCursor(pagination.getAfter())) {
            addConstraintViolation(context, "Invalid 'after' cursor format");
            return false;
        }

        if (!isValidCursor(pagination.getBefore())) {
            addConstraintViolation(context, "Invalid 'before' cursor format");
            return false;
        }

        return true;
    }

    private boolean isValidCursor(String cursor) {
        if (!StringUtils.hasText(cursor)) {
            return true; // Null or empty cursors are valid
        }

        try {
            // Validate that cursor is valid Base64
            Base64.getDecoder().decode(cursor);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}