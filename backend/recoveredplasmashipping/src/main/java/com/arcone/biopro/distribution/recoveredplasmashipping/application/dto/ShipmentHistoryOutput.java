package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Builder
public record ShipmentHistoryOutput(
    Long id,
    Long shipmentId,
    String comments,
    String createEmployeeId,
    ZonedDateTime createDate

) implements Serializable {
}
