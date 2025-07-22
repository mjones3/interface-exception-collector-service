package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.controller.errors.DeviceValidationFailureException;
import com.arcone.biopro.distribution.irradiation.application.dto.BatchProductDTO;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.DeviceRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ValidateDeviceOnCloseBatchUseCase {

    private final DeviceRepository deviceRepository;
    private final BatchRepository batchRepository;

    public Flux<BatchProductDTO> execute(String deviceId, String location) {
        return deviceRepository.findByDeviceIdAndLocation(deviceId, location)
            .switchIfEmpty(Mono.error(new DeviceValidationFailureException("Device not in current location")))
            .flatMapMany(device ->
                batchRepository.findActiveBatchByDeviceId(DeviceId.of(deviceId))
                    .switchIfEmpty(Mono.error(new DeviceValidationFailureException("Device is not listed in any open batch")))
                    .flatMapMany(batch ->
                        batchRepository.findBatchItemsByBatchId(batch.getId().getValue())
                            .map(item -> BatchProductDTO.builder()
                                .unitNumber(item.unitNumber().value())
                                .productCode(item.productCode())
                                .productFamily(item.productFamily())
                                .build())
                    )
            );
    }
}
