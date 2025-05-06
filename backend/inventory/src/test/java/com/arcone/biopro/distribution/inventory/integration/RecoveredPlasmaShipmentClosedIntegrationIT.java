package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.dto.RecoveredPlasmaShipmentClosedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.RecoveredPlasmaShipmentClosedUseCase;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.verification.utils.KafkaHelper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.arcone.biopro.distribution.inventory.BioProConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=recovered_plasma_shipment_closed-test-group",
        "default.location=TestLocation"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9096", "port=9096"})
public class RecoveredPlasmaShipmentClosedIntegrationIT {

    private static final String SHIPMENT_NUMBER = "shipmentNumber";
    private static final String RECOVERED_PLASMA_SHIPMENT_CLOSED = "RecoveredPlasmaShipmentClosed";

    @Autowired
    private KafkaHelper kafkaHelper;

    @MockBean
    private RecoveredPlasmaShipmentClosedUseCase useCase;

    @BeforeEach
    void setUp() {
        when(useCase.execute(any(RecoveredPlasmaShipmentClosedInput.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Should Publish, Listen Recovered Plasma Shipment Closed Event")
    public void shouldPublishListenRecoveredPlasmaShipmentClosedEvent() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/recovered_plasma_shipment_closed.json", RECOVERED_PLASMA_SHIPMENT_CLOSED);
        ArgumentCaptor<RecoveredPlasmaShipmentClosedInput> captor = ArgumentCaptor.forClass(RecoveredPlasmaShipmentClosedInput.class);
        verify(useCase, times(1)).execute(captor.capture());
        RecoveredPlasmaShipmentClosedInput capturedInput = captor.getValue();

        assertThat(capturedInput).isNotNull();
        assertThat(capturedInput.shipmentNumber()).isEqualTo(payloadJson.path(PAYLOAD).path(SHIPMENT_NUMBER).asText());
        assertThat(capturedInput.cartonList().size()).isEqualTo(2);
        assertThat(capturedInput.cartonList().getFirst().packedProducts().getFirst().status()).isEqualTo(InventoryStatus.SHIPPED.name());
    }

    @Test
    @DisplayName("Should Process Shipment with 1000 Cartons")
    public void shouldProcessShipmentWith1000Cartons() throws InterruptedException, IOException {

        String jsonPath = generate1000CartonsJson();
        var payloadJson = kafkaHelper.publishEvent(jsonPath, RECOVERED_PLASMA_SHIPMENT_CLOSED);

        ArgumentCaptor<RecoveredPlasmaShipmentClosedInput> captor = ArgumentCaptor.forClass(RecoveredPlasmaShipmentClosedInput.class);
        verify(useCase, times(1)).execute(captor.capture());
        RecoveredPlasmaShipmentClosedInput capturedInput = captor.getValue();

        assertThat(capturedInput).isNotNull();
        assertThat(capturedInput.shipmentNumber()).isEqualTo(payloadJson.path(PAYLOAD).path(SHIPMENT_NUMBER).asText());
        assertThat(capturedInput.cartonList().size()).isEqualTo(1000);

        assertThat(capturedInput.cartonList().getFirst().cartonNumber()).isEqualTo("BPM4000");
        assertThat(capturedInput.cartonList().getLast().cartonNumber()).isEqualTo("BPM4999");

        capturedInput.cartonList().forEach(carton -> {
            assertThat(carton.packedProducts()).isNotEmpty();
            assertThat(carton.packedProducts().getFirst().status()).isEqualTo(InventoryStatus.SHIPPED.name());
        });
    }

    @Test
    @DisplayName("Should Process Shipment with 1000 Products in One Carton")
    public void shouldProcessShipmentWith1000ProductsInOneCarton() throws InterruptedException, IOException {

        String jsonPath = generate1000ProductsJson();
        JsonNode payloadJson = kafkaHelper.publishEvent(jsonPath, RECOVERED_PLASMA_SHIPMENT_CLOSED);

        ArgumentCaptor<RecoveredPlasmaShipmentClosedInput> captor = ArgumentCaptor.forClass(RecoveredPlasmaShipmentClosedInput.class);
        verify(useCase, times(1)).execute(captor.capture());
        RecoveredPlasmaShipmentClosedInput capturedInput = captor.getValue();

        assertThat(capturedInput).isNotNull();
        assertThat(capturedInput.shipmentNumber()).isEqualTo(payloadJson.path(PAYLOAD).path(SHIPMENT_NUMBER).asText());
        assertThat(capturedInput.cartonList().size()).isEqualTo(1);

        assertThat(capturedInput.cartonList().getFirst().cartonNumber()).isEqualTo("BPM4565");
        assertThat(capturedInput.cartonList().getFirst().packedProducts().size()).isEqualTo(1000);

        assertThat(capturedInput.cartonList().getFirst().packedProducts().getFirst().unitNumber()).isEqualTo("W036825100000");
        assertThat(capturedInput.cartonList().getFirst().packedProducts().getLast().unitNumber()).isEqualTo("W036825100999");

        capturedInput.cartonList().getFirst().packedProducts().forEach(product -> {
            assertThat(product.status()).isEqualTo(InventoryStatus.SHIPPED.name());
        });
    }

    private String generate1000CartonsJson() throws IOException {
        var resource = new ClassPathResource("json/recovered_plasma_shipment_closed_1000_cartons.json").getFile().toPath();
        String template = Files.readString(resource);

        String cartons = IntStream.range(0, 1000)
            .mapToObj(i -> {
                String cartonNumber = "BPM" + (4000 + i);
                String unitNumber = "W0368251" + String.format("%05d", i);
                return String.format(
                    "{\"cartonNumber\": \"%s\", \"packedProducts\": [{\"unitNumber\": \"%s\", \"productCode\": \"E0685V00\", \"status\": \"SHIPPED\"}]}",
                    cartonNumber, unitNumber
                );
            })
            .collect(Collectors.joining(",\n      "));

        String jsonContent = template.replace("${cartons}", cartons);
        Files.writeString(resource, jsonContent);

        return "json/recovered_plasma_shipment_closed_1000_cartons.json";
    }


    private String generate1000ProductsJson() throws IOException {
        var resource = new ClassPathResource("json/recovered_plasma_shipment_closed_1000_products.json").getFile().toPath();
        String template = Files.readString(resource);

        String products = IntStream.range(0, 1000)
            .mapToObj(i -> {
                String unitNumber = "W0368251" + String.format("%05d", i);
                return String.format(
                    "{\"unitNumber\": \"%s\", \"productCode\": \"E0685V00\", \"status\": \"SHIPPED\"}",
                    unitNumber
                );
            })
            .collect(Collectors.joining(",\n          "));

        String jsonContent = template.replace("${products}", products);
        Files.writeString(resource, jsonContent);

        return "json/recovered_plasma_shipment_closed_1000_products.json";
    }

}
