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
public class RecoveredPlasmaShipmentClosedOutboundContext {

    private final LoggingConfiguration loggingConfiguration;
    private JSONObject shipmentClosed;
    private JSONObject shipmentClosedOutbound;
    private String shipmentNumber;

    private CountDownLatch latchShipmentClosed = new CountDownLatch(1);
    private CountDownLatch latchShipmentClosedOutbound = new CountDownLatch(1);
    private String outboundPayload;

    public RecoveredPlasmaShipmentClosedOutboundContext(LoggingConfiguration loggingConfiguration) {
        this.loggingConfiguration = loggingConfiguration;
    }


    @KafkaListener(topics = "${topics.recovered-plasma-shipment.shipment-closed.topic-name:RecoveredPlasmaShipmentClosed}",groupId = "automation-group")
    public void receiveShipmentCompleted(ConsumerRecord<?, ?> consumerRecord) {
        log.debug("received recovered plasma shipment closed payload='{}'", consumerRecord.toString());
        latchShipmentClosed.countDown();
    }

    @KafkaListener(topics = "${topics.recovered-plasma-shipment.shipment-closed-outbound.topic-name:RecoveredPlasmaShipmentClosedOutbound}", groupId = "automation-group")
    public void receiveShipmentCompletedOutbound(ConsumerRecord<?, ?> consumerRecord) throws JSONException {
        log.debug("received recovered plasma shipment closed outbound payload='{}'", consumerRecord.toString());
        outboundPayload = consumerRecord.value().toString();
        this.shipmentClosedOutbound = new JSONObject(outboundPayload);
        latchShipmentClosedOutbound.countDown();
    }

    public void resetLatch() {
        latchShipmentClosed = new CountDownLatch(1);

    }

}
