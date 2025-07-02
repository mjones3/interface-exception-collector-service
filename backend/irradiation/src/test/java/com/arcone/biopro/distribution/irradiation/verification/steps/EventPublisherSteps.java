package com.arcone.biopro.distribution.irradiation.verification.steps;

import com.arcone.biopro.distribution.irradiation.domain.event.InventoryEventPublisher;
import com.arcone.biopro.distribution.irradiation.verification.utils.LogMonitor;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventPublisherSteps {

    private final InventoryEventPublisher inventoryEventPublisher;

    private final LogMonitor logMonitor;

    @Then("the irradiation updated event should be produced with the {string} value in the payload for the following units:")
    public void theInventoryUpdatedEventShouldProduceTheUpdateTypeInThePayloadForTheFollowingUnits(String updateType, DataTable dataTable) throws InterruptedException {

        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        String expectedUnitNumber = products.getFirst().get("Unit Number");
        String expectedProductCode = products.getFirst().get("Final Product Code");
        String statusIncluded = products.getFirst().get("Status Included");
        if (Objects.isNull(statusIncluded)) {
            logMonitor.await("Inventory Updated Message .*" + expectedUnitNumber + ".*" + expectedProductCode + ".*" + updateType);
        } else {
            logMonitor.await("Inventory Updated Message .*" + expectedUnitNumber + ".*" + expectedProductCode + ".*" + updateType + ".*" + statusIncluded);

        }

    }
}
