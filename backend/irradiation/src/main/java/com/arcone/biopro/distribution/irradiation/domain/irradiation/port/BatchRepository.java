package com.arcone.biopro.distribution.irradiation.domain.irradiation.port;

import com.arcone.biopro.distribution.irradiation.application.irradiation.command.SubmitBatchCommand;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.ImportedBloodCenter;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchItem;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface BatchRepository {
    Mono<Batch> findActiveBatchByDeviceId(DeviceId deviceId);
    Flux<BatchItem> findBatchItemsByBatchId(Long batchId);
    Mono<Batch> findById(BatchId batchId);
    Mono<Batch> submitBatch(DeviceId deviceId, SubmitBatchCommand command, List<BatchItem> batchItems);
    Mono<Batch> completeBatch(BatchId batchId, LocalDateTime endTime);
    Mono<Void> updateBatchItemNewProductCode(BatchId batchId, String unitNumber, String productCode, String newProductCode);
    Mono<BatchItem> findBatchItem(BatchId batchId, String unitNumber, String productCode);
    Mono<Boolean> isUnitAlreadyIrradiated(String unitNumber, String productCode);
    Mono<Boolean> isUnitBeingIrradiated(String unitNumber, String productCode);
    Mono<Void> markBatchItemAsTimingRuleValidated(String unitNumber, String productCode);
    Mono<ImportedBloodCenter> findImportedBloodCenterByBatchItemId(Long batchItemId);
    Mono<Batch> findLatestBatchWithItemByUnitAndProduct(String unitNumber, String productCode);
}
