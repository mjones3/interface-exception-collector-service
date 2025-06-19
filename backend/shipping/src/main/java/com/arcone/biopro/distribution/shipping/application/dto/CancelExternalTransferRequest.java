package com.arcone.biopro.distribution.shipping.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record CancelExternalTransferRequest(
    Long externalTransferId,
    String employeeId
) implements Serializable {

}
