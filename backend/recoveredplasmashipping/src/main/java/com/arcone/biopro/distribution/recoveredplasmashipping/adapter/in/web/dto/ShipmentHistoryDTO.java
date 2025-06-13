package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Builder
public record ShipmentHistoryDTO(
    Long id,
    Long shipmentId,
    String comments,
    String createEmployeeId,
    ZonedDateTime createDate

) implements Serializable {
}
