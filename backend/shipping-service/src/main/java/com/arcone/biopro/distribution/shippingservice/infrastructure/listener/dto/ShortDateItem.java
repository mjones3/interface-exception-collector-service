package com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto;

import java.io.Serializable;
import java.util.List;

public record ShortDateItem(
    String unitNumber,
    String productCode,
    String storageLocation

) implements Serializable {
}
