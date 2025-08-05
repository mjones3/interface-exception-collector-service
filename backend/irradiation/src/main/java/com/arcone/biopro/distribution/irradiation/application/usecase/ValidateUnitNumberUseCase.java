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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidateUnitNumberUseCase {
    private final InventoryClient inventoryClient;
    private final IrradiationInventoryMapper mapper;
    private final BatchRepository batchRepository;
    private final ProductDeterminationRepository productDeterminationRepository;

    public Flux<IrradiationInventoryOutput> execute(String unitNumber, String location) {
        return this.getValidInventories(unitNumber, location)
            .switchIfEmpty(Mono.error(new NoEligibleProductForIrradiationException()))
            .flatMapMany(inventories -> Flux.fromIterable(inventories)
                .flatMap(this::enrichWithIrradiationFlags));
    }

    private Mono<List<Inventory>> getValidInventories(String unitNumber, String location) {
        Location targetLocation = new Location(location);
        return inventoryClient.getInventoryByUnitNumber(new UnitNumber(unitNumber))
            .log("INFO")
            .switchIfEmpty(Mono.error(NoEligibleProductForIrradiationException::new))
            .collectList()
            .map(inventories -> new IrradiationAggregate(null, inventories, null))
            .flatMap(aggregate -> {
                var validInventories = aggregate.getValidInventoriesForIrradiation(targetLocation);
                log.debug("ValidInventories for irradiation: {}", validInventories);
                return validInventories.isEmpty()
                    ? Mono.error(new NoEligibleProductForIrradiationException())
                    : Mono.just(validInventories);
            });
    }

    private Mono<IrradiationInventoryOutput> enrichWithIrradiationFlags(Inventory inventory) {
        String unitNumberValue = inventory.getUnitNumber().value();

        Mono<Boolean> alreadyIrradiated = batchRepository.isUnitAlreadyIrradiated(unitNumberValue, inventory.getProductCode());
        Mono<Boolean> notConfigurable = productDeterminationRepository
            .existsBySourceProductCode(new ProductCode(inventory.getProductCode()))
            .map(exists -> !exists);
        Mono<Boolean> isBeingIrradiated = batchRepository.isUnitBeingIrradiated(inventory.getUnitNumber().value(), inventory.getProductCode());


        return Mono.zip(alreadyIrradiated, notConfigurable, isBeingIrradiated)
            .map(tuple -> buildEnrichedDto(inventory, tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    private IrradiationInventoryOutput buildEnrichedDto(Inventory inventory,
                                                       boolean alreadyIrradiated, boolean notConfigurable, boolean isBeingIrradiated) {
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
            .isImported(dto.isImported())
            .quarantines(dto.quarantines())
            .alreadyIrradiated(alreadyIrradiated)
            .notConfigurableForIrradiation(notConfigurable)
            .isBeingIrradiated(isBeingIrradiated)
            .build();
    }
}
