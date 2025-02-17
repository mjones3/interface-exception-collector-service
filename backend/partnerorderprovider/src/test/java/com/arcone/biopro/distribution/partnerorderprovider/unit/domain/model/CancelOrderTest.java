package com.arcone.biopro.distribution.partnerorderprovider.unit.domain.model;

import com.arcone.biopro.distribution.partnerorderprovider.domain.model.CancelOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CancelOrderTest {


    @Test
    public void shouldCreate(){

        var cancelOrder = new CancelOrder("externalId", "cancelDate", "cancelEmployeeCode","cancelReason");
        Assertions.assertNotNull(cancelOrder.getId());
        Assertions.assertEquals("externalId",cancelOrder.getExternalId());
        Assertions.assertEquals("cancelDate",cancelOrder.getCancelDate());
        Assertions.assertEquals("cancelEmployeeCode",cancelOrder.getCancelEmployeeCode());
        Assertions.assertEquals("cancelReason",cancelOrder.getCancelReason());


    }

    @Test
    public void shouldNotCreate(){

        assertThrows(NullPointerException.class, () -> new CancelOrder(null, "cancelDate", "cancelEmployeeCode","cancelReason"),"External id cannot be null");
        assertThrows(NullPointerException.class, () -> new CancelOrder("ExternalID", null, "cancelEmployeeCode","cancelReason"),"Cancel Date cannot be null");
        assertThrows(NullPointerException.class, () -> new CancelOrder("ExternalID", "cancelDate", "cancelEmployeeCode",null),"Cancel Reason cannot be null");

    }

}
