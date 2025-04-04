package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Location;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LocationTest {


    @Test
    void shouldCreateLocation() {
        var location = new Location(1L,"NAME","CODE","ExternalId","Address1","Address2"
            ,"POSTAL_CODE","CITY","STATE");

        Assertions.assertNotNull(location);
        Assertions.assertEquals(1L, location.getId());
        Assertions.assertEquals("NAME", location.getName());
        Assertions.assertEquals("CODE", location.getCode());
        Assertions.assertEquals("ExternalId", location.getExternalId());
        Assertions.assertEquals("Address1", location.getAddressLine1());
        Assertions.assertEquals("Address2", location.getAddressLine2());
        Assertions.assertEquals("POSTAL_CODE", location.getPostalCode());
        Assertions.assertEquals("CITY", location.getCity());
        Assertions.assertEquals("STATE", location.getState());
    }

    @Test
    void shouldNotCreateLocation() {

        try {
            new Location(1L,null,"CODE","ExternalId","Address1","Address2"
                ,"POSTAL_CODE","CITY","STATE");
            Assertions.fail();
        }catch (IllegalArgumentException e){

            Assertions.assertEquals("Name cannot be null or blank",e.getMessage());
        }

        try {
            new Location(1L,"null",null,"ExternalId","Address1","Address2"
                ,"POSTAL_CODE","CITY","STATE");
            Assertions.fail();
        }catch (IllegalArgumentException e){

            Assertions.assertEquals("Code cannot be null or blank",e.getMessage());
        }

        try {
            new Location(1L,"null","null","ExternalId",null,"Address2"
                ,"POSTAL_CODE","CITY","STATE");
            Assertions.fail();
        }catch (IllegalArgumentException e){

            Assertions.assertEquals("Address Line1 cannot be null or blank",e.getMessage());
        }

        try {
            new Location(1L,"null","null","ExternalId","Address1","Address2"
                ,null,"CITY","STATE");
            Assertions.fail();
        }catch (IllegalArgumentException e){

            Assertions.assertEquals("Postal Code cannot be null or blank",e.getMessage());
        }

        try {
            new Location(1L,"NAME","CODE","ExternalId","Address1","Address2"
                ,"POSTAL_CODE",null,"STATE");
            Assertions.fail();
        }catch (IllegalArgumentException e){

            Assertions.assertEquals("City cannot be null or blank",e.getMessage());
        }

        try {
            new Location(1L,"NAME","CODE","ExternalId","Address1","Address2"
                ,"POSTAL_CODE","CITY",null);
            Assertions.fail();
        }catch (IllegalArgumentException e){

            Assertions.assertEquals("State cannot be null or blank",e.getMessage());
        }
    }

    @Test
    void shouldAddLocationProperty() {
        var location = new Location(1L,"NAME","CODE","ExternalId","Address1","Address2"
            ,"POSTAL_CODE","CITY","STATE");

        Assertions.assertNotNull(location);
        Assertions.assertEquals(1L, location.getId());

        location.addProperty("KEY","VALUE");

        Assertions.assertEquals(1,location.getLocationProperties().size());
        Assertions.assertEquals(1L,location.getLocationProperties().getFirst().getLocationId());
        Assertions.assertEquals("KEY",location.getLocationProperties().getFirst().getPropertyKey());
        Assertions.assertEquals("VALUE",location.getLocationProperties().getFirst().getPropertyValue());

    }

    @Test
    void shouldNotAddInvalidLocationProperty() {

        var location = new Location(1L,"NAME","CODE","ExternalId","Address1","Address2"
            ,"POSTAL_CODE","CITY","STATE");

        try {

            location.addProperty(null,"VALUE");
            Assertions.fail();
        }catch (IllegalArgumentException e){
            Assertions.assertEquals("Property Key cannot be null or empty",e.getMessage());
        }
    }

    @Test
    void shouldFindProperty() {
        var location = new Location(1L,"NAME","CODE","ExternalId","Address1","Address2"
            ,"POSTAL_CODE","CITY","STATE");

        location.addProperty("KEY","VALUE");
        location.addProperty("KEY2","VALUE2");
        location.addProperty("KEY3","VALUE3");

        var property  = location.findProperty("KEY");

        Assertions.assertNotNull(property);
        Assertions.assertTrue(property.isPresent());
        Assertions.assertEquals("KEY",property.get().getPropertyKey());
        Assertions.assertEquals("VALUE",property.get().getPropertyValue());

    }

    @Test
    void shouldNotFindProperty() {
        var location = new Location(1L,"NAME","CODE","ExternalId","Address1","Address2"
            ,"POSTAL_CODE","CITY","STATE");

        location.addProperty("KEY","VALUE");
        location.addProperty("KEY2","VALUE2");
        location.addProperty("KEY3","VALUE3");

        var property  = location.findProperty("KEY5");

        Assertions.assertNotNull(property);
        Assertions.assertTrue(property.isEmpty());
    }

}
