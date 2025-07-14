package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.repository;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchItem;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.BatchEntity;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.BatchEntityMapper;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.BatchItemEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

interface BatchEntityRepository extends ReactiveCrudRepository<BatchEntity, Long> {
    Mono<BatchEntity> findByDeviceIdAndEndTimeIsNull(String deviceId);
}

interface BatchItemEntityRepository extends ReactiveCrudRepository<BatchItemEntity, Long> {
    Flux<BatchItemEntity> findByBatchId(Long batchId);
}

@Repository
public class BatchRepositoryImpl implements BatchRepository {
    private final BatchEntityRepository batchRepository;
    private final BatchItemEntityRepository batchItemRepository;
    private final BatchEntityMapper mapper;

    public BatchRepositoryImpl(BatchEntityRepository batchRepository,
                              BatchItemEntityRepository batchItemRepository,
                              BatchEntityMapper mapper) {
        this.batchRepository = batchRepository;
        this.batchItemRepository = batchItemRepository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Batch> findActiveBatchByDeviceId(DeviceId deviceId) {
        return batchRepository.findByDeviceIdAndEndTimeIsNull(deviceId.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Batch> submitBatch(DeviceId deviceId, LocalDateTime startTime, List<BatchItem> batchItems) {
        BatchEntity batchEntity = BatchEntity.builder()
                .deviceId(deviceId.getValue())
                .startTime(startTime)
                .createDate(ZonedDateTime.now())
                .modificationDate(ZonedDateTime.now())
                .build();

        return batchRepository.save(batchEntity)
                .flatMap(savedBatch -> {
                    List<BatchItemEntity> itemEntities = batchItems.stream()
                            .map(item -> mapper.toEntity(item, savedBatch.getId()))
                            .peek(entity -> {
                                entity.setCreateDate(ZonedDateTime.now());
                                entity.setModificationDate(ZonedDateTime.now());
                            })
                            .toList();

                    return batchItemRepository.saveAll(itemEntities)
                            .then(Mono.just(mapper.toDomain(savedBatch)));
                });
    }

}
