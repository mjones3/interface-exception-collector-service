package com.arcone.biopro.distribution.partnerorderprovider.unit.domain.model;

import com.arcone.biopro.distribution.partnerorderprovider.domain.model.PartnerOrderPickUpType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PartnerOrderPickUpTypeTest {


    @Test
    public void shouldCreate(){
        var result = new PartnerOrderPickUpType(Boolean.TRUE,"Phone Number");
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Boolean.TRUE,result.isWillCallPickUp());
        Assertions.assertEquals("Phone Number",result.getPhoneNumber());

    }

    @Test
    public void shouldCreateWhenWillPickUpFalse(){
        var result = new PartnerOrderPickUpType(Boolean.FALSE,null);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Boolean.FALSE,result.isWillCallPickUp());
        Assertions.assertNull(result.getPhoneNumber());

    }

    @Test
    public void shouldNotCreate(){

        try{
            new PartnerOrderPickUpType(Boolean.TRUE,null);
            Assertions.fail();
        }catch (IllegalArgumentException e){

            Assertions.assertEquals("Phone Number cannot be null or empty",e.getMessage());
        }
    }

}
