package com.arcone.biopro.distribution.irradiation.unit.adapter;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.controller.errors.DeviceValidationFailureException;
import com.arcone.biopro.distribution.irradiation.adapter.in.web.dto.ConfigurationResponseDTO;
import com.arcone.biopro.distribution.irradiation.adapter.in.web.mapper.ConfigurationDTOMapper;
import com.arcone.biopro.distribution.irradiation.adapter.irradiation.IrradiationResource;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateDeviceUseCase;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateUnitNumberUseCase;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
        String unitNumber = "W777725001001";
        String location = "123456789";
        when(validateUnitNumberUseCase.execute(unitNumber, location))
                .thenReturn(Flux.just(
                    new Inventory(UnitNumber.of("W777725001001"), "PROD001", Location.of("123456789"), "AVAILABLE"),
                    new Inventory(UnitNumber.of("W777725001002"), "PROD002", Location.of("123456789"), "AVAILABLE")
                ));

        Flux<Inventory> result = irradiationResource.validateUnit(unitNumber, location);

        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty flux when no inventory found")
    void validateUnit_ShouldReturnEmptyFlux_WhenNoInventoryFound() {
        String unitNumber = "INVALID-UNIT";
        String location = "123456789";
        when(validateUnitNumberUseCase.execute(unitNumber, location))
                .thenReturn(Flux.empty());

        Flux<Inventory> result = irradiationResource.validateUnit(unitNumber, location);

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

        Flux<Inventory> result = irradiationResource.validateUnit(unitNumber, location);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }
}
