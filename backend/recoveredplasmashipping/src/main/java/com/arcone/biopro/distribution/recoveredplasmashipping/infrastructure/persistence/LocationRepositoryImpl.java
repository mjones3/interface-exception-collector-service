package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Location;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.LocationEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class LocationRepositoryImpl implements LocationRepository {

    private final LocationEntityRepository locationEntityRepository;
    private final LocationPropertyEntityRepository locationPropertyEntityRepository;
    private final LocationEntityMapper locationEntityMapper;



    @Override
    public Flux<Location> findAll() {
        return locationEntityRepository.findAllByActiveIsTrueOrderByNameAsc()
            .map(locationEntity -> locationEntityMapper.toDomain(locationEntity,null));
    }

    @Override
    public Mono<Location> findOneById(Long id) {
        return locationEntityRepository.findById(id)
            .flatMap(locationEntity -> locationPropertyEntityRepository.findByLocationId(locationEntity.getId())
                .collectList()
                .map(locationProperties -> locationEntityMapper.toDomain(locationEntity,locationProperties)));
    }

    @Override
    public Mono<Location> findOneByCode(String code) {
        return locationEntityRepository.findByCode(code)
            .flatMap(locationEntity -> locationPropertyEntityRepository.findByLocationId(locationEntity.getId())
                .collectList()
                .map(locationProperties -> locationEntityMapper.toDomain(locationEntity,locationProperties)));
    }
}
