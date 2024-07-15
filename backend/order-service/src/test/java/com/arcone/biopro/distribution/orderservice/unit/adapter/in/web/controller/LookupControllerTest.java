package com.arcone.biopro.distribution.orderservice.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.orderservice.adapter.in.web.controller.LookupController;
import com.arcone.biopro.distribution.orderservice.adapter.in.web.dto.LookupDTO;
import com.arcone.biopro.distribution.orderservice.application.mapper.LookupMapper;
import com.arcone.biopro.distribution.orderservice.domain.model.Lookup;
import com.arcone.biopro.distribution.orderservice.domain.model.LookupId;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = { LookupController.class, LookupMapper.class })
class LookupControllerTest {

    @Autowired
    LookupController lookupController;

    @Autowired
    LookupMapper lookupMapper;

    @MockBean
    LookupService lookupService;

    @Test
    void testFindAllLookupsByType() {
        // Arrange
        var type = "CONFIG";
        var lookups = new Lookup[] {
            new Lookup(new LookupId(type, "optionValue1"), "description1", 1, true),
            new Lookup(new LookupId(type, "optionValue2"), "description2", 2, true),
            new Lookup(new LookupId(type, "optionValue3"), "description3", 3, true)
        };
        when(this.lookupService.findAllByType(type)).thenReturn(Flux.just(lookups));

        // Act
        var response = this.lookupController.findAllLookupsByType(type)
            .toStream()
            .toArray(LookupDTO[]::new);

        // Assert
        for (int i = 0; i < response.length; i++) {
            var lookup = lookups[i];
            var lookupDTO = response[i];

            assertEquals(lookup.getId().type(), lookupDTO.type());
            assertEquals(lookup.getId().optionValue(), lookupDTO.optionValue());
            assertEquals(lookup.getDescriptionKey(), lookupDTO.descriptionKey());
            assertEquals(lookup.getOrderNumber(), lookupDTO.orderNumber());
            assertEquals(lookup.isActive(), lookupDTO.active());
        }
    }

    @Test
    void testInsertLookup() {
        var lookup = new Lookup(new LookupId("type", "optionValue"), "description", 1, true);
        when(this.lookupService.insert(lookup))
            .thenAnswer(i -> Mono.just(i.getArgument(0)));

        var response = this.lookupController
            .insertLookup(
                LookupDTO.builder()
                    .type(lookup.getId().type())
                    .optionValue(lookup.getId().optionValue())
                    .descriptionKey(lookup.getDescriptionKey())
                    .orderNumber(lookup.getOrderNumber())
                    .active(lookup.isActive())
                    .build()
            )
            .block();

        assertEquals(lookup.getId().type(), response.type());
        assertEquals(lookup.getId().optionValue(), response.optionValue());
        assertEquals(lookup.getDescriptionKey(), response.descriptionKey());
        assertEquals(lookup.getOrderNumber(), response.orderNumber());
        assertEquals(lookup.isActive(), response.active());
    }

    @Test
    void testUpdateLookup() {
        var lookup = new Lookup(new LookupId("type", "optionValue"), "description", 1, true);
        when(lookupService.update(lookup))
            .thenAnswer(i -> Mono.just(i.getArgument(0)));

        var response = this.lookupController
            .updateLookup(
                LookupDTO.builder()
                    .type(lookup.getId().type())
                    .optionValue(lookup.getId().optionValue())
                    .descriptionKey(lookup.getDescriptionKey())
                    .orderNumber(lookup.getOrderNumber())
                    .active(lookup.isActive())
                    .build()
            )
            .block();

        assertEquals(lookup.getId().type(), response.type());
        assertEquals(lookup.getId().optionValue(), response.optionValue());
        assertEquals(lookup.getDescriptionKey(), response.descriptionKey());
        assertEquals(lookup.getOrderNumber(), response.orderNumber());
        assertEquals(lookup.isActive(), response.active());
    }

    @Test
    void testDeleteLookup() {
        var lookup = new Lookup(new LookupId("type", "optionValue"), "description", 1, true);
        when(lookupService.delete(lookup))
            .thenAnswer(i -> {
                var lookupArg = i.<Lookup>getArgument(0);
                return Mono.just(new Lookup(lookupArg.getId(), lookupArg.getDescriptionKey(), lookupArg.getOrderNumber(), false));
            });

        var response = this.lookupController
            .deleteLookup(
                LookupDTO.builder()
                    .type(lookup.getId().type())
                    .optionValue(lookup.getId().optionValue())
                    .descriptionKey(lookup.getDescriptionKey())
                    .orderNumber(lookup.getOrderNumber())
                    .active(lookup.isActive())
                    .build()
            )
            .block();

        assertEquals(lookup.getId().type(), response.type());
        assertEquals(lookup.getId().optionValue(), response.optionValue());
        assertEquals(lookup.getDescriptionKey(), response.descriptionKey());
        assertEquals(lookup.getOrderNumber(), response.orderNumber());
        assertFalse(response.active());
    }

}
