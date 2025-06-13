package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

@Builder
public record PrintUnacceptableUnitReportCommandInput(
    Long shipmentId,
    String employeeId,
    String locationCode
) {
}
