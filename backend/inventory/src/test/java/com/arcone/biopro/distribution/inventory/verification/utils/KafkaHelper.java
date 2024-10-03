package com.arcone.biopro.distribution.inventory.verification.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

@Component
@Slf4j
public class KafkaHelper {

    private final ReactiveKafkaProducerTemplate<String, Object> template;

    public KafkaHelper(ReactiveKafkaProducerTemplate<String, Object> template) {
        this.template = template;
    }

    public Mono<SenderResult<Void>> sendEvent(String topic, String key, Object message) {
        log.info("Sending Kafka Message {} {}", topic, message);
        var producerRecord = new ProducerRecord<>(topic, key, message);
        return template.send(producerRecord);
    }
}
