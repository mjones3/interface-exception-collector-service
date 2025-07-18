package com.arcone.biopro.distribution.irradiation.verification.api.steps;

import com.arcone.biopro.distribution.irradiation.application.dto.IrradiationInventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateUnitNumberUseCase;
import com.arcone.biopro.distribution.irradiation.verification.api.support.IrradiationContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextConfiguration
public class IrradiationSteps {

    private static final Logger log = LoggerFactory.getLogger(IrradiationSteps.class);

    @Autowired
    private ValidateUnitNumberUseCase validateUnitNumberUseCase;

    @Autowired
    private IrradiationContext irradiationContext;


    @Given("I'm in the irradiation service at the location {string}")
    public void iMInTheIrradiationService(String location) {
        irradiationContext.setLocation(location);
        log.info("Starting irradiation testing.");
    }

    @Then("I see the product {string} from unit number {string} is in the list of products for selection")
    public void iSeeTheProductFromUnitNumberIsInTheListOfProductsForSelection(String productCode, String unitNumber) {
        assertTrue(irradiationContext.getInventoryList().stream().anyMatch(i-> i.productCode().equals(productCode) && i.unitNumber().equals(unitNumber)));
    }

    @Given("I have the following inventory products:")
    public void iHaveTheFollowingInventoryProducts(DataTable dataTable) {
        log.info("faking inventory products");
    }

    @Then("I verify that there are only {int} product\\(s) eligible for irradiation for the unit number {string}")
    public void iVerifyThatThereAreOnlyProductSEligibleForIrradiationForTheUnitNumber(int numberOfProducts, String unitNumber) {
        assertEquals(irradiationContext.getInventoryList().stream().toList().size(), numberOfProducts);
    }

    @Then("I verify that there are {int} product\\(s) eligible for irradiation for the unit number {string}")
    public void iVerifyThatThereAreProductSEligibleForIrradiationForTheUnitNumber(int numberOfProducts, String unitNumber) {
        assertEquals(irradiationContext.getInventoryList().stream().toList().size(), numberOfProducts);
    }

    @Then("I see the product {string} from unit number {string} is NOT in the list of products for selection")
    public void iSeeTheProductFromUnitNumberIsNOTInTheListOfProductsForSelection(String productCode, String unitNumber) {
        assertFalse(irradiationContext.getInventoryList().stream().anyMatch(i-> i.productCode().equals(productCode) && i.unitNumber().equals(unitNumber)));
    }

    @Then("I see the product {string} from unit number {string} has {string} flag set to {word}")
    public void iSeeTheProductFromUnitNumberHasFlagSetTo(String productCode, String unitNumber, String flagName, String flagValue) {
        boolean expectedValue = Boolean.parseBoolean(flagValue);

        IrradiationInventoryOutput product = irradiationContext.getInventoryList().stream()
                .filter(i -> productCode.equals(i.productCode()) && unitNumber.equals(i.unitNumber()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Product " + productCode + " not found for unit " + unitNumber));

        boolean actualValue = switch (flagName) {
            case "alreadyIrradiated" -> product.alreadyIrradiated();
            case "notConfigurableForIrradiation" -> product.notConfigurableForIrradiation();
            default -> throw new IllegalArgumentException("Unknown flag: " + flagName);
        };

        assertEquals(expectedValue, actualValue, "Flag " + flagName + " should be " + expectedValue + " but was " + actualValue);
    }

    @Then("I verify that product {string} in the unit {string} is flagged as already irradiated")
    public void iVerifyThatProductInTheUnitIsFlaggedAsAlreadyIrradiated(String productCode, String unitNumber) {
        assertEquals(irradiationContext.getInventoryList().getFirst().productCode(), productCode);
        assertEquals(irradiationContext.getInventoryList().getFirst().unitNumber(), unitNumber);
        assertTrue(irradiationContext.getInventoryList().getFirst().alreadyIrradiated());
    }

    @Then("I verify that product {string} in the unit {string} is flagged as not configurable for irradiation")
    public void iVerifyThatProductInTheUnitIsFlaggedAsNotConfigurableForIrradiation(String productCode, String unitNumber) {
        assertEquals(irradiationContext.getInventoryList().getFirst().productCode(), productCode);
        assertEquals(irradiationContext.getInventoryList().getFirst().unitNumber(), unitNumber);
        assertTrue(irradiationContext.getInventoryList().getFirst().notConfigurableForIrradiation());
    }

    @Then("I see the error message {string}")
    public void iSeeTheErrorMessage(String errorMessage) {
        assertEquals(irradiationContext.getResponseErrors().getFirst().getMessage(), errorMessage);
    }

    @Then("I verify that product {string} in the unit {string} is flagged as quarantined that stops manufacturing")
    public void iVerifyThatProductInTheUnitIsFlaggedAsQuarantinedThatStopsManufacturing(String productCode, String unitNumber) {
        assertEquals(irradiationContext.getInventoryList().getFirst().productCode(), productCode);
        assertEquals(irradiationContext.getInventoryList().getFirst().unitNumber(), unitNumber);
        assertTrue(irradiationContext.getInventoryList().getFirst().quarantines().getFirst().stopsManufacturing());
    }

    @Then("I verify that product {string} in the unit {string} is flagged as quarantined that does not stops manufacturing")
    public void iVerifyThatProductInTheUnitIsFlaggedAsQuarantinedThatDoesNotStopsManufacturing(String productCode, String unitNumber) {
        assertEquals(irradiationContext.getInventoryList().getFirst().productCode(), productCode);
        assertEquals(irradiationContext.getInventoryList().getFirst().unitNumber(), unitNumber);
        assertFalse(irradiationContext.getInventoryList().getFirst().quarantines().getFirst().stopsManufacturing());
    }

    @Then("I verify that product {string} in the unit {string} is flagged as discarded as {string}")
    public void iVerifyThatProductInTheUnitIsFlaggedAsDiscardedAs(String productCode, String unitNumber, String discardReason) {
        assertEquals(irradiationContext.getInventoryList().getFirst().productCode(), productCode);
        assertEquals(irradiationContext.getInventoryList().getFirst().unitNumber(), unitNumber);
        assertTrue(irradiationContext.getInventoryList().getFirst().status().equalsIgnoreCase("DISCARDED"));
        assertTrue(irradiationContext.getInventoryList().getFirst().statusReason().equalsIgnoreCase(discardReason));
    }

    @Then("I verify that product {string} in the unit {string} is flagged as unsuitable with reason {string}")
    public void iVerifyThatProductInTheUnitIsFlaggedAsUnsuitableWithReason(String productCode, String unitNumber, String unsuitableReason) {
        assertEquals(irradiationContext.getInventoryList().getFirst().productCode(), productCode);
        assertEquals(irradiationContext.getInventoryList().getFirst().unitNumber(), unitNumber);
        assertNotNull(irradiationContext.getInventoryList().getFirst().unsuitableReason());
        assertTrue(irradiationContext.getInventoryList().getFirst().unsuitableReason().equalsIgnoreCase(unsuitableReason));
    }

    @Then("I verify that product {string} in the unit {string} is flagged as expired")
    public void iVerifyThatProductInTheUnitIsFlaggedAsExpired(String productCode, String unitNumber) {
        assertEquals(irradiationContext.getInventoryList().getFirst().productCode(), productCode);
        assertEquals(irradiationContext.getInventoryList().getFirst().unitNumber(), unitNumber);
        assertTrue(irradiationContext.getInventoryList().getFirst().expired());
    }
}
