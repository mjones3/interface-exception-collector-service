package com.arcone.biopro.distribution.shipping.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Builder
public record ExternalTransferDTO(

    Long id,
    CustomerDTO customerTo,
    CustomerDTO customerFrom,
    String hospitalTransferId,
    LocalDate transferDate,
    String createEmployeeId,
    String status,
    List<ExternalTransferItemDTO> externalTransferItems
) implements Serializable {

}
