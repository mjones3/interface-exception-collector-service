package com.arcone.biopro.distribution.eventbridge.unit.domain.model;

import com.arcone.biopro.distribution.eventbridge.domain.model.ShipmentCompletedOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.model.ShipmentLineItem;
import com.arcone.biopro.distribution.eventbridge.domain.model.vo.ShipmentCustomer;
import com.arcone.biopro.distribution.eventbridge.domain.model.vo.ShipmentLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

class ShipmentCompletedOutboundTest {

    @Test
    public void shouldCreateDomain(){
        var target = new ShipmentCompletedOutbound(1L,"EXTERNAL_ID", ZonedDateTime.now(), new ShipmentCustomer("CODE","TYPE"), new ShipmentLocation("LOCATION_CODE","LOCATION_NAME"));
        Assertions.assertNotNull(target);
    }

    @Test
    public void shouldNotCreateDomain(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ShipmentCompletedOutbound(null,"EXTERNAL_ID", ZonedDateTime.now()
            , new ShipmentCustomer("CODE","TYPE"), new ShipmentLocation("LOCATION_CODE","LOCATION_NAME")));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ShipmentCompletedOutbound(1L,null, ZonedDateTime.now(), new ShipmentCustomer("CODE","TYPE"), new ShipmentLocation("LOCATION_CODE","LOCATION_NAME")));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ShipmentCompletedOutbound(1L,"EXTERNAL_ID", null,new ShipmentCustomer("CODE","TYPE"), new ShipmentLocation("LOCATION_CODE","LOCATION_NAME")));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ShipmentCompletedOutbound(1L,"EXTERNAL_ID", ZonedDateTime.now(),null, new ShipmentLocation("LOCATION_CODE","LOCATION_NAME")));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ShipmentCompletedOutbound(1L,"EXTERNAL_ID", ZonedDateTime.now(),new ShipmentCustomer("CODE","TYPE"), null));
    }

    @Test
    public void shouldAddService(){
        var target = new ShipmentCompletedOutbound(1L,"EXTERNAL_ID", ZonedDateTime.now(), new ShipmentCustomer("CODE","TYPE"), new ShipmentLocation("LOCATION_CODE","LOCATION_NAME"));
        Assertions.assertNotNull(target);
        target.addService("SERVICE_CODE",10);

        Assertions.assertEquals(1,target.getServices().size());
        Assertions.assertEquals("SERVICE_CODE",target.getServices().getFirst().code());
        Assertions.assertEquals(10,target.getServices().getFirst().quantity());

    }

    @Test
    public void shouldAddLineItem(){
        var target = new ShipmentCompletedOutbound(1L,"EXTERNAL_ID", ZonedDateTime.now(), new ShipmentCustomer("CODE","TYPE"), new ShipmentLocation("LOCATION_CODE","LOCATION_NAME"));
        Assertions.assertNotNull(target);
        target.addLineItem(new ShipmentLineItem("FAMILY",10));

        Assertions.assertEquals(1,target.getLineItems().size());
        Assertions.assertEquals("FAMILY",target.getLineItems().getFirst().getProductFamily());
        Assertions.assertEquals(10,target.getLineItems().getFirst().getQuantityOrdered());

    }

    @Test
    public void shouldCalculateQuantityShipped(){
        var target = new ShipmentCompletedOutbound(1L,"EXTERNAL_ID", ZonedDateTime.now(), new ShipmentCustomer("CODE","TYPE"), new ShipmentLocation("LOCATION_CODE","LOCATION_NAME"));
        Assertions.assertNotNull(target);

        Assertions.assertEquals(0,target.getQuantityShipped());

        var lineItem = new ShipmentLineItem("FAMILY",10);

        lineItem.addProduct("UNIT","PRODUCT_CODE","ABO_RH", LocalDateTime.now(),ZonedDateTime.now());
        target.addLineItem(lineItem);

        Assertions.assertEquals(1,target.getQuantityShipped());


    }


}
