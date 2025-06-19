package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

@Builder
public record PrintShippingSummaryReportCommandInput(
    Long shipmentId,
    String employeeId,
    String locationCode
) {
}
