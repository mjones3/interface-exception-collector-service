package com.arcone.biopro.exception.collector.domain.event.outbound;

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
 * Kafka event published when a critical exception alert is generated.
 * Notifies alerting systems and operations teams about urgent issues requiring
 * immediate attention.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CriticalExceptionAlertEvent extends BaseEvent {

    @NotNull(message = "Payload is required")
    @Valid
    @JsonProperty("payload")
    private CriticalExceptionAlertPayload payload;

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CriticalExceptionAlertPayload {

        @NotNull(message = "Exception ID is required")
        @JsonProperty("exceptionId")
        private Long exceptionId;

        @NotNull(message = "Transaction ID is required")
        @JsonProperty("transactionId")
        private String transactionId;

        @NotNull(message = "Alert level is required")
        @JsonProperty("alertLevel")
        private String alertLevel;

        @NotNull(message = "Alert reason is required")
        @JsonProperty("alertReason")
        private String alertReason;

        @NotNull(message = "Interface type is required")
        @JsonProperty("interfaceType")
        private String interfaceType;

        @NotNull(message = "Exception reason is required")
        @JsonProperty("exceptionReason")
        private String exceptionReason;

        @JsonProperty("customerId")
        private String customerId;

        @NotNull(message = "Escalation team is required")
        @JsonProperty("escalationTeam")
        private String escalationTeam;

        @NotNull(message = "Requires immediate action flag is required")
        @JsonProperty("requiresImmediateAction")
        private Boolean requiresImmediateAction;

        @NotNull(message = "Estimated impact is required")
        @JsonProperty("estimatedImpact")
        private String estimatedImpact;

        @JsonProperty("customersAffected")
        private Integer customersAffected;
    }
}