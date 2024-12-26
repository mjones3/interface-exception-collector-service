package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryInput;
import com.arcone.biopro.distribution.inventory.application.usecase.LabelAppliedUseCase;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
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
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.arcone.biopro.distribution.inventory.BioProConstants.LABEL_APPLIED_TOPIC;
import static com.arcone.biopro.distribution.inventory.BioProConstants.PAYLOAD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=label-applied-test-group"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9094", "port=9094"})
public class LabelAppliedIntegrationIT {

    @Autowired
    private KafkaHelper kafkaHelper;

    @MockBean
    private LabelAppliedUseCase labelAppliedUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogMonitor logMonitor;

    @BeforeEach
    void setUp() {
        when(labelAppliedUseCase.execute(any(InventoryInput.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Should publish, receive, map and call usecase with correct input for label applied")
    public void test1() throws InterruptedException, IOException {
        var payloadJson = publishCreatedEvent("json/label_applied.json", LABEL_APPLIED_TOPIC);
        ArgumentCaptor<InventoryInput> captor = ArgumentCaptor.forClass(InventoryInput.class);
        verify(labelAppliedUseCase, times(1)).execute(captor.capture());
        InventoryInput capturedInput = captor.getValue();
        assertDefaultProductCreatedValues(capturedInput, payloadJson);
    }

    private static void assertDefaultProductCreatedValues(InventoryInput capturedInput, JsonNode payloadJson) {
        assertThat(capturedInput.unitNumber()).isEqualTo(payloadJson.path(PAYLOAD).path("unitNumber").asText());
        assertThat(capturedInput.productCode()).isEqualTo(payloadJson.path(PAYLOAD).path("productCode").asText());
        assertThat(capturedInput.shortDescription()).isEqualTo(payloadJson.path(PAYLOAD).path("productDescription").asText());
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(payloadJson.path(PAYLOAD).path("expirationDate").asText(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
        assertThat(capturedInput.expirationDate()).isEqualTo(zonedDateTime.toLocalDateTime());
        assertThat(capturedInput.isLicensed().toString()).isEqualTo(payloadJson.path(PAYLOAD).path("isLicensed").asText());
        assertThat(capturedInput.weight()).isEqualTo(payloadJson.path(PAYLOAD).path("weight").asInt());
        assertThat(capturedInput.collectionDate()).isEqualTo(payloadJson.path(PAYLOAD).path("collectionDate").asText());
        assertThat(capturedInput.location()).isEqualTo(payloadJson.path(PAYLOAD).path("location").asText());
        assertThat(capturedInput.productFamily()).isEqualTo(payloadJson.path(PAYLOAD).path("productFamily").asText());
        assertThat(capturedInput.aboRh()).isEqualTo(AboRhType.valueOf(payloadJson.path(PAYLOAD).path("aboRh").asText()));
    }

    private JsonNode publishCreatedEvent(String path, String topic) throws IOException, InterruptedException {
        var resource = new ClassPathResource(path).getFile().toPath();
        var payloadJson = objectMapper.readTree(Files.newInputStream(resource));
        kafkaHelper.sendEvent(topic, topic + "test-key", payloadJson).block();
        logMonitor.await("Processed message.*");
        return payloadJson;
    }
}
