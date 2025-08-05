package com.arcone.biopro.distribution.eventbridge.unit.infrastructure.service;

import com.arcone.biopro.distribution.eventbridge.infrastructure.service.SchemaValidationService;
import com.arcone.biopro.distribution.eventbridge.unit.util.TestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;


class SchemaValidationShipmentCompletedServiceTest {
    private static final String SHIPMENT_COMPLETED_SCHEMA = "schema/shipment-completed.json";

    @Test
    public void shouldBeValidShipmentCompletedSchema() throws Exception {

        var service = new SchemaValidationService(new ObjectMapper());

        var json = TestUtil.resource("shipment-completed-event.json").replace("\"{order-number}\"", "1");

        StepVerifier
            .create(service.validateSchema(json, SHIPMENT_COMPLETED_SCHEMA))
            .verifyComplete();
    }

    @Test
    public void shouldNotBeValidShipmentCompletedSchemaWhenMissingFields() throws Exception {

        var service = new SchemaValidationService(new ObjectMapper());

        var json = TestUtil.resource("shipment-completed-event.json").replace("\"{order-number}\"", "1")
            .replace("\"shipmentId\": 1,","");

        StepVerifier
            .create(service.validateSchema(json, SHIPMENT_COMPLETED_SCHEMA))
            .verifyError();
    }
}
