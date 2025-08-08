package com.arcone.biopro.exception.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO for error responses providing consistent error information across all
 * endpoints.
 * Used for validation errors, not found errors, and other API errors.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response for API failures")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "400")
    private Integer status;

    @Schema(description = "Error type or category", example = "VALIDATION_ERROR")
    private String error;

    @Schema(description = "Human-readable error message", example = "Invalid request parameters")
    private String message;

    @Schema(description = "API path where the error occurred", example = "/api/v1/exceptions")
    private String path;

    @Schema(description = "When the error occurred", example = "2025-08-04T10:30:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime timestamp;

    @Schema(description = "Detailed validation errors for field-specific issues")
    private List<FieldError> fieldErrors;

    /**
     * DTO for field-specific validation errors.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Field-specific validation error")
    public static class FieldError {

        @Schema(description = "Name of the field with the error", example = "interfaceType")
        private String field;

        @Schema(description = "Rejected value", example = "INVALID_TYPE")
        private Object rejectedValue;

        @Schema(description = "Error message for this field", example = "Must be one of: ORDER, COLLECTION, DISTRIBUTION, RECRUITMENT")
        private String message;
    }
}