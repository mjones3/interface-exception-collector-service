package com.arcone.biopro.distribution.irradiation.unit.adapter.out.kafka.producer;

import com.arcone.biopro.distribution.irradiation.adapter.common.EventMessage;
import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.ProductModified;
import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.producer.ProductModifiedProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductModifiedProducerTest {

    @Mock
    private ReactiveKafkaProducerTemplate<String, EventMessage<ProductModified>> kafkaTemplate;

    @Mock
    private SenderResult<Void> senderResult;

    @Test
    void publishProductModified_ShouldSendMessageSuccessfully() {
        // Given
        String topic = "product.modified";
        ProductModifiedProducer producer = new ProductModifiedProducer(kafkaTemplate, topic);
        
        ProductModified payload = ProductModified.builder()
            .unitNumber("W123456748999")
            .productCode("E468900")
            .productDescription("APH FFP")
            .parentProductCode("E468901")
            .productFamily("PLASMA_TRANSFUSABLE")
            .expirationDate("02/03/2025")
            .expirationTime("23:59")
            .modificationLocation("1FS")
            .build();

        when(kafkaTemplate.send(eq(topic), eq("W123456748999"), any(EventMessage.class)))
            .thenReturn(Mono.just(senderResult));

        // When & Then
        StepVerifier.create(producer.publishProductModified(payload))
            .verifyComplete();
    }

    @Test
    void publishProductModified_ShouldHandleError() {
        // Given
        String topic = "product.modified";
        ProductModifiedProducer producer = new ProductModifiedProducer(kafkaTemplate, topic);
        
        ProductModified payload = ProductModified.builder()
            .unitNumber("W123456748999")
            .productCode("E468900")
            .productDescription("APH FFP")
            .parentProductCode("E468901")
            .productFamily("PLASMA_TRANSFUSABLE")
            .expirationDate("02/03/2025")
            .expirationTime("23:59")
            .modificationLocation("1FS")
            .build();

        when(kafkaTemplate.send(eq(topic), eq("W123456748999"), any(EventMessage.class)))
            .thenReturn(Mono.error(new RuntimeException("Kafka error")));

        // When & Then
        StepVerifier.create(producer.publishProductModified(payload))
            .expectError(RuntimeException.class)
            .verify();
    }
}