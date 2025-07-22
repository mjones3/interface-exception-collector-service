package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.repository;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
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
    Mono<Boolean> existsByUnitNumber(String unitNumber);
}

@Repository
public class BatchRepositoryImpl implements BatchRepository {
    private final BatchEntityRepository batchRepository;
    private final BatchItemEntityRepository batchItemRepository;
    private final BatchEntityMapper mapper;
    private final DatabaseClient databaseClient;

    public BatchRepositoryImpl(BatchEntityRepository batchRepository,
                              BatchItemEntityRepository batchItemRepository,
                              BatchEntityMapper mapper,
                              DatabaseClient databaseClient) {
        this.batchRepository = batchRepository;
        this.batchItemRepository = batchItemRepository;
        this.mapper = mapper;
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Batch> findActiveBatchByDeviceId(DeviceId deviceId) {
        return batchRepository.findByDeviceIdAndEndTimeIsNull(deviceId.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Flux<BatchItem> findBatchItemsByBatchId(Long batchId) {
        return batchItemRepository.findByBatchId(batchId)
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

    @Override
    public Mono<Boolean> isUnitAlreadyIrradiated(String unitNumber, String productCode) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM bld_batch_item bi
            JOIN bld_batch b ON bi.batch_id = b.id
            WHERE bi.unit_number = :unitNumber AND b.end_time IS NOT NULL AND bi.product_code = :productCode
            """;

        return databaseClient.sql(sql)
                .bind("unitNumber", unitNumber)
                .bind("productCode", productCode)
                .map(row -> row.get(0, Boolean.class))
                .one();
    }

    @Override
    public Mono<Boolean> isUnitBeingIrradiated(String unitNumber, String productCode) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM bld_batch_item bi
            JOIN bld_batch b ON bi.batch_id = b.id
            WHERE bi.unit_number = :unitNumber AND b.end_time IS NULL AND bi.product_code = :productCode
            """;

        return databaseClient.sql(sql)
                .bind("unitNumber", unitNumber)
                .bind("productCode", productCode)
                .map(row -> row.get(0, Boolean.class))
                .one();
    }

}
