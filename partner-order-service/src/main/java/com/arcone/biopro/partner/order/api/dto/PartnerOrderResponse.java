package com.arcone.biopro.partner.order.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for partner order submissions.
 * Supports both success and error response formats.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Partner order submission response")
public class PartnerOrderResponse {

    @Schema(description = "Generated transaction ID for the order", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    @JsonProperty("transactionId")
    private UUID transactionId;

    @Schema(description = "Order processing status", example = "ACCEPTED", required = true)
    @JsonProperty("status")
    private String status;

    @Schema(description = "Response message", example = "Order accepted for processing", required = true)
    @JsonProperty("message")
    private String message;

    @Schema(description = "Response timestamp", example = "2025-08-14T10:30:00Z", required = true)
    @JsonProperty("timestamp")
    private OffsetDateTime timestamp;

    @Schema(description = "Correlation ID for tracing", example = "corr-550e8400-e29b-41d4-a716-446655440000")
    @JsonProperty("correlationId")
    private UUID correlationId;

    @Schema(description = "External ID from the original request", example = "ORDER-123456")
    @JsonProperty("externalId")
    private String externalId;

    @Schema(description = "Location code from the original request", example = "HOSP-NYC-001")
    @JsonProperty("locationCode")
    private String locationCode;

    /**
     * Creates a success response for accepted orders.
     */
    public static PartnerOrderResponse success(UUID transactionId, String externalId,
            String locationCode, UUID correlationId) {
        return PartnerOrderResponse.builder()
                .transactionId(transactionId)
                .status("ACCEPTED")
                .message("Order accepted for processing")
                .timestamp(OffsetDateTime.now())
                .correlationId(correlationId)
                .externalId(externalId)
                .locationCode(locationCode)
                .build();
    }

    /**
     * Creates an error response for rejected orders.
     */
    public static PartnerOrderResponse error(String status, String message,
            String externalId, UUID correlationId) {
        return PartnerOrderResponse.builder()
                .status(status)
                .message(message)
                .timestamp(OffsetDateTime.now())
                .correlationId(correlationId)
                .externalId(externalId)
                .build();
    }

    /**
     * Creates a validation error response.
     */
    public static PartnerOrderResponse validationError(String message,
            String externalId, UUID correlationId) {
        return error("VALIDATION_FAILED", message, externalId, correlationId);
    }

    /**
     * Creates a duplicate error response.
     */
    public static PartnerOrderResponse duplicateError(String externalId, UUID correlationId) {
        return error("DUPLICATE_EXTERNAL_ID",
                "Order with external ID '" + externalId + "' already exists",
                externalId, correlationId);
    }
}