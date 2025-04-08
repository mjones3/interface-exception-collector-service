package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.LookupOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.LookupOutputMapperImpl;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Lookup;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.LookupEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(classes = { LookupOutputMapperImpl.class })
public class LookupOutputMapperTest {

    @Autowired
    LookupOutputMapper lookupOutputMapper;

    @Test
    void shouldMapToOutput() {
        var lookup = Lookup.fromRepository(
            LookupEntity.builder()
                .id(1L)
                .type("type")
                .optionValue("optionValue")
                .descriptionKey("descriptionKey")
                .orderNumber(1)
                .active(true)
                .build()
        );

        var lookupOutput = lookupOutputMapper.mapToOutput(lookup);
        assertEquals(Long.valueOf(1L), lookupOutput.id());
        assertEquals("type", lookupOutput.type());
        assertEquals("optionValue", lookupOutput.optionValue());
        assertEquals("descriptionKey", lookupOutput.descriptionKey());
        assertEquals(1, lookupOutput.orderNumber());
        assertTrue(lookupOutput.active());
    }

}
