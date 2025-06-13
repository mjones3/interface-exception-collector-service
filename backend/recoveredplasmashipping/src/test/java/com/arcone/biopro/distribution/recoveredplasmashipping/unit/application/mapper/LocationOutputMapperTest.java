package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.LocationOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Location;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class LocationOutputMapperTest {


    private LocationOutputMapper locationMapper;

    @BeforeEach()
    public void setUp() {
        locationMapper = Mappers.getMapper(LocationOutputMapper.class);
    }

    @Test
    public void toLocationOutput() {

        var location = new Location(1L,"NAME","CODE","ExternalId","Address1","Address2"
            ,"POSTAL_CODE","CITY","STATE");

        location.addProperty("KEY1","VALUE1");

        var locationOutput = locationMapper.toLocationOutput(location);

        Assertions.assertNotNull(locationOutput);
        Assertions.assertEquals("NAME",locationOutput.name());
        Assertions.assertEquals("CITY",locationOutput.city());
        Assertions.assertEquals("CODE",locationOutput.code());
        Assertions.assertEquals("STATE",locationOutput.state());
        Assertions.assertEquals("Address1",locationOutput.addressLine1());
        Assertions.assertEquals("POSTAL_CODE",locationOutput.postalCode());
        Assertions.assertEquals("Address2",locationOutput.addressLine2());
        Assertions.assertEquals("VALUE1",locationOutput.properties().get("KEY1"));

    }

    @Test
    public void toLocationOutputWhenPropertiesEmpty() {

        var location = new Location(1L,"NAME","CODE","ExternalId","Address1","Address2"
            ,"POSTAL_CODE","CITY","STATE");

        var locationOutput = locationMapper.toLocationOutput(location);

        Assertions.assertNotNull(locationOutput);
        Assertions.assertEquals("NAME",locationOutput.name());
        Assertions.assertEquals("CITY",locationOutput.city());
        Assertions.assertEquals("CODE",locationOutput.code());
        Assertions.assertEquals("STATE",locationOutput.state());
        Assertions.assertEquals("Address1",locationOutput.addressLine1());
        Assertions.assertEquals("POSTAL_CODE",locationOutput.postalCode());
        Assertions.assertEquals("Address2",locationOutput.addressLine2());
        Assertions.assertTrue(locationOutput.properties().isEmpty());

    }

}
