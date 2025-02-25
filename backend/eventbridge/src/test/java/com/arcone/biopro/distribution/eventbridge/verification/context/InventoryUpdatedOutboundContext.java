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
public class InventoryUpdatedOutboundContext {

    private final LoggingConfiguration loggingConfiguration;
    private JSONObject inventoryUpdated;
    private JSONObject inventoryUpdatedOutbound;
    private String unitNumber;
    private String productCode;
    private String updateType;

    private CountDownLatch latchInventoryUpdated = new CountDownLatch(1);
    private CountDownLatch latchInventoryUpdatedOutbound = new CountDownLatch(1);
    private String inventoryUpdatedOutboundPayload;

    public InventoryUpdatedOutboundContext(LoggingConfiguration loggingConfiguration) {
        this.loggingConfiguration = loggingConfiguration;
    }


    @KafkaListener(topics = "${topics.inventory.inventory-updated.topic-name}",groupId = "automation-group")
    public void receiveInventoryUpdated(ConsumerRecord<?, ?> consumerRecord) {
        log.info("received inventory updated payload='{}'", consumerRecord.toString());
        latchInventoryUpdated.countDown();
    }

    @KafkaListener(topics = "${topics.inventory.inventory-updated-outbound.topic-name}", groupId = "automation-group")
    public void receiveInventoryUpdatedOutbound(ConsumerRecord<?, ?> consumerRecord) throws JSONException {
        log.info("received inventory updated outbound payload='{}'", consumerRecord.toString());
        inventoryUpdatedOutboundPayload = consumerRecord.value().toString();
        this.inventoryUpdatedOutbound = new JSONObject(inventoryUpdatedOutboundPayload);
        latchInventoryUpdatedOutbound.countDown();
    }

    public void resetLatch() {
        latchInventoryUpdated = new CountDownLatch(1);
    }

}
