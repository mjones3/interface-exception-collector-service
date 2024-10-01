package com.arcone.biopro.distribution.shipping.verification.support;

import com.arcone.biopro.distribution.shipping.verification.support.types.OrderFulfilledEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
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

    public Mono<SenderResult<Void>> sendEvent(String key, Object message, String topic) {
        log.info("Sending Kafka Message {} {}", topic, message);
        var producerRecord = new ProducerRecord<>(topic, key, message);
        return template.send(producerRecord);
    }
}
