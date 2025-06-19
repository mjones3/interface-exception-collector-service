package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.dto.ShipmentCompletedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.ShipmentCompletedUseCase;
import com.arcone.biopro.distribution.inventory.verification.utils.KafkaHelper;
import com.arcone.biopro.distribution.inventory.verification.utils.LogMonitor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static com.arcone.biopro.distribution.inventory.BioProConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=shipment-test-group",
        "default.location=TestLocation"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class ShipmentCompletedIntegrationIT {

    @Autowired
    private KafkaHelper kafkaHelper;

    @MockBean
    private ShipmentCompletedUseCase shipmentCompletedUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogMonitor logMonitor;

    @BeforeEach
    void setUp() {
        when(shipmentCompletedUseCase.execute(any(ShipmentCompletedInput.class))).thenReturn(Mono.empty());
    }

    @Test
    public void testKafkaListenerReceivesAndProcessesMessage() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/shipment_completed.json", SHIPMENT_COMPLETED_TOPIC);
        ArgumentCaptor<ShipmentCompletedInput> captor = ArgumentCaptor.forClass(ShipmentCompletedInput.class);
        verify(shipmentCompletedUseCase, times(1)).execute(captor.capture());
        ShipmentCompletedInput capturedInput = captor.getValue();

        assertThat(capturedInput).isNotNull();
        assertThat(capturedInput.shipmentId()).isEqualTo(payloadJson.path(PAYLOAD).path("shipmentId").asText());
        assertThat(capturedInput.orderNumber()).isEqualTo(payloadJson.path(PAYLOAD).path("orderNumber").asText());
        assertThat(capturedInput.shipmentType().name()).isEqualTo(payloadJson.path(PAYLOAD).path("shipmentType").asText());
        assertThat(capturedInput.performedBy()).isEqualTo(payloadJson.path(PAYLOAD).path("performedBy").asText());
        assertThat(capturedInput.shipmentId()).isEqualTo(payloadJson.path(PAYLOAD).path("shipmentId").asText());
        assertThat(capturedInput.shipmentId()).isEqualTo(payloadJson.path(PAYLOAD).path("shipmentId").asText());

        assertThat(capturedInput.lineItems()).hasSize(payloadJson.path(PAYLOAD).path("lineItems").size());
        for (int i = 0; i < payloadJson.path(PAYLOAD).path("lineItems").size(); i++) {
            assertThat(capturedInput.lineItems().get(i).products()).hasSize(payloadJson.path(PAYLOAD).path("lineItems").get(i).path("products").size());
            for (int j = 0; j < payloadJson.path(PAYLOAD).path("products").size(); j++) {
                assertThat(capturedInput.lineItems().get(i).products().get(j).productCode())
                    .isEqualTo(payloadJson.path(PAYLOAD).path("lineItems").get(i).path("products").get(j).path("productCode").asText());
                assertThat(capturedInput.lineItems().get(i).products().get(j).unitNumber())
                    .isEqualTo(payloadJson.path(PAYLOAD).path("lineItems").get(i).path("products").get(j).path("unitNumber").asText());
            }
        }
    }
}
