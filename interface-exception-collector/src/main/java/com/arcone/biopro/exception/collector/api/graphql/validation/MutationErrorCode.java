package com.arcone.biopro.exception.collector.api.graphql.validation;

/**
 * Enumeration of specific error codes for GraphQL mutation operations.
 * Provides detailed error categorization for better client error handling
 * and debugging support.
 */
public enum MutationErrorCode {

    // Input Validation Errors (VALIDATION_xxx)
    INVALID_TRANSACTION_ID("VALIDATION_001", "Invalid transaction ID format"),
    MISSING_REQUIRED_FIELD("VALIDATION_002", "Required field is missing or empty"),
    INVALID_FIELD_VALUE("VALIDATION_003", "Field value is invalid or out of range"),
    INVALID_REASON_LENGTH("VALIDATION_004", "Reason text exceeds maximum length"),
    INVALID_NOTES_LENGTH("VALIDATION_005", "Notes text exceeds maximum length"),
    INVALID_PRIORITY_VALUE("VALIDATION_006", "Invalid retry priority value"),
    INVALID_RESOLUTION_METHOD("VALIDATION_007", "Invalid resolution method specified"),

    // Business Rule Errors (BUSINESS_xxx)
    EXCEPTION_NOT_FOUND("BUSINESS_001", "Exception not found for the specified transaction ID"),
    NOT_RETRYABLE("BUSINESS_002", "Exception is not retryable"),
    RETRY_LIMIT_EXCEEDED("BUSINESS_003", "Maximum retry attempts exceeded"),
    INVALID_STATUS_TRANSITION("BUSINESS_004", "Invalid status transition for current exception state"),
    PENDING_RETRY_EXISTS("BUSINESS_005", "A retry operation is already pending for this exception"),
    ALREADY_RESOLVED("BUSINESS_006", "Exception is already resolved and cannot be modified"),
    ALREADY_ACKNOWLEDGED("BUSINESS_007", "Exception is already acknowledged"),
    NO_PENDING_RETRY("BUSINESS_008", "No pending retry found to cancel"),
    RETRY_ALREADY_COMPLETED("BUSINESS_009", "Retry operation has already completed"),
    INVALID_EXCEPTION_STATE("BUSINESS_010", "Exception is in an invalid state for this operation"),

    // Permission and Security Errors (SECURITY_xxx)
    INSUFFICIENT_PERMISSIONS("SECURITY_001", "Insufficient permissions for this operation"),
    BULK_OPERATION_NOT_ALLOWED("SECURITY_002", "Bulk operations not allowed for this user role"),
    BULK_SIZE_EXCEEDED("SECURITY_003", "Bulk operation size exceeds maximum allowed limit"),
    OPERATION_NOT_AUTHORIZED("SECURITY_004", "User not authorized to perform this operation"),
    RATE_LIMIT_EXCEEDED("SECURITY_005", "Rate limit exceeded for mutation operations"),

    // System and Database Errors (SYSTEM_xxx)
    DATABASE_ERROR("SYSTEM_001", "Database operation failed"),
    EXTERNAL_SERVICE_ERROR("SYSTEM_002", "External service unavailable or failed"),
    CONCURRENT_MODIFICATION("SYSTEM_003", "Resource was modified by another operation"),
    OPERATION_TIMEOUT("SYSTEM_004", "Operation timed out"),
    RESOURCE_LOCKED("SYSTEM_005", "Resource is currently locked by another operation"),

    // Retry-Specific Errors (RETRY_xxx)
    RETRY_NOT_ALLOWED("RETRY_001", "Retry operation is not allowed for this exception"),
    RETRY_SERVICE_UNAVAILABLE("RETRY_002", "Retry service is currently unavailable"),
    RETRY_CONFIGURATION_ERROR("RETRY_003", "Retry configuration is invalid or missing"),
    MAX_CONCURRENT_RETRIES("RETRY_004", "Maximum concurrent retry operations exceeded"),

    // Acknowledgment-Specific Errors (ACK_xxx)
    ACKNOWLEDGMENT_NOT_ALLOWED("ACK_001", "Acknowledgment is not allowed for this exception"),
    INVALID_ACKNOWLEDGMENT_DATA("ACK_002", "Acknowledgment data is invalid or incomplete"),
    ACKNOWLEDGMENT_EXPIRED("ACK_003", "Acknowledgment period has expired"),

    // Resolution-Specific Errors (RESOLVE_xxx)
    RESOLUTION_NOT_ALLOWED("RESOLVE_001", "Resolution is not allowed for this exception"),
    INVALID_RESOLUTION_DATA("RESOLVE_002", "Resolution data is invalid or incomplete"),
    RESOLUTION_REQUIRES_ACKNOWLEDGMENT("RESOLVE_003", "Exception must be acknowledged before resolution"),

    // Cancellation-Specific Errors (CANCEL_xxx)
    CANCELLATION_NOT_ALLOWED("CANCEL_001", "Cancellation is not allowed for this retry"),
    CANCELLATION_TOO_LATE("CANCEL_002", "Retry has progressed too far to be cancelled"),
    CANCELLATION_FAILED("CANCEL_003", "Failed to cancel the retry operation");

    private final String code;
    private final String defaultMessage;

    MutationErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    /**
     * Gets the error code string.
     *
     * @return the error code
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the default error message.
     *
     * @return the default message
     */
    public String getDefaultMessage() {
        return defaultMessage;
    }

    /**
     * Gets the error category based on the code prefix.
     *
     * @return the error category
     */
    public String getCategory() {
        if (code.startsWith("VALIDATION_")) {
            return "INPUT_VALIDATION";
        } else if (code.startsWith("BUSINESS_")) {
            return "BUSINESS_RULE";
        } else if (code.startsWith("SECURITY_")) {
            return "SECURITY";
        } else if (code.startsWith("SYSTEM_")) {
            return "SYSTEM";
        } else if (code.startsWith("RETRY_")) {
            return "RETRY_OPERATION";
        } else if (code.startsWith("ACK_")) {
            return "ACKNOWLEDGMENT";
        } else if (code.startsWith("RESOLVE_")) {
            return "RESOLUTION";
        } else if (code.startsWith("CANCEL_")) {
            return "CANCELLATION";
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * Determines if this error is retryable by the client.
     *
     * @return true if the operation can be retried
     */
    public boolean isRetryable() {
        // System errors and external service errors are typically retryable
        return code.startsWith("SYSTEM_") || 
               this == EXTERNAL_SERVICE_ERROR ||
               this == OPERATION_TIMEOUT ||
               this == RETRY_SERVICE_UNAVAILABLE;
    }

    /**
     * Determines if this error is a client error (4xx equivalent).
     *
     * @return true if this is a client error
     */
    public boolean isClientError() {
        return code.startsWith("VALIDATION_") ||
               code.startsWith("BUSINESS_") ||
               code.startsWith("SECURITY_") ||
               code.startsWith("RETRY_") ||
               code.startsWith("ACK_") ||
               code.startsWith("RESOLVE_") ||
               code.startsWith("CANCEL_");
    }

    /**
     * Determines if this error is a server error (5xx equivalent).
     *
     * @return true if this is a server error
     */
    public boolean isServerError() {
        return code.startsWith("SYSTEM_");
    }

    @Override
    public String toString() {
        return code + ": " + defaultMessage;
    }
}