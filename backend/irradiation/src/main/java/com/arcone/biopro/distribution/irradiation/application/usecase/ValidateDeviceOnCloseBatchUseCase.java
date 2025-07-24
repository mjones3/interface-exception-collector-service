package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.controller.errors.DeviceValidationFailureException;
import com.arcone.biopro.distribution.irradiation.application.dto.BatchProductDTO;
import com.arcone.biopro.distribution.irradiation.domain.exception.InventoryValidationException;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.DeviceRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.InventoryClient;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.client.InventoryOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidateDeviceOnCloseBatchUseCase {

    private final DeviceRepository deviceRepository;
    private final BatchRepository batchRepository;
    private final InventoryClient inventoryClient;

    public Flux<BatchProductDTO> execute(String deviceId, String location) {
        return deviceRepository.findByDeviceIdAndLocation(deviceId, location)
            .switchIfEmpty(Mono.error(new DeviceValidationFailureException("Device not in current location")))
            .flatMapMany(device -> batchRepository.findActiveBatchByDeviceId(DeviceId.of(deviceId)))
            .switchIfEmpty(Mono.error(new DeviceValidationFailureException("Device is not listed in any open batch")))
            .flatMap(batch -> batchRepository.findBatchItemsByBatchId(batch.getId().getValue()))
            .flatMap(item -> fetchProduct(item.unitNumber(), item.productCode())
                .map(inventoryOutput -> BatchProductDTO.builder()
                    .unitNumber(inventoryOutput.unitNumber())
                    .productCode(inventoryOutput.productCode())
                    .productFamily(inventoryOutput.productFamily())
                    .productDescription(inventoryOutput.productDescription())
                    .status(inventoryOutput.inventoryStatus())
                    .build()));
    }

    private Mono<InventoryOutput> fetchProduct(UnitNumber unitNumber, String productCode) {
        return inventoryClient.getInventoryByUnitNumberAndProductCode(unitNumber, productCode)
            .doOnNext(inventory -> log.info("Inventory validation successful for unit: {} product: {}", unitNumber, productCode))
            .onErrorMap(throwable -> throwable instanceof InventoryValidationException ?
                throwable :
                new InventoryValidationException("Inventory validation failed: " + throwable.getMessage()));
    }
}
