package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.dto.RecoveredPlasmaCartonUnpackedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.RecoveredPlasmaCartonUnpackedUseCase;
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
        "spring.kafka.consumer.group-id=recovered_plasma_carton_unpacked-test-group",
        "default.location=TestLocation"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9082", "port=9082"})
public class RecoveredPlasmaCartonUnpackedIntegrationIT {

    @Autowired
    private KafkaHelper kafkaHelper;

    @MockitoBean
    private RecoveredPlasmaCartonUnpackedUseCase useCase;

    @Value("${topic.recovered-plasma-carton-unpacked.name}")
    private String recoveredPlasmaCartonUnpackedTopic;

    @BeforeEach
    void setUp() {
        when(useCase.execute(any(RecoveredPlasmaCartonUnpackedInput.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("should publish, listen recovery plasma carton unpacked event")
    public void test1() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/recovered_plasma_carton_unpacked.json", recoveredPlasmaCartonUnpackedTopic);
        ArgumentCaptor<RecoveredPlasmaCartonUnpackedInput> captor = ArgumentCaptor.forClass(RecoveredPlasmaCartonUnpackedInput.class);
        verify(useCase, times(1)).execute(captor.capture());
        RecoveredPlasmaCartonUnpackedInput capturedInput = captor.getValue();

        assertThat(capturedInput).isNotNull();
        assertThat(capturedInput.cartonNumber()).isEqualTo(payloadJson.path(PAYLOAD).path("cartonNumber").asText());
        assertThat(capturedInput.unpackedProducts().size()).isEqualTo(2);
        assertThat(capturedInput.unpackedProducts().getFirst().status()).isEqualTo("REMOVED");
    }
}
