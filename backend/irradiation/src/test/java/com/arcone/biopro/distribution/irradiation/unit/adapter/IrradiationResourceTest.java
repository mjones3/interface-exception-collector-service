package com.arcone.biopro.distribution.irradiation.unit.adapter;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.controller.errors.DeviceValidationFailureException;
import com.arcone.biopro.distribution.irradiation.adapter.in.web.dto.CheckDigitResponseDTO;
import com.arcone.biopro.distribution.irradiation.adapter.in.web.dto.ConfigurationResponseDTO;
import com.arcone.biopro.distribution.irradiation.adapter.in.web.mapper.ConfigurationDTOMapper;
import com.arcone.biopro.distribution.irradiation.adapter.irradiation.IrradiationResource;
import com.arcone.biopro.distribution.irradiation.application.dto.IrradiationInventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.dto.BatchProductDTO;
import com.arcone.biopro.distribution.irradiation.application.usecase.CheckDigitUseCase;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateDeviceUseCase;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateUnitNumberUseCase;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateDeviceOnCloseBatchUseCase;
import com.arcone.biopro.distribution.irradiation.domain.model.Configuration;
import com.arcone.biopro.distribution.irradiation.domain.repository.ConfigurationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IrradiationResourceTest {

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private ConfigurationDTOMapper configurationDTOMapper;

    @Mock
    private ValidateDeviceUseCase validateDeviceUseCase;

    @Mock
    private ValidateUnitNumberUseCase validateUnitNumberUseCase;

    @Mock
    private CheckDigitUseCase checkDigitUseCase;

    @Mock
    private ValidateDeviceOnCloseBatchUseCase validateDeviceOnCloseBatchUseCase;

    @InjectMocks
    private IrradiationResource irradiationResource;


    @Test
    @DisplayName("Should return configuration response DTOs")
    void readConfiguration_Success() {
        Configuration config = Configuration.builder().build();
        ConfigurationResponseDTO responseDTO = ConfigurationResponseDTO.builder().build();

        when(configurationService.readConfiguration(anyList()))
            .thenReturn(Flux.just(config));
        when(configurationDTOMapper.toResponseDTO(any(Configuration.class)))
            .thenReturn(responseDTO);

        Flux<ConfigurationResponseDTO> result = irradiationResource.readConfiguration(List.of("key1"));

        StepVerifier.create(result)
            .expectNext(responseDTO)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return true when device validation succeeds")
    void validateDevice_ShouldReturnTrue_WhenValidationSucceeds() {
        String deviceId = "AUTO-DEVICE004";
        String location = "123456789";
        when(validateDeviceUseCase.execute(deviceId, location)).thenReturn(Mono.just(true));

        Mono<Boolean> result = irradiationResource.validateDevice(deviceId, location);

        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should throw exception when device not found")
    void validateDevice_ShouldThrowException_WhenDeviceNotFound() {
        String deviceId = "INVALID-DEVICE";
        String location = "123456789";
        when(validateDeviceUseCase.execute(deviceId, location))
            .thenReturn(Mono.error(new DeviceValidationFailureException("Device not found")));

        Mono<Boolean> result = irradiationResource.validateDevice(deviceId, location);

        StepVerifier.create(result)
            .expectError(DeviceValidationFailureException.class)
            .verify();
    }

    @Test
    @DisplayName("Should throw exception when device not in current location")
    void validateDevice_ShouldThrowException_WhenDeviceNotInCurrentLocation() {
        String deviceId = "AUTO-DEVICE005";
        String location = "123456789";
        when(validateDeviceUseCase.execute(deviceId, location))
            .thenReturn(Mono.error(new DeviceValidationFailureException("Device not in current location")));

        Mono<Boolean> result = irradiationResource.validateDevice(deviceId, location);

        StepVerifier.create(result)
            .expectErrorMatches(throwable ->
                throwable instanceof DeviceValidationFailureException &&
                    throwable.getMessage().equals("Device not in current location"))
            .verify();
    }

    @Test
    @DisplayName("Should throw exception when device already in use")
    void validateDevice_ShouldThrowException_WhenDeviceAlreadyInUse() {
        String deviceId = "AUTO-DEVICE007";
        String location = "DEFAULT_LOCATION";
        when(validateDeviceUseCase.execute(deviceId, location))
            .thenReturn(Mono.error(new DeviceValidationFailureException("Device already in use")));

        Mono<Boolean> result = irradiationResource.validateDevice(deviceId, location);

        StepVerifier.create(result)
            .expectErrorMatches(throwable ->
                throwable instanceof DeviceValidationFailureException &&
                    throwable.getMessage().equals("Device already in use"))
            .verify();
    }

    @Test
    @DisplayName("Should return inventory list when unit validation succeeds")
    void validateUnit_ShouldReturnInventoryList_WhenValidationSucceeds() {
        // Given
        String unitNumber = "W777725001001";
        String location = "123456789";

        List<IrradiationInventoryOutput> expectedInventories = Arrays.asList(
            IrradiationInventoryOutput.builder()
                .unitNumber("W777725001001")
                .productCode("E0867V00")
                .location("123456789")
                .status("AVAILABLE")
                .productDescription("Blood Sample Type A")
                .productFamily("BLOOD_SAMPLES")
                .statusReason("Quality Check In Progress")
                .unsuitableReason(null)
                .expired(false)
                .build(),
            IrradiationInventoryOutput.builder()
                .unitNumber("W777725001001")
                .productCode("E0868V00")
                .location("123456789")
                .status("DISCARDED")
                .productDescription("Blood Sample Type A")
                .productFamily("BLOOD_SAMPLES")
                .statusReason("Quality Check In Progress")
                .unsuitableReason(null)
                .expired(false)
                .build(),
            IrradiationInventoryOutput.builder()
                .unitNumber("W777725001001")
                .productCode("E0869V00")
                .location("123456789")
                .status("IN_TRANSIT")
                .productDescription("Blood Sample Type A")
                .productFamily("BLOOD_SAMPLES")
                .statusReason("Quality Check In Progress")
                .unsuitableReason(null)
                .expired(false)
                .build()
        );

        // When
        when(validateUnitNumberUseCase.execute(unitNumber, location))
            .thenReturn(Flux.fromIterable(expectedInventories));

        Flux<IrradiationInventoryOutput> result = irradiationResource.validateUnit(unitNumber, location);

        // Then
        StepVerifier.create(result)
            .expectNext(expectedInventories.get(0))
            .expectNext(expectedInventories.get(1))
            .expectNext(expectedInventories.get(2))
            .verifyComplete();

        verify(validateUnitNumberUseCase).execute(unitNumber, location);
    }

    @Test
    @DisplayName("Should return empty flux when no inventory found")
    void validateUnit_ShouldReturnEmptyFlux_WhenNoInventoryFound() {
        String unitNumber = "INVALID-UNIT";
        String location = "123456789";
        when(validateUnitNumberUseCase.execute(unitNumber, location))
            .thenReturn(Flux.empty());

        Flux<IrradiationInventoryOutput> result = irradiationResource.validateUnit(unitNumber, location);

        StepVerifier.create(result)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should propagate error when unit validation fails")
    void validateUnit_ShouldPropagateError_WhenValidationFails() {
        String unitNumber = "W777725001001";
        String location = "123456789";
        when(validateUnitNumberUseCase.execute(unitNumber, location))
            .thenReturn(Flux.error(new RuntimeException("Validation failed")));

        Flux<IrradiationInventoryOutput> result = irradiationResource.validateUnit(unitNumber, location);

        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    @DisplayName("Should return valid response when check digit is valid")
    void checkDigit_ShouldReturnValidResponse_WhenCheckDigitIsValid() {
        // Given
        String unitNumber = "W777725001001";
        String checkDigit = "F";
        CheckDigitResponseDTO expectedResponse = new CheckDigitResponseDTO(true);

        when(checkDigitUseCase.checkDigit(unitNumber, checkDigit))
            .thenReturn(Mono.just(expectedResponse));

        // When
        Mono<CheckDigitResponseDTO> result = irradiationResource.checkDigit(unitNumber, checkDigit);

        // Then
        StepVerifier.create(result)
            .expectNext(expectedResponse)
            .verifyComplete();

        verify(checkDigitUseCase).checkDigit(unitNumber, checkDigit);
    }

    @Test
    @DisplayName("Should return invalid response when check digit is invalid")
    void checkDigit_ShouldReturnInvalidResponse_WhenCheckDigitIsInvalid() {
        String unitNumber = "W777725001001";
        String checkDigit = "X";
        CheckDigitResponseDTO expectedResponse = new CheckDigitResponseDTO(false);

        when(checkDigitUseCase.checkDigit(unitNumber, checkDigit))
            .thenReturn(Mono.just(expectedResponse));

        Mono<CheckDigitResponseDTO> result = irradiationResource.checkDigit(unitNumber, checkDigit);

        StepVerifier.create(result)
            .expectNext(expectedResponse)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should propagate error when check digit validation fails")
    void checkDigit_ShouldPropagateError_WhenValidationFails() {
        String unitNumber = "W777725001001";
        String checkDigit = "F";

        when(checkDigitUseCase.checkDigit(unitNumber, checkDigit))
            .thenReturn(Mono.error(new RuntimeException("Validation failed")));

        Mono<CheckDigitResponseDTO> result = irradiationResource.checkDigit(unitNumber, checkDigit);

        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    @DisplayName("Should handle null stopsManufacturing in quarantines")
    void validateUnit_ShouldHandleNullStopsManufacturing_InQuarantines() {
        // Given
        String unitNumber = "W036825280464";
        String location = "123456789";

        List<com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.InventoryQuarantine> quarantines = List.of(
            new com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.InventoryQuarantine(
                "Quality Hold", "Pending review", null // null stopsManufacturing
            )
        );

        IrradiationInventoryOutput inventoryWithNullQuarantine = IrradiationInventoryOutput.builder()
            .unitNumber(unitNumber)
            .productCode("E453300")
            .location(location)
            .status("AVAILABLE")
            .productDescription("APH AS1 LR RBC C2")
            .productFamily("RED_BLOOD_CELLS_LEUKOREDUCED")
            .statusReason(null)
            .unsuitableReason(null)
            .expired(false)
            .alreadyIrradiated(false)
            .notConfigurableForIrradiation(false)
            .quarantines(quarantines)
            .build();
        when(validateUnitNumberUseCase.execute(unitNumber, location))
            .thenReturn(Flux.just(inventoryWithNullQuarantine));

        Flux<IrradiationInventoryOutput> result = irradiationResource.validateUnit(unitNumber, location);

        StepVerifier.create(result)
            .expectNextMatches(inventory ->
                inventory.quarantines() != null &&
                inventory.quarantines().size() == 1 &&
                inventory.quarantines().getFirst().stopsManufacturing() == null
            )
            .verifyComplete();

        verify(validateUnitNumberUseCase).execute(unitNumber, location);
    }

    @Test
    @DisplayName("Should return batch products when device validation succeeds")
    void validateDeviceOnCloseBatch_ShouldReturnBatchProducts_WhenValidationSucceeds() {
        String deviceId = "AUTO-DEVICE004";
        String location = "123456789";
        
        List<BatchProductDTO> expectedProducts = List.of(
            BatchProductDTO.builder()
                .unitNumber("W777725001001")
                .productCode("E0867V00")
                .productFamily("BLOOD_SAMPLES")
                .productDescription("Blood Sample Type A")
                .status("AVAILABLE")
                .quarantines(List.of())
                .build()
        );
        
        when(validateDeviceOnCloseBatchUseCase.execute(deviceId, location))
            .thenReturn(Flux.fromIterable(expectedProducts));
        
        Flux<BatchProductDTO> result = irradiationResource.validateDeviceOnCloseBatch(deviceId, location);
        
        StepVerifier.create(result)
            .expectNext(expectedProducts.get(0))
            .verifyComplete();
        
        verify(validateDeviceOnCloseBatchUseCase).execute(deviceId, location);
    }

    @Test
    @DisplayName("Should propagate error when device validation fails")
    void validateDeviceOnCloseBatch_ShouldPropagateError_WhenValidationFails() {
        String deviceId = "INVALID-DEVICE";
        String location = "123456789";
        
        when(validateDeviceOnCloseBatchUseCase.execute(deviceId, location))
            .thenReturn(Flux.error(new DeviceValidationFailureException("Device not found")));
        
        Flux<BatchProductDTO> result = irradiationResource.validateDeviceOnCloseBatch(deviceId, location);
        
        StepVerifier.create(result)
            .expectError(DeviceValidationFailureException.class)
            .verify();
    }
}
