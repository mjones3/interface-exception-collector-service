package com.arcone.biopro.distribution.irradiation.application.irradiation.usecase;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.aggregate.IrradiationAggregate;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.DeviceRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.Collections;

@Service
public class ValidateDeviceUseCase {
    private final DeviceRepository deviceRepository;

    public ValidateDeviceUseCase(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public Mono<Boolean> execute(String deviceId, String location) {
        return deviceRepository.findByDeviceId(DeviceId.of(deviceId))
                .map(device -> new IrradiationAggregate(device, Collections.emptyList()))
                .map(aggregate -> aggregate.validateDevice(Location.of(location)))
                .defaultIfEmpty(false);
    }
}