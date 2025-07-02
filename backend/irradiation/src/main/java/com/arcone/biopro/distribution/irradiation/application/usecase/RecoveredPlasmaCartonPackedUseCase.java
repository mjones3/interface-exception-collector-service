package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.dto.RecoveredPlasmaCartonPackedInput;
import com.arcone.biopro.distribution.irradiation.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.irradiation.domain.event.InventoryEventPublisher;
import com.arcone.biopro.distribution.irradiation.domain.event.InventoryUpdatedApplicationEvent;
import com.arcone.biopro.distribution.irradiation.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.InventoryUpdateType;
import com.arcone.biopro.distribution.irradiation.domain.repository.InventoryAggregateRepository;
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
public class RecoveredPlasmaCartonPackedUseCase implements UseCase<Mono<InventoryOutput>, RecoveredPlasmaCartonPackedInput> {

    InventoryAggregateRepository inventoryAggregateRepository;

    InventoryOutputMapper inventoryOutputMapper;

    InventoryEventPublisher inventoryEventPublisher;


    @Override
    public Mono<InventoryOutput> execute(RecoveredPlasmaCartonPackedInput input) {
        log.info("Processing RecoveredPlasmaCartonPacked event for carton: {}", input.cartonNumber());

        if (input.packedProducts() == null || input.packedProducts().isEmpty()) {
            log.warn("No packed products found in carton: {}", input.cartonNumber());
            return Mono.empty();
        }

        return Flux.fromIterable(input.packedProducts())
                .flatMap(packedProduct -> inventoryAggregateRepository.findByUnitNumberAndProductCode(packedProduct.unitNumber(), packedProduct.productCode()))
                .map(inventoryAggregate -> inventoryAggregate.putInTheCarton(input.cartonNumber()))
                .flatMap(inventoryAggregateRepository::saveInventory)
                .collectList()
                .map(List::getLast)
                .map(InventoryAggregate::getInventory)
                .doOnSuccess(inventory -> inventoryEventPublisher.publish(new InventoryUpdatedApplicationEvent(inventory, InventoryUpdateType.PACKED)))
                .map(inventoryOutputMapper::toOutput);
    }
}
