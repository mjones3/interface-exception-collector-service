package com.arcone.biopro.distribution.eventbridge.unit.domain.model;

import com.arcone.biopro.distribution.eventbridge.domain.model.OrderCreatedOutbound;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

class OrderCreatedOutboundTest {

    @Test
    public void shouldCreateDomain(){
        var target = new OrderCreatedOutbound(
            1,
            "EXTDIS3150001",
            "CREATED",
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
            UUID.randomUUID(),
            List.of());
        Assertions.assertNotNull(target);
    }

    @Test
    public void shouldNotCreateDomain(){
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderCreatedOutbound(
                null,
                "EXTDIS3150001",
                "CREATED",
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
                UUID.randomUUID(),
                List.of()));
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderCreatedOutbound(
                1,
                null,
                "CREATED",
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
                UUID.randomUUID(),
                List.of()));
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderCreatedOutbound(
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
                UUID.randomUUID(),
                List.of()));
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderCreatedOutbound(
                1,
                "EXTDIS3150001",
                "CREATED",
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
                UUID.randomUUID(),
                List.of()));
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderCreatedOutbound(
                1,
                "EXTDIS3150001",
                "CREATED",
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
                UUID.randomUUID(),
                List.of()));
    }
}