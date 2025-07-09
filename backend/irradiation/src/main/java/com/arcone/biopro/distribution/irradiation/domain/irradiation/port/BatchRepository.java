package com.arcone.biopro.distribution.irradiation.domain.irradiation.port;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import reactor.core.publisher.Mono;

public interface BatchRepository {
    Mono<Batch> findActiveBatchByDeviceId(DeviceId deviceId);
}
