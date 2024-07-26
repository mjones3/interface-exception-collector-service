package com.arcone.biopro.distribution.orderservice.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.orderservice.domain.model.Lookup;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.LookupId;
import com.arcone.biopro.distribution.orderservice.infrastructure.mapper.LookupEntityMapper;
import com.arcone.biopro.distribution.orderservice.infrastructure.persistence.LookupEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(classes = { LookupEntityMapper.class })
class LookupEntityMapperTest {

    @Autowired
    LookupEntityMapper mapper;

    @Test
    public void testMapToEntity() {
        var lookup = new Lookup(new LookupId("type", "optionValue"), "description", 1, true);
        var entity = mapper.mapToEntity(lookup);

        assertEquals(lookup.getId().getType(), entity.getType());
        assertEquals(lookup.getId().getOptionValue(), entity.getOptionValue());
        assertEquals(lookup.getDescriptionKey(), entity.getDescriptionKey());
        assertEquals(lookup.getOrderNumber(), entity.getOrderNumber());
        assertEquals(lookup.isActive(), entity.isActive());
    }

    @Test
    public void testMapToDomain() {
        var entity = LookupEntity.builder()
            .type("type")
            .optionValue("optionValue")
            .descriptionKey("description")
            .orderNumber(1)
            .active(true)
            .build();
        var lookup = mapper.mapToDomain(entity);

        assertEquals(entity.getType(), lookup.getId().getType());
        assertEquals(entity.getOptionValue(), lookup.getId().getOptionValue());
        assertEquals(entity.getDescriptionKey(), lookup.getDescriptionKey());
        assertEquals(entity.getOrderNumber(), lookup.getOrderNumber());
        assertEquals(entity.isActive(), lookup.isActive());
    }

}
