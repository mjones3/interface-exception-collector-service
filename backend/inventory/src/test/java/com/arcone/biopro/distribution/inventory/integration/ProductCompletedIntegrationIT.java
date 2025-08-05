package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.dto.ProductCompletedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.ProductCompletedUseCase;
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

    @MockitoBean
    private ProductCompletedUseCase productCompletedUseCase;

    @BeforeEach
    void setUp() {
        when(productCompletedUseCase.execute(any(ProductCompletedInput.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Should Receive Apheresis Plasma Product Completed And Save The Volume")
    public void test1() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/apheresis/plasma/product_completed_volume.json", APHERESIS_PLASMA_PRODUCT_COMPLETED_TOPIC);
        ArgumentCaptor<ProductCompletedInput> captor = ArgumentCaptor.forClass(ProductCompletedInput.class);
        verify(productCompletedUseCase, times(1)).execute(captor.capture());
        ProductCompletedInput capturedInput = captor.getValue();
        assertDefaultProductCreatedValues(capturedInput, payloadJson);
    }

    @Test
    @DisplayName("Should Receive Apheresis Plasma Product Completed And Save The Volume And Anticoagulant Volume")
    public void test2() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/apheresis/plasma/product_completed.json", APHERESIS_PLASMA_PRODUCT_COMPLETED_TOPIC);
        ArgumentCaptor<ProductCompletedInput> captor = ArgumentCaptor.forClass(ProductCompletedInput.class);
        verify(productCompletedUseCase, times(1)).execute(captor.capture());
        ProductCompletedInput capturedInput = captor.getValue();
        assertDefaultProductCreatedValues(capturedInput, payloadJson);
    }

    @Test
    @DisplayName("Should Receive Apheresis RBC Product Completed")
    public void test3() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/apheresis/rbc/product_completed.json", APHERESIS_RBC_PRODUCT_COMPLETED_TOPIC);
        ArgumentCaptor<ProductCompletedInput> captor = ArgumentCaptor.forClass(ProductCompletedInput.class);
        verify(productCompletedUseCase, times(1)).execute(captor.capture());
        ProductCompletedInput capturedInput = captor.getValue();
        assertDefaultProductCreatedValues(capturedInput, payloadJson);
    }

    @Test
    @DisplayName("Should Receive Apheresis PLATELET Product Completed")
    public void test4() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/apheresis/platelet/product_completed.json", APHERESIS_PLATELET_PRODUCT_COMPLETED_TOPIC);
        ArgumentCaptor<ProductCompletedInput> captor = ArgumentCaptor.forClass(ProductCompletedInput.class);
        verify(productCompletedUseCase, times(1)).execute(captor.capture());
        ProductCompletedInput capturedInput = captor.getValue();
        assertDefaultProductCreatedValues(capturedInput, payloadJson);
    }

    @Test
    @DisplayName("Should Receive WHOLEBLOOD Product Completed")
    public void test5() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/wholeblood/product_completed.json", WHOLEBLOOD_COMPLETED_TOPIC);
        ArgumentCaptor<ProductCompletedInput> captor = ArgumentCaptor.forClass(ProductCompletedInput.class);
        verify(productCompletedUseCase, times(1)).execute(captor.capture());
        ProductCompletedInput capturedInput = captor.getValue();
        assertDefaultProductCreatedValues(capturedInput, payloadJson);
    }

    private static void assertDefaultProductCreatedValues(ProductCompletedInput capturedInput, JsonNode payloadJson) {
        assertThat(capturedInput.unitNumber()).isEqualTo(payloadJson.path(PAYLOAD).path("unitNumber").asText());
        assertThat(capturedInput.productCode()).isEqualTo(payloadJson.path(PAYLOAD).path("productCode").asText());
    }
}
