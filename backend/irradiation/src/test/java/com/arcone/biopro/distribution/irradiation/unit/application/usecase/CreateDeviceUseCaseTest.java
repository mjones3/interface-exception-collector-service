package com.arcone.biopro.distribution.irradiation.unit.application.usecase;

import com.arcone.biopro.distribution.irradiation.application.mapper.DeviceMapper;
import com.arcone.biopro.distribution.irradiation.application.usecase.CreateDeviceUseCase;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Device;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.DeviceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateDeviceUseCaseTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceMapper deviceMapper;

    @Mock
    private Device device;

    @InjectMocks
    private CreateDeviceUseCase createDeviceUseCase;

    @Test
    @DisplayName("Should create device when device category is IRRADIATOR")
    void execute_ShouldCreateDevice_WhenDeviceCategoryIsIrradiator() {
        CreateDeviceUseCase.Input input = new CreateDeviceUseCase.Input(
                "AUTO-DEVICE001", "123456789", "ACTIVE", "IRRADIATOR");
        when(deviceMapper.toDevice(input)).thenReturn(device);
        when(deviceRepository.save(device)).thenReturn(Mono.just(device));

        Mono<Device> result = createDeviceUseCase.execute(input);

        StepVerifier.create(result)
                .expectNext(device)
                .verifyComplete();
        verify(deviceMapper).toDevice(input);
        verify(deviceRepository).save(device);
    }

    @Test
    @DisplayName("Should return empty when device category is not IRRADIATOR")
    void execute_ShouldReturnEmpty_WhenDeviceCategoryIsNotIrradiator() {
        CreateDeviceUseCase.Input input = new CreateDeviceUseCase.Input(
                "AUTO-DEVICE002", "123456789", "ACTIVE", "OTHER");

        Mono<Device> result = createDeviceUseCase.execute(input);

        StepVerifier.create(result)
                .verifyComplete();
        verify(deviceMapper, never()).toDevice(any());
        verify(deviceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle repository error")
    void execute_ShouldHandleRepositoryError_WhenSaveFails() {
        CreateDeviceUseCase.Input input = new CreateDeviceUseCase.Input(
                "AUTO-DEVICE003", "123456789", "ACTIVE", "IRRADIATOR");
        when(deviceMapper.toDevice(input)).thenReturn(device);
        when(deviceRepository.save(device)).thenReturn(Mono.error(new RuntimeException("Save failed")));

        Mono<Device> result = createDeviceUseCase.execute(input);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }
}