package com.arcone.biopro.exception.collector.api.graphql.validation;

import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionFilters;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Validator for date range business rules.
 */
public class DateRangeValidator implements ConstraintValidator<ValidDateRange, ExceptionFilters.DateRangeInput> {

    private int maxRangeDays;
    private boolean allowFuture;

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.maxRangeDays = constraintAnnotation.maxRangeDays();
        this.allowFuture = constraintAnnotation.allowFuture();
    }

    @Override
    public boolean isValid(ExceptionFilters.DateRangeInput dateRange, ConstraintValidatorContext context) {
        if (dateRange == null) {
            return true; // Let @NotNull handle null validation
        }

        OffsetDateTime startDate = dateRange.getFrom();
        OffsetDateTime endDate = dateRange.getTo();

        if (startDate == null || endDate == null) {
            return true; // Let individual field validation handle nulls
        }

        // Check if start date is before end date
        if (startDate.isAfter(endDate)) {
            addConstraintViolation(context, "Start date must be before end date");
            return false;
        }

        // Check if dates are in the future (if not allowed)
        OffsetDateTime now = OffsetDateTime.now();
        if (!allowFuture) {
            if (startDate.isAfter(now)) {
                addConstraintViolation(context, "Start date cannot be in the future");
                return false;
            }
            if (endDate.isAfter(now)) {
                addConstraintViolation(context, "End date cannot be in the future");
                return false;
            }
        }

        // Check maximum range
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > maxRangeDays) {
            addConstraintViolation(context,
                    String.format("Date range cannot exceed %d days (current range: %d days)",
                            maxRangeDays, daysBetween));
            return false;
        }

        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}