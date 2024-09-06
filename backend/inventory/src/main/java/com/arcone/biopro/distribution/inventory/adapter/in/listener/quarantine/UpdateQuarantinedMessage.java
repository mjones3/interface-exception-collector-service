package com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine;

import java.time.ZonedDateTime;

public record UpdateQuarantinedMessage(
    Long id,
    String unitNumber,
    String productCode,
    String newReason,
    String comments,
    Boolean stopsManufacturing,
    String performedBy,
    ZonedDateTime createDate) {}
