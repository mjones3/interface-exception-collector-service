package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.dto.ShipmentCompletedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.ShipmentCompletedUseCase;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntityRepository;
import com.arcone.biopro.distribution.inventory.verification.utils.KafkaHelper;
import com.arcone.biopro.distribution.inventory.verification.utils.LogMonitor;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    private static final String TOPIC = "ShipmentCompleted";

    @Autowired
    private KafkaHelper kafkaHelper;

    @MockBean
    private ShipmentCompletedUseCase shipmentCompletedUseCase;

    @MockBean
    private InventoryEntityRepository inventoryEntityRepository;


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogMonitor logMonitor;

    @BeforeEach
    void setUp() {
        when(shipmentCompletedUseCase.execute(any(ShipmentCompletedInput.class))).thenReturn(Mono.empty());
    }

    @Test
    public void testKafkaListenerReceivesAndProcessesMessage() throws InterruptedException, JsonProcessingException {
        var payloadObject = objectMapper.readValue(PAYLOAD, Object.class);
        kafkaHelper.sendEvent(TOPIC, "test-key", payloadObject).block();

        logMonitor.await("Processed message.*");

        ArgumentCaptor<ShipmentCompletedInput> captor = ArgumentCaptor.forClass(ShipmentCompletedInput.class);

        verify(shipmentCompletedUseCase, times(1)).execute(captor.capture());

        ShipmentCompletedInput capturedInput = captor.getValue();

        assertThat(capturedInput).isNotNull();
        assertThat(capturedInput.shipmentId()).isEqualTo("8");
        assertThat(capturedInput.orderNumber()).isEqualTo("109");
        assertThat(capturedInput.performedBy()).isEqualTo("4c973896-5761-41fc-8217-07c5d13a004b");
        assertThat(capturedInput.lineItems().getFirst().products().getFirst().unitNumber()).isEqualTo("W812530106085");
        assertThat(capturedInput.lineItems().getFirst().products().getFirst().productCode()).isEqualTo("E0685V00");
        assertThat(capturedInput.lineItems().get(0).products().get(1).unitNumber()).isEqualTo("W812530106086");
        assertThat(capturedInput.lineItems().get(0).products().get(1).productCode()).isEqualTo("E0869V00");
        assertThat(capturedInput.lineItems().get(1).products().getFirst().unitNumber()).isEqualTo("W812530106083");
        assertThat(capturedInput.lineItems().get(1).products().getFirst().productCode()).isEqualTo("E0863V00");
    }

    private static final String PAYLOAD = """
        {
            "eventVersion": "1.0",
            "eventType": "ShipmentCompleted",
            "payload": {
              "shipmentId": 8,
              "orderNumber": 109,
              "externalOrderId": "EXTERNAL_ID",
              "performedBy": "4c973896-5761-41fc-8217-07c5d13a004b",
              "locationCode": "123456789",
              "locationName": "MDL Hub 1",
              "customerCode": "1",
              "customerType": "CUSTOMER_TYPE",
              "createDate": "2024-10-03T15:44:31.960157Z",
              "lineItems": [
                {
                  "productFamily": "RED_BLOOD_CELLS_LEUKOREDUCED",
                  "quantity": 2,
                  "bloodType": "ABP",
                  "products": [
                    {
                      "unitNumber": "W812530106085",
                      "productFamily": "RED_BLOOD_CELLS_LEUKOREDUCED",
                      "productCode": "E0685V00",
                      "aboRh": "ABP",
                      "collectionDate": "2011-12-03T09:15:30Z",
                      "expirationDate": "2024-09-03T10:15:30",
                      "createDate": "2024-10-03T15:44:42.328889299Z",
                      "attributes": [
                        {
                         "WEIGHT": "100"
                        },
                        {
                         "VOLUME": "100"
                        }
                      ]
                    },
                    {
                      "unitNumber": "W812530106086",
                      "productFamily": "PLASMA_TRANSFUSABLE",
                      "productCode": "E0869V00",
                      "aboRh": "OP",
                      "collectionDate": "2024-12-03T09:15:30Z",
                      "expirationDate": "2025-12-03T10:15:30",
                      "createDate": "2024-10-03T15:44:42.328889299Z",
                      "attributes": [
                        {
                         "WEIGHT": "100"
                        },
                        {
                         "VOLUME": "100"
                        }
                      ]
                    }
                  ]
                },
                {
                  "productFamily": "RED_BLOOD_CELLS_LEUKOREDUCED",
                  "quantity": 2,
                  "bloodType": "ABP",
                  "products": [
                    {
                      "unitNumber": "W812530106083",
                      "productFamily": "RED_BLOOD_CELLS_LEUKOREDUCED",
                      "productCode": "E0863V00",
                      "aboRh": "ABP",
                      "collectionDate": "2011-12-03T09:15:30Z",
                      "expirationDate": "2024-09-03T10:15:30",
                      "createDate": "2024-10-03T15:44:42.328889299Z",
                      "attributes": [
                        {
                         "WEIGHT": "100"
                        },
                        {
                         "VOLUME": "100"
                        }
                      ]
                    }
                  ]
                }
              ],
              "services": [
                {
                 "code": "CODE",
                  "quantity":1
                }
              ]
            }
          }
        }""";
}
