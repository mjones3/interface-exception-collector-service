package com.arcone.biopro.distribution.eventbridge.verification.context;

import com.arcone.biopro.distribution.eventbridge.infrastructure.config.LoggingConfiguration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
@Getter
@Setter
@Slf4j
@Profile("AUTOMATION")
public class OrderCreatedOutboundContext {

    private final LoggingConfiguration loggingConfiguration;
    private JSONObject orderCreated;
    private JSONObject orderCreatedOutbound;
    private String orderNumber;
    private String externalId;
    private String orderStatus;

    private CountDownLatch latchOrderCreated = new CountDownLatch(1);
    private CountDownLatch latchOrderCreatedOutbound = new CountDownLatch(1);
    private String orderCreatedOutboundPayload;

    public OrderCreatedOutboundContext(LoggingConfiguration loggingConfiguration) {
        this.loggingConfiguration = loggingConfiguration;
    }

    @KafkaListener(topics = "${topics.order.order-created.topic-name}", groupId = "automation-group")
    public void receiveOrderCreated(ConsumerRecord<?, ?> consumerRecord) {
        log.info("received order created payload='{}'", consumerRecord.toString());
        latchOrderCreated.countDown();
    }

    @KafkaListener(topics = "${topics.order.order-created-outbound.topic-name}", groupId = "automation-group")
    public void receiveOrderCreatedOutbound(ConsumerRecord<?, ?> consumerRecord) throws JSONException {
        log.info("received order created outbound payload='{}'", consumerRecord.toString());
        orderCreatedOutboundPayload = consumerRecord.value().toString();
        this.orderCreatedOutbound = new JSONObject(orderCreatedOutboundPayload);
        latchOrderCreatedOutbound.countDown();
    }

    public void resetLatch() {
        latchOrderCreated = new CountDownLatch(1);
    }
}