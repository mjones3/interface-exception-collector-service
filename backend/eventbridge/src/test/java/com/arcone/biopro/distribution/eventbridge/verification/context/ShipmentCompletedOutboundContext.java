package com.arcone.biopro.distribution.eventbridge.verification.context;

import com.arcone.biopro.distribution.eventbridge.infrastructure.config.LoggingConfiguration;
import io.cucumber.spring.ScenarioScope;
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
public class ShipmentCompletedOutboundContext {

    private final LoggingConfiguration loggingConfiguration;
    private JSONObject shipmentCompleted;
    private JSONObject shipmentCompletedOutbound;
    private Long shipmentId;
    private Long orderNumber;
    private String externalId;

    private CountDownLatch latchShipmentCompleted = new CountDownLatch(1);
    private CountDownLatch latchShipmentCompletedOutbound = new CountDownLatch(1);
    private String shipmentCompletedOutboundPayload;

    public ShipmentCompletedOutboundContext(LoggingConfiguration loggingConfiguration) {
        this.loggingConfiguration = loggingConfiguration;
    }


    @KafkaListener(topics = "${topics.shipment.shipment-completed.topic-name:ShipmentCompleted}",groupId = "automation-group")
    public void receiveShipmentCompleted(ConsumerRecord<?, ?> consumerRecord) {
        log.info("received shipment completed payload='{}'", consumerRecord.toString());
        latchShipmentCompleted.countDown();
    }

    @KafkaListener(topics = "${topics.shipment.shipment-completed-outbound.topic-name:ShipmentCompletedOutbound}", groupId = "automation-group")
    public void receiveShipmentCompletedOutbound(ConsumerRecord<?, ?> consumerRecord) throws JSONException {
        log.info("received shipment completed outbound payload='{}'", consumerRecord.toString());
        shipmentCompletedOutboundPayload = consumerRecord.value().toString();
        this.shipmentCompletedOutbound = new JSONObject(shipmentCompletedOutboundPayload);
        latchShipmentCompletedOutbound.countDown();
    }

    public void resetLatch() {
        latchShipmentCompleted = new CountDownLatch(1);
    }

}
