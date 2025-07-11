package com.arcone.biopro.distribution.order.unit.application.usecase;

import com.arcone.biopro.distribution.order.application.exception.NoResultsFoundException;
import com.arcone.biopro.distribution.order.application.mapper.LocationOutputMapper;
import com.arcone.biopro.distribution.order.application.usecase.LocationUseCase;
import com.arcone.biopro.distribution.order.domain.model.Location;
import com.arcone.biopro.distribution.order.domain.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class LocationUseCaseTest {

    private LocationUseCase locationUseCase;
    private LocationRepository locationRepository;

    @BeforeEach
    public void setup() {
        locationRepository = Mockito.mock(LocationRepository.class);
        locationUseCase = new LocationUseCase(locationRepository, Mappers.getMapper(LocationOutputMapper.class));
    }

    @Test
    public void shouldFindAll(){

        Mockito.when(locationRepository.findAll()).thenReturn(Flux.just(Mockito.mock(Location.class),Mockito.mock(Location.class)));

        StepVerifier.create(locationUseCase.findAll())
            .expectNextCount(2)
            .verifyComplete();

    }

    @Test
    public void shouldNotFindAll(){

        Mockito.when(locationRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(locationUseCase.findAll())
            .expectError(NoResultsFoundException.class)
            .verify();

    }

}
