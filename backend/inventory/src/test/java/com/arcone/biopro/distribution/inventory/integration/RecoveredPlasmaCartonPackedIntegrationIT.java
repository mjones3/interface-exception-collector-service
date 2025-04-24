package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.dto.RecoveredPlasmaCartonPackedInput;
import com.arcone.biopro.distribution.inventory.application.dto.UnsuitableInput;
import com.arcone.biopro.distribution.inventory.application.usecase.RecoveredPlasmaCartonPackedUseCase;
import com.arcone.biopro.distribution.inventory.application.usecase.UnsuitableUseCase;
import com.arcone.biopro.distribution.inventory.verification.utils.KafkaHelper;
import com.arcone.biopro.distribution.inventory.verification.utils.LogMonitor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;

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
public class RecoveredPlasmaCartonPackedIntegrationIT {

    @Autowired
    private KafkaHelper kafkaHelper;

    @MockBean
    private RecoveredPlasmaCartonPackedUseCase useCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogMonitor logMonitor;

    @BeforeEach
    void setUp() {
        when(useCase.execute(any(RecoveredPlasmaCartonPackedInput.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("should publish, listen recovery plasma carton packed event")
    public void test1() throws InterruptedException, IOException {
        var payloadJson = publishCreatedEvent("json/recovery_plasma_carton_packed.json", RECOVER_PLASMA_CARTON_PACKED_TOPIC);
        ArgumentCaptor<RecoveredPlasmaCartonPackedInput> captor = ArgumentCaptor.forClass(RecoveredPlasmaCartonPackedInput.class);
        verify(useCase, times(1)).execute(captor.capture());
        RecoveredPlasmaCartonPackedInput capturedInput = captor.getValue();

        assertThat(capturedInput).isNotNull();
        assertThat(capturedInput.cartonNumber()).isEqualTo(payloadJson.path(PAYLOAD).path("cartonNumber").asText());
    }

    private JsonNode publishCreatedEvent(String json, String topic) throws IOException, InterruptedException {
        var resource = new ClassPathResource(json).getFile().toPath();
        var payloadJson = objectMapper.readTree(Files.newInputStream(resource));
        kafkaHelper.sendEvent(topic, topic + "test-key", payloadJson).block();
        logMonitor.await("Processed message.*");
        return payloadJson;
    }
}
