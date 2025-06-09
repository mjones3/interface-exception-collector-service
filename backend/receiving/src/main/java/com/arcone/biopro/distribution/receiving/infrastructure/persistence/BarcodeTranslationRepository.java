package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.graphql.data.GraphQlRepository;
import reactor.core.publisher.Mono;

@GraphQlRepository
public interface BarcodeTranslationRepository extends ReactiveCrudRepository<BarcodeTranslationEntity, Long> {

    Mono<BarcodeTranslationEntity> findByFromValueAndSixthDigit(String fromValue, String sixthDigit);

}
