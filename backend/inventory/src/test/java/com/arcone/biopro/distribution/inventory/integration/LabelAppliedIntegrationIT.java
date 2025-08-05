package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryInput;
import com.arcone.biopro.distribution.inventory.application.usecase.LabelAppliedUseCase;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntityRepository;
import com.arcone.biopro.distribution.inventory.verification.utils.KafkaHelper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.io.IOException;
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
        "spring.kafka.consumer.group-id=label-applied-test-group",
        "default.location=TestLocation"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9094", "port=9094"})
public class LabelAppliedIntegrationIT {

    @Autowired
    private KafkaHelper kafkaHelper;

    @MockitoBean
    private LabelAppliedUseCase labelAppliedUseCase;

    @MockitoBean
    private InventoryEntityRepository inventoryEntityRepository;

    @BeforeEach
    void setUp() {
        when(labelAppliedUseCase.execute(any(InventoryInput.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Should publish, receive, map and call usecase with correct input for label applied")
    public void test1() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/label_applied.json", LABEL_APPLIED_TOPIC);
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
        assertThat(capturedInput.inventoryLocation()).isEqualTo(payloadJson.path(PAYLOAD).path("location").asText());
        assertThat(capturedInput.productFamily()).isEqualTo(payloadJson.path(PAYLOAD).path("productFamily").asText());
        assertThat(capturedInput.aboRh()).isEqualTo(AboRhType.valueOf(payloadJson.path(PAYLOAD).path("aboRh").asText()));
    }
}
