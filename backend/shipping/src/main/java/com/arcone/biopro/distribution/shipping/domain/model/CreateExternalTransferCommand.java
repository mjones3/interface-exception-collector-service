package com.arcone.biopro.distribution.shipping.domain.model;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;

@Builder
public record CreateExternalTransferCommand(
    String customerCode,
    String hospitalTransferId,
    LocalDate transferDate,
    String createEmployeeId
) implements Serializable {
}
