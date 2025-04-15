package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages.AddCartonPage;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Slf4j
public class AddCartonProductsSteps {

    @Autowired
    private AddCartonPage addCartonPage;

    @Then("I should be redirected to the Add Carton Products page.")
    public void iShouldBeRedirectedToTheAddCartonProductsPage() {
        addCartonPage.waitForLoad();
    }

    @And("I should see the carton details:")
    public void iShouldSeeTheCartonDetails(DataTable dataTable) {
        Map<String, String> table = dataTable.asMap(String.class, String.class);
        addCartonPage.verifyCartonDetails(table);
    }

    @When("I click to go back to Shipment Details page.")
    public void iClickToGoBackToShipmentDetailsPage() {
        addCartonPage.clickBack();
    }
}
