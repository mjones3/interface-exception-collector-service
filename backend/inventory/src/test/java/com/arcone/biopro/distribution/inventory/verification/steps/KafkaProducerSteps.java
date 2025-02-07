package com.arcone.biopro.distribution.inventory.verification.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"${topic.inventory-updated.name}"})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class KafkaProducerSteps {

    private static final BlockingQueue<ConsumerRecord<String, String>> appliedRecords = new LinkedBlockingQueue<>();

    @KafkaListener(topics = "${topic.inventory-updated.name}")
    public void inventoryUpdatedReceived(ConsumerRecord<String, String> record) {
        appliedRecords.add(record);
        log.info("Received inventory updated record: {}", record);
    }

    @Then("the inventory updated event should be produced with the {string} value in the payload for the following units:")
    public void theInventoryUpdatedEventShouldProduceTheUpdateTypeInThePayloadForTheFollowingUnits(String updateType, DataTable dataTable) throws InterruptedException {

        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> product : products) {
            var unitNumber = product.get("Unit Number");
            var finalProductCode = product.get("Final Product Code");


        ConsumerRecord<String, String> receivedMessage = appliedRecords.poll(1, TimeUnit.MINUTES);
        assert receivedMessage != null;

        assertTrue(receivedMessage.value().contains(unitNumber));
        assertTrue(receivedMessage.value().contains(finalProductCode));
        assertTrue(receivedMessage.value().contains(updateType));

        }
    }
}
