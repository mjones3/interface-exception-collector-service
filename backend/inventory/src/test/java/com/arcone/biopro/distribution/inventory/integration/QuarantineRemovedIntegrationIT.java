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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9086", "port=9086"})
public class QuarantineRemovedIntegrationIT {

    @Autowired
    private KafkaHelper kafkaHelper;

    @MockitoBean
    private RemoveQuarantinedUseCase quarantineRemovedUseCase;

    @BeforeEach
    void setUp() {
        when(quarantineRemovedUseCase.execute(any(RemoveQuarantineInput.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("should publish, listen, map and call use case with correct converted inputs for Quarantine Removed")
    public void testQuarantineRemoved() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/quarantine_removed.json", QUARANTINE_REMOVED_TOPIC);
        ArgumentCaptor<RemoveQuarantineInput> captor = ArgumentCaptor.forClass(RemoveQuarantineInput.class);
        verify(quarantineRemovedUseCase, timeout(5000).times(1)).execute(captor.capture());
        RemoveQuarantineInput capturedInput = captor.getValue();

        assertThat(capturedInput).isNotNull();
        assertThat(capturedInput.quarantineId()).isEqualTo(payloadJson.path(PAYLOAD).path("id").asLong());
    }
}
