package com.arcone.biopro.distribution.recoveredplasmashipping.unit.adapter.in.web.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.LocationMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.LocationOutput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Map;

class LocationMapperTest {

    private LocationMapper locationMapper;

    @BeforeEach()
    public void setUp() {
        locationMapper = Mappers.getMapper(LocationMapper.class);
    }

    @Test
    public void toLocationDto() {

        var dto = locationMapper.toDto(LocationOutput
            .builder()
                .id(1L)
                .name("Name")
                .city("City")
                .code("Code")
                .state("State")
                .addressLine1("AddressLine1")
                .postalCode("PostalCode")
                .addressLine2("AddressLine2")
                .properties(Map.of("KEY1","VALUE1", "KEY2","VALUE2"))
            .build());

        Assertions.assertNotNull(dto);
        Assertions.assertEquals("Name",dto.name());
        Assertions.assertEquals("City",dto.city());
        Assertions.assertEquals("Code",dto.code());
        Assertions.assertEquals("State",dto.state());
        Assertions.assertEquals("AddressLine1",dto.addressLine1());
        Assertions.assertEquals("PostalCode",dto.postalCode());
        Assertions.assertEquals("AddressLine2",dto.addressLine2());
        Assertions.assertEquals("VALUE1",dto.properties().get("KEY1"));

    }

}
