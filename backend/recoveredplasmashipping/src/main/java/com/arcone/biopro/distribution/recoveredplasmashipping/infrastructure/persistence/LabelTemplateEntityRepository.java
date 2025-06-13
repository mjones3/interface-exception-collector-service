package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface LabelTemplateEntityRepository extends ReactiveCrudRepository<LabelTemplateEntity, Long> {

    Mono<LabelTemplateEntity> findByTypeAndActiveIsTrue(final String type);
}
