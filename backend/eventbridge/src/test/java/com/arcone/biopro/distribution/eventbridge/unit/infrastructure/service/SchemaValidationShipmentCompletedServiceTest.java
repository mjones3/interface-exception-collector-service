package com.arcone.biopro.distribution.eventbridge.unit.infrastructure.service;

import com.arcone.biopro.distribution.eventbridge.infrastructure.service.SchemaValidationShipmentCompletedService;
import com.arcone.biopro.distribution.eventbridge.unit.util.TestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;


class SchemaValidationShipmentCompletedServiceTest {

    @Test
    public void shouldBeValidShipmentCompletedSchema() throws Exception {

        var service = new SchemaValidationShipmentCompletedService(new ObjectMapper());

        var json = TestUtil.resource("shipment-completed-event.json").replace("\"{order-number}\"", "1");

        StepVerifier
            .create(service.validateShipmentCompletedSchema(json))
            .verifyComplete();
    }

    @Test
    public void shouldNotBeValidShipmentCompletedSchemaWhenMissingFields() throws Exception {

        var service = new SchemaValidationShipmentCompletedService(new ObjectMapper());

        var json = TestUtil.resource("shipment-completed-event.json").replace("\"{order-number}\"", "1")
            .replace("\"shipmentId\": 1,","");

        StepVerifier
            .create(service.validateShipmentCompletedSchema(json))
            .verifyError();
    }
}
