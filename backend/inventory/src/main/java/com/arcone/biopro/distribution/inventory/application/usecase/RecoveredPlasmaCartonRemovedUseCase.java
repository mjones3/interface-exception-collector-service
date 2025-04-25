package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.PackedProductInput;
import com.arcone.biopro.distribution.inventory.application.dto.RecoveredPlasmaCartonRemovedInput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecoveredPlasmaCartonRemovedUseCase implements UseCase<Mono<InventoryOutput>, RecoveredPlasmaCartonRemovedInput> {

    InventoryAggregateRepository inventoryAggregateRepository;

    InventoryOutputMapper inventoryOutputMapper;

    @Override
    public Mono<InventoryOutput> execute(RecoveredPlasmaCartonRemovedInput input) {
        log.info("Processing RecoveredPlasmaCartonRemoved event for carton: {}", input.cartonNumber());

        if (input.packedProducts() == null || input.packedProducts().isEmpty()) {
            log.warn("No packed products found in carton: {}", input.cartonNumber());
            return Mono.empty();
        }

        return Flux.fromIterable(input.packedProducts())
            .filter(this::isUnpacked)
            .flatMap(packedProduct -> inventoryAggregateRepository.findByUnitNumberAndProductCode(packedProduct.unitNumber(), packedProduct.productCode()))
            .map(inventoryAggregate -> inventoryAggregate.removeFromCarton(input.cartonNumber()))
            .flatMap(inventoryAggregateRepository::saveInventory)
            .collectList()
            .map(List::getLast)
            .map(InventoryAggregate::getInventory)
            .map(inventoryOutputMapper::toOutput);
    }

    private boolean isUnpacked(PackedProductInput packedProductInput) {
        return "UNPACKED".equals(packedProductInput.status());
    }
}
