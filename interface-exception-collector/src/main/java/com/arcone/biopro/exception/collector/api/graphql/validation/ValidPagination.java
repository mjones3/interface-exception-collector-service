package com.arcone.biopro.exception.collector.api.graphql.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for pagination parameters.
 * Validates pagination limits and cursor format.
 */
@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PaginationValidator.class)
@Documented
public @interface ValidPagination {

    String message() default "Invalid pagination parameters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Maximum allowed page size.
     */
    int maxPageSize() default 100;

    /**
     * Default page size if not specified.
     */
    int defaultPageSize() default 20;
}