package com.arcone.biopro.distribution.receiving.verification.steps;

import com.arcone.biopro.distribution.receiving.verification.controllers.ImportProductsController;
import com.arcone.biopro.distribution.receiving.verification.pages.EnterShippingInformationPage;
import com.arcone.biopro.distribution.receiving.verification.pages.ProductInformationPage;
import com.arcone.biopro.distribution.receiving.verification.support.TestUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.openqa.selenium.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@Slf4j
public class ImportProductSteps {

    @Autowired
    private ImportProductsController importProductsController;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private EnterShippingInformationPage enterShippingInformationPage;

    @Autowired
    private ProductInformationPage productInformationPage;

    @Value("${default.employee.id}")
    private String employeeId;

    private Map apiResponse;
    private boolean isTemperatureValid;
    private boolean isTransitTimeValid;

    @Given("I request to enter shipping data for a {string} product category and location code {string}.")
    public void iRequestToEnterShippingDataForAProductCategory(String temperatureCategory , String locationCode) {
        this.apiResponse = importProductsController.enterShippingInformation(temperatureCategory , locationCode);
        Assert.assertNotNull(apiResponse);
    }


    @Then("I should be able to enter information for the following attributes: {string}.")
    public void iShouldBeAbleToEnterInformationForTheFollowingAttributes(String shippingInformationAttributes) {
        Assert.assertNotNull(apiResponse);
        var data = (Map) apiResponse.get("data");
        String[] shippingAttributes = testUtils.getCommaSeparatedList(shippingInformationAttributes);
        for (int i = 0; i < shippingAttributes.length; i++) {

            var attributeArray = shippingAttributes[i].split(":");
            var attribute = attributeArray[0];
            var attributeValue = attributeArray[1];
            Assert.assertEquals(attributeValue, data.get(attribute).toString());
        }

    }

    @Given("I am at the Enter Shipping Information Page.")
    public void iAmAtTheEnterShippingInformationPage() throws InterruptedException {
        enterShippingInformationPage.navigateToEnterShippingInformation();
    }

    @When("I select to enter information for a {string} product category.")
    public void iSelectToEnterInformationForAProductCategory(String temperatureCategory) {
        enterShippingInformationPage.selectTemperatureCategory(temperatureCategory);

    }

    @Then("I {string} be able to fill the following fields: {string}.")
    public void iShouldBeAbleToFillTheFollowingFields(String shouldShouldNot, String fields) {
        String[] shippingInformationFields = testUtils.getCommaSeparatedList(fields);
        var visible = "should".equals(shouldShouldNot);
        for (String attribute : shippingInformationFields) {
            if (!attribute.isBlank()) {
                enterShippingInformationPage.setRandomFormValue(attribute, visible);
            }
        }
    }

    @Then("The temperature field should be {string}.")
    public void theTemperatureFieldShouldBe(String enabledDisabled) {
        if ("enabled".equals(enabledDisabled)) {
            enterShippingInformationPage.waitForTemperatureFieldToBeEnabled();
            Assert.assertTrue(enterShippingInformationPage.isTemperatureFieldEnabled());
        } else if ("disabled".equals(enabledDisabled)) {
            Assert.assertFalse(enterShippingInformationPage.isTemperatureFieldEnabled());
        } else {
            Assert.fail("The temperature field should be enabled or disabled");
        }
    }

    @When("I enter thermometer ID {string}.")
    public void iEnterThermometerID(String thermometerId) throws InterruptedException {
        enterShippingInformationPage.enterThermometerId(thermometerId);
    }

    @When("I request to validate the temperature of {string} for the Temperature Category {string}.")
    public void iRequestToValidateTheTemperatureOfForTheTemperatureCategory(String temperatureValue, String temperatureCategory) {
        isTemperatureValid = importProductsController.isTemperatureValid(temperatureCategory, temperatureValue);
    }

    @Then("The system {string} accept the temperature.")
    public void theSystemAcceptTheTemperature(String shouldShouldNot) {
        if ("should".equalsIgnoreCase(shouldShouldNot)) {
            Assert.assertTrue(isTemperatureValid);
        } else if ("should not".equalsIgnoreCase(shouldShouldNot)) {
            Assert.assertFalse(isTemperatureValid);
        } else {
            Assert.fail("Invalid value for should/ShouldNot");
        }
    }

