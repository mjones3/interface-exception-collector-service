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
public class OrderRejectedOutboundContext {

    private final LoggingConfiguration loggingConfiguration;
    private JSONObject orderRejected;
    private JSONObject orderRejectedOutbound;
    private String externalId;
    private String rejectedReason;
    private String operation;

    private CountDownLatch latchOrderRejected = new CountDownLatch(1);
    private CountDownLatch latchOrderRejectedOutbound = new CountDownLatch(1);
    private String orderRejectedOutboundPayload;

    public OrderRejectedOutboundContext(LoggingConfiguration loggingConfiguration) {
        this.loggingConfiguration = loggingConfiguration;
    }

    @KafkaListener(topics = "${topics.order.order-rejected.topic-name}", groupId = "automation-group")
    public void receiveOrderRejected(ConsumerRecord<?, ?> consumerRecord) {
        log.info("received order rejected payload='{}'", consumerRecord.toString());
        latchOrderRejected.countDown();
    }

    @KafkaListener(topics = "${topics.order.order-rejected-outbound.topic-name}", groupId = "automation-group")
    public void receiveOrderRejectedOutbound(ConsumerRecord<?, ?> consumerRecord) throws JSONException {
        log.info("received order rejected outbound payload='{}'", consumerRecord.toString());
        orderRejectedOutboundPayload = consumerRecord.value().toString();
        this.orderRejectedOutbound = new JSONObject(orderRejectedOutboundPayload);
        latchOrderRejectedOutbound.countDown();
    }

    public void resetLatch() {
        latchOrderRejected = new CountDownLatch(1);
    }
}