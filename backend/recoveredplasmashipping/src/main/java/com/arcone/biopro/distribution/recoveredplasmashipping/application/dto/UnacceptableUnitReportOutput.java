package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record UnacceptableUnitReportOutput(
    String shipmentNumber,
    String reportTitle,
    String dateTimeExported,
    String noProductsFlaggedMessage,
    List<UnacceptableUnitReportItemOutput> failedProducts

) {

}
