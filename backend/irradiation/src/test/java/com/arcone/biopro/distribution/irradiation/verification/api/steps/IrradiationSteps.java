package com.arcone.biopro.distribution.irradiation.verification.api.steps;

import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateUnitNumberUseCase;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.BatchEntity;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.DeviceEntity;
import com.arcone.biopro.distribution.irradiation.verification.api.support.IrradiationContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextConfiguration
public class IrradiationSteps {

    private static final Logger log = LoggerFactory.getLogger(IrradiationSteps.class);
    @Autowired
    private ReactiveCrudRepository<DeviceEntity, Long> deviceRepository;

    @Autowired
    private ValidateUnitNumberUseCase validateUnitNumberUseCase;

    @Autowired
    private IrradiationContext irradiationContext;

    @Autowired
    private ReactiveCrudRepository<BatchEntity, Long> batchRepository;


    @Given("I'm in the irradiation service at the location {string}")
    public void iMInTheIrradiationService(String location) {
        irradiationContext.setLocation(location);
        log.info("Starting irradiation testing.");
    }

    @When("I scan the unit number {string} in irradiation")
    public void iScanTheUnitNumberInIrradiation(String unitNumber) {
        List<Inventory> inventories = validateUnitNumberUseCase.execute(unitNumber, irradiationContext.getLocation())
            .collectList()
            .block(Duration.ofSeconds(10));
        irradiationContext.setInventoryList(inventories);
    }

    @Then("I see the product {string} from unit number {string} is in the list of products for selection")
    public void iSeeTheProductFromUnitNumberIsInTheListOfProductsForSelection(String productCode, String unitNumber) {
        assertTrue(irradiationContext.getInventoryList().stream().anyMatch(i-> i.getProductCode().equals(productCode) && i.getUnitNumber().value().equals(unitNumber)));
    }

    @Given("I have the following inventory products:")
    public void iHaveTheFollowingInventoryProducts(DataTable dataTable) {
        log.info("faking inventory products");
    }
}
