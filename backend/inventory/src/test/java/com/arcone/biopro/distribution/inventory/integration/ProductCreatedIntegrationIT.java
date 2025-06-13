package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.dto.ProductCreatedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.ProductCreatedUseCase;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.io.IOException;

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
        "spring.kafka.consumer.group-id=created-test-group",
        "default.location=TestLocation"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9093", "port=9093"})
public class ProductCreatedIntegrationIT {

    @Autowired
    private KafkaHelper kafkaHelper;

    @MockBean
    private ProductCreatedUseCase productCreatedUseCase;

    @MockBean
    private InventoryEntityRepository inventoryEntityRepository;

    @BeforeEach
    void setUp() {
        when(productCreatedUseCase.execute(any(ProductCreatedInput.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Should publish, receive, map and call usecase with correct input for apheresis RBC")
    public void test1() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/apheresis/rbc/product_created.json", APHERESIS_RBC_PRODUCT_CREATED_TOPIC);
        ArgumentCaptor<ProductCreatedInput> captor = ArgumentCaptor.forClass(ProductCreatedInput.class);
        verify(productCreatedUseCase, times(1)).execute(captor.capture());
        ProductCreatedInput capturedInput = captor.getValue();
        assertDefaultProductCreatedValues(capturedInput, payloadJson);
    }

    @Test
    @DisplayName("Should publish, receive, map and call usecase with correct input for apheresis PLASMA")
    public void test2() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/apheresis/plasma/product_created.json", APHERESIS_PLASMA_PRODUCT_CREATED_TOPIC);
        ArgumentCaptor<ProductCreatedInput> captor = ArgumentCaptor.forClass(ProductCreatedInput.class);
        verify(productCreatedUseCase, times(1)).execute(captor.capture());
        ProductCreatedInput capturedInput = captor.getValue();
        assertDefaultProductCreatedValues(capturedInput, payloadJson);
    }

    @Test
    @DisplayName("Should publish, receive, map and call usecase with correct input for wholeblood")
    public void test3() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/wholeblood/product_created.json", WHOLEBLOOD_CREATED_TOPIC);
        ArgumentCaptor<ProductCreatedInput> captor = ArgumentCaptor.forClass(ProductCreatedInput.class);
        verify(productCreatedUseCase, times(1)).execute(captor.capture());
        ProductCreatedInput capturedInput = captor.getValue();
        assertDefaultProductCreatedValues(capturedInput, payloadJson);
    }

    @Test
    @DisplayName("Should publish, receive, map and call usecase with correct input for apheresis PLATELET")
    public void test4() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/apheresis/platelet/product_created.json", APHERESIS_PLATELET_PRODUCT_CREATED_TOPIC);
        ArgumentCaptor<ProductCreatedInput> captor = ArgumentCaptor.forClass(ProductCreatedInput.class);
        verify(productCreatedUseCase, times(1)).execute(captor.capture());
        ProductCreatedInput capturedInput = captor.getValue();
        assertDefaultProductCreatedValues(capturedInput, payloadJson);
    }

    private static void assertDefaultProductCreatedValues(ProductCreatedInput capturedInput, JsonNode payloadJson) {
        assertThat(capturedInput.unitNumber()).isEqualTo(payloadJson.path(PAYLOAD).path("unitNumber").asText());
        assertThat(capturedInput.productCode()).isEqualTo(payloadJson.path(PAYLOAD).path("productCode").asText());
        assertThat(capturedInput.productDescription()).isEqualTo(payloadJson.path(PAYLOAD).path("productDescription").asText());
        assertThat(capturedInput.expirationDate()).isEqualTo(payloadJson.path(PAYLOAD).path("expirationDate").asText());
        assertThat(capturedInput.weight()).isEqualTo(payloadJson.path(PAYLOAD).path("weight").path("value").asInt());
        assertThat(capturedInput.collectionDate()).isEqualTo(payloadJson.path(PAYLOAD).path("drawTime").asText());
        assertThat(capturedInput.inventoryLocation()).isEqualTo(payloadJson.path(PAYLOAD).path("manufacturingLocation").asText());
        assertThat(capturedInput.productFamily()).isEqualTo(payloadJson.path(PAYLOAD).path("productFamily").asText());
        assertThat(capturedInput.aboRh()).isEqualTo(AboRhType.valueOf(payloadJson.path(PAYLOAD).path("aboRh").asText()));

        assertThat(capturedInput.inputProducts()).hasSize(1);
        assertThat(capturedInput.inputProducts().getFirst().unitNumber())
            .isEqualTo(payloadJson.path(PAYLOAD).path("inputProducts").get(0).path("unitNumber").asText());
        assertThat(capturedInput.inputProducts().getFirst().productCode())
            .isEqualTo(payloadJson.path(PAYLOAD).path("inputProducts").get(0).path("productCode").asText());
    }
}
