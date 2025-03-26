package com.arcone.biopro.distribution.shipping.infrastructure.listener.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ExternalTransferOutputItem(
    String unitNumber,
    String productCode
) implements Serializable {
}
