package com.arcone.biopro.distribution.irradiation.domain.irradiation.port;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.client.InventoryOutput;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InventoryClient {
    Flux<Inventory> getInventoryByUnitNumber(UnitNumber unitNumber);
    Mono<InventoryOutput> getInventoryByUnitNumberAndProductCode(UnitNumber unitNumber, String productCode);
}
