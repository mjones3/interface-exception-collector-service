package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.dto.ProductModifiedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.ProductModifiedUseCase;
import com.arcone.biopro.distribution.inventory.verification.utils.KafkaHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
        "spring.kafka.consumer.group-id=product-modified-test-group",
        "default.location=TestLocation"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9084", "port=9084"})
public class ProductModifiedListenerIntegrationIT {

    @Autowired
    private KafkaHelper kafkaHelper;

    @MockBean
    private ProductModifiedUseCase useCase;

    @Value("${topic.product-modified.name}")
    private String productModifiedTopic;

    @BeforeEach
    void setUp() {
        when(useCase.execute(any(ProductModifiedInput.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("should publish, listen product modified event")
    public void test1() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/product_modified.json", productModifiedTopic);
        ArgumentCaptor<ProductModifiedInput> captor = ArgumentCaptor.forClass(ProductModifiedInput.class);
        verify(useCase, times(1)).execute(captor.capture());
        ProductModifiedInput capturedInput = captor.getValue();

        assertThat(capturedInput).isNotNull();
        assertThat(capturedInput.unitNumber()).isEqualTo(payloadJson.path(PAYLOAD).path(UNIT_NUMBER).asText());
        assertThat(capturedInput.productCode()).isEqualTo(payloadJson.path(PAYLOAD).path(PRODUCT_CODE).asText());
        assertThat(capturedInput.shortDescription()).isEqualTo(payloadJson.path(PAYLOAD).path("productDescription").asText());
        assertThat(capturedInput.parentProductCode()).isEqualTo(payloadJson.path(PAYLOAD).path("parentProductCode").asText());
        assertThat(capturedInput.productFamily()).isEqualTo(payloadJson.path(PAYLOAD).path("productFamily").asText());
        assertThat(capturedInput.expirationDate()).isEqualTo(payloadJson.path(PAYLOAD).path("expirationDate").asText());
        assertThat(capturedInput.expirationTime()).isEqualTo(payloadJson.path(PAYLOAD).path("expirationTime").asText());
        assertThat(capturedInput.modificationLocation()).isEqualTo(payloadJson.path(PAYLOAD).path("modificationLocation").asText());
    }
}
