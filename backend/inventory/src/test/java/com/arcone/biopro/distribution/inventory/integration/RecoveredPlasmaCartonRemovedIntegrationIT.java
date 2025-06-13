package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.dto.RecoveredPlasmaCartonRemovedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.RecoveredPlasmaCartonRemovedUseCase;
import com.arcone.biopro.distribution.inventory.verification.utils.KafkaHelper;
import com.arcone.biopro.distribution.inventory.verification.utils.LogMonitor;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static com.arcone.biopro.distribution.inventory.BioProConstants.RECOVER_PLASMA_CARTON_REMOVED_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=recovered_plasma_carton_removed-test-group",
        "default.location=TestLocation"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9096", "port=9096"})
public class RecoveredPlasmaCartonRemovedIntegrationIT {

    @Autowired
    private KafkaHelper kafkaHelper;

    @MockBean
    private RecoveredPlasmaCartonRemovedUseCase useCase;

    @Value("${topic.recovered-plasma-carton-removed.name}")
    private String recoveredPlasmaCartonRemovedTopic;

    @BeforeEach
    void setUp() {
        when(useCase.execute(any(RecoveredPlasmaCartonRemovedInput.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("should publish, listen recovery plasma carton removed event")
    public void test1() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/recovered_plasma_carton_removed.json", recoveredPlasmaCartonRemovedTopic);
        ArgumentCaptor<RecoveredPlasmaCartonRemovedInput> captor = ArgumentCaptor.forClass(RecoveredPlasmaCartonRemovedInput.class);
        verify(useCase, times(1)).execute(captor.capture());
        RecoveredPlasmaCartonRemovedInput capturedInput = captor.getValue();

        assertThat(capturedInput).isNotNull();
        assertThat(capturedInput.cartonNumber()).isEqualTo(payloadJson.path(PAYLOAD).path("cartonNumber").asText());
        assertThat(capturedInput.packedProducts().size()).isEqualTo(1);
        assertThat(capturedInput.packedProducts().getFirst().status()).isEqualTo("UNPACKED");
    }
}
