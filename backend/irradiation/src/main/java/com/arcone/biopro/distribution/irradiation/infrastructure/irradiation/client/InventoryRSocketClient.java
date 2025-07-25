package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.client;

import com.arcone.biopro.distribution.irradiation.adapter.in.socket.dto.GetInventoryByUnitNumberAndProductCodeRequest;
import com.arcone.biopro.distribution.irradiation.domain.exception.InventoryValidationException;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.InventoryClient;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import com.arcone.biopro.distribution.irradiation.infrastructure.mapper.InventoryOutputMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
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
            .doOnEach(inv -> log.debug("getInventoryByUnitNumber result: {}", inv.get()))
            .doOnError(error -> log.error("Error getInventoryByUnitNumber: {}", error.getMessage()))
            .map(mapper::toDomain);
    }

    @Override
    public Mono<InventoryOutput> getInventoryByUnitNumberAndProductCode(UnitNumber unitNumber, String productCode) {
        return requester
            .route("getInventoryByUnitNumberAndProductCode")
            .data(new GetInventoryByUnitNumberAndProductCodeRequest(unitNumber.value(),productCode))
            .retrieveMono(InventoryOutput.class)
            .doOnNext(response -> log.debug("Found inventory: {}", response))
            .onErrorResume(error -> {
                log.error("Error getting inventory by unit number and product code: {}", error.getMessage());
                return Mono.error(new InventoryValidationException("Inventory service not available"));
            });

    }
}
