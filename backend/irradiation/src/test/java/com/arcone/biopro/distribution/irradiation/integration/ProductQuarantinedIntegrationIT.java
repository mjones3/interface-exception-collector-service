package com.arcone.biopro.distribution.irradiation.integration;

import com.arcone.biopro.distribution.irradiation.application.dto.*;
import com.arcone.biopro.distribution.irradiation.application.usecase.*;
import com.arcone.biopro.distribution.irradiation.verification.utils.KafkaHelper;
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

import static com.arcone.biopro.distribution.irradiation.BioProConstants.*;
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
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9088", "port=9088"})
public class ProductQuarantinedIntegrationIT {

    @Autowired
    private KafkaHelper kafkaHelper;

    @MockBean
    private AddQuarantinedUseCase productQuarantinedUseCase;

    @BeforeEach
    void setUp() {
        when(productQuarantinedUseCase.execute(any(AddQuarantineInput.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("should publish, listen, map and call use case with correct converted inputs for Product Quarantined")
    public void testProductQuarantined() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/product_quarantined.json", PRODUCT_QUARANTINED_TOPIC);
        ArgumentCaptor<AddQuarantineInput> captor = ArgumentCaptor.forClass(AddQuarantineInput.class);
        verify(productQuarantinedUseCase, timeout(5000).times(1)).execute(captor.capture());
        AddQuarantineInput capturedInput = captor.getValue();

        assertThat(capturedInput).isNotNull();
        assertThat(capturedInput.product().unitNumber()).isEqualTo(payloadJson.path(PAYLOAD).path("unitNumber").asText());
        assertThat(capturedInput.product().productCode()).isEqualTo(payloadJson.path(PAYLOAD).path("productCode").asText());
        assertThat(capturedInput.reason()).isEqualTo(payloadJson.path(PAYLOAD).path("reason").asText());
        assertThat(capturedInput.comments()).isEqualTo(payloadJson.path(PAYLOAD).path("comments").asText());
    }
}
