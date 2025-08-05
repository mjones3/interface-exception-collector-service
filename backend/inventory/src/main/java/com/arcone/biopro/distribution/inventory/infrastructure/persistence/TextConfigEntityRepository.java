package com.arcone.biopro.distribution.inventory.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TextConfigEntityRepository extends ReactiveCrudRepository<TextConfigEntity, UUID> {
    Mono<TextConfigEntity> findByKeyCode(String keyCode);
}
