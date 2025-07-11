package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.repository;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.BatchEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

interface BatchEntityRepository extends ReactiveCrudRepository<BatchEntity, Long> {
    Mono<BatchEntity> findByDeviceIdAndEndTimeIsNull(String deviceId);
}

@Repository
public class BatchRepositoryImpl implements BatchRepository {
    private final BatchEntityRepository repository;

    public BatchRepositoryImpl(BatchEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Batch> findActiveBatchByDeviceId(DeviceId deviceId) {
        return repository.findByDeviceIdAndEndTimeIsNull(deviceId.getValue())
            .map(batchEntity -> new Batch(
                BatchId.of(batchEntity.getId()),
                DeviceId.of(batchEntity.getDeviceId()),
                batchEntity.getStartTime(),
                batchEntity.getEndTime()));
    }
}
