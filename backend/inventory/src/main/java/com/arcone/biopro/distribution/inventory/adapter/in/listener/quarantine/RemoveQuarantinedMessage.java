package com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine;

import java.time.ZonedDateTime;

public record RemoveQuarantinedMessage(
    Long id, String unitNumber,
                                       String productCode,
                                       String reason,
                                       Boolean stopsManufacturing,
                                       String performedBy,
                                       ZonedDateTime createDate) {}
