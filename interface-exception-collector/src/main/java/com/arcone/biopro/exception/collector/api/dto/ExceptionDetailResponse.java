package com.arcone.biopro.exception.collector.api.dto;

import com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO for detailed exception response containing complete exception information
 * including retry history and related exceptions. Implements requirements
 * US-008.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Detailed exception information including history and related data")
public class ExceptionDetailResponse {

    @Schema(description = "Unique exception identifier", example = "12345")
    private Long id;

    @Schema(description = "Unique transaction identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private String transactionId;

    @Schema(description = "Interface type that generated the exception", example = "ORDER")
    private InterfaceType interfaceType;

    @Schema(description = "Detailed description of the exception", example = "Order already exists for customer CUST001")
    private String exceptionReason;

    @Schema(description = "Operation that caused the exception", example = "CREATE_ORDER")
    private String operation;

    @Schema(description = "External identifier from the source system", example = "ORDER-ABC123")
    private String externalId;

    @Schema(description = "Current status of the exception", example = "NEW")
    private ExceptionStatus status;

    @Schema(description = "Severity level of the exception", example = "MEDIUM")
    private ExceptionSeverity severity;

    @Schema(description = "Category classification of the exception", example = "BUSINESS_RULE")
    private ExceptionCategory category;

    @Schema(description = "Whether the exception can be retried", example = "true")
    private Boolean retryable;

    @Schema(description = "Customer identifier associated with the exception", example = "CUST001")
    private String customerId;

    @Schema(description = "Location code where the exception occurred", example = "LOC001")
    private String locationCode;

    @Schema(description = "When the exception originally occurred", example = "2025-08-04T10:30:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime timestamp;

    @Schema(description = "When the exception was processed by the system", example = "2025-08-04T10:30:05Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime processedAt;

    @Schema(description = "When the exception was acknowledged", example = "2025-08-04T10:45:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime acknowledgedAt;

    @Schema(description = "Who acknowledged the exception", example = "john.doe@biopro.com")
    private String acknowledgedBy;

    @Schema(description = "When the exception was resolved", example = "2025-08-04T11:30:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime resolvedAt;

    @Schema(description = "Who resolved the exception", example = "jane.smith@biopro.com")
    private String resolvedBy;

    @Schema(description = "Number of retry attempts made", example = "2")
    private Integer retryCount;

    @Schema(description = "Maximum number of retry attempts allowed", example = "3")
    private Integer maxRetries;

    @Schema(description = "When the exception was last retried", example = "2025-08-04T11:15:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime lastRetryAt;

    @Schema(description = "When the exception record was created", example = "2025-08-04T10:30:05Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime createdAt;

    @Schema(description = "When the exception record was last updated", example = "2025-08-04T11:30:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime updatedAt;

    @Schema(description = "Original payload from the source system (if requested and available)")
    private Object originalPayload;

    @Schema(description = "Complete order data retrieved from Partner Order Service or mock server (if available and requested)")
    private Object orderReceived;

    @Schema(description = "Whether order data retrieval was attempted", example = "true")
    private Boolean orderRetrievalAttempted;

    @Schema(description = "Error message if order retrieval failed", example = "Connection timeout to order service")
    private String orderRetrievalError;

    @Schema(description = "When order data was successfully retrieved", example = "2025-08-04T10:35:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime orderRetrievedAt;

    @Schema(description = "History of retry attempts for this exception")
    private List<RetryAttemptResponse> retryHistory;

    @Schema(description = "Related exceptions for the same customer")
    private List<ExceptionListResponse> relatedExceptions;

    /**
     * Legacy method for backward compatibility.
     * @deprecated Use maxRetries field directly
     */
    @Deprecated
    public Integer getMaxRetries() {
        return this.maxRetries;
    }
}