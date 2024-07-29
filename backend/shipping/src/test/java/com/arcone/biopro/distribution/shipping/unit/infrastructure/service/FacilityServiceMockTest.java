package com.arcone.biopro.distribution.shipping.unit.infrastructure.service;

import com.arcone.biopro.distribution.shipping.infrastructure.service.FacilityServiceMock;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

class FacilityServiceMockTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp(){
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void shouldFindFacilityById(){

        var target = new FacilityServiceMock(objectMapper);

        var facility = target.getFacilityId(3);

        StepVerifier.create(facility)
            .consumeNextWith(detail -> {
                assertEquals(Optional.of(3), Optional.of(detail.id()));
                assertEquals(Optional.of("IC39"), Optional.of(detail.externalId()));
                assertEquals(Optional.of("Charlotte Main"), Optional.of(detail.name()));
                assertNotNull(detail.properties());
                assertEquals(Optional.of("(704) 972-4742"), Optional.of(detail.properties().get("PHONE_NUMBER")));
            })
            .verifyComplete();
    }
    @Test
    public void shouldThrowErrorWhenNotFindFacilityById(){

        var target = new FacilityServiceMock(objectMapper);

        var facility = target.getFacilityId(5);

        StepVerifier.create(facility)
            .expectError(RuntimeException.class);
    }
}
