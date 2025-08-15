package com.arcone.biopro.partner.order.domain.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Event published when a partner order fails validation.
 * This event is consumed by the Interface Exception Collector Service
 * to track validation failures and provide debugging information.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InvalidOrderEvent extends BaseEvent {

    /**
     * The event payload containing validation failure details.
     */
    @JsonProperty("payload")
    private InvalidOrderPayload payload;

    /**
     * Payload data for InvalidOrder events.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvalidOrderPayload {

        /**
         * List of validation error messages.
         */
        @JsonProperty("validationErrors")
        private List<String> validationErrors;

        /**
         * Complete original request payload that failed validation.
         */
        @JsonProperty("originalRequest")
        private Object originalRequest;

        /**
         * Timestamp when the validation failure occurred.
         */
        @JsonProperty("failedAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        private OffsetDateTime failedAt;

        /**
         * External ID from the original request (if available).
         */
        @JsonProperty("externalId")
        private String externalId;

        /**
         * Location code from the original request (if available).
         */
        @JsonProperty("locationCode")
        private String locationCode;
    }
}