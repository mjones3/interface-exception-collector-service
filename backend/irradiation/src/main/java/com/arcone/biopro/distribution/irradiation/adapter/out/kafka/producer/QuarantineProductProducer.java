package com.arcone.biopro.distribution.irradiation.adapter.out.kafka.producer;

import com.arcone.biopro.distribution.irradiation.adapter.common.EventMessage;
import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.QuarantineProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class QuarantineProductProducer {
    private final ReactiveKafkaProducerTemplate<String, EventMessage<QuarantineProduct>> producerQuarantineTemplate;
    private final String topic;

    public QuarantineProductProducer(ReactiveKafkaProducerTemplate<String, EventMessage<QuarantineProduct>> producerQuarantineTemplate,
                                    @Value("${topic.product.quarantine.name}") String topic) {
        this.producerQuarantineTemplate = producerQuarantineTemplate;
        this.topic = topic;
    }

    public Mono<Void> publishQuarantineProduct(QuarantineProduct payload) {
        var message = new EventMessage<>("QuarantineProduct", "1.0", payload);

        String key = payload.products().isEmpty() ? "unknown" : payload.products().getFirst().unitNumber();
        return producerQuarantineTemplate.send(topic, key, message)
            .doOnSuccess(result -> log.info("Sent quarantine event for {} products", payload.products().size()))
            .doOnError(error -> log.error("Error sending quarantine event", error))
            .then();
    }
}
