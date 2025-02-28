package com.arcone.biopro.distribution.shipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;

@Builder
public record CreateExternalTransferRequestDTO(

    String customerCode,
    String hospitalTransferId,
    LocalDate transferDate,
    String createEmployeeId

) implements Serializable {
}
