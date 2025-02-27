package com.arcone.biopro.distribution.eventbridge.unit.infrastructure.service;

import com.arcone.biopro.distribution.eventbridge.infrastructure.service.SchemaValidationInventoryUpdatedService;
import com.arcone.biopro.distribution.eventbridge.unit.util.TestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;


class SchemaValidationInventoryUpdatedServiceTest {

    @Test
    public void shouldBeValidInventoryUpdatedSchema() throws Exception {

        var service = new SchemaValidationInventoryUpdatedService(new ObjectMapper());

        var json = TestUtil.resource("inventory-updated-event.json")
            .replace("{unit-number}", "W035625205983")
            .replace("{product-code}", "E067800")
            .replace("{update-type}", "CREATED");

        StepVerifier
            .create(service.validateInventoryUpdatedSchema(json))
            .verifyComplete();
    }

    @Test
    public void shouldNotBeValidInventoryUpdatedSchemaWhenMissingFields() throws Exception {

        var service = new SchemaValidationInventoryUpdatedService(new ObjectMapper());

        var json = TestUtil.resource("inventory-updated-event.json")
            .replace("{unit-number}", "W035625205983")
            .replace("{product-code}", "E067800")
            .replace("\"bloodType\": \"OP\"", "");

        StepVerifier
            .create(service.validateInventoryUpdatedSchema(json))
            .verifyError();
    }
}
