package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

@Builder
public record PrintShippingSummaryReportRequestDTO(
    Long shipmentId,
    String employeeId,
    String locationCode
) {
}
