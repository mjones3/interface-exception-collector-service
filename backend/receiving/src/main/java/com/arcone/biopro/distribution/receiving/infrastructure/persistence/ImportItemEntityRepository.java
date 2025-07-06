package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ImportItemEntityRepository extends ReactiveCrudRepository<ImportItemEntity, Long>{

    Flux<ImportItemEntity> findAllByImportId(Long importId);
    Mono<Void> deleteAllByImportId(Long importId);
}
