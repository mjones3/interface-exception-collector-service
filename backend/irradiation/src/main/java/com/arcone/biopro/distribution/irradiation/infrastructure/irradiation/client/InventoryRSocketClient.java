package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.client;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.InventoryClient;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import com.arcone.biopro.distribution.irradiation.infrastructure.mapper.InventoryOutputMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class InventoryRSocketClient implements InventoryClient {

    private final RSocketRequester requester;
    private final InventoryOutputMapper mapper;
    
    public InventoryRSocketClient(@Qualifier("inventoryRSocketRequester") RSocketRequester requester, 
                                  InventoryOutputMapper mapper) {
        this.requester = requester;
        this.mapper = mapper;
    }


    @Override
    public Flux<Inventory> getInventoryByUnitNumber(UnitNumber unitNumber) {
        return requester
            .route("getInventoryByUnitNumber")
            .data(unitNumber.value())
            .retrieveFlux(InventoryOutput.class)
            .map(mapper::toDomain);
    }
}
