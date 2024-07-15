package com.arcone.biopro.distribution.orderservice.unit.application.mapper;

import com.arcone.biopro.distribution.orderservice.adapter.in.web.dto.LookupDTO;
import com.arcone.biopro.distribution.orderservice.application.mapper.LookupMapper;
import com.arcone.biopro.distribution.orderservice.domain.model.Lookup;
import com.arcone.biopro.distribution.orderservice.domain.model.LookupId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(classes = { LookupMapper.class })
class LookupMapperTest {

    @Autowired
    LookupMapper lookupMapper;

    @Test
    void testMapToDTO() {
        // Setup
        var lookup = new Lookup(new LookupId("type", "optionValue"), "description", 1, true);

        // Execute
        var result = lookupMapper.mapToDTO(lookup);

        // Verify
        assertEquals("type", result.type());
        assertEquals("optionValue", result.optionValue());
        assertEquals("description", result.descriptionKey());
        assertEquals(1, result.orderNumber());
        assertTrue(result.active());
    }

    @Test
    void testMapToDomain() {
        // Setup
        var lookupDTO = LookupDTO.builder()
            .type("type")
            .optionValue("optionValue")
            .descriptionKey("description")
            .orderNumber(1)
            .active(true)
            .build();

        // Execute
        var result = lookupMapper.mapToDomain(lookupDTO);

        // Verify
        assertEquals("type", result.getId().type());
        assertEquals("optionValue", result.getId().optionValue());
        assertEquals("description", result.getDescriptionKey());
        assertEquals(1, result.getOrderNumber());
        assertTrue(result.isActive());
    }

}
