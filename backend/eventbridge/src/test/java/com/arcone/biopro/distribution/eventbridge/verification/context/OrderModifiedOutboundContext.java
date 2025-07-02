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
public class OrderModifiedOutboundContext {

    private final LoggingConfiguration loggingConfiguration;
    private JSONObject orderModified;
    private JSONObject orderModifiedOutbound;
    private String orderNumber;
    private String externalId;
    private String orderStatus;

    private CountDownLatch latchOrderModified = new CountDownLatch(1);
    private CountDownLatch latchOrderModifiedOutbound = new CountDownLatch(1);
    private String orderModifiedOutboundPayload;

    public OrderModifiedOutboundContext(LoggingConfiguration loggingConfiguration) {
        this.loggingConfiguration = loggingConfiguration;
    }

    @KafkaListener(topics = "${topics.order.order-modified.topic-name}", groupId = "automation-group")
    public void receiveOrderModified(ConsumerRecord<?, ?> consumerRecord) {
        log.info("received order modified payload='{}'", consumerRecord.toString());
        latchOrderModified.countDown();
    }

    @KafkaListener(topics = "${topics.order.order-modified-outbound.topic-name}", groupId = "automation-group")
    public void receiveOrderModifiedOutbound(ConsumerRecord<?, ?> consumerRecord) throws JSONException {
        log.info("received order modified outbound payload='{}'", consumerRecord.toString());
        orderModifiedOutboundPayload = consumerRecord.value().toString();
        this.orderModifiedOutbound = new JSONObject(orderModifiedOutboundPayload);
        latchOrderModifiedOutbound.countDown();
    }

    public void resetLatch() {
        latchOrderModified = new CountDownLatch(1);
    }
}