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
 * Kafka event representing a collection rejection from the Collection Service.
 * Contains all necessary information to create an interface exception.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CollectionRejectedEvent extends BaseEvent {

    @NotNull(message = "Payload is required")
    @Valid
    @JsonProperty("payload")
    private CollectionRejectedPayload payload;

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollectionRejectedPayload {

        @NotNull(message = "Transaction ID is required")
        @JsonProperty("transactionId")
        private String transactionId;

        @JsonProperty("collectionId")
        private String collectionId;

        @NotNull(message = "Operation is required")
        @JsonProperty("operation")
        private String operation;

        @NotNull(message = "Rejected reason is required")
        @JsonProperty("rejectedReason")
        private String rejectedReason;

        @JsonProperty("donorId")
        private String donorId;

        @JsonProperty("locationCode")
        private String locationCode;
    }
}