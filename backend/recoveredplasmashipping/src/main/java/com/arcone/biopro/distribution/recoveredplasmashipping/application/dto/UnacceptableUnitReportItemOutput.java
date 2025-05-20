package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record UnacceptableUnitReportItemOutput(

    String cartonNumber,
    Integer cartonSequenceNumber,
    String unitNumber,
    String productCode,
    String failureReason,
    ZonedDateTime createDate
) {
}
