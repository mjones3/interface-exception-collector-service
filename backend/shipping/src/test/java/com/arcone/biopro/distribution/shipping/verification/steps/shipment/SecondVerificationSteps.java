package com.arcone.biopro.distribution.shipping.verification.steps.shipment;

import com.arcone.biopro.distribution.shipping.verification.pages.distribution.HomePage;
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

import java.util.Arrays;
import java.util.List;

@Slf4j
@SpringBootTest
public class SecondVerificationSteps {

    private Long shipmentId;
    private String unitNumber;
    private String productCode;
    private Integer totalPacked = 0;
    private Integer totalVerified = 0;

    @Autowired
    ShipmentTestingController shipmentTestingController;

    @Autowired
    VerifyProductsPage verifyProductsPage;

    @Autowired
    private ScreenshotService screenshot;

    @Value("${save.all.screenshots}")
    private boolean saveAllScreenshots;

    @Autowired
    private HomePage homePage;

    @Given("I have a shipment for order {string} with the unit {string} and product code {string} packed.")
    public void createPackedShipment(String orderNumber, String unitNumber, String productCode){
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        this.shipmentId = shipmentTestingController.createPackedShipment(orderNumber, List.of(unitNumber),List.of(productCode));

        Assert.assertNotNull(this.shipmentId);
        this.totalPacked = 1;

    }

    @Given("I have a shipment for order {string} with the units {string} and product codes {string} packed.")
    public void createPackedShipmentMultipleUnits(String orderNumber, String unitNumbers, String productCodes){
        var units = Arrays.stream(unitNumbers.split(",")).toList();
        var productCodeList = Arrays.stream(productCodes.split(",")).toList();

        this.unitNumber = units.getFirst();
        this.productCode = productCodeList.getFirst();
        this.shipmentId = shipmentTestingController.createPackedShipment(orderNumber,units,productCodeList);

        Assert.assertNotNull(this.shipmentId);
        this.totalPacked = units.size();

    }

    @Given("I have a shipment for order {string} with the unit {string} and product code {string} verified.")
    public void createVerifiedShipment(String orderNumber, String unitNumber, String productCode){
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        this.shipmentId = shipmentTestingController.createPackedShipment(orderNumber, List.of(unitNumber),List.of(productCode));
        shipmentTestingController.verifyShipment(this.shipmentId);
    }

    @Then("I should be redirected to the verify products page.")
    public void shouldBeRedirectedToVerifyProductsPage() {
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
        this.totalVerified++;
    }

    @And("I should see the log of verified products being updated.")
    public void verifyLogInProgress() {
        String progress = verifyProductsPage.getProgressLog();
        var progressText = String.format("%s/%s",totalVerified,totalPacked);
        Assert.assertEquals(progress, progressText);
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

    @And("I am on the verify products page.")
    public void iAmOnTheVerifyProductsPage() throws InterruptedException {
        homePage.goTo();
        verifyProductsPage.goToPage(this.shipmentId.toString());
    }
}
