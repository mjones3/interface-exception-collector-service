package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.client;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.InventoryClient;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class InventoryRSocketClient implements InventoryClient {
//    private final RSocketRequester rSocketRequester;
//
//    public InventoryRSocketClient(RSocketRequester rSocketRequester) {
//        this.rSocketRequester = rSocketRequester;
//    }

    @Override
    public Flux<Inventory> getInventoryByUnitNumber(UnitNumber unitNumber) {
        return Flux.empty();
//        return rSocketRequester
//                .route("getInventoryByUnitNumber")
//                .data(unitNumber.getValue())
//                .retrieveFlux(InventoryOutput.class)
//                .map(output -> new Inventory(
//                        UnitNumber.of(output.getUnitNumber()),
//                        output.getProductCode(),
//                        Location.of(output.getLocation()),
//                        output.getStatus()
//                ));
    }
}
