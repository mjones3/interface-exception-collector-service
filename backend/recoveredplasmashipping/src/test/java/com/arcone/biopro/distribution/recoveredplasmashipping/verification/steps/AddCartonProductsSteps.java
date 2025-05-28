package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CartonTestingController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CreateShipmentController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages.ManageCartonPage;
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
    private ManageCartonPage manageCartonPage;
    @Autowired
    private CreateShipmentController createShipmentController;
    @Autowired
    private SharedContext sharedContext;
    @Autowired
    private TestUtils testUtils;
    @Autowired
    private CartonTestingController cartonTestingController;

    @Given("I should be redirected to the Manage Carton Products page.")
    public void iShouldBeRedirectedToTheAddCartonProductsPage() {
        manageCartonPage.waitForLoad();
    }

    @And("I should see the carton details:")
    public void iShouldSeeTheCartonDetails(DataTable dataTable) {
        Map<String, String> table = dataTable.asMap(String.class, String.class);
        manageCartonPage.verifyCartonDetails(table);
    }

    @When("I fill/pack a(n) {string} product with the unit number {string}, product code {string} and product type {string}.")
    public void addProductToCarton(String productQuality, String unitNumber, String productCode, String productType) {
        String cartonId = sharedContext.getCreateCartonResponseList().getFirst().get("id").toString();
        cartonTestingController.packCartonProduct(cartonId, unitNumber, productCode, sharedContext.getLocationCode());
    }

    @And("The product unit number {string} and product code {string} {string} be packed in the carton.")
    public void theProductUnitNumberAndProductCodeBePackedInTheCarton(String unitNumber, String productCode, String option) {
        log.debug("PACKED PRODUCTS {}", sharedContext.getPackedProductsList());
        log.debug("CARTON Response {}", sharedContext.getLastCartonResponse());

        if (option.equals("should")) {
            Assert.assertTrue(cartonTestingController.checkProductIsPacked(unitNumber, productCode));
        } else if (option.equals("should not")) {
            Assert.assertFalse(cartonTestingController.checkProductIsPacked(unitNumber, productCode));
        } else {
            Assert.fail("The option " + option + " is not valid.");
        }
    }

    @And("The product unit number {string} and product code {string} {string} be verified in the carton.")
    public void theProductUnitNumberAndProductCodeBeVerifiedInTheCarton(String unitNumber, String productCode, String option) {
        if (option.equals("should")) {
            Assert.assertTrue(cartonTestingController.checkProductIsVerified(unitNumber, productCode));
        } else if (option.equals("should not")) {
            Assert.assertFalse(cartonTestingController.checkProductIsVerified(unitNumber, productCode));
        } else {
            Assert.fail("The option " + option + " is not valid.");
        }
    }

    @When("I pack a product with the unit number {string}, product code {string} and volume {string}.")
    public void iPackAProductWithTheUnitNumberProductCodeAndVolume(String unitNumber, String productCode, String productVolume) {
        addProductToCarton("", unitNumber, productCode, "");
    }

    @When("I pack a product with the unit number {string} and product code {string} into the carton sequence {int}.")
    public void iPackAProductInASpecificCarton(String unitNumber, String productCode, int cartonSequence) {
        String cartonId = sharedContext.getCreateCartonResponseList().get(cartonSequence - 1).get("id").toString();
        cartonTestingController.packCartonProduct(cartonId, unitNumber, productCode, sharedContext.getLocationCode());
    }

    @And("I navigate to the Add Carton Products page for the carton sequence number {int}.")
    public void iNavigateToTheAddCartonProductsPageForTheCartonNumber(int sequenceNumber) throws InterruptedException {
        String cartonId = sharedContext.getCreateCartonResponseList().get(sequenceNumber - 1).get("id").toString();
        manageCartonPage.navigateToCarton(cartonId);
    }

    @And("I navigate to the Manage Carton Products page for the carton sequence number {int}.")
    public void iNavigateToTheManageCartonProductsPageForTheCartonNumber(int sequenceNumber) throws InterruptedException {
        iNavigateToTheAddCartonProductsPageForTheCartonNumber(sequenceNumber);
    }

    @When("I add/scan an {string} product with the unit number {string}, product code {string} and product type {string}.")
    public void iFillAnAcceptableProductWithTheUnitNumberProductCodeAndProductType(String productQuality, String unitNumber, String productCode, String productType) throws InterruptedException {
        var productList = testUtils.getCommaSeparatedList(unitNumber);
        var productCodeList = testUtils.getCommaSeparatedList(productCode);
        Assert.assertEquals(productList.length, productCodeList.length);
        for (int i = 0; i < productList.length; i++) {
            manageCartonPage.addProduct(productList[i], productCodeList[i]);
            Thread.sleep(500);
        }
    }

    @When("I add/scan to verify an {string} product with the unit number {string}, product code {string} and product type {string}.")
    public void verifyAnAcceptableProductWithTheUnitNumberProductCodeAndProductType(String productQuality, String unitNumber, String productCode, String productType) throws InterruptedException {
        var productList = testUtils.getCommaSeparatedList(unitNumber);
        var productCodeList = testUtils.getCommaSeparatedList(productCode);
        Assert.assertEquals(productList.length, productCodeList.length);

        for (int i = 0; i < productList.length; i++) {
            manageCartonPage.verifyProduct(productList[i], productCodeList[i]);
            Thread.sleep(500);
        }
    }

    @Then("I should see the product in the packed/verified list with unit number {string} and product code {string}.")
    public void iShouldSeeTheProductInThePackedListWithUnitNumberAndProductCode(String unitNumber, String productCode) {
        var unitList = testUtils.getCommaSeparatedList(unitNumber);
        var productList = testUtils.getCommaSeparatedList(productCode);
        Assert.assertEquals(unitList.length, productList.length);

        for (var i = 0; i > productList.length; i++) {
            Assert.assertTrue(manageCartonPage.verifyProductIsPacked(testUtils.removeUnitNumberScanDigits(unitNumber), testUtils.removeProductCodeScanDigits(productCode)));
        }
    }

    @And("I have packed the following products:")
    public void iHavePackedTheFollowingProducts(DataTable dataTable) throws InterruptedException {
        var headers = dataTable.row(0);
        String cartonId = sharedContext.getCreateCartonResponseList().getFirst().get("id").toString();

        for (int i = 1; i < dataTable.height(); i++) {
            var row = dataTable.row(i);
            cartonTestingController.packCartonProduct(cartonId, row.get(headers.indexOf("unit_number")), row.get(headers.indexOf("product_code")), sharedContext.getLocationCode());
        }
    }

    @When("I pack a product with the unit number {string}, product code {string}.")
    public void iPackAProductWithTheUnitNumberProductCode(String unitNumber, String productCode) throws InterruptedException {
        String cartonId = sharedContext.getCreateCartonResponseList().getFirst().get("id").toString();
        cartonTestingController.packCartonProduct(cartonId, unitNumber, productCode, sharedContext.getLocationCode());

    }

    @When("I choose to navigate back to Shipment Details page.")
    public void iChooseToNavigateBack() {
        manageCartonPage.clickBackToShipmentDetails();
    }

    @And("I have the unit numbers {string}, product codes {string} and product types {string} packed which become unacceptable.")
    public void iHaveTheUnitNumbersProductCodesAndProductTypesPackedWhichBecomeUnsuitable(String unitNumbers, String productCodes, String productTypes) {
        String cartonId = sharedContext.getCreateCartonResponseList().getFirst().get("id").toString();
        String[] unitNumbersArray = testUtils.getCommaSeparatedList(unitNumbers);
        String[] productCodesArray = testUtils.getCommaSeparatedList(productCodes);
        String[] productTypesArray = testUtils.getCommaSeparatedList(productTypes);

        for (int i = 0; i < unitNumbersArray.length; i++) {
            cartonTestingController.insertPackedProduct(cartonId, unitNumbersArray[i], productCodesArray[i], productTypesArray[i]);
        }
    }
}
