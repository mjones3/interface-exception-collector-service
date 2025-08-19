package com.arcone.biopro.exception.collector.api.graphql.validation;

import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

/**
 * Validator for retry operation business rules.
 */
public class RetryOperationValidator implements ConstraintValidator<ValidRetryOperation, RetryExceptionInput> {

    private int maxReasonLength;

    @Override
    public void initialize(ValidRetryOperation constraintAnnotation) {
        this.maxReasonLength = constraintAnnotation.maxReasonLength();
    }

    @Override
    public boolean isValid(RetryExceptionInput input, ConstraintValidatorContext context) {
        if (input == null) {
            return true; // Let @NotNull handle null validation
        }

        // Validate reason length
        String reason = input.getReason();
        if (StringUtils.hasText(reason) && reason.length() > maxReasonLength) {
            addConstraintViolation(context,
                    String.format("Retry reason cannot exceed %d characters (current: %d)",
                            maxReasonLength, reason.length()));
            return false;
        }

        // Validate reason is not just whitespace
        if (StringUtils.hasText(reason) && reason.trim().isEmpty()) {
            addConstraintViolation(context, "Retry reason cannot be empty or contain only whitespace");
            return false;
        }

        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}