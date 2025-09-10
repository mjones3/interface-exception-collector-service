package com.arcone.biopro.exception.collector.api.graphql.validation;

import com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents the result of a validation operation with detailed error categorization.
 * Provides structured validation results for GraphQL mutations with specific error codes
 * and categorization for better client error handling.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {

    /**
     * Whether the validation passed successfully.
     */
    @Builder.Default
    private boolean valid = true;

    /**
     * List of validation errors found during validation.
     */
    @Builder.Default
    private List<GraphQLError> errors = new ArrayList<>();

    /**
     * Timestamp when validation was performed.
     */
    @Builder.Default
    private Instant validationTimestamp = Instant.now();

    /**
     * Additional validation context or metadata.
     */
    private Map<String, Object> validationContext;

    /**
     * The operation being validated (e.g., "retry", "acknowledge", "resolve", "cancel").
     */
    private String operation;

    /**
     * The transaction ID being validated.
     */
    private String transactionId;

    /**
     * Creates a successful validation result.
     *
     * @return ValidationResult indicating success
     */
    public static ValidationResult success() {
        return ValidationResult.builder()
                .valid(true)
                .errors(new ArrayList<>())
                .build();
    }

    /**
     * Creates a successful validation result with context.
     *
     * @param operation     the operation being validated
     * @param transactionId the transaction ID
     * @return ValidationResult indicating success
     */
    public static ValidationResult success(String operation, String transactionId) {
        return ValidationResult.builder()
                .valid(true)
                .errors(new ArrayList<>())
                .operation(operation)
                .transactionId(transactionId)
                .build();
    }

    /**
     * Creates a failed validation result with a single error.
     *
     * @param error the validation error
     * @return ValidationResult indicating failure
     */
    public static ValidationResult failure(GraphQLError error) {
        List<GraphQLError> errors = new ArrayList<>();
        errors.add(error);
        return ValidationResult.builder()
                .valid(false)
                .errors(errors)
                .build();
    }

    /**
     * Creates a failed validation result with multiple errors.
     *
     * @param errors the validation errors
     * @return ValidationResult indicating failure
     */
    public static ValidationResult failure(List<GraphQLError> errors) {
        return ValidationResult.builder()
                .valid(false)
                .errors(new ArrayList<>(errors))
                .build();
    }

    /**
     * Creates a failed validation result with context.
     *
     * @param operation     the operation being validated
     * @param transactionId the transaction ID
     * @param errors        the validation errors
     * @return ValidationResult indicating failure
     */
    public static ValidationResult failure(String operation, String transactionId, List<GraphQLError> errors) {
        return ValidationResult.builder()
                .valid(false)
                .errors(new ArrayList<>(errors))
                .operation(operation)
                .transactionId(transactionId)
                .build();
    }

    /**
     * Adds a validation error to this result.
     *
     * @param error the error to add
     * @return this ValidationResult for method chaining
     */
    public ValidationResult addError(GraphQLError error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
        this.valid = false;
        return this;
    }

    /**
     * Adds multiple validation errors to this result.
     *
     * @param errors the errors to add
     * @return this ValidationResult for method chaining
     */
    public ValidationResult addErrors(List<GraphQLError> errors) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.addAll(errors);
        if (!errors.isEmpty()) {
            this.valid = false;
        }
        return this;
    }

    /**
     * Checks if this validation result has any errors.
     *
     * @return true if there are validation errors
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Gets the number of validation errors.
     *
     * @return the error count
     */
    public int getErrorCount() {
        return errors != null ? errors.size() : 0;
    }

    /**
     * Checks if this validation result is invalid (has errors).
     *
     * @return true if validation failed
     */
    public boolean isInvalid() {
        return !valid;
    }

    /**
     * Sets validation context information.
     *
     * @param operation     the operation being validated
     * @param transactionId the transaction ID
     * @return this ValidationResult for method chaining
     */
    public ValidationResult withContext(String operation, String transactionId) {
        this.operation = operation;
        this.transactionId = transactionId;
        return this;
    }

    /**
     * Sets additional validation context metadata.
     *
     * @param context the validation context
     * @return this ValidationResult for method chaining
     */
    public ValidationResult withValidationContext(Map<String, Object> context) {
        this.validationContext = context;
        return this;
    }
}