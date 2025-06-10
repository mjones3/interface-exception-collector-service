package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import com.arcone.biopro.distribution.receiving.domain.model.Lookup;
import com.arcone.biopro.distribution.receiving.domain.repository.LookupRepository;
import com.arcone.biopro.distribution.receiving.infrastructure.mapper.LookupEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
@RequiredArgsConstructor
public class LookupRepositoryImpl implements LookupRepository {

    private final LookupEntityRepository lookupEntityRepository;
    private final LookupEntityMapper lookupEntityMapper;

    @Override
    public Flux<Lookup> findAllByType(final String type) {
        return lookupEntityRepository
            .findAllByTypeAndActiveIsTrueOrderByOrderNumberAsc(type)
            .map(lookupEntityMapper::mapToDomain);
    }

}
