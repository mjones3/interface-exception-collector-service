package com.arcone.biopro.exception.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating and publishing exception events to Kafka.
 * This is primarily used for testing the end-to-end Kafka flow.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create and publish an exception event")
public class CreateExceptionRequest {

    @NotBlank(message = "External ID is required")
    @Schema(description = "External identifier from the source system", example = "ORDER-123456")
    @JsonProperty("externalId")
    private String externalId;

    @NotNull(message = "Operation is required")
    @Schema(description = "Operation that was being performed", example = "CREATE_ORDER")
    @JsonProperty("operation")
    private OrderOperation operation;

    @NotBlank(message = "Rejected reason is required")
    @Schema(description = "Reason for the rejection/exception", example = "Insufficient inventory for blood type O-negative")
    @JsonProperty("rejectedReason")
    private String rejectedReason;

    @Schema(description = "Customer identifier", example = "CUST-MOUNT-SINAI-001")
    @JsonProperty("customerId")
    private String customerId;

    @Schema(description = "Location code where the exception occurred", example = "HOSP-NYC-001")
    @JsonProperty("locationCode")
    private String locationCode;

    @Schema(description = "List of order items involved in the exception")
    @JsonProperty("orderItems")
    private List<OrderItem> orderItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Order item details")
    public static class OrderItem {

        @NotBlank(message = "Blood type is required")
        @Schema(description = "Blood type", example = "O-")
        @JsonProperty("bloodType")
        private String bloodType;

        @NotBlank(message = "Product family is required")
        @Schema(description = "Product family", example = "RED_BLOOD_CELLS")
        @JsonProperty("productFamily")
        private String productFamily;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Schema(description = "Quantity requested", example = "2")
        @JsonProperty("quantity")
        private Integer quantity;
    }

    /**
     * Enumeration of valid order operations.
     */
    @Schema(description = "Valid order operations")
    public enum OrderOperation {
        CREATE_ORDER,
        MODIFY_ORDER,
        CANCEL_ORDER
    }
}