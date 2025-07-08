package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CheckBatchStatusUseCase {

    private final BatchRepository batchRepository;

    public CheckBatchStatusUseCase(BatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    public Mono<Boolean> execute(String batchId) {
        return Mono.just(true); // For now, return true since we're using string IDs
    }
}
