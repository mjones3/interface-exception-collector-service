package com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine;

import java.time.ZonedDateTime;

public record AddQuarantinedMessage(
    Long id,
    String unitNumber,
    String productCode,
    String reason,
    String comments,
    Boolean stopsManufacturing,
    String performedBy,
    ZonedDateTime createDate) {}
