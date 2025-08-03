package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchItem;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for Batch domain entity to database entity conversion.
 */
@Component
public class BatchEntityMapper {

    public Batch toDomain(BatchEntity entity) {
        return new Batch(
                BatchId.of(entity.getId()),
                DeviceId.of(entity.getDeviceId()),
                entity.getStartTime(),
                entity.getEndTime()
        );
    }

    public BatchEntity toEntity(Batch batch) {
        return BatchEntity.builder()
                .id(batch.getId().getValue())
                .deviceId(batch.getDeviceId().getValue())
                .startTime(batch.getStartTime())
                .endTime(batch.getEndTime())
                .build();
    }

    public BatchItem toDomain(BatchItemEntity entity) {
        return BatchItem.builder()
                .id(entity.getId())
                .unitNumber(new UnitNumber(entity.getUnitNumber()))
                .productCode(entity.getProductCode())
                .lotNumber(entity.getLotNumber())
                .newProductCode(entity.getNewProductCode())
                .expirationDate(entity.getExpirationDate())
                .productFamily(entity.getProductFamily())
                .build();
    }

    public BatchItemEntity toEntity(BatchItem batchItem, Long batchId) {
        return BatchItemEntity.builder()
                .id(batchItem.id())
                .batchId(batchId)
                .unitNumber(batchItem.unitNumber().value())
                .productCode(batchItem.productCode())
                .lotNumber(batchItem.lotNumber())
                .newProductCode(batchItem.newProductCode())
                .expirationDate(batchItem.expirationDate())
                .productFamily(batchItem.productFamily())
                .build();
    }
}
