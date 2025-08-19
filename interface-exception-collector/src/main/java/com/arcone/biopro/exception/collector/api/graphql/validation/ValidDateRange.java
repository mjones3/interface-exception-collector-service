package com.arcone.biopro.exception.collector.api.graphql.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for date ranges.
 * Validates that start date is before end date and within reasonable limits.
 */
@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
@Documented
public @interface ValidDateRange {

    String message() default "Invalid date range";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Maximum allowed range in days.
     */
    int maxRangeDays() default 365;

    /**
     * Whether future dates are allowed.
     */
    boolean allowFuture() default false;
}