package com.arcone.biopro.distribution.irradiation.infrastructure.persistence;

import com.arcone.biopro.distribution.irradiation.infrastructure.persistence.entity.ConfigurationEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.graphql.data.GraphQlRepository;
import reactor.core.publisher.Flux;

import java.util.List;

@GraphQlRepository
public interface ConfigurationEntityRepository extends ReactiveCrudRepository<ConfigurationEntity, String> {

    Flux<ConfigurationEntity> findByKeyIn(List<String> keys);

}
