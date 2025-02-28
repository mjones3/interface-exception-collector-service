package com.arcone.biopro.distribution.partnerorderprovider.unit.domain.model;

import com.arcone.biopro.distribution.partnerorderprovider.domain.model.ModifyOrder;
import com.arcone.biopro.distribution.partnerorderprovider.domain.model.PartnerOrderPickUpType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ModifyOrderTest {


    @Test
    public void shouldCreate(){

        var response = new ModifyOrder(UUID.randomUUID(),"externalId", "locationCode","modifyReason","modifyDate", "modifyEmployeeCode"
            ,"deliveryType","shippingMethod","productCategory","desiredShippingDate"
            ,"comments",new PartnerOrderPickUpType(true,"phoneNumber"));
        Assertions.assertNotNull(response.getId());
        Assertions.assertEquals("externalId",response.getExternalId());
        Assertions.assertEquals("locationCode",response.getLocationCode());
        Assertions.assertEquals("modifyEmployeeCode",response.getModifyEmployeeCode());
        Assertions.assertEquals("shippingMethod",response.getShippingMethod());
        Assertions.assertEquals("productCategory",response.getProductCategory());
        Assertions.assertEquals("desiredShippingDate",response.getDesiredShippingDate());
        Assertions.assertEquals("comments",response.getComments());
        Assertions.assertEquals("modifyReason",response.getModifyReason());
        Assertions.assertTrue(response.getPartnerOrderPickUpType().isWillCallPickUp());
        Assertions.assertEquals("phoneNumber",response.getPartnerOrderPickUpType().getPhoneNumber());



    }

    @Test
    public void shouldNotCreate(){

        assertThrows(NullPointerException.class, () -> new ModifyOrder(null,"externalId", "locationCode", "modifyReason","modifyDate","modifyEmployeeCode"
            ,"deliveryType","shippingMethod","productCategory","desiredShippingDate"
            ,"comments",new PartnerOrderPickUpType(true,"phoneNumber")),"Id cannot be null");

        assertThrows(NullPointerException.class, () -> new ModifyOrder(UUID.randomUUID(),null, "locationCode","modifyReason","modifyDate", "modifyEmployeeCode"
            ,"deliveryType","shippingMethod","productCategory","desiredShippingDate"
            ,"comments",new PartnerOrderPickUpType(true,"phoneNumber")),"External id cannot be null");

        assertThrows(NullPointerException.class, () -> new ModifyOrder(UUID.randomUUID(),"null", null, "modifyReason","modifyDate","modifyEmployeeCode"
            ,"deliveryType","shippingMethod","productCategory","desiredShippingDate"
            ,"comments",new PartnerOrderPickUpType(true,"phoneNumber")),"Location code cannot be null");


        assertThrows(NullPointerException.class, () -> new ModifyOrder(UUID.randomUUID(),"null", "null", "modifyReason","modifyDate","null"
            ,null,"shippingMethod","productCategory","desiredShippingDate"
            ,"comments",new PartnerOrderPickUpType(true,"phoneNumber")),"Delivery Type cannot be null");

        assertThrows(NullPointerException.class, () -> new ModifyOrder(UUID.randomUUID(),"null", "null", "modifyReason","modifyDate","null"
            ,"null",null,"productCategory","desiredShippingDate"
            ,"comments",new PartnerOrderPickUpType(true,"phoneNumber")),"Shipping Method cannot be null");


        assertThrows(NullPointerException.class, () -> new ModifyOrder(UUID.randomUUID(),"null", "null", "modifyReason",null,"null"
            ,"null",null,"productCategory","desiredShippingDate"
            ,"comments",new PartnerOrderPickUpType(true,"phoneNumber")),"Modify Date cannot be null");


        assertThrows(NullPointerException.class, () -> new ModifyOrder(UUID.randomUUID(),"null", "null", "modifyReason","modifyDate",null
            ,"null",null,"productCategory","desiredShippingDate"
            ,"comments",new PartnerOrderPickUpType(true,"phoneNumber")),"Modify Employee code cannot be null");

        assertThrows(NullPointerException.class, () -> new ModifyOrder(UUID.randomUUID(),"null", "null", null,"modifyDate",null
            ,"null",null,"productCategory","desiredShippingDate"
            ,"comments",new PartnerOrderPickUpType(true,"phoneNumber")),"Modify Reason cannot be null");

    }

}
