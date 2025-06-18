package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ImportItemPropertyEntityRepository extends ReactiveCrudRepository<ImportItemPropertyEntity, Long> {

    Flux<ImportItemPropertyEntity> findAllByImportItemId(Long importItemId);
}
