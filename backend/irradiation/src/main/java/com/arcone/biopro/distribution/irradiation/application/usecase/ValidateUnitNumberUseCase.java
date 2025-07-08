package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.aggregate.IrradiationAggregate;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.InventoryClient;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ValidateUnitNumberUseCase {
    private final InventoryClient inventoryClient;

    public ValidateUnitNumberUseCase(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    public Flux<Inventory> execute(String unitNumber, String location) {
        Location targetLocation = Location.of(location);
        return inventoryClient.getInventoryByUnitNumber(UnitNumber.of(unitNumber))
                .collectList()
                .map(inventories -> new IrradiationAggregate(null, inventories))
                .flatMapMany(aggregate -> Flux.fromIterable(aggregate.getValidInventories(targetLocation)));
    }
}
