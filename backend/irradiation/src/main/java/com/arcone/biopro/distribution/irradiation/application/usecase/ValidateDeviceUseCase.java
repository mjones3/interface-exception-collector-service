package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.aggregate.IrradiationAggregate;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.DeviceRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class ValidateDeviceUseCase {
    private final DeviceRepository deviceRepository;
    private final BatchRepository batchRepository;

    public Mono<Boolean> execute(String deviceId, String location) {
        DeviceId deviceIdObj = DeviceId.of(deviceId);
        Location locationObj = Location.of(location);

        return deviceRepository.findByDeviceId(deviceIdObj)
            .switchIfEmpty(Mono.error(new RuntimeException("Device not found")))
            .flatMap(device -> {
                IrradiationAggregate aggregate = new IrradiationAggregate(device, Collections.emptyList(), null);
                
                if (!aggregate.validateDevice(locationObj)) {
                    return Mono.error(new RuntimeException("Device not in current location"));
                }
                
                return batchRepository.findActiveBatchByDeviceId(deviceIdObj)
                    .flatMap(batch -> {
                        IrradiationAggregate aggregateWithBatch = new IrradiationAggregate(device, Collections.emptyList(), batch);
                        if (aggregateWithBatch.validateDeviceIsInUse(batch)) {
                            return Mono.error(new RuntimeException("Device already in use"));
                        }
                        return Mono.just(true);
                    })
                    .defaultIfEmpty(true);
            });
    }
}
