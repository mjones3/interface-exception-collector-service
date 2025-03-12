package com.arcone.biopro.distribution.eventbridge.unit.infrastructure.service;

import com.arcone.biopro.distribution.eventbridge.infrastructure.service.SchemaValidationService;
import com.arcone.biopro.distribution.eventbridge.unit.util.TestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;


class SchemaValidationInventoryUpdatedServiceTest {
    private static final String INVENTORY_UPDATED_SCHEMA = "schema/inventory-updated.json";

    @Test
    public void shouldBeValidInventoryUpdatedSchema() throws Exception {

        var service = new SchemaValidationService(new ObjectMapper());

        var json = TestUtil.resource("inventory-updated-event.json")
            .replace("{unit-number}", "W035625205983")
            .replace("{product-code}", "E067800")
            .replace("{update-type}", "LABEL_APPLIED");

        StepVerifier
            .create(service.validateSchema(json, INVENTORY_UPDATED_SCHEMA))
            .verifyComplete();
    }

    @Test
    public void shouldNotBeValidInventoryUpdatedSchemaWhenMissingFields() throws Exception {

        var service = new SchemaValidationService(new ObjectMapper());

        var json = TestUtil.resource("inventory-updated-event.json")
            .replace("{unit-number}", "W035625205983")
            .replace("{product-code}", "E067800")
            .replace("\"bloodType\": \"OP\"", "");

        StepVerifier
            .create(service.validateSchema(json, INVENTORY_UPDATED_SCHEMA))
            .verifyError();
    }
}