    @When("I enter the temperature {string}.")
    public void iEnterTheTemperature(String temperatureValue) throws InterruptedException {
        enterShippingInformationPage.setTemperature(temperatureValue);
    }

    @Then("The continue option should be {string}.")
    public void theContinueOptionShouldBe(String enabledDisabled) {
        if ("enabled".equals(enabledDisabled)) {
            enterShippingInformationPage.waitForContinueButtonToBeEnabled();
            Assert.assertTrue(enterShippingInformationPage.isContinueButtonEnabled());
        } else if ("disabled".equals(enabledDisabled)) {
            Assert.assertFalse(enterShippingInformationPage.isContinueButtonEnabled());
        } else {
            Assert.fail("The continue button should be enabled or disabled");
        }
    }

    @When("I request to validate the total transit time of Stat date time as {string}, Start Time Zone as {string}, End date time as {string} and End Time Zone as {string}  for the Temperature Category {string}.")
    public void validateTransitTime(String startDateTime, String startTimeZone, String endDateTime, String endTimeZone, String temperatureCategory) {
        isTransitTimeValid = importProductsController.isTotalTransitTimeValid(temperatureCategory, startDateTime, startTimeZone, endDateTime, endTimeZone);
    }

    @Then("The system {string} accept the transit time.")
    public void theSystemShouldAcceptTheTransitTime(String shouldShouldNot) {
        if ("should".equalsIgnoreCase(shouldShouldNot)) {
            Assert.assertTrue(isTransitTimeValid);
        } else if ("should not".equalsIgnoreCase(shouldShouldNot)) {
            Assert.assertFalse(isTransitTimeValid);
        } else {
            Assert.fail("Invalid value for should/ShouldNot");
        }
    }

    @And("I should receive the total transit time as {string}.")
    public void iShouldReceiveTheTotalTransitTimeAs(String totalTransitTime) {
        String actualTotalTransitTime = importProductsController.getTotalTransitTime();
        log.debug("Expecting total transit time to be: {}. Received {}", totalTransitTime, actualTotalTransitTime);
        Assert.assertEquals(totalTransitTime, actualTotalTransitTime);
    }

    @Then("The end time zone field should be pre defined as {string}.")
    public void theEndTimeZoneFieldShouldBePreDefinedAs(String tz) {
        enterShippingInformationPage.verifyDefaultTzIs(tz);
    }

    @And("I enter the Stat date time as {string}, Start Time Zone as {string}, End date time as {string}.")
    public void iEnterTheStatDateTimeAsStartTimeZoneAsEndDateTimeAs(String startDateTime, String startTz, String endDateTime) {
        String startDate = startDateTime.split(" ")[0];
        String startTime = startDateTime.split(" ")[1];
        String amPm = startDateTime.split(" ")[2];

        enterShippingInformationPage.setStartTransitDate(startDate);
        enterShippingInformationPage.setStartTransitTime(startTime + " " + amPm);
        enterShippingInformationPage.selectStartTransitTimeZone(true, startTz);

        String endDate = endDateTime.split(" ")[0];
        String endTime = endDateTime.split(" ")[1];
        String amPmEnd = endDateTime.split(" ")[2];

        enterShippingInformationPage.setEndTransitDate(endDate);
        enterShippingInformationPage.setEndTransitTime(endTime + " " + amPmEnd);
    }

    @When("I choose calculate total transit time.")
    public void iChooseCalculateTotalTransitTime() {
        enterShippingInformationPage.pressEnter();
    }

    @And("I {string} see the total transit time as {string}.")
    public void iSeeTheTotalTransitTimeAs(String shouldShouldNot, String totalTransitTime) {
        if ("should".equalsIgnoreCase(shouldShouldNot)) {
            enterShippingInformationPage.verifyTotalTransitTimeVisibilityIs(true, totalTransitTime);
        } else if ("should not".equalsIgnoreCase(shouldShouldNot)) {
            enterShippingInformationPage.verifyTotalTransitTimeVisibilityIs(false, null);
        } else {
            Assert.fail("Invalid value for should/ShouldNot");
        }
    }

