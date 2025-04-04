package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.LocationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.NoResultsFoundException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.LocationOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationUseCase implements LocationService {

    private final LocationRepository locationRepository;
    private final LocationOutputMapper locationOutputMapper;


    @Override
    public Flux<LocationOutput> findAll() {
        return locationRepository.findAll()
            .switchIfEmpty(Mono.error(NoResultsFoundException::new))
            .map(locationOutputMapper::toLocationOutput);
    }

    @Override
    public Mono<LocationOutput> findById(Long id) {
        return locationRepository.findOneById(id)
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", id))))
            .map(locationOutputMapper::toLocationOutput);
    }
}
