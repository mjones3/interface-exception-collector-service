package com.arcone.biopro.exception.collector.domain.event.inbound;

import com.arcone.biopro.exception.collector.domain.event.base.BaseEvent;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Kafka event representing validation errors from any interface service.
 * Contains all necessary information to create an interface exception.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ValidationErrorEvent extends BaseEvent {

    @NotNull(message = "Payload is required")
    @Valid
    @JsonProperty("payload")
    private ValidationErrorPayload payload;

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationErrorPayload {

        @NotNull(message = "Transaction ID is required")
        @JsonProperty("transactionId")
        private String transactionId;

        @NotNull(message = "Interface type is required")
        @JsonProperty("interfaceType")
        private String interfaceType;

        @NotNull(message = "Validation errors are required")
        @JsonProperty("validationErrors")
        private List<ValidationError> validationErrors;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {

        @JsonProperty("field")
        private String field;

        @JsonProperty("rejectedValue")
        private String rejectedValue;

        @NotNull(message = "Error message is required")
        @JsonProperty("message")
        private String message;

        @JsonProperty("errorCode")
        private String errorCode;
    }
}