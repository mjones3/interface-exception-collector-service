package com.arcone.biopro.distribution.irradiation.domain.irradiation.port;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import reactor.core.publisher.Flux;

public interface InventoryClient {
    Flux<Inventory> getInventoryByUnitNumber(UnitNumber unitNumber);
}