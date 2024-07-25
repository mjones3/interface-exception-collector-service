package com.arcone.biopro.distribution.shipping.infrastructure.listener.dto;

import java.io.Serializable;

public record ShortDateItem(
    String unitNumber,
    String productCode,
    String storageLocation

) implements Serializable {
}
