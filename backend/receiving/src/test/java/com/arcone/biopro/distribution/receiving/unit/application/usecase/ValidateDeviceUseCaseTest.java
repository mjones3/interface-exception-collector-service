package com.arcone.biopro.distribution.receiving.unit.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.DeviceOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateDeviceInput;
import com.arcone.biopro.distribution.receiving.application.mapper.DeviceOutputMapper;
import com.arcone.biopro.distribution.receiving.application.usecase.ValidateDeviceUseCase;
import com.arcone.biopro.distribution.receiving.domain.model.Device;
import com.arcone.biopro.distribution.receiving.domain.model.vo.Barcode;
import com.arcone.biopro.distribution.receiving.domain.model.vo.BloodCenterLocation;
import com.arcone.biopro.distribution.receiving.domain.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateDeviceUseCaseTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceOutputMapper deviceOutputMapper;

    @InjectMocks
    private ValidateDeviceUseCase validateDeviceUseCase;

    private ValidateDeviceInput validateDeviceInput;
    private Device device;
    private DeviceOutput deviceOutput;

    @BeforeEach
    void setUp() {
        // Initialize test data
        validateDeviceInput = new ValidateDeviceInput("BC123", "LOC1");
        device = Mockito.mock(Device.class);
        deviceOutput = DeviceOutput.builder().build();
    }

    @Test
    void validateDevice_Success() {
        // Arrange
        when(deviceRepository.findFirstByBloodCenterIdAndLocationAndActiveIsTrue(
            any(Barcode.class),
            any(BloodCenterLocation.class)))
            .thenReturn(Mono.just(device));

        when(deviceOutputMapper.toOutput(device))
            .thenReturn(deviceOutput);

        // Act & Assert
        StepVerifier.create(validateDeviceUseCase.validateDevice(validateDeviceInput))
            .assertNext(output -> {
                assertThat(output).isNotNull();
                assertThat(output.notifications()).isEmpty();
                assertThat(output.data()).isEqualTo(deviceOutput);
            })
            .verifyComplete();
    }

    @Test
    void validateDevice_DeviceNotFound() {
        // Arrange
        when(deviceRepository.findFirstByBloodCenterIdAndLocationAndActiveIsTrue(
            any(Barcode.class),
            any(BloodCenterLocation.class)))
            .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(validateDeviceUseCase.validateDevice(validateDeviceInput))
            .assertNext(output -> {
                assertThat(output).isNotNull();
                assertThat(output.notifications()).hasSize(1);
                assertThat(output.data()).isNull();

                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertThat(notification.useCaseMessage().message())
                    .isEqualTo(UseCaseMessageType.VALIDATE_DEVICE_ERROR.getMessage());
                assertThat(notification.useCaseMessage().code())
                    .isEqualTo(UseCaseMessageType.VALIDATE_DEVICE_ERROR.getCode());
                assertThat(notification.useCaseMessage().type())
                    .isEqualTo(UseCaseMessageType.VALIDATE_DEVICE_ERROR.getType());
            })
            .verifyComplete();
    }

    @Test
    void validateDevice_RepositoryError() {
        // Arrange
        when(deviceRepository.findFirstByBloodCenterIdAndLocationAndActiveIsTrue(
            any(Barcode.class),
            any(BloodCenterLocation.class)))
            .thenReturn(Mono.error(new RuntimeException("Database error")));

        // Act & Assert
        StepVerifier.create(validateDeviceUseCase.validateDevice(validateDeviceInput))
            .assertNext(output -> {
                assertThat(output).isNotNull();
                assertThat(output.notifications()).hasSize(1);
                assertThat(output.data()).isNull();

                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertThat(notification.useCaseMessage().message())
                    .isEqualTo(UseCaseMessageType.VALIDATE_DEVICE_ERROR.getMessage());
                assertThat(notification.useCaseMessage().code())
                    .isEqualTo(UseCaseMessageType.VALIDATE_DEVICE_ERROR.getCode());
                assertThat(notification.useCaseMessage().type())
                    .isEqualTo(UseCaseMessageType.VALIDATE_DEVICE_ERROR.getType());
            })
            .verifyComplete();
    }

    @Test
    void validateDevice_MapperError() {
        // Arrange
        when(deviceRepository.findFirstByBloodCenterIdAndLocationAndActiveIsTrue(
            any(Barcode.class),
            any(BloodCenterLocation.class)))
            .thenReturn(Mono.just(device));

        when(deviceOutputMapper.toOutput(any()))
            .thenThrow(new RuntimeException("Mapping error"));

        // Act & Assert
        StepVerifier.create(validateDeviceUseCase.validateDevice(validateDeviceInput))
            .assertNext(output -> {
                assertThat(output).isNotNull();
                assertThat(output.notifications()).hasSize(1);
                assertThat(output.data()).isNull();

                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertThat(notification.useCaseMessage().message())
                    .isEqualTo(UseCaseMessageType.VALIDATE_DEVICE_ERROR.getMessage());
            })
            .verifyComplete();
    }
}

