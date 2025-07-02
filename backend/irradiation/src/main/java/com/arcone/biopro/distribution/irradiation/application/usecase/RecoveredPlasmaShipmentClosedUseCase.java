package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.dto.RecoveredPlasmaShipmentClosedInput;
import com.arcone.biopro.distribution.irradiation.application.dto.ShipmentPackedProductInput;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecoveredPlasmaShipmentClosedUseCase implements UseCase<Mono<InventoryOutput>, RecoveredPlasmaShipmentClosedInput> {

    InventoryAggregateRepository inventoryAggregateRepository;
    InventoryOutputMapper inventoryOutputMapper;
    InventoryEventPublisher inventoryEventPublisher;

    @Override
    public Mono<InventoryOutput> execute(RecoveredPlasmaShipmentClosedInput input) {
        log.info("Processing RecoveredPlasmaShipmentClosed event for shipment: {}", input.shipmentNumber());

        if (Objects.isNull(input.cartonList()) || input.cartonList().isEmpty()) {
            log.warn("No cartons found in shipment: {}", input.shipmentNumber());
            return Mono.empty();
        }

        List<ShipmentPackedProductInput> packedProducts = new ArrayList<>();

        input.cartonList().forEach(carton -> {
            if (Objects.nonNull(carton.packedProducts()) && !carton.packedProducts().isEmpty()) {
                packedProducts.addAll(carton.packedProducts());
            }
        });

        if (packedProducts.isEmpty()) {
            log.warn("No packed products found in shipment: {}", input.shipmentNumber());
            return Mono.empty();
        }

        return Flux.fromIterable(packedProducts)
            .flatMap(packedProduct -> inventoryAggregateRepository.findByUnitNumberAndProductCode(packedProduct.unitNumber(), packedProduct.productCode()))
            .map(InventoryAggregate::cartonShipped)
            .flatMap(inventoryAggregateRepository::saveInventory)
            .collectList()
            .map(List::getLast)
            .map(InventoryAggregate::getInventory)
            .doOnSuccess(inventory -> inventoryEventPublisher.publish(new InventoryUpdatedApplicationEvent(inventory, InventoryUpdateType.SHIPPED)))
            .map(inventoryOutputMapper::toOutput);

    }
}
