package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.application.dto.IrradiationInventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.mapper.IrradiationInventoryMapper;
import com.arcone.biopro.distribution.irradiation.domain.exception.NoEligibleProductForIrradiationException;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.aggregate.IrradiationAggregate;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.InventoryClient;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.ProductDeterminationRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.ProductCode;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidateUnitNumberUseCase {
    private final InventoryClient inventoryClient;
    private final IrradiationInventoryMapper mapper;
    private final BatchRepository batchRepository;
    private final ProductDeterminationRepository productDeterminationRepository;

    public Flux<IrradiationInventoryOutput> execute(String unitNumber, String location) {
        return getValidInventories(unitNumber, location)
            .flatMapMany(this::filterBeingIrradiatedUnits)
            .switchIfEmpty(Mono.error(new NoEligibleProductForIrradiationException()))
            .flatMap(this::enrichWithIrradiationFlags);
    }

    private Mono<List<Inventory>> getValidInventories(String unitNumber, String location) {
        Location targetLocation = new Location(location);
        return inventoryClient.getInventoryByUnitNumber(new UnitNumber(unitNumber))
            .switchIfEmpty(Mono.error(NoEligibleProductForIrradiationException::new))
            .collectList()
            .map(inventories -> new IrradiationAggregate(null, inventories, null))
            .flatMap(aggregate -> {
                var validInventories = aggregate.getValidInventoriesForIrradiation(targetLocation);
                return validInventories.isEmpty()
                    ? Mono.error(new NoEligibleProductForIrradiationException())
                    : Mono.just(validInventories);
            });
    }

    private Flux<Inventory> filterBeingIrradiatedUnits(List<Inventory> inventories) {
        return Flux.fromIterable(inventories)
            .flatMap(inventory ->
                batchRepository.isUnitBeingIrradiated(inventory.getUnitNumber().value())
                    .filter(beingIrradiated -> !beingIrradiated)
                    .map(ignored -> inventory)
            );
    }

    private Mono<IrradiationInventoryOutput> enrichWithIrradiationFlags(Inventory inventory) {
        String unitNumberValue = inventory.getUnitNumber().value();

        Mono<Boolean> alreadyIrradiated = batchRepository.isUnitAlreadyIrradiated(unitNumberValue);
        Mono<Boolean> notConfigurable = productDeterminationRepository
            .existsBySourceProductCode(new ProductCode(inventory.getProductCode()))
            .map(exists -> !exists);

        return Mono.zip(alreadyIrradiated, notConfigurable)
            .map(tuple -> buildEnrichedDto(inventory, tuple.getT1(), tuple.getT2()));
    }

    private IrradiationInventoryOutput buildEnrichedDto(Inventory inventory,
                                                       boolean alreadyIrradiated, boolean notConfigurable) {
        var dto = mapper.toDomain(inventory);
        return IrradiationInventoryOutput.builder()
            .unitNumber(dto.unitNumber())
            .productCode(dto.productCode())
            .location(dto.location())
            .status(dto.status())
            .productDescription(dto.productDescription())
            .productFamily(dto.productFamily())
            .shortDescription(dto.shortDescription())
            .isLabeled(dto.isLabeled())
            .statusReason(dto.statusReason())
            .unsuitableReason(dto.unsuitableReason())
            .expired(dto.expired())
            .quarantines(dto.quarantines())
            .alreadyIrradiated(alreadyIrradiated)
            .notConfigurableForIrradiation(notConfigurable)
            .build();
    }
}
