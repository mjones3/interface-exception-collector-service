package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.application.dto.IrradiationInventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.mapper.IrradiationInventoryMapper;
import com.arcone.biopro.distribution.irradiation.domain.exception.NoEligibleProductForIrradiationException;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.aggregate.IrradiationAggregate;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.InventoryClient;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ValidateUnitNumberUseCase {
    private final InventoryClient inventoryClient;
    private final IrradiationInventoryMapper mapper;

    public Flux<IrradiationInventoryOutput> execute(String unitNumber, String location) {
        Location targetLocation = new Location(location);
        return inventoryClient.getInventoryByUnitNumber(new UnitNumber(unitNumber))
            .switchIfEmpty(Mono.error(NoEligibleProductForIrradiationException::new))
            .collectList()
            .map(inventories -> new IrradiationAggregate(null, inventories, null))
            .flatMap(aggregate -> {
                var validInventories = aggregate.getValidInventoriesForIrradiation(targetLocation);
                if (validInventories.isEmpty()) {
                    return Mono.error(new NoEligibleProductForIrradiationException());
                }
                return Mono.just(validInventories);
            })
            .flatMapMany(Flux::fromIterable)
            .map(mapper::toDomain);
    }
}
