package com.arcone.biopro.distribution.eventbridge.unit.domain.model;

import com.arcone.biopro.distribution.eventbridge.domain.model.OrderRejectedOutbound;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;



class OrderRejectedOutboundTest {

    @Test
    public void shouldCreateDomain(){
        var target = new OrderRejectedOutbound(
            "EXTDIS3150001",
            "Order rejected due to validation failure",
            "CREATE",
            "df7092f3-78a6-4d17-ae2c-e9de07db6f3c");
        Assertions.assertNotNull(target);
    }

    @Test
    public void shouldNotCreateDomain(){
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderRejectedOutbound(
                null,
                "Order rejected due to validation failure",
                "CREATE",
                "df7092f3-78a6-4d17-ae2c-e9de07db6f3c"));
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderRejectedOutbound(
                "EXTDIS3150001",
                null,
                "CREATE",
                "df7092f3-78a6-4d17-ae2c-e9de07db6f3c"));
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderRejectedOutbound(
                "EXTDIS3150001",
                "Order rejected due to validation failure",
                null,
                "df7092f3-78a6-4d17-ae2c-e9de07db6f3c"));
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderRejectedOutbound(
                "EXTDIS3150001",
                "Order rejected due to validation failure",
                "CREATE",
                null));

    }
}