package com.arcone.biopro.exception.collector.api.graphql.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for transaction IDs.
 * Validates that transaction ID follows the expected format and business rules.
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TransactionIdValidator.class)
@Documented
public @interface ValidTransactionId {

    String message() default "Invalid transaction ID format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Whether null values are allowed.
     */
    boolean allowNull() default false;
}