    @And("I have an imported batch created with the following details:")
    public void iHaveAnImportedBatchCreatedWithTheFollowingDetails(DataTable dataTable) {
        var data = dataTable.asMap(String.class, String.class);
        importProductsController.createImportedBatch(
            data.get("temperatureCategory"),
            data.get("transitStartDateTime"),
            data.get("transitStartTimeZone"),
            data.get("transitEndDateTime"),
            data.get("transitEndTimeZone"),
            data.get("temperature"),
            data.get("thermometerCode"),
            data.get("locationCode"),
            data.get("comments"),
            employeeId
        );
    }

    @When("I request to enter the product information with Unit Number as {string} , Product Code as {string}, Blood Type as {string} Expiration date as {string} , License status as {string} and Visual Inspection as {string}.")
    public void iRequestToEnterTheProductInformationWithUnitNumberAsProductCodeAsBloodTypeAsExpirationDateAsLicenseStatusAsAndVisualInspectionAs(String unitNumber, String productCode, String bloodType, String expirationDate, String licenseStatus, String visualInspection) {
        importProductsController.createImportItem(unitNumber, productCode, bloodType, expirationDate, licenseStatus, visualInspection);
    }

    @Then("The product {string} {string} be added into list of added products.")
    public void theProductShouldBeAddIntoListOfAddedProducts(String unitNumber, String shouldShouldNot) {
        if ("should".equalsIgnoreCase(shouldShouldNot)) {
            Assert.assertTrue(importProductsController.isUnitImported(unitNumber));
        } else if ("should not".equalsIgnoreCase(shouldShouldNot)) {
            Assert.assertFalse(importProductsController.isUnitImported(unitNumber));
        } else {
            Assert.fail("Invalid value for should/ShouldNot");
        }
    }

    @And("The product {string} {string} be flagged for quarantine.")
    public void theProductShouldBeFlaggedForQuarantine(String unitNumber, String shouldShouldNot) {
        if ("should".equalsIgnoreCase(shouldShouldNot)) {
            Assert.assertTrue(importProductsController.isImportedUnitQuarantined(unitNumber));
        } else if ("should not".equalsIgnoreCase(shouldShouldNot)) {
            Assert.assertFalse(importProductsController.isImportedUnitQuarantined(unitNumber));
        } else {
            Assert.fail("Invalid value for should/ShouldNot");
        }
    }

    @And("I am at the Enter Product Information Page.")
    public void iAmAtTheEnterProductInformationPage() throws InterruptedException {
        productInformationPage.goTo();
        productInformationPage.waitForPageToLoad();
    }

    @And("I scan the product information with Unit Number as {string}, Product Code as {string}, Blood Type as {string}, and Expiration date as {string}.")
    public void iScanTheProductInformationWithUnitNumberAsProductCodeAsBloodTypeAsAndExpirationDateAs(String unitNumber, String productCode, String bloodType, String expirationDate) throws InterruptedException {
        try {
            productInformationPage.scanUnitNumber(unitNumber);
            productInformationPage.scanBloodType(bloodType);
            productInformationPage.scanProductCode(productCode);
            productInformationPage.setExpirationDate(expirationDate);
        } catch (TimeoutException e) {
            log.debug("Skipping the timeout failure to validate the toaster");
        }
    }

    @And("I select License status as {string}.")
    public void iSelectLicenseStatusAs(String licenseStatus) {
        productInformationPage.selectLicenseStatus(licenseStatus);
    }

    @And("I select Visual Inspection as {string}.")
    public void iSelectVisualInspectionAs(String inspectionStatus) {
        productInformationPage.selectVisualInspection(inspectionStatus);
    }

    @When("I choose to add a product.")
    public void iChooseToAddAProduct() {
        productInformationPage.addProduct();
    }

    @Then("I {string} see product unit number {string} and product code {string} in the list of added products.")
    public void iShouldSeeProductUnitNumberAndProductCodeInTheListOfAddedProducts(String shouldShouldNot, String unitNumber, String productCode) {
        if ("should".equalsIgnoreCase(shouldShouldNot)) {
        productInformationPage.verifyProductAdded(unitNumber, productCode, true);
        } else if ("should not".equalsIgnoreCase(shouldShouldNot)) {
        productInformationPage.verifyProductAdded(unitNumber, productCode, false);
        } else {
            Assert.fail("Invalid value for should/ShouldNot");
        }
    }

