package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface InternalTransferItemEntityRepository extends ReactiveCrudRepository<InternalTransferItemEntity, Long> {

    Flux<InternalTransferItemEntity> findAllByInternalTransferId(Long internalTransferId);
}
