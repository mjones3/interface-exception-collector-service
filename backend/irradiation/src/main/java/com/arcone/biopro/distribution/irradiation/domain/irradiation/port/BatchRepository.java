package com.arcone.biopro.distribution.irradiation.domain.irradiation.port;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchId;
import reactor.core.publisher.Mono;

public interface BatchRepository {
    Mono<Batch> findByBatchId(BatchId id);
}
