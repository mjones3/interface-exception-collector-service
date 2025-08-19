package com.arcone.biopro.exception.collector.api.graphql.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Validator for transaction ID format and business rules.
 */
public class TransactionIdValidator implements ConstraintValidator<ValidTransactionId, String> {

    private static final Pattern TRANSACTION_ID_PATTERN = Pattern.compile("^[A-Za-z0-9\\-_]{8,64}$");
    private boolean allowNull;

    @Override
    public void initialize(ValidTransactionId constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return allowNull;
        }

        if (!StringUtils.hasText(value)) {
            addConstraintViolation(context, "Transaction ID cannot be empty");
            return false;
        }

        if (!TRANSACTION_ID_PATTERN.matcher(value).matches()) {
            addConstraintViolation(context,
                    "Transaction ID must be 8-64 characters long and contain only alphanumeric characters, hyphens, and underscores");
            return false;
        }

        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}