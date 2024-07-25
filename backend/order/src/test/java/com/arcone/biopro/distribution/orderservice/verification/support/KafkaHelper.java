package com.arcone.biopro.distribution.orderservice.verification.support;

import com.arcone.biopro.distribution.orderservice.application.dto.OrderReceivedEventDTO;
import com.arcone.biopro.distribution.orderservice.application.dto.OrderReceivedEventPayloadDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaHelper {

    @Value("${kafka.order-received.topic-name:OrderReceived}")
    private String orderReceivedTopic;

    private final ReactiveKafkaProducerTemplate<String, OrderReceivedEventDTO> producerTemplate;

    public Mono<SenderResult<Void>> sendPartnerOrderReceivedEvent(String key, OrderReceivedEventDTO payload) {
        log.info("Sending Kafka Message {} {}",orderReceivedTopic, payload);
        var producerRecord = new ProducerRecord<>(orderReceivedTopic, key, payload);
        return producerTemplate.send(producerRecord);
    }
}
