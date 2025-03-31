package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.dto.ProductCompletedInput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductCompletedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.ProductCompletedUseCase;
import com.arcone.biopro.distribution.inventory.application.usecase.ProductCreatedUseCase;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntityRepository;
import com.arcone.biopro.distribution.inventory.verification.utils.KafkaHelper;
import com.arcone.biopro.distribution.inventory.verification.utils.LogMonitor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        "spring.kafka.consumer.group-id=completed-test-group",
        "default.location=TestLocation"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9093", "port=9093"})
public class ProductCompletedIntegrationIT {

    @Autowired
    private KafkaHelper kafkaHelper;

    @MockBean
    private ProductCompletedUseCase productCompletedUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogMonitor logMonitor;

    @BeforeEach
    void setUp() {
        when(productCompletedUseCase.execute(any(ProductCompletedInput.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Should Receive Apheresis Plasma Product Completed And Save The Volume")
    public void shouldReceiveApheresisPlasmaProductCompletedAndSaveTheVolume() throws InterruptedException, IOException {
        var payloadJson = publishCreatedEvent("json/apheresis/plasma/product_completed_volume.json", APHERESIS_PLASMA_PRODUCT_COMPLETED_TOPIC);
        ArgumentCaptor<ProductCompletedInput> captor = ArgumentCaptor.forClass(ProductCompletedInput.class);
        verify(productCompletedUseCase, times(1)).execute(captor.capture());
        ProductCompletedInput capturedInput = captor.getValue();
        assertDefaultProductCreatedValues(capturedInput, payloadJson);
    }

    @Test
    @DisplayName("Should Receive Apheresis Plasma Product Completed And Save The Volume And Anticoagulant Volume")
    public void shouldReceiveApheresisPlasmaProductCompletedAndSaveTheVolumeAndAnticoagulantVolume() throws InterruptedException, IOException {
        var payloadJson = publishCreatedEvent("json/apheresis/plasma/product_completed.json", APHERESIS_PLASMA_PRODUCT_COMPLETED_TOPIC);
        ArgumentCaptor<ProductCompletedInput> captor = ArgumentCaptor.forClass(ProductCompletedInput.class);
        verify(productCompletedUseCase, times(1)).execute(captor.capture());
        ProductCompletedInput capturedInput = captor.getValue();
        assertDefaultProductCreatedValues(capturedInput, payloadJson);
    }

    private static void assertDefaultProductCreatedValues(ProductCompletedInput capturedInput, JsonNode payloadJson) {
        assertThat(capturedInput.unitNumber()).isEqualTo(payloadJson.path(PAYLOAD).path("unitNumber").asText());
        assertThat(capturedInput.productCode()).isEqualTo(payloadJson.path(PAYLOAD).path("productCode").asText());
    }

    private JsonNode publishCreatedEvent(String path, String topic) throws IOException, InterruptedException {
        var resource = new ClassPathResource(path).getFile().toPath();
        var payloadJson = objectMapper.readTree(Files.newInputStream(resource));
        kafkaHelper.sendEvent(topic, topic + "test-key", payloadJson).block();
        logMonitor.await("Processed message.*");
        return payloadJson;
    }
}
