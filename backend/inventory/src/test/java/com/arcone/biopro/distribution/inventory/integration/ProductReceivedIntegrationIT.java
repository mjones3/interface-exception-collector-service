package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.dto.ProductsReceivedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.ProductsReceivedUseCase;
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

import static com.arcone.biopro.distribution.inventory.BioProConstants.PAYLOAD;
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
        "spring.kafka.consumer.group-id=kafka-listeners-test-group",
        "default.location=TestLocation"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9079", "port=9079"})
public class ProductReceivedIntegrationIT {

    @Autowired
    private KafkaHelper kafkaHelper;

    @Value("${topic.products-received.name}")
    private String productsReceivedTopic;

    @MockBean
    private ProductsReceivedUseCase productsReceivedUseCase;

    @BeforeEach
    void setUp() {
        when(productsReceivedUseCase.execute(any(ProductsReceivedInput.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("should publish, listen Product Received Event")
    public void testProductReceived() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/products_received.json", productsReceivedTopic);
        ArgumentCaptor<ProductsReceivedInput> captor = ArgumentCaptor.forClass(ProductsReceivedInput.class);
        verify(productsReceivedUseCase, times(1)).execute(captor.capture());
        ProductsReceivedInput capturedInput = captor.getValue();

        assertThat(capturedInput).isNotNull();
        assertThat(capturedInput.products().getFirst().inventoryLocation()).isEqualTo(payloadJson.path(PAYLOAD).path("locationCode").asText());
        assertThat(capturedInput.products().getFirst().unitNumber()).isEqualTo(payloadJson.path(PAYLOAD).path("products").get(0).path("unitNumber").asText());

    }
}
