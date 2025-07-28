package com.arcone.biopro.distribution.irradiation.unit.application.usecase;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.controller.errors.DeviceValidationFailureException;
import com.arcone.biopro.distribution.irradiation.application.mapper.BatchProductMapper;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateDeviceOnCloseBatchUseCase;
import com.arcone.biopro.distribution.irradiation.domain.exception.InventoryValidationException;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import com.arcone.biopro.distribution.irradiation.application.dto.BatchProductDTO;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Device;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.DeviceRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.InventoryClient;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchItem;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.client.InventoryOutput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateDeviceOnCloseBatchUseCaseTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private BatchProductMapper batchProductMapper;

    @Mock
    private Device device;

    @Mock
    private Batch batch;

    @InjectMocks
    private ValidateDeviceOnCloseBatchUseCase validateDeviceOnCloseBatchUseCase;

    @Test
    @DisplayName("Should return batch products when validation succeeds")
    void execute_ShouldReturnBatchProducts_WhenValidationSucceeds() {
        String deviceId = "AUTO-DEVICE004";
        String location = "123456789";

        InventoryOutput inventoryOutput = InventoryOutput.builder()
            .unitNumber("W777725001001")
            .productCode("E0867V00")
            .productFamily("BLOOD_SAMPLES")
            .productDescription("Blood Sample Type A")
            .inventoryStatus("AVAILABLE")
            .quarantines(List.of())
            .build();

        BatchItem batchItem = BatchItem.builder()
            .unitNumber(new UnitNumber("W777725001001"))
            .productCode("E0867V00")
            .build();

        when(deviceRepository.findByDeviceIdAndLocation(deviceId, location))
            .thenReturn(Mono.just(device));
        when(batchRepository.findActiveBatchByDeviceId(any(DeviceId.class)))
            .thenReturn(Mono.just(batch));
        when(batch.getId()).thenReturn(BatchId.of(1L));
        when(batchRepository.findBatchItemsByBatchId(1L))
            .thenReturn(Flux.just(batchItem));
        when(inventoryClient.getInventoryByUnitNumberAndProductCode(any(UnitNumber.class), anyString()))
            .thenReturn(Mono.just(inventoryOutput));
        when(batchProductMapper.toDTO(inventoryOutput))
            .thenReturn(BatchProductDTO.builder()
                .unitNumber("W777725001001")
                .productCode("E0867V00")
                .productFamily("BLOOD_SAMPLES")
                .productDescription("Blood Sample Type A")
                .status("AVAILABLE")
                .isImported(false)
                .quarantines(List.of())
                .build());

        Flux<BatchProductDTO> result = validateDeviceOnCloseBatchUseCase.execute(deviceId, location);

        StepVerifier.create(result)
            .expectNextMatches(dto ->
                "W777725001001".equals(dto.unitNumber()) &&
                "E0867V00".equals(dto.productCode()) &&
                "BLOOD_SAMPLES".equals(dto.productFamily()) &&
                "AVAILABLE".equals(dto.status()) &&
                Boolean.FALSE.equals(dto.isImported()))
            .verifyComplete();
    }

    @Test
    @DisplayName("Should throw exception when device not found")
    void execute_ShouldThrowException_WhenDeviceNotFound() {
        String deviceId = "INVALID-DEVICE";
        String location = "123456789";

        when(deviceRepository.findByDeviceIdAndLocation(deviceId, location))
            .thenReturn(Mono.empty());

        Flux<BatchProductDTO> result = validateDeviceOnCloseBatchUseCase.execute(deviceId, location);

        StepVerifier.create(result)
            .expectErrorMatches(throwable ->
                throwable instanceof DeviceValidationFailureException &&
                throwable.getMessage().equals("Device not in current location"))
            .verify();
    }

    @Test
    @DisplayName("Should throw exception when no active batch found")
    void execute_ShouldThrowException_WhenNoActiveBatchFound() {
        String deviceId = "AUTO-DEVICE004";
        String location = "123456789";

        when(deviceRepository.findByDeviceIdAndLocation(deviceId, location))
            .thenReturn(Mono.just(device));
        when(batchRepository.findActiveBatchByDeviceId(any(DeviceId.class)))
            .thenReturn(Mono.empty());

        Flux<BatchProductDTO> result = validateDeviceOnCloseBatchUseCase.execute(deviceId, location);

        StepVerifier.create(result)
            .expectErrorMatches(throwable ->
                throwable instanceof DeviceValidationFailureException &&
                throwable.getMessage().equals("Device is not listed in any open batch"))
            .verify();
    }

    @Test
    @DisplayName("Should map inventory validation exception")
    void execute_ShouldMapInventoryValidationException_WhenInventoryClientFails() {
        String deviceId = "AUTO-DEVICE004";
        String location = "123456789";

        BatchItem batchItem = BatchItem.builder()
            .unitNumber(new UnitNumber("W777725001001"))
            .productCode("E0867V00")
            .build();

        when(deviceRepository.findByDeviceIdAndLocation(deviceId, location))
            .thenReturn(Mono.just(device));
        when(batchRepository.findActiveBatchByDeviceId(any(DeviceId.class)))
            .thenReturn(Mono.just(batch));
        when(batch.getId()).thenReturn(BatchId.of(1L));
        when(batchRepository.findBatchItemsByBatchId(1L))
            .thenReturn(Flux.just(batchItem));
        when(inventoryClient.getInventoryByUnitNumberAndProductCode(any(UnitNumber.class), anyString()))
            .thenReturn(Mono.error(new RuntimeException("Inventory service error")));

        Flux<BatchProductDTO> result = validateDeviceOnCloseBatchUseCase.execute(deviceId, location);

        StepVerifier.create(result)
            .expectErrorMatches(throwable ->
                throwable instanceof InventoryValidationException &&
                throwable.getMessage().contains("Inventory validation failed"))
            .verify();
    }
}
