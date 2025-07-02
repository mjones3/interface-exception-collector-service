package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.dto.ProductsImportedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.ProductsImportedUseCase;
import com.arcone.biopro.distribution.inventory.verification.utils.KafkaHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.io.IOException;

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
        "spring.kafka.consumer.group-id=kafka-listeners-test-group",
        "default.location=TestLocation"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9081", "port=9081"})
public class ProductImportedIntegrationIT {

    @Autowired
    private KafkaHelper kafkaHelper;

    @Value("${topic.products-imported.name}")
    private String productsImportedTopic;

    @MockitoBean
    private ProductsImportedUseCase productsImportedUseCase;

    @BeforeEach
    void setUp() {
        when(productsImportedUseCase.execute(any(ProductsImportedInput.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("should publish, listen Product Imported Event")
    public void testProductImported() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/products_imported.json", productsImportedTopic);
        ArgumentCaptor<ProductsImportedInput> captor = ArgumentCaptor.forClass(ProductsImportedInput.class);
        verify(productsImportedUseCase, times(1)).execute(captor.capture());
        ProductsImportedInput capturedInput = captor.getValue();

        assertThat(capturedInput).isNotNull();
        assertThat(capturedInput.getProducts().getFirst().inventoryLocation()).isEqualTo(payloadJson.path(PAYLOAD).path("locationCode").asText());
    }
}
