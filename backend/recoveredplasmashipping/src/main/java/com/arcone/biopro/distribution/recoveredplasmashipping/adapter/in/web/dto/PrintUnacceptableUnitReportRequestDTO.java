package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

@Builder
public record PrintUnacceptableUnitReportRequestDTO(
    Long shipmentId,
    String employeeId,
    String locationCode
) {
}
