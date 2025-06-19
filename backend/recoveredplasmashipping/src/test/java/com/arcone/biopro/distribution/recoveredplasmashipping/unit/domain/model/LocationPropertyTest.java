package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.LocationProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LocationPropertyTest {

    @Test
    public void shouldCreateLocationProperty() {
        var property = new LocationProperty(1L,"KEY","VALUE");

        Assertions.assertNotNull(property);
        Assertions.assertEquals(1L,property.getLocationId());
        Assertions.assertEquals("KEY",property.getPropertyKey());
        Assertions.assertEquals("VALUE",property.getPropertyValue());
    }


    @Test
    public void shouldNotCreateLocationProperty() {

        try {
            new LocationProperty(null,"KEY","VALUE");
            Assertions.fail();
        }catch (IllegalArgumentException e){

            Assertions.assertEquals("Location ID cannot be null",e.getMessage());
        }

        try {
            new LocationProperty(1L,null,"VALUE");
            Assertions.fail();
        }catch (IllegalArgumentException e){

            Assertions.assertEquals("Property Key cannot be null or empty",e.getMessage());
        }

        try {
            new LocationProperty(1L,"KEY",null);
            Assertions.fail();
        }catch (IllegalArgumentException e){

            Assertions.assertEquals("Property Value cannot be null or empty",e.getMessage());
        }

        try {
            new LocationProperty(1L,"","VALUE");
            Assertions.fail();
        }catch (IllegalArgumentException e){

            Assertions.assertEquals("Property Key cannot be null or empty",e.getMessage());
        }

        try {
            new LocationProperty(1L,"KEY","");
            Assertions.fail();
        }catch (IllegalArgumentException e){

            Assertions.assertEquals("Property Value cannot be null or empty",e.getMessage());
        }
    }
}
