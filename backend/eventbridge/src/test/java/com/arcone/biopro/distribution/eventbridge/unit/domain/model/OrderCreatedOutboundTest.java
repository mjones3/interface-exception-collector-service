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
        // Test null orderNumber
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderCreatedOutbound(
                null, "EXTDIS3150001", "CREATED", "123456789", Instant.now(),
                "ee1bf88e-2137-4a17-835a-d43e7b738374", "CUSTOMER", "SCHEDULED", "FEDEX", "FROZEN",
                LocalDate.now(), "A1235", "A1235", "Comments", false, null,
                UUID.randomUUID(), List.of()));
        
        // Test null externalId
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderCreatedOutbound(
                1, null, "CREATED", "123456789", Instant.now(),
                "ee1bf88e-2137-4a17-835a-d43e7b738374", "CUSTOMER", "SCHEDULED", "FEDEX", "FROZEN",
                LocalDate.now(), "A1235", "A1235", "Comments", false, null,
                UUID.randomUUID(), List.of()));
        
        // Test null shipmentType
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderCreatedOutbound(
                1, "EXTDIS3150001", "CREATED", "123456789", Instant.now(),
                "ee1bf88e-2137-4a17-835a-d43e7b738374", null, "SCHEDULED", "FEDEX", "FROZEN",
                LocalDate.now(), "A1235", "A1235", "Comments", false, null,
                UUID.randomUUID(), List.of()));
        
        // Test null desiredShippingDate
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderCreatedOutbound(
                1, "EXTDIS3150001", "CREATED", "123456789", Instant.now(),
                "ee1bf88e-2137-4a17-835a-d43e7b738374", "CUSTOMER", "SCHEDULED", "FEDEX", "FROZEN",
                null, "A1235", "A1235", "Comments", false, null,
                UUID.randomUUID(), List.of()));
        
        // Test null shippingCustomerCode
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderCreatedOutbound(
                1, "EXTDIS3150001", "CREATED", "123456789", Instant.now(),
                "ee1bf88e-2137-4a17-835a-d43e7b738374", "CUSTOMER", "SCHEDULED", "FEDEX", "FROZEN",
                LocalDate.now(), null, "A1235", "Comments", false, null,
                UUID.randomUUID(), List.of()));
        
        // Test null billingCustomerCode
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderCreatedOutbound(
                1, "EXTDIS3150001", "CREATED", "123456789", Instant.now(),
                "ee1bf88e-2137-4a17-835a-d43e7b738374", "CUSTOMER", "SCHEDULED", "FEDEX", "FROZEN",
                LocalDate.now(), "A1235", null, "Comments", false, null,
                UUID.randomUUID(), List.of()));
        
        // Test null transactionId
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new OrderCreatedOutbound(
                1, "EXTDIS3150001", "CREATED", "123456789", Instant.now(),
                "ee1bf88e-2137-4a17-835a-d43e7b738374", "CUSTOMER", "SCHEDULED", "FEDEX", "FROZEN",
                LocalDate.now(), "A1235", "A1235", "Comments", false, null,
                null, List.of()));
    }
}