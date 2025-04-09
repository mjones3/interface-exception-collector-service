package com.arcone.biopro.distribution.recoveredplasmashipping.unit.adapter.in.web.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.LookupDTOMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.LookupDTOMapperImpl;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.LookupOutput;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(classes = { LookupDTOMapperImpl.class })
class LookupDTOMapperTest {

    @Autowired
    LookupDTOMapper lookupDTOMapper;

    @Test
    void shouldMapToDTO() {
        var lookupOutput = LookupOutput.builder()
            .id(1L)
            .type("type")
            .optionValue("optionValue")
            .descriptionKey("descriptionKey")
            .orderNumber(1)
            .active(true)
            .build();

        var lookupDTO = lookupDTOMapper.mapToDTO(lookupOutput);
        assertEquals(Long.valueOf(1L), lookupDTO.id());
        assertEquals("type", lookupDTO.type());
        assertEquals("optionValue", lookupDTO.optionValue());
        assertEquals("descriptionKey", lookupDTO.descriptionKey());
        assertEquals(1, lookupDTO.orderNumber());
        assertTrue(lookupDTO.active());
    }

}
