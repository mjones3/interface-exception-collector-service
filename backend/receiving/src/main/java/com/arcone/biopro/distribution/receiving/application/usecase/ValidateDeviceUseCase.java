package com.arcone.biopro.distribution.receiving.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.DeviceOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateDeviceInput;
import com.arcone.biopro.distribution.receiving.application.exception.DeviceNotFoundForKeyException;
import com.arcone.biopro.distribution.receiving.application.mapper.DeviceOutputMapper;
import com.arcone.biopro.distribution.receiving.domain.model.vo.Barcode;
import com.arcone.biopro.distribution.receiving.domain.model.vo.BloodCenterLocation;
import com.arcone.biopro.distribution.receiving.domain.repository.DeviceRepository;
import com.arcone.biopro.distribution.receiving.domain.service.ValidateDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidateDeviceUseCase implements ValidateDeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceOutputMapper deviceOutputMapper;

    @Override
    public Mono<UseCaseOutput<DeviceOutput>> validateDevice(ValidateDeviceInput validateDeviceInput) {
        return deviceRepository.findFirstByBloodCenterIdAndLocationAndActiveIsTrue(new Barcode(validateDeviceInput.bloodCenterId()),new BloodCenterLocation(validateDeviceInput.locationCode()))
            .switchIfEmpty(Mono.error(new DeviceNotFoundForKeyException(validateDeviceInput.bloodCenterId())))
            .flatMap(device -> Mono.just(new UseCaseOutput<>(Collections.emptyList(), deviceOutputMapper.toOutput(device), null)))
            .onErrorResume(error -> {
                log.error("Not able to validate device {}",error.getMessage());
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(UseCaseMessageType.VALIDATE_DEVICE_ERROR.getMessage())
                            .code(UseCaseMessageType.VALIDATE_DEVICE_ERROR.getCode())
                            .type(UseCaseMessageType.VALIDATE_DEVICE_ERROR.getType())
                            .build())
                    .build()), null, null));
            });
    }

}
