package com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered;

import java.util.List;

public record RecoveredPlasmaShipmentClosedMessage(
    String shipmentNumber,
    List<CartonMessage> cartonList
) {}
