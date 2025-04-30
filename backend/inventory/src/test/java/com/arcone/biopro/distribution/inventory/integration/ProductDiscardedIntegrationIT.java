package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.dto.*;
import com.arcone.biopro.distribution.inventory.application.usecase.*;
import com.arcone.biopro.distribution.inventory.verification.utils.KafkaHelper;
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
        "spring.kafka.consumer.group-id=kafka-listeners-test-group",
        "default.location=TestLocation"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9089", "port=9089"})
public class ProductDiscardedIntegrationIT {

    @Autowired
    private KafkaHelper kafkaHelper;

    @MockBean
    private ProductDiscardedUseCase productDiscardedUseCase;

    @BeforeEach
    void setUp() {
        when(productDiscardedUseCase.execute(any(ProductDiscardedInput.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("should publish, listen, map and call use case with correct converted inputs for Product Discarded")
    public void testProductDiscarded() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/product_discarded.json", PRODUCT_DISCARDED_TOPIC);
        ArgumentCaptor<ProductDiscardedInput> captor = ArgumentCaptor.forClass(ProductDiscardedInput.class);
        verify(productDiscardedUseCase, times(1)).execute(captor.capture());
        ProductDiscardedInput capturedInput = captor.getValue();

        assertThat(capturedInput).isNotNull();
        assertThat(capturedInput.unitNumber()).isEqualTo(payloadJson.path(PAYLOAD).path("unitNumber").asText());
        assertThat(capturedInput.productCode()).isEqualTo(payloadJson.path(PAYLOAD).path("productCode").asText());
        assertThat(capturedInput.reason()).isEqualTo(payloadJson.path(PAYLOAD).path("reasonDescriptionKey").asText());
        assertThat(capturedInput.comments()).isEqualTo(payloadJson.path(PAYLOAD).path("comments").asText());
    }
}