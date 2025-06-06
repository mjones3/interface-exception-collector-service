package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import com.arcone.biopro.distribution.receiving.domain.model.enumeration.ParseType;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.graphql.data.GraphQlRepository;
import reactor.core.publisher.Mono;


@GraphQlRepository
public interface BarcodePatternRepository extends ReactiveCrudRepository<BarcodePatternEntity, Long> {

    Mono<BarcodePatternEntity> findByParseType(ParseType parseType);

}
