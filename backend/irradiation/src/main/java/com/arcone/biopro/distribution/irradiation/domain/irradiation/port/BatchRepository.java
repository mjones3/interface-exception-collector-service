package com.arcone.biopro.distribution.irradiation.domain.irradiation.port;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchItem;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface BatchRepository {
    Mono<Batch> findActiveBatchByDeviceId(DeviceId deviceId);
    Mono<Batch> submitBatch(DeviceId deviceId, LocalDateTime startTime, List<BatchItem> batchItems);
    Mono<Boolean> isUnitAlreadyIrradiated(String unitNumber);
    Mono<Boolean> isUnitBeingIrradiated(String unitNumber);
}
