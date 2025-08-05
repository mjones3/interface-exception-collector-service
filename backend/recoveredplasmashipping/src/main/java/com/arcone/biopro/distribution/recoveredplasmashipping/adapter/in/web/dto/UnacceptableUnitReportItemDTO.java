package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record UnacceptableUnitReportItemDTO(
    String cartonNumber,
    Integer cartonSequenceNumber,
    String unitNumber,
    String productCode,
    String failureReason,
    ZonedDateTime createDate
) {
}
