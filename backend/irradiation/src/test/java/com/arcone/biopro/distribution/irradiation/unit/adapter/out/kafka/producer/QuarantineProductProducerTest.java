package com.arcone.biopro.distribution.irradiation.unit.adapter.out.kafka.producer;

import com.arcone.biopro.distribution.irradiation.adapter.common.EventMessage;
import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.QuarantineProduct;
import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.QuarantineProductInput;
import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.producer.QuarantineProductProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuarantineProductProducerTest {

    @Mock
    private ReactiveKafkaProducerTemplate<String, EventMessage<QuarantineProduct>> kafkaTemplate;

    @Mock
    private SenderResult<Void> senderResult;

    @Test
    void publishQuarantineProduct_ShouldSendMessageSuccessfully() {
        // Given
        String topic = "product.quarantine";
        QuarantineProductProducer producer = new QuarantineProductProducer(kafkaTemplate, topic);
        
        List<QuarantineProductInput> productInputs = List.of(
            QuarantineProductInput.builder()
                .unitNumber("W123456789012")
                .productCode("PLASAPHP")
                .build(),
            QuarantineProductInput.builder()
                .unitNumber("W123456789013")
                .productCode("PLASAPHP")
                .build(),
            QuarantineProductInput.builder()
                .unitNumber("W123456789014")
                .productCode("PLASAPHP")
                .build()
        );
        
        QuarantineProduct payload = QuarantineProduct.builder()
            .products(productInputs)
            .triggeredBy("IRRADIATION_SYSTEM")
            .reasonKey("IRRADIATION INCOMPLETE")
            .comments("comments here")
            .performedBy("IRRADIATION_SYSTEM")
            .build();

        when(kafkaTemplate.send(eq(topic), eq("W123456789012"), any(EventMessage.class)))
            .thenReturn(Mono.just(senderResult));

        // When & Then
        StepVerifier.create(producer.publishQuarantineProduct(payload))
            .verifyComplete();
    }

    @Test
    void publishQuarantineProduct_ShouldUseUnknownKeyWhenProductsEmpty() {
        // Given
        String topic = "product.quarantine";
        QuarantineProductProducer producer = new QuarantineProductProducer(kafkaTemplate, topic);
        
        QuarantineProduct payload = QuarantineProduct.builder()
            .products(List.of())
            .build();

        when(kafkaTemplate.send(eq(topic), eq("unknown"), any(EventMessage.class)))
            .thenReturn(Mono.just(senderResult));

        // When & Then
        StepVerifier.create(producer.publishQuarantineProduct(payload))
            .verifyComplete();
    }

    @Test
    void publishQuarantineProduct_ShouldHandleError() {
        // Given
        String topic = "product.quarantine";
        QuarantineProductProducer producer = new QuarantineProductProducer(kafkaTemplate, topic);
        
        QuarantineProductInput productInput = QuarantineProductInput.builder()
            .unitNumber("W123456789012")
            .productCode("PLASAPHP")
            .build();
        
        QuarantineProduct payload = QuarantineProduct.builder()
            .products(List.of(productInput))
            .triggeredBy("IRRADIATION_SYSTEM")
            .reasonKey("IRRADIATION INCOMPLETE")
            .comments("comments here")
            .performedBy("IRRADIATION_SYSTEM")
            .build();

        when(kafkaTemplate.send(eq(topic), eq("W123456789012"), any(EventMessage.class)))
            .thenReturn(Mono.error(new RuntimeException("Kafka error")));

        // When & Then
        StepVerifier.create(producer.publishQuarantineProduct(payload))
            .expectError(RuntimeException.class)
            .verify();
    }
}