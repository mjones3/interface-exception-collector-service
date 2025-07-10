package com.arcone.biopro.distribution.irradiation.unit.application.usecase;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.controller.errors.DeviceValidationFailureException;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateDeviceUseCase;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Device;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.DeviceRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateDeviceUseCaseTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private Device device;

    @Mock
    private Batch batch;

    @InjectMocks
    private ValidateDeviceUseCase validateDeviceUseCase;

    @Test
    @DisplayName("Should return true when device validation succeeds")
    void execute_ShouldReturnTrue_WhenDeviceValidationSucceeds() {
        String deviceId = "AUTO-DEVICE004";
        String location = "123456789";
        when(deviceRepository.findByDeviceId(any(DeviceId.class))).thenReturn(Mono.just(device));
        when(device.isAtLocation(any(Location.class))).thenReturn(true);
        when(batchRepository.findActiveBatchByDeviceId(any(DeviceId.class))).thenReturn(Mono.empty());

        Mono<Boolean> result = validateDeviceUseCase.execute(deviceId, location);

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should throw exception when device not found")
    void execute_ShouldThrowException_WhenDeviceNotFound() {
        String deviceId = "INVALID-DEVICE";
        String location = "123456789";
        when(deviceRepository.findByDeviceId(any(DeviceId.class))).thenReturn(Mono.empty());

        Mono<Boolean> result = validateDeviceUseCase.execute(deviceId, location);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof DeviceValidationFailureException &&
                    throwable.getMessage().equals("Device not found"))
                .verify();
    }

    @Test
    @DisplayName("Should throw exception when device not in current location")
    void execute_ShouldThrowException_WhenDeviceNotInCurrentLocation() {
        String deviceId = "AUTO-DEVICE005";
        String location = "123456789";
        when(deviceRepository.findByDeviceId(any(DeviceId.class))).thenReturn(Mono.just(device));
        when(device.isAtLocation(any(Location.class))).thenReturn(false);

        Mono<Boolean> result = validateDeviceUseCase.execute(deviceId, location);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof DeviceValidationFailureException &&
                    throwable.getMessage().equals("Device not in current location"))
                .verify();
    }

    @Test
    @DisplayName("Should throw exception when device already in use")
    void execute_ShouldThrowException_WhenDeviceAlreadyInUse() {
        String deviceId = "AUTO-DEVICE007";
        String location = "DEFAULT_LOCATION";
        when(deviceRepository.findByDeviceId(any(DeviceId.class))).thenReturn(Mono.just(device));
        when(device.isAtLocation(any(Location.class))).thenReturn(true);
        when(batchRepository.findActiveBatchByDeviceId(any(DeviceId.class))).thenReturn(Mono.just(batch));
        when(batch.isActive()).thenReturn(true);

        Mono<Boolean> result = validateDeviceUseCase.execute(deviceId, location);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof DeviceValidationFailureException &&
                    throwable.getMessage().equals("Device already in use"))
                .verify();
    }

    @Test
    @DisplayName("Should return true when device has inactive batch")
    void execute_ShouldReturnTrue_WhenDeviceHasInactiveBatch() {
        String deviceId = "AUTO-DEVICE008";
        String location = "123456789";
        when(deviceRepository.findByDeviceId(any(DeviceId.class))).thenReturn(Mono.just(device));
        when(device.isAtLocation(any(Location.class))).thenReturn(true);
        when(batchRepository.findActiveBatchByDeviceId(any(DeviceId.class))).thenReturn(Mono.just(batch));
        when(batch.isActive()).thenReturn(false);

        Mono<Boolean> result = validateDeviceUseCase.execute(deviceId, location);

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }
}