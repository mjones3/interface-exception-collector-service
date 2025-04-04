package com.arcone.biopro.distribution.recoveredplasmashipping.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.controller.LocationController;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.LocationMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.LocationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.LocationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;

import java.util.Map;

import static org.mockito.BDDMockito.given;

class LocationControllerTest {

    private LocationController locationController;
    private LocationService locationService;


    @BeforeEach
    void setUp() {
        locationService = Mockito.mock(LocationService.class);
        locationController = new LocationController(locationService, Mappers.getMapper(LocationMapper.class));

    }

    @Test
    public void shouldListAll() {

        var list = Flux.just(LocationOutput
            .builder()
                .id(1L)
                .code("CODE")
                .city("CITY")
                .properties(Map.of("KEY1","VALUE1"))
            .build());

        given(this.locationService.findAll())
            .willReturn(list);

        // Act
        var response = locationController.findAll();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(1, response.collectList().block().size());
        Assertions.assertEquals(1L, response.collectList().block().getFirst().id());
        Assertions.assertEquals("CODE", response.collectList().block().getFirst().code());
        Assertions.assertEquals("CITY", response.collectList().block().getFirst().city());


    }
}
