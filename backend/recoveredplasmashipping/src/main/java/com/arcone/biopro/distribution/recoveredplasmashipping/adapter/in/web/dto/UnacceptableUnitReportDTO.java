package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record UnacceptableUnitReportDTO(
    String shipmentNumber,
    String reportTitle,
    String dateTimeExported,
    String noProductsFlaggedMessage,
    List<UnacceptableUnitReportItemDTO> failedProducts
) {
}
