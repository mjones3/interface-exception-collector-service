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

/**
 * Kafka event representing a distribution failure from the Distribution
 * Service.
 * Contains all necessary information to create an interface exception.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DistributionFailedEvent extends BaseEvent {

    @NotNull(message = "Payload is required")
    @Valid
    @JsonProperty("payload")
    private DistributionFailedPayload payload;

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistributionFailedPayload {

        @NotNull(message = "Transaction ID is required")
        @JsonProperty("transactionId")
        private String transactionId;

        @JsonProperty("distributionId")
        private String distributionId;

        @NotNull(message = "Operation is required")
        @JsonProperty("operation")
        private String operation;

        @NotNull(message = "Failure reason is required")
        @JsonProperty("failureReason")
        private String failureReason;

        @JsonProperty("customerId")
        private String customerId;

        @JsonProperty("destinationLocation")
        private String destinationLocation;
    }
}