    @And("The add product option should be {string}.")
    public void theAddProductOptionShouldBe(String enabledDisabled) {
        if ("enabled".equals(enabledDisabled)) {
            Assert.assertTrue(productInformationPage.isAddProductButtonEnabled());
        } else if ("disabled".equals(enabledDisabled)) {
            Assert.assertFalse(productInformationPage.isAddProductButtonEnabled());
        } else {
            Assert.fail("The add product button should be enabled or disabled");
        }
    }
    public void theAddProductOptionShouldBeDisabled() {
    }

    @Given("I want to validate the product details printed in the label.")
    public void iWantToValidateTheProductDetailsPrintedInTheLabel() {

    }

    @When("I request to validate the scanned product information for the Temperature Category as {string} barcode type as {string} and the scanned value as {string}.")
    public void iScanTheProductInformationForTheTemperatureCategoryAsBarcodeTypeAsAndTheScannedValueAs(String temperatureCategory, String barcodePattern, String barcodeValue) {
        var response = importProductsController.validateBarcode(temperatureCategory,barcodePattern,barcodeValue);
        Assert.assertNotNull(response);
        this.apiResponse = (Map) response.get("data");
    }

    @Then("I should receive the barcode validation result as {string} and the result value should be {string} and result description as {string}.")
    public void iShouldReceiveTheBarcodeValidationResultAs(String isValid,String result, String resultDescription) {
        Assert.assertEquals(this.apiResponse.get("valid").toString(),isValid);
        if("true".equals(isValid)){
            Assert.assertEquals(this.apiResponse.get("result").toString(),result);
            var description = this.apiResponse.get("resultDescription");
            if(description != null){
                Assert.assertEquals(description,resultDescription);
            }
        }else{
            Assert.assertNull(this.apiResponse.get("result"));
            Assert.assertNull(this.apiResponse.get("resultDescription"));
        }
    }
    @And("Then system {string} return the result message as {string}.")
    public void thenSystemReturnTheResultMessageAs(String shouldShouldNot, String message) {
        if ("should".equalsIgnoreCase(shouldShouldNot)) {
            Assert.assertEquals(this.apiResponse.get("message").toString(),message);
        } else if ("should not".equalsIgnoreCase(shouldShouldNot)) {
            Assert.assertNull(this.apiResponse.get("message"));
        } else {
            Assert.fail("Invalid value for should/ShouldNot");
        }
    }

    @And("I have the products added in the batch with Unit Number as {string} , Product Code as {string}, Blood Type as {string}, Expiration date as {string}, License status as {string} and Visual Inspection as {string}.")
    public void iHaveTheProductsAddedInTheBatchWithUnitNumberAsProductCodeAsBloodTypeAsExpirationDateAsAndProductCategoryAsLicenseStatusAsAndVisualInspectionAs(String unitNumber, String productCode, String bloodType, String expirationDate, String licenseStatus, String visualInspection) {
        this.iRequestToEnterTheProductInformationWithUnitNumberAsProductCodeAsBloodTypeAsExpirationDateAsLicenseStatusAsAndVisualInspectionAs(unitNumber, productCode, bloodType, expirationDate, licenseStatus, visualInspection);
    }

    @When("I request to complete the last import batch created.")
    public void iRequestToCompleteTheLastImportBatchCreated() {
        var response = importProductsController.completeImport();
        Assert.assertNotNull(response);
    }

    @Then("The status of the import batch should be {string}")
    public void theStatusOfTheImportBatchShouldBe(String status) {
        Assert.assertEquals(importProductsController.getCompleteImportStatus(),status);
    }

    @And("The complete import process option should be {string}")
    public void theCompleteImportProcessOptionShouldBe(String enabledDisabled) {
        if ("enabled".equals(enabledDisabled)) {
            Assert.assertTrue(productInformationPage.isCompleteImportButtonEnabled(true));
        } else if ("disabled".equals(enabledDisabled)) {
            Assert.assertFalse(productInformationPage.isCompleteImportButtonEnabled(false));
        } else {
            Assert.fail("The completed import should be enabled or disabled");
        }
    }

    @When("I choose to complete the import process.")
    public void iChooseToCompleteTheImportProcess() {
        productInformationPage.completeImport();
    }

    @And("I should be redirect to the Enter Shipping Information Page.")
    public void iShouldBeRedirectToTheEnterShippingInformationPage() {
        enterShippingInformationPage.waitForLoad();
    }
}
