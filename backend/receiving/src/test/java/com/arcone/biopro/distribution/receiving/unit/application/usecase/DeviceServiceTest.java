package com.arcone.biopro.distribution.receiving.unit.application.usecase;

import com.arcone.biopro.distribution.receiving.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.receiving.application.usecase.DeviceService;
import com.arcone.biopro.distribution.receiving.domain.model.Device;
import com.arcone.biopro.distribution.receiving.domain.model.vo.Barcode;
import com.arcone.biopro.distribution.receiving.domain.repository.DeviceRepository;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.DeviceCreatedMessage;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.DevicePayload;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.DeviceUpdatedMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.ZonedDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private DeviceService deviceService;

    private DeviceCreatedMessage createdMessage;
    private DeviceUpdatedMessage updatedMessage;
    private Device device;
    private Barcode barcode;


    @BeforeEach
    void setUp() {
        // Initialize test data
        createdMessage = Mockito.mock(DeviceCreatedMessage.class);
        updatedMessage = Mockito.mock(DeviceUpdatedMessage.class);
        device = Mockito.mock(Device.class);
        barcode = new Barcode("TEST123");
    }

    @Test
    void createDevice_Success() {
        // Arrange
        when(createdMessage.getPayload()).thenReturn(getPayload());
        when(deviceRepository.save(any(Device.class))).thenReturn(Mono.just(device));

        // Act & Assert
        StepVerifier.create(deviceService.createDevice(createdMessage))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    void createDevice_Error() {
        // Arrange
        when(createdMessage.getPayload()).thenReturn(getPayload());
        when(deviceRepository.save(device)).thenReturn(Mono.error(new RuntimeException("Save failed")));

        // Act & Assert
        StepVerifier.create(deviceService.createDevice(createdMessage))
            .verifyComplete(); // Should return empty on error
    }

    @Test
    void updateDevice_Success() {
        // Arrange
        when(updatedMessage.getPayload()).thenReturn(getPayload());
        Device existingDevice = Mockito.mock(Device.class);
        when(existingDevice.getId()).thenReturn(1L);

        when(deviceRepository.findFirstByBloodCenterId(any(Barcode.class))).thenReturn(Mono.just(existingDevice));
        when(deviceRepository.save(any(Device.class))).thenReturn(Mono.just(device));

        // Act & Assert
        StepVerifier.create(deviceService.updateDevice(updatedMessage))
            .expectNext(device)
            .verifyComplete();

        verify(deviceRepository).findFirstByBloodCenterId(any(Barcode.class));
        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    void updateDevice_DeviceNotFound() {
        // Arrange
        when(updatedMessage.getPayload()).thenReturn(getPayload());

        when(deviceRepository.findFirstByBloodCenterId(any(Barcode.class))).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(deviceService.updateDevice(updatedMessage))
            .expectError(DomainNotFoundForKeyException.class)
            .verify();
    }

    @Test
    void updateDevice_SaveError() {
        // Arrange
        when(updatedMessage.getPayload()).thenReturn(getPayload());

        Device existingDevice = Mockito.mock(Device.class);
        when(existingDevice.getId()).thenReturn(1L);

        when(deviceRepository.findFirstByBloodCenterId(any(Barcode.class))).thenReturn(Mono.just(existingDevice));
        when(deviceRepository.save(device)).thenReturn(Mono.error(new RuntimeException("Save failed")));

        // Act & Assert
        StepVerifier.create(deviceService.updateDevice(updatedMessage))
            .expectError(RuntimeException.class)
            .verify();
    }

    private DevicePayload getPayload(){
        var payload = new DevicePayload();
        payload.setId("TEST123");
        payload.setDevice("THERMOMETER");
        payload.setName("name");
        payload.setLocation("123456");
        payload.setDeviceCategory("TEMPERATURE");
        payload.setStatus("ACTIVE");
        payload.setSerialNumber("SERIAL_NUMBER");
        payload.setCreateDate(ZonedDateTime.now());

        return payload;
    }
}
