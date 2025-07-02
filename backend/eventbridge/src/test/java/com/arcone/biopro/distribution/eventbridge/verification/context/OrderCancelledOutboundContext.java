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
public class OrderCancelledOutboundContext {

    private final LoggingConfiguration loggingConfiguration;
    private JSONObject orderCancelled;
    private JSONObject orderCancelledOutbound;
    private String orderNumber;
    private String externalId;
    private String orderStatus;

    private CountDownLatch latchOrderCancelled = new CountDownLatch(1);
    private CountDownLatch latchOrderCancelledOutbound = new CountDownLatch(1);
    private String orderCancelledOutboundPayload;

    public OrderCancelledOutboundContext(LoggingConfiguration loggingConfiguration) {
        this.loggingConfiguration = loggingConfiguration;
    }

    @KafkaListener(topics = "${topics.order.order-cancelled.topic-name}", groupId = "automation-group")
    public void receiveOrderCancelled(ConsumerRecord<?, ?> consumerRecord) {
        log.info("received order cancelled payload='{}'", consumerRecord.toString());
        latchOrderCancelled.countDown();
    }

    @KafkaListener(topics = "${topics.order.order-cancelled-outbound.topic-name}", groupId = "automation-group")
    public void receiveOrderCancelledOutbound(ConsumerRecord<?, ?> consumerRecord) throws JSONException {
        log.info("received order cancelled outbound payload='{}'", consumerRecord.toString());
        orderCancelledOutboundPayload = consumerRecord.value().toString();
        this.orderCancelledOutbound = new JSONObject(orderCancelledOutboundPayload);
        latchOrderCancelledOutbound.countDown();
    }

    public void resetLatch() {
        latchOrderCancelled = new CountDownLatch(1);
    }
}