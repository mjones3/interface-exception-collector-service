package com.arcone.biopro.exception.collector.api.graphql.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for retry operations.
 * Validates retry business rules and constraints.
 */
@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RetryOperationValidator.class)
@Documented
public @interface ValidRetryOperation {

    String message() default "Invalid retry operation";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Maximum allowed reason length.
     */
    int maxReasonLength() default 500;
}