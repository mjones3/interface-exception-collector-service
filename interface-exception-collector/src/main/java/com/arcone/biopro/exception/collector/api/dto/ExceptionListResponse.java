package com.arcone.biopro.exception.collector.api.dto;

import com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * DTO for exception list response containing essential exception information
 * for listing endpoints. Implements requirements US-007 for exception listing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Exception information for list responses")
public class ExceptionListResponse {

    @Schema(description = "Unique exception identifier", example = "12345")
    private Long id;

    @Schema(description = "Unique transaction identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private String transactionId;

    @Schema(description = "Interface type that generated the exception", example = "ORDER")
    private InterfaceType interfaceType;

    @Schema(description = "Brief description of the exception", example = "Order already exists")
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

    @Schema(description = "Number of retry attempts made", example = "2")
    private Integer retryCount;

    @Schema(description = "When the exception was last retried", example = "2025-08-04T11:15:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime lastRetryAt;

    @Schema(description = "Whether order data is available for this exception", example = "true")
    private Boolean hasOrderData;
}