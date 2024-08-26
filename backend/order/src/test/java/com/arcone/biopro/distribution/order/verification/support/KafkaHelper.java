package com.arcone.biopro.distribution.order.verification.support;

import com.arcone.biopro.distribution.order.application.dto.OrderReceivedEventDTO;
import com.arcone.biopro.distribution.order.application.dto.ShipmentCreatedEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

@Component
@Slf4j
public class KafkaHelper {

    private String orderReceivedTopic;

    private String shipmentCreatedTopic;

    private final ReactiveKafkaProducerTemplate<String, OrderReceivedEventDTO> partnerOrderProducerTemplate;

    private final ReactiveKafkaProducerTemplate<String, ShipmentCreatedEventDTO> shipmentCreatedProducerTemplate;

    public KafkaHelper(@Value("${kafka.order-received.topic-name:OrderReceived}") String orderReceivedTopic
        , @Value("${kafka.shipment-created.topic-name:ShipmentCreated}") String shipmentCreatedTopic
        , @Qualifier("partner-order") ReactiveKafkaProducerTemplate<String, OrderReceivedEventDTO> partnerOrderProducerTemplate
        , @Qualifier("shipment-created") ReactiveKafkaProducerTemplate<String, ShipmentCreatedEventDTO> shipmentCreatedProducerTemplate) {
        this.orderReceivedTopic = orderReceivedTopic;
        this.shipmentCreatedTopic = shipmentCreatedTopic;
        this.partnerOrderProducerTemplate = partnerOrderProducerTemplate;
        this.shipmentCreatedProducerTemplate = shipmentCreatedProducerTemplate;
    }

    public Mono<SenderResult<Void>> sendPartnerOrderReceivedEvent(String key, OrderReceivedEventDTO payload) {
        log.info("Sending Kafka Message {} {}", orderReceivedTopic, payload);
        var producerRecord = new ProducerRecord<>(orderReceivedTopic, key, payload);
        return partnerOrderProducerTemplate.send(producerRecord);
    }

    public Mono<SenderResult<Void>> sendShipmentCreatedEvent(String key, ShipmentCreatedEventDTO payload) {
        log.info("Sending Kafka Message {} {}", shipmentCreatedTopic, payload);
        var producerRecord = new ProducerRecord<>(shipmentCreatedTopic, key, payload);
        return shipmentCreatedProducerTemplate.send(producerRecord);
    }
}
