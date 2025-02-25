package com.arcone.biopro.distribution.eventbridge.infrastructure.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class SchemaValidationShipmentCompletedService {

    private static final String SHIPMENT_COMPLETED_SCHEMA = "schema/shipment-completed.json";
    private final ObjectMapper objectMapper;

    public Mono<Void> validateShipmentCompletedSchema(String payload) {
        try {

            var fileInputStream = new ClassPathResource(SHIPMENT_COMPLETED_SCHEMA).getInputStream();

            JsonSchema jsonSchema  = JsonSchemaFactory.getInstance( SpecVersion.VersionFlag.V7 )
                .getSchema( fileInputStream);

            Set<ValidationMessage> errors = jsonSchema.validate(objectMapper.readTree(payload));
            if(!errors.isEmpty()){
                log.error("JSON Invalid {}",errors);
                return Mono.error(new RuntimeException("Invalid Shipment Completed schema"));
            }
            return Mono.empty();
        } catch (IOException e) {
            log.error("Error while validating shipment completed schema {}", e.getMessage());
            return Mono.error(new RuntimeException("Not Able to parse JSON Schema"));
        }
    }
}
