package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.repository;

import com.arcone.biopro.distribution.irradiation.application.irradiation.command.SubmitBatchCommand;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchItem;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.BatchEntity;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.BatchEntityMapper;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.BatchItemEntity;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.ImportedBloodCenterEntity;
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
    Mono<BatchItemEntity> findByBatchIdAndUnitNumberAndProductCode(Long batchId, String unitNumber, String productCode);
}

@Repository
public class BatchRepositoryImpl implements BatchRepository {
    private final BatchEntityRepository batchRepository;
    private final BatchItemEntityRepository batchItemRepository;
    private final BatchEntityMapper mapper;
    private final DatabaseClient databaseClient;
    private final ImportedBloodCenterEntityRepository importedBloodCenterEntityRepository;

    public BatchRepositoryImpl(BatchEntityRepository batchRepository,
                               BatchItemEntityRepository batchItemRepository,
                               BatchEntityMapper mapper,
                               DatabaseClient databaseClient, ImportedBloodCenterEntityRepository importedBloodCenterEntityRepository) {
        this.batchRepository = batchRepository;
        this.batchItemRepository = batchItemRepository;
        this.mapper = mapper;
        this.databaseClient = databaseClient;
        this.importedBloodCenterEntityRepository = importedBloodCenterEntityRepository;
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
    public Mono<Batch> submitBatch(DeviceId deviceId, SubmitBatchCommand command, List<BatchItem> batchItems) {
        BatchEntity batchEntity = BatchEntity.builder()
                .deviceId(deviceId.getValue())
                .startTime(command.startTime())
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
                        .collectList()
                        .flatMap(savedItems -> Flux.fromIterable(savedItems)
                            .filter(savedItem -> batchItems.stream()
                                .anyMatch(item -> item.unitNumber().value().equals(savedItem.getUnitNumber())
                                    && item.productCode().equals(savedItem.getProductCode())
                                    && item.isImported()))
                            .flatMap(savedItem -> command.batchItems().stream()
                                .filter(batchItemDTO -> batchItemDTO.unitNumber().equals(savedItem.getUnitNumber()) &&
                                    batchItemDTO.productCode().equals(savedItem.getProductCode()))
                                .findFirst()
                                .map(batchItemDTO -> {
                                    ImportedBloodCenterEntity entity = ImportedBloodCenterEntity.builder()
                                        .productId(savedItem.getId())
                                        .name(batchItemDTO.bloodCenterName())
                                        .address(batchItemDTO.address())
                                        .registrationNumber(batchItemDTO.registrationNumber())
                                        .licenseNumber(batchItemDTO.licenseNumber())
                                        .createDate(ZonedDateTime.now())
                                        .modificationDate(ZonedDateTime.now())
                                        .build();
                                    return importedBloodCenterEntityRepository.save(entity);
                                })
                                .orElse(Mono.empty()))
                            .then(Mono.just(mapper.toDomain(savedBatch))));
                });
    }

    @Override
    public Mono<Boolean> isUnitAlreadyIrradiated(String unitNumber, String productCode) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM bld_batch_item bi
            JOIN bld_batch b ON bi.batch_id = b.id
            WHERE bi.unit_number = :unitNumber AND b.end_time IS NOT NULL AND bi.new_product_code = :productCode
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

    @Override
    public Mono<Batch> findById(BatchId batchId) {
        return batchRepository.findById(batchId.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Batch> completeBatch(BatchId batchId, LocalDateTime endTime) {
        return batchRepository.findById(batchId.getValue())
                .flatMap(entity -> {
                    entity.setEndTime(endTime);
                    entity.setModificationDate(ZonedDateTime.now());
                    return batchRepository.save(entity);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> updateBatchItemNewProductCode(BatchId batchId, String unitNumber, String productCode, String newProductCode) {
        String sql = """
            UPDATE bld_batch_item
            SET new_product_code = :newProductCode, modification_date = NOW()
            WHERE batch_id = :batchId AND unit_number = :unitNumber AND product_code = :productCode
            """;

        return databaseClient.sql(sql)
                .bind("newProductCode", newProductCode)
                .bind("batchId", batchId.getValue())
                .bind("unitNumber", unitNumber)
                .bind("productCode", productCode)
                .fetch()
                .rowsUpdated()
                .then();
    }

    @Override
    public Mono<BatchItem> findBatchItem(BatchId batchId, String unitNumber, String productCode) {
        return batchItemRepository.findByBatchIdAndUnitNumberAndProductCode(
                batchId.getValue(), unitNumber, productCode)
                .map(this::mapToBatchItemWithDefaults);
    }

    @Override
    public Mono<Void> markBatchItemAsTimingRuleValidated(String unitNumber, String productCode) {
        String sql = """
            UPDATE bld_batch_item
            SET is_timing_rule_validated = true, modification_date = NOW()
            WHERE unit_number = :unitNumber AND product_code = :productCode
            AND batch_id = (
                SELECT bi.batch_id FROM bld_batch_item bi
                JOIN bld_batch b ON bi.batch_id = b.id
                WHERE bi.unit_number = :unitNumber AND bi.product_code = :productCode
                ORDER BY b.start_time DESC LIMIT 1
            )
            """;

        return databaseClient.sql(sql)
            .bind("unitNumber", unitNumber)
            .bind("productCode", productCode)
            .fetch()
            .rowsUpdated()
            .then();
    }

    @Override
    public Mono<Batch> findLatestBatchWithItemByUnitAndProduct(String unitNumber, String productCode) {
        String sql = """
            SELECT b.*
            FROM bld_batch_item bi
            JOIN bld_batch b ON bi.batch_id = b.id
            WHERE bi.unit_number = :unitNumber AND bi.product_code = :productCode
            ORDER BY b.start_time DESC
            LIMIT 1
            """;

        return databaseClient.sql(sql)
            .bind("unitNumber", unitNumber)
            .bind("productCode", productCode)
            .map(row -> BatchEntity.builder()
                .id(row.get("id", Long.class))
                .deviceId(row.get("device_id", String.class))
                .startTime(row.get("start_time", LocalDateTime.class))
                .endTime(row.get("end_time", LocalDateTime.class))
                .createDate(row.get("create_date", ZonedDateTime.class))
                .modificationDate(row.get("modification_date", ZonedDateTime.class))
                .build())
            .one()
            .map(mapper::toDomain);
    }

    private BatchItem mapToBatchItemWithDefaults(BatchItemEntity entity) {
        return BatchItem.builder()
                .unitNumber(new com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber(entity.getUnitNumber()))
                .productCode(entity.getProductCode())
                .lotNumber(entity.getLotNumber())
                .newProductCode(entity.getNewProductCode())
                .expirationDate(entity.getExpirationDate())
                .productFamily(entity.getProductFamily())
                .productDescription(null)
                .irradiated(false)
                .isTimingRuleValidated(entity.getIsTimingRuleValidated())
                .build();
    }

}
