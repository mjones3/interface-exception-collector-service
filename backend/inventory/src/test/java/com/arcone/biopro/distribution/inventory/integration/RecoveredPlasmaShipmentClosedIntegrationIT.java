package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.dto.RecoveredPlasmaShipmentClosedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.RecoveredPlasmaShipmentClosedUseCase;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.verification.utils.KafkaHelper;
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
        "default.location=TestLocation",
        "spring.kafka.producer.properties.max.request.size=10485760"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9096", "port=9096", "message.max.bytes=10485760"})
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

    private RecoveredPlasmaShipmentClosedInput generalAssertions(int expectedCartonCount, int expectedProductsCount, String initialCartonNumber, String finalCartonNumber) throws InterruptedException, IOException {
        ArgumentCaptor<RecoveredPlasmaShipmentClosedInput> captor = ArgumentCaptor.forClass(RecoveredPlasmaShipmentClosedInput.class);
        verify(useCase, times(1)).execute(captor.capture());
        RecoveredPlasmaShipmentClosedInput capturedInput = captor.getValue();

        assertThat(capturedInput).isNotNull();
        assertThat(capturedInput.cartonList().size()).isEqualTo(expectedCartonCount);
        assertThat(capturedInput.cartonList().getFirst().cartonNumber()).isEqualTo(initialCartonNumber);
        assertThat(capturedInput.cartonList().getLast().cartonNumber()).isEqualTo(finalCartonNumber);
        if (expectedCartonCount > 0) {
            assertThat(capturedInput.cartonList().getFirst().packedProducts()).isNotEmpty();
            assertThat(capturedInput.cartonList().getFirst().packedProducts().getFirst().status()).isEqualTo(InventoryStatus.SHIPPED.name());
        }
        if (expectedProductsCount > 0) {
            assertThat(capturedInput.cartonList().getFirst().packedProducts().size()).isEqualTo(expectedProductsCount);
        }
        return capturedInput;
    }

    @Test
    @DisplayName("Should Publish, Listen Recovered Plasma Shipment Closed Event")
    public void shouldPublishListenRecoveredPlasmaShipmentClosedEvent() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/recovered_plasma_shipment_closed.json", RECOVERED_PLASMA_SHIPMENT_CLOSED);
        RecoveredPlasmaShipmentClosedInput capturedInput = generalAssertions(2, 1, "BPM4565", "BPM4566");

        assertThat(capturedInput).isNotNull();
        assertThat(capturedInput.shipmentNumber()).isEqualTo(payloadJson.path(PAYLOAD).path(SHIPMENT_NUMBER).asText());
    }

    @Test
    @DisplayName("Should Process Shipment with 1000 Cartons")
    public void shouldProcessShipmentWith1000Cartons() throws InterruptedException, IOException {
        String jsonPath = generateCartonsJson(1000);
        kafkaHelper.publishEvent(jsonPath, RECOVERED_PLASMA_SHIPMENT_CLOSED);
        generalAssertions(1000, 1,"BPM4000","BPM4999");
    }

    @Test
    @DisplayName("Should Process Shipment with 1000 Products in One Carton")
    public void shouldProcessShipmentWith1000ProductsInOneCarton() throws InterruptedException, IOException {
        String jsonPath = generateProductsJson(1000);
        kafkaHelper.publishEvent(jsonPath, RECOVERED_PLASMA_SHIPMENT_CLOSED);
        RecoveredPlasmaShipmentClosedInput capturedInput = generalAssertions(1, 1000, "BPM4565", "BPM4565");

        assertThat(capturedInput.cartonList().getFirst().packedProducts().getFirst().unitNumber()).isEqualTo("W777725100000");
        assertThat(capturedInput.cartonList().getFirst().packedProducts().getLast().unitNumber()).isEqualTo("W777725100999");
        capturedInput.cartonList().getFirst().packedProducts().forEach(product -> {
            assertThat(product.status()).isEqualTo(InventoryStatus.SHIPPED.name());
        });
    }

    @Test
    @DisplayName("Should Process Shipment with 10,000 Products in One Carton")
    public void shouldProcessShipmentWith10000ProductsInOneCarton() throws InterruptedException, IOException {
        String jsonPath = generateProductsJson(10000);
        kafkaHelper.publishEvent(jsonPath, RECOVERED_PLASMA_SHIPMENT_CLOSED);
        RecoveredPlasmaShipmentClosedInput capturedInput = generalAssertions(1, 10000, "BPM4565", "BPM4565");

        assertThat(capturedInput.cartonList().getFirst().packedProducts().getFirst().unitNumber()).isEqualTo("W777725100000");
        assertThat(capturedInput.cartonList().getFirst().packedProducts().getLast().unitNumber()).isEqualTo("W777725109999");

        capturedInput.cartonList().getFirst().packedProducts().forEach(product -> {
            assertThat(product.status()).isEqualTo(InventoryStatus.SHIPPED.name());
        });
    }

    @Test
    @DisplayName("Should Process Shipment with 10,000 Cartons")
    public void shouldProcessShipmentWith10000Cartons() throws InterruptedException, IOException {
        String jsonPath = generateCartonsJson(10000);
        kafkaHelper.publishEvent(jsonPath, RECOVERED_PLASMA_SHIPMENT_CLOSED);
        RecoveredPlasmaShipmentClosedInput capturedInput = generalAssertions(10000, 1, "BPM4000","BPM13999");

        capturedInput.cartonList().forEach(carton -> {
            assertThat(carton.packedProducts()).isNotEmpty();
            assertThat(carton.packedProducts().getFirst().status()).isEqualTo(InventoryStatus.SHIPPED.name());
        });
    }

    private String generateCartonsJson(int cartonsAmount) throws IOException {
        String fileName = switch(cartonsAmount) {
            case 1000 -> "json/recovered_plasma_shipment_closed_1000_cartons.json";
            case 10000 -> "json/recovered_plasma_shipment_closed_10000_cartons.json";
            default -> throw new IllegalArgumentException("Unsupported size: " + cartonsAmount);
        };
        var resource = new ClassPathResource(fileName).getFile().toPath();
        String template = Files.readString(resource);

        String cartons = IntStream.range(0, cartonsAmount)
            .mapToObj(i -> {
                String cartonNumber = "BPM" + (4000 + i);
                String unitNumber = "W7777251" + String.format("%05d", i);
                return String.format(
                    "{\"cartonNumber\": \"%s\", \"packedProducts\": [{\"unitNumber\": \"%s\", \"productCode\": \"E0685V00\", \"status\": \"SHIPPED\"}]}",
                    cartonNumber, unitNumber
                );
            })
            .collect(Collectors.joining(",\n      "));

        String jsonContent = template.replace("${cartons}", cartons);
        Files.writeString(resource, jsonContent);

        return fileName;
    }

    private String generateProductsJson(int productsAmount) throws IOException {
        String fileName = switch(productsAmount) {
            case 1000 -> "json/recovered_plasma_shipment_closed_1000_products.json";
            case 10000 -> "json/recovered_plasma_shipment_closed_10000_products.json";
            default -> throw new IllegalArgumentException("Unsupported size: " + productsAmount);
        };

        var resource = new ClassPathResource(fileName).getFile().toPath();
        String template = Files.readString(resource);

        String products = IntStream.range(0, productsAmount)
            .mapToObj(i -> {
                String unitNumber = "W7777251" + String.format("%05d", i);
                return String.format(
                    "{\"unitNumber\": \"%s\", \"productCode\": \"E0685V00\", \"status\": \"SHIPPED\"}",
                    unitNumber
                );
            })
            .collect(Collectors.joining(",\n          "));

        String jsonContent = template.replace("${products}", products);
        Files.writeString(resource, jsonContent);

        return fileName;
    }

}
