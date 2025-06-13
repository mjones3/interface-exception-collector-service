package com.arcone.biopro.distribution.shipping.adapter.in.web.dto;

import java.io.Serializable;

public record CancelExternalTransferRequestDTO(
    Long externalTransferId,
    String employeeId
) implements Serializable {
}
