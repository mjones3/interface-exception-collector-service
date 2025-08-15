package com.arcone.biopro.exception.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO for exception creation requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after creating and publishing an exception event")
public class CreateExceptionResponse {

    @Schema(description = "Generated transaction ID for the exception", example = "550e8400-e29b-41d4-a716-446655440000")
    @JsonProperty("transactionId")
    private String transactionId;

    @Schema(description = "Generated event ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("eventId")
    private String eventId;

    @Schema(description = "Correlation ID for tracking", example = "corr-123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("correlationId")
    private String correlationId;

    @Schema(description = "When the event was created and published")
    @JsonProperty("publishedAt")
    private OffsetDateTime publishedAt;

    @Schema(description = "Kafka topic the event was published to", example = "OrderRejected")
    @JsonProperty("topic")
    private String topic;

    @Schema(description = "Status of the publish operation", example = "SUCCESS")
    @JsonProperty("status")
    private String status;

    @Schema(description = "Additional message about the operation", example = "Exception event published successfully")
    @JsonProperty("message")
    private String message;
}