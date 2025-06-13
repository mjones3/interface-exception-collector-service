package com.arcone.biopro.distribution.recoveredplasmashipping.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.LookupEntityMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.LookupEntityMapperImpl;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.LookupEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(classes = { LookupEntityMapperImpl.class })
class LookupEntityMapperTest {

    @Autowired
    LookupEntityMapper lookupEntityMapper;

    @Test
    void shouldMapToDomain() {
        var lookupEntity = LookupEntity.builder()
            .id(1L)
            .type("type")
            .optionValue("optionValue")
            .descriptionKey("descriptionKey")
            .orderNumber(1)
            .active(true)
            .build();

        var lookup = lookupEntityMapper.mapToDomain(lookupEntity);
        assertEquals(Long.valueOf(1L), lookup.getId());
        assertEquals("type", lookup.getType());
        assertEquals("optionValue", lookup.getOptionValue());
        assertEquals("descriptionKey", lookup.getDescriptionKey());
        assertEquals(1, lookup.getOrderNumber());
        assertTrue(lookup.isActive());
    }

}
