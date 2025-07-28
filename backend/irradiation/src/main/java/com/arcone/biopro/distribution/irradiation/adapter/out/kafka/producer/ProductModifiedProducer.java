package com.arcone.biopro.distribution.irradiation.adapter.out.kafka.producer;

import com.arcone.biopro.distribution.irradiation.adapter.common.EventMessage;
import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.ProductModified;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ProductModifiedProducer {
    private final ReactiveKafkaProducerTemplate<String, EventMessage<ProductModified>> producerProductModifiedTemplate;
    private final String topic;

    public ProductModifiedProducer(ReactiveKafkaProducerTemplate<String, EventMessage<ProductModified>> producerProductModifiedTemplate,
                                  @Value("${topic.product.modified.name}") String topic) {
        this.producerProductModifiedTemplate = producerProductModifiedTemplate;
        this.topic = topic;
    }



    public Mono<Void> publishProductModified(ProductModified payload) {
        var message = new EventMessage<>("ProductModified", "1.0", payload);

        return producerProductModifiedTemplate.send(topic, payload.unitNumber(), message)
            .doOnSuccess(result -> log.info("Sent product modified event for unit: {}", payload.unitNumber()))
            .doOnError(error -> log.error("Error sending product modified event", error))
            .then();
    }
}
