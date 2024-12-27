package com.arcone.biopro.distribution.inventory.adapter.in.listener.checkin;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.created.ValueUnit;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.vo.InputProduct;

import java.time.ZonedDateTime;
import java.util.List;

public record CheckInCompletedMessage(
    String unitNumber,
    String productCode,
    String productDescription,
    String productFamily,
    String collectionLocation,
    ZonedDateTime drawTime,
    AboRhType aboRh) {
}
