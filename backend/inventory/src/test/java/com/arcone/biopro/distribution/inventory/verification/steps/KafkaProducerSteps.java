package com.arcone.biopro.distribution.inventory.verification.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.DataTableType;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;

@EmbeddedKafka(partitions = 1, topics = {"${topic.inventory-updated.name}"})
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class KafkaProducerSteps {
    @KafkaListener(topics = "${topic.inventory-updated.name}")
    public void inventoryUpdatedReceived(ConsumerRecord<String, String> record) {
        log.info("Received inventory updated record: {}", record);
    }

    @Then("the inventory updated event should be produced with the {string} value in the payload for the following units:")
    public void theInventoryUpdatedEventShouldProduceTheUpdateTypeInThePayloadForTheFollowingUnits(String updateType, DataTable dataTable) {

    }
}
