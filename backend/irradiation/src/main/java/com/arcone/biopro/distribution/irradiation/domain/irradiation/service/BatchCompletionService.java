package com.arcone.biopro.distribution.irradiation.domain.irradiation.service;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.aggregate.IrradiationAggregate;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchId;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface BatchCompletionService {
    
    Mono<IrradiationAggregate> prepareBatchCompletion(
        BatchId batchId, 
        List<IrradiationAggregate.BatchItemCompletion> itemCompletions,
        LocalDateTime completionTime
    );
    
    Mono<Void> completeBatch(IrradiationAggregate aggregate);
}