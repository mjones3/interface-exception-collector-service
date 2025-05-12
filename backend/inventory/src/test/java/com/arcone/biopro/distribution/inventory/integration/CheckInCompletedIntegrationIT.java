package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.dto.CheckInCompletedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.CheckInCompletedUseCase;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.verification.utils.KafkaHelper;
import com.fasterxml.jackson.databind.JsonNode;
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

import static com.arcone.biopro.distribution.inventory.BioProConstants.CHECK_IN_COMPLETED_TOPIC;
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
        "spring.kafka.consumer.group-id=checkin-test-group",
        "default.location=TestLocation"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9095", "port=9095"})
public class CheckInCompletedIntegrationIT {

    @Autowired
    private KafkaHelper kafkaHelper;

    @MockBean
    private CheckInCompletedUseCase checkInCompletedUseCase;

    @BeforeEach
    void setUp() {
        when(checkInCompletedUseCase.execute(any(CheckInCompletedInput.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Should publish, receive, map and call usecase with correct input for check in completed payload")
    public void test1() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/check_in_completed.json", CHECK_IN_COMPLETED_TOPIC);
        ArgumentCaptor<CheckInCompletedInput> captor = ArgumentCaptor.forClass(CheckInCompletedInput.class);
        verify(checkInCompletedUseCase, times(1)).execute(captor.capture());
        CheckInCompletedInput capturedInput = captor.getValue();
        assertDefaultProductCreatedValues(capturedInput, payloadJson);
    }

    private static void assertDefaultProductCreatedValues(CheckInCompletedInput capturedInput, JsonNode payloadJson) {
        assertThat(capturedInput.unitNumber()).isEqualTo(payloadJson.path(PAYLOAD).path("unitNumber").asText());
        assertThat(capturedInput.productCode()).isEqualTo(payloadJson.path(PAYLOAD).path("productCode").asText());
        assertThat(capturedInput.productDescription()).isEqualTo(payloadJson.path(PAYLOAD).path("productDescription").asText());
        assertThat(capturedInput.collectionDate()).isEqualTo(payloadJson.path(PAYLOAD).path("drawTime").asText());
        assertThat(capturedInput.location()).isEqualTo(payloadJson.path(PAYLOAD).path("collectionLocation").asText());
        assertThat(capturedInput.productFamily()).isEqualTo(payloadJson.path(PAYLOAD).path("productFamily").asText());
        assertThat(capturedInput.aboRh()).isEqualTo(AboRhType.valueOf(payloadJson.path(PAYLOAD).path("aboRh").asText()));
    }
}
