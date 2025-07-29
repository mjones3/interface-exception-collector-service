package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface InternalTransferEntityRepository extends ReactiveCrudRepository<InternalTransferEntity, Long> {

    Mono<InternalTransferEntity> findByOrderNumber(Long orderNumber);

}
