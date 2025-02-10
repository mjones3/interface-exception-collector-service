package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.domain.event.InventoryEvent;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryEventPublisher;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryUpdatedApplicationEvent;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.testcontainers.shaded.com.google.common.util.concurrent.AtomicDouble;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@Slf4j
@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventPublisherSteps {

    private final InventoryEventPublisher inventoryEventPublisher;

    @Then("the inventory updated event should be produced with the {string} value in the payload for the following units:")
    public void theInventoryUpdatedEventShouldProduceTheUpdateTypeInThePayloadForTheFollowingUnits(String updateType, DataTable dataTable) throws InterruptedException {
        var eventCaptor = ArgumentCaptor.forClass(InventoryUpdatedApplicationEvent.class);
        verify(inventoryEventPublisher).publish(eventCaptor.capture());
        var capturedEvent = eventCaptor.getValue();
        assertEquals(capturedEvent.inventoryUpdateType().toString(), updateType);
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        assertEquals(1, products.size());
        String expectedUnitNumber = products.getFirst().get("Unit Number");
        String expectedProductCode = products.getFirst().get("Final Product Code");
        assertEquals(expectedUnitNumber, capturedEvent.inventory().getUnitNumber().value());
        assertEquals(expectedProductCode, capturedEvent.inventory().getProductCode().value());
    }
}
