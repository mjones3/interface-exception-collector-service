package com.arcone.biopro.distribution.shipping.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ExternalTransferItemEntityRepository extends ReactiveCrudRepository<ExternalTransferItemEntity, Long> {

    Flux<ExternalTransferItemEntity> findAllByExternalTransferId(final Long externalTransferId);

}
