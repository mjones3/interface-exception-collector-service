package com.arcone.biopro.distribution.irradiation.domain.irradiation.port;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Device;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import reactor.core.publisher.Mono;

public interface DeviceRepository {
    Mono<Device> findByDeviceId(DeviceId deviceId);
    Mono<Device> save(Device device);
}