package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.dto.UnsuitableInput;
import com.arcone.biopro.distribution.inventory.application.usecase.UnsuitableUseCase;
import com.arcone.biopro.distribution.inventory.verification.utils.KafkaHelper;
import com.arcone.biopro.distribution.inventory.verification.utils.LogMonitor;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        "spring.kafka.consumer.group-id=unsuitable-test-group",
        "default.location=TestLocation"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9096", "port=9096"})
public class ProductUnsuitableIntegrationIT {

    @Autowired
    private KafkaHelper kafkaHelper;

    @MockBean
    private UnsuitableUseCase useCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogMonitor logMonitor;

    @BeforeEach
    void setUp() {
        when(useCase.execute(any(UnsuitableInput.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("should publish, listen, map and call use case with correct converted inputs for Product Unsuitable")
    public void test1() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/product_unsuitable.json", PRODUCT_UNSUITABLE_TOPIC);
        ArgumentCaptor<UnsuitableInput> captor = ArgumentCaptor.forClass(UnsuitableInput.class);
        verify(useCase, times(1)).execute(captor.capture());
        UnsuitableInput capturedInput = captor.getValue();

        assertThat(capturedInput).isNotNull();
        assertThat(capturedInput.unitNumber()).isEqualTo(payloadJson.path(PAYLOAD).path("unitNumber").asText());
        assertThat(capturedInput.productCode()).isEqualTo(payloadJson.path(PAYLOAD).path("productCode").asText());
        assertThat(capturedInput.reasonKey()).isEqualTo(payloadJson.path(PAYLOAD).path("reasonKey").asText());
    }

    @Test
    @DisplayName("should publish, listen, map and call use case with correct converted inputs for Unit Unsuitable")
    public void test2() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/unit_unsuitable.json", UNIT_UNSUITABLE_TOPIC);
        ArgumentCaptor<UnsuitableInput> captor = ArgumentCaptor.forClass(UnsuitableInput.class);
        verify(useCase, times(1)).execute(captor.capture());
        UnsuitableInput capturedInput = captor.getValue();

        assertThat(capturedInput).isNotNull();
        assertThat(capturedInput.unitNumber()).isEqualTo(payloadJson.path(PAYLOAD).path("unitNumber").asText());
        assertThat(capturedInput.productCode()).isNull();
        assertThat(capturedInput.reasonKey()).isEqualTo(payloadJson.path(PAYLOAD).path("reasonKey").asText());
    }
}
