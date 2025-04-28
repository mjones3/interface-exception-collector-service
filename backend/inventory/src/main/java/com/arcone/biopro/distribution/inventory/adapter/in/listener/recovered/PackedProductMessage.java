package com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered;

public record PackedProductMessage(
        String unitNumber,
        String productCode,
        String unpackedByEmployeeId,
        String status
    ) {}
