package com.arcone.biopro.partner.order.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for payload retrieval requests.
 * Used by the Interface Exception Collector Service to retrieve original
 * payloads for retry operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Original payload retrieval response")
public class PayloadResponse {

    @Schema(description = "Transaction ID for the order", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    @JsonProperty("transactionId")
    private UUID transactionId;

    @Schema(description = "Complete original order payload as submitted", required = true)
    @JsonProperty("originalPayload")
    private JsonNode originalPayload;

    @Schema(description = "When the order was originally submitted", example = "2025-08-14T10:30:00Z", required = true)
    @JsonProperty("submittedAt")
    private OffsetDateTime submittedAt;

    @Schema(description = "Size of the payload in bytes", example = "1024")
    @JsonProperty("payloadSize")
    private Long payloadSize;

    @Schema(description = "External ID from the original order", example = "ORDER-123456")
    @JsonProperty("externalId")
    private String externalId;

    @Schema(description = "Location code from the original order", example = "HOSP-NYC-001")
    @JsonProperty("locationCode")
    private String locationCode;

    @Schema(description = "Current status of the order", example = "RECEIVED")
    @JsonProperty("status")
    private String status;

    @Schema(description = "When the order was last processed", example = "2025-08-14T10:30:05Z")
    @JsonProperty("processedAt")
    private OffsetDateTime processedAt;

    /**
     * Creates a payload response from order entity data.
     */
    public static PayloadResponse fromOrderData(UUID transactionId, JsonNode originalPayload,
            OffsetDateTime submittedAt, String externalId,
            String locationCode, String status,
            OffsetDateTime processedAt) {
        return PayloadResponse.builder()
                .transactionId(transactionId)
                .originalPayload(originalPayload)
                .submittedAt(submittedAt)
                .payloadSize(originalPayload != null ? (long) originalPayload.toString().length() : 0L)
                .externalId(externalId)
                .locationCode(locationCode)
                .status(status)
                .processedAt(processedAt)
                .build();
    }
}