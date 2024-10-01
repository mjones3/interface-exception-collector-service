package com.arcone.biopro.distribution.shipping.unit.application.mapper;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.LookupDTO;
import com.arcone.biopro.distribution.shipping.application.mapper.LookupMapper;
import com.arcone.biopro.distribution.shipping.domain.model.Lookup;
import com.arcone.biopro.distribution.shipping.domain.model.vo.LookupId;
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
        assertEquals("type", result.getId().getType());
        assertEquals("optionValue", result.getId().getOptionValue());
        assertEquals("description", result.getDescriptionKey());
        assertEquals(1, result.getOrderNumber());
        assertTrue(result.isActive());
    }

}
