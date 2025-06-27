package com.arcone.biopro.distribution.eventbridge.unit.domain.model;

import com.arcone.biopro.distribution.eventbridge.domain.model.OrderCancelledOutbound;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

class OrderCancelledOutboundTest {

    @Test
    public void shouldCreateDomain(){
        var target = new OrderCancelledOutbound(
            1,
            "EXTDIS3150001",
            "CANCELLED",
            "123456789",
            Instant.now(),
            "ee1bf88e-2137-4a17-835a-d43e7b738374",
            "CUSTOMER",
            "SCHEDULED",
            "FEDEX",
            "FROZEN",
            LocalDate.now(),
            "A1235",
            "A1235",
            "Comments",
            false,
            null,
            0,
            10,
            10,
            List.of(),
            "ee1bf88e-2137-4a17-835a-d43e7b738374",
            Instant.now(),
            "Customer no longer need",
            "df7092f3-78a6-4d17-ae2c-e9de07db6f3c");
        Assertions.assertNotNull(target);
    }

    @Test
    public void shouldNotCreateDomain(){
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderCancelledOutbound(
                null,
                "EXTDIS3150001",
                "CANCELLED",
                "123456789",
                Instant.now(),
                "ee1bf88e-2137-4a17-835a-d43e7b738374",
                "CUSTOMER",
                "SCHEDULED",
                "FEDEX",
                "FROZEN",
                LocalDate.now(),
                "A1235",
                "A1235",
                "Comments",
                false,
                null,
                0,
                10,
                10,
                List.of(),
                "ee1bf88e-2137-4a17-835a-d43e7b738374",
                Instant.now(),
                "Customer no longer need",
                "df7092f3-78a6-4d17-ae2c-e9de07db6f3c"));
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderCancelledOutbound(
                1,
                null,
                "CANCELLED",
                "123456789",
                Instant.now(),
                "ee1bf88e-2137-4a17-835a-d43e7b738374",
                "CUSTOMER",
                "SCHEDULED",
                "FEDEX",
                "FROZEN",
                LocalDate.now(),
                "A1235",
                "A1235",
                "Comments",
                false,
                null,
                0,
                10,
                10,
                List.of(),
                "ee1bf88e-2137-4a17-835a-d43e7b738374",
                Instant.now(),
                "Customer no longer need",
                "df7092f3-78a6-4d17-ae2c-e9de07db6f3c"));
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderCancelledOutbound(
                1,
                "EXTDIS3150001",
                null,
                "123456789",
                Instant.now(),
                "ee1bf88e-2137-4a17-835a-d43e7b738374",
                "CUSTOMER",
                "SCHEDULED",
                "FEDEX",
                "FROZEN",
                LocalDate.now(),
                "A1235",
                "A1235",
                "Comments",
                false,
                null,
                0,
                10,
                10,
                List.of(),
                "ee1bf88e-2137-4a17-835a-d43e7b738374",
                Instant.now(),
                "Customer no longer need",
                "df7092f3-78a6-4d17-ae2c-e9de07db6f3c"));
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderCancelledOutbound(
                1,
                "EXTDIS3150001",
                "CANCELLED",
                null,
                Instant.now(),
                "ee1bf88e-2137-4a17-835a-d43e7b738374",
                "CUSTOMER",
                "SCHEDULED",
                "FEDEX",
                "FROZEN",
                LocalDate.now(),
                "A1235",
                "A1235",
                "Comments",
                false,
                null,
                0,
                10,
                10,
                List.of(),
                "ee1bf88e-2137-4a17-835a-d43e7b738374",
                Instant.now(),
                "Customer no longer need",
                "df7092f3-78a6-4d17-ae2c-e9de07db6f3c"));
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderCancelledOutbound(
                1,
                "EXTDIS3150001",
                "CANCELLED",
                "123456789",
                null,
                "ee1bf88e-2137-4a17-835a-d43e7b738374",
                "CUSTOMER",
                "SCHEDULED",
                "FEDEX",
                "FROZEN",
                LocalDate.now(),
                "A1235",
                "A1235",
                "Comments",
                false,
                null,
                0,
                10,
                10,
                List.of(),
                "ee1bf88e-2137-4a17-835a-d43e7b738374",
                Instant.now(),
                "Customer no longer need",
                "df7092f3-78a6-4d17-ae2c-e9de07db6f3c"));
    }
}
