package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CreateShipmentController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages.AddCartonPage;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.TestUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Slf4j
public class AddCartonProductsSteps {

    @Autowired
    private AddCartonPage addCartonPage;
    @Autowired
    private CreateShipmentController createShipmentController;
    @Autowired
    private SharedContext sharedContext;
    @Autowired
    private TestUtils testUtils;

    @Given("I should be redirected to the Add Carton Products page.")
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

    @When("I fill an {string} product with the unit number {string}, product code {string} and product type {string}.")
    public void addProductToCarton(String productQuality, String unitNumber, String productCode, String productType) {
        String cartonId = sharedContext.getCreateCartonResponseList().getFirst().get("id").toString();
        createShipmentController.packCartonProduct(cartonId, unitNumber, productCode, sharedContext.getLocationCode());
    }

    @And("The product unit number {string} and product code {string} {string} be packed in the carton.")
    public void theProductUnitNumberAndProductCodeBePackedInTheCarton(String unitNumber, String productCode, String option) {
        if (option.equals("should")) {
            Assert.assertTrue(createShipmentController.verifyProductIsPacked(unitNumber, productCode));
        } else if (option.equals("should not")) {
            Assert.assertFalse(createShipmentController.verifyProductIsPacked(unitNumber, productCode));
        } else {
            Assert.fail("The option " + option + " is not valid.");
        }
    }

    @When("I pack a product with the unit number {string}, product code {string} and volume {string}.")
    public void iPackAProductWithTheUnitNumberProductCodeAndVolume(String unitNumber, String productCode, String productVolume) {
        addProductToCarton("", unitNumber, productCode, "");
    }

    @And("I navigate to the Add Carton Products page for the carton sequence number {int}.")
    public void iNavigateToTheAddCartonProductsPageForTheCartonNumber(int sequenceNumber) throws InterruptedException {
        String cartonId = sharedContext.getCreateCartonResponseList().get(sequenceNumber - 1).get("id").toString();
        addCartonPage.navigateToCarton(cartonId);
    }

    @When("I add an {string} product with the unit number {string}, product code {string} and product type {string}.")
    public void iFillAnAcceptableProductWithTheUnitNumberProductCodeAndProductType(String productQuality, String unitNumber, String productCode, String productType) {
        addCartonPage.addProduct(unitNumber, productCode);
    }

    @Then("I should see the product in the packed list with unit number {string} and product code {string}.")
    public void iShouldSeeTheProductInThePackedListWithUnitNumberAndProductCode(String unitNumber, String productCode) {
        Assert.assertTrue(addCartonPage.verifyProductIsPacked(testUtils.removeUnitNumberScanDigits(unitNumber), testUtils.removeProductCodeScanDigits(productCode)));
    }

    @And("I have packed the following products:")
    public void iHavePackedTheFollowingProducts(DataTable dataTable) {
        var headers = dataTable.row(0);
        String cartonId = sharedContext.getCreateCartonResponseList().getFirst().get("id").toString();

        for (int i = 1; i < dataTable.height(); i++) {
            var row = dataTable.row(i);
            createShipmentController.packCartonProduct(cartonId, row.get(headers.indexOf("unit_number")), row.get(headers.indexOf("product_code")), sharedContext.getLocationCode());
        }
    }

    @When("I pack a product with the unit number {string}, product code {string}.")
    public void iPackAProductWithTheUnitNumberProductCode(String unitNumber, String productCode) {
        String cartonId = sharedContext.getCreateCartonResponseList().getFirst().get("id").toString();
        createShipmentController.packCartonProduct(cartonId, unitNumber, productCode, sharedContext.getLocationCode());

    }

    @When("I choose to submit the carton.")
    public void iChooseToSubmitTheCarton() {
        addCartonPage.clickSubmit();
    }
}
