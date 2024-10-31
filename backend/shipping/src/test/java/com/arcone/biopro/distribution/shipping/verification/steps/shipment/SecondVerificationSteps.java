package com.arcone.biopro.distribution.shipping.verification.steps.shipment;

import com.arcone.biopro.distribution.shipping.verification.pages.distribution.VerifyProductsPage;
import com.arcone.biopro.distribution.shipping.verification.support.ScreenshotService;
import com.arcone.biopro.distribution.shipping.verification.support.controllers.ShipmentTestingController;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class SecondVerificationSteps {

    private Long shipmentId;
    private String unitNumber;
    private String productCode;

    @Autowired
    ShipmentTestingController shipmentTestingController;

    @Autowired
    VerifyProductsPage verifyProductsPage;

    @Autowired
    private ScreenshotService screenshot;

    @Value("${save.all.screenshots}")
    private boolean saveAllScreenshots;

    @Given("I have a shipment for order {string} with the unit {string} and product code {string} packed.")
    public void createPackedShipment(String orderNumber, String unitNumber, String productCode){
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        this.shipmentId = shipmentTestingController.createPackedShipment(orderNumber,unitNumber,productCode);

        Assert.assertNotNull(this.shipmentId);

    }

    @Then("I should be redirected to the verify products page.")
    public void shouldBeRedirectedToVerifyProductsPage(){
        verifyProductsPage.isPageOpen(this.shipmentId.toString());
    }

    @Then("I can see the Order Information Details and the Shipping Information Details.")
    public void checkPageContent(){
        verifyProductsPage.viewPageContent();
        screenshot.attachConditionalScreenshot(saveAllScreenshots);
    }

    @When("I scan the unit {string} with product code {string}.")
    public void scanUnitAndProduct(String unitNumber, String productCode) throws InterruptedException {
        verifyProductsPage.scanUnitAndProduct(unitNumber, productCode);
    }

    @Then("I should see the unit added to the verified products table.")
    public void checkVerifiedProductIsPresent() {
        Assert.assertTrue(verifyProductsPage.isProductVerified(unitNumber, productCode));
    }

    @And("I should see the log of verified products being updated.")
    public void verifyLogInProgress() {
        String progress = verifyProductsPage.getProgressLog();
        var progressValue = progress.split("/")[0];
        Assert.assertNotEquals("0", progressValue);
    }

    @And("The complete shipment option should be enabled.")
    public void theCompleteShipmentOptionShouldBeEnabled() {
        Assert.assertTrue(verifyProductsPage.isCompleteShipmentButtonEnabled());
    }

    @And("I should not see the unit added to the verified products table.")
    public void verifyProductsNotAdded() {
        Assert.assertTrue(verifyProductsPage.isProductNotVerified(unitNumber, productCode));
    }

    @And("The complete shipment option should not be enabled.")
    public void theCompleteShipmentOptionShouldNotBeEnabled() {
        Assert.assertTrue(verifyProductsPage.isCompleteShipmentButtonDisabled());
    }
}
