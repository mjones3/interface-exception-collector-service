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

        var facility = target.getFacilityId("123456789");

        StepVerifier.create(facility)
            .consumeNextWith(detail -> {
                assertEquals(Optional.of(1), Optional.of(detail.id()));
                assertEquals(Optional.of("123456789"), Optional.of(detail.externalId()));
                assertEquals(Optional.of("MDL Hub 1"), Optional.of(detail.name()));
                assertNotNull(detail.properties());
                assertEquals(Optional.of("123-456-7894"), Optional.of(detail.properties().get("PHONE_NUMBER")));
            })
            .verifyComplete();
    }
    @Test
    public void shouldThrowErrorWhenNotFindFacilityById(){

        var target = new FacilityServiceMock(objectMapper);

        var facility = target.getFacilityId("TEST");

        StepVerifier.create(facility)
            .expectError(RuntimeException.class);
    }
}
