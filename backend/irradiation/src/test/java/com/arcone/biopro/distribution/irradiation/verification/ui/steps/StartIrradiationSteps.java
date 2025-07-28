package com.arcone.biopro.distribution.irradiation.verification.ui.steps;

import com.arcone.biopro.distribution.irradiation.verification.ui.pages.StartIrradiationPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.AllArgsConstructor;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;

import static org.junit.Assert.assertTrue;

@AllArgsConstructor
public class StartIrradiationSteps {
    private final StartIrradiationPage startIrradiationPage;

    @When("I scan the lot number {string}")
    public void iScanTheLotNumber(String lotnumber) {
        startIrradiationPage.scanLotNumber(lotnumber);
    }

    @Then("I verify the product {string} is displayed for selection")
    public void iVerifyTheProductIsDisplayedForSelection(String productCode) {
        assertTrue(startIrradiationPage.productIsDisplayedForSelection(productCode));
    }

    @When("I select the product {string}")
    public void iSelectTheProduct(String productCode) {
        startIrradiationPage.selectProductForIrradiation(productCode);
    }
}
