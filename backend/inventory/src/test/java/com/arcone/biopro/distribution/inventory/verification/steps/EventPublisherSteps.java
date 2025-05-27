package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.domain.event.InventoryEventPublisher;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryUpdatedApplicationEvent;
import com.arcone.biopro.distribution.inventory.verification.utils.LogMonitor;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventPublisherSteps {

    private final InventoryEventPublisher inventoryEventPublisher;

    private final LogMonitor logMonitor;

    @Then("the inventory updated event should be produced with the {string} value in the payload for the following units:")
    public void theInventoryUpdatedEventShouldProduceTheUpdateTypeInThePayloadForTheFollowingUnits(String updateType, DataTable dataTable) throws InterruptedException {

        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        String expectedUnitNumber = products.getFirst().get("Unit Number");
        String expectedProductCode = products.getFirst().get("Final Product Code");
        logMonitor.await("Inventory Updated Message .*" + expectedUnitNumber + ".*" + expectedProductCode + ".*" + updateType);


    }
}
