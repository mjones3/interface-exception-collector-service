package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.dto.LabelInvalidedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.LabelInvalidedUseCase;
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
        "spring.kafka.consumer.group-id=product-modified-test-group",
        "default.location=TestLocation"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9083", "port=9083"})
public class LabelInvalidedListenerIntegrationIT {

    @Autowired
    private KafkaHelper kafkaHelper;

    @MockBean
    private LabelInvalidedUseCase useCase;

    @Value("${topic.label-invalided.name}")
    private String labelInvalidedTopic;

    @BeforeEach
    void setUp() {
        when(useCase.execute(any(LabelInvalidedInput.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("should publish, listen product modified event")
    public void test1() throws InterruptedException, IOException {
        var payloadJson = kafkaHelper.publishEvent("json/label_invalided.json", labelInvalidedTopic);
        ArgumentCaptor<LabelInvalidedInput> captor = ArgumentCaptor.forClass(LabelInvalidedInput.class);
        verify(useCase, times(1)).execute(captor.capture());
        LabelInvalidedInput capturedInput = captor.getValue();

        assertThat(capturedInput).isNotNull();
        assertThat(capturedInput.unitNumber()).isEqualTo(payloadJson.path(PAYLOAD).path(UNIT_NUMBER).asText());
        assertThat(capturedInput.productCode()).isEqualTo(payloadJson.path(PAYLOAD).path(PRODUCT_CODE).asText());
    }
}
