package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Lookup;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LookupRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.LookupEntityMapper;
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
