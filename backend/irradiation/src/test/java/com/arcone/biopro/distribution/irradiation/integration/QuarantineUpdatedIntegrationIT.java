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
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9085", "port=9085"})
public class QuarantineUpdatedIntegrationIT {

    @Autowired
    private KafkaHelper kafkaHelper;

    @MockBean
    private UpdateQuarantinedUseCase quarantineUpdatedUseCase;

    @BeforeEach
    void setUp() {
        when(quarantineUpdatedUseCase.execute(any(UpdateQuarantineInput.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("should publish, listen, map and call use case with correct converted inputs for Quarantine Updated")
    public void testQuarantineUpdated() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/quarantine_updated.json", QUARANTINE_UPDATED_TOPIC);
        ArgumentCaptor<UpdateQuarantineInput> captor = ArgumentCaptor.forClass(UpdateQuarantineInput.class);
        verify(quarantineUpdatedUseCase, timeout(5000).times(1)).execute(captor.capture());
        UpdateQuarantineInput capturedInput = captor.getValue();

        assertThat(capturedInput).isNotNull();
        assertThat(capturedInput.product().unitNumber()).isEqualTo(payloadJson.path(PAYLOAD).path("unitNumber").asText());
        assertThat(capturedInput.product().productCode()).isEqualTo(payloadJson.path(PAYLOAD).path("productCode").asText());
        assertThat(capturedInput.reason()).isEqualTo(payloadJson.path(PAYLOAD).path("newReason").asText());
        assertThat(capturedInput.comments()).isEqualTo(payloadJson.path(PAYLOAD).path("comments").asText());
    }
}
