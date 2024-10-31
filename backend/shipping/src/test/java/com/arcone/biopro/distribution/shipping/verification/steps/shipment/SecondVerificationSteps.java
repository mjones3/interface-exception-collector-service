package com.arcone.biopro.distribution.shipping.verification.steps.shipment;

import com.arcone.biopro.distribution.shipping.verification.pages.distribution.VerifyProductsPage;
import com.arcone.biopro.distribution.shipping.verification.support.ScreenshotService;
import com.arcone.biopro.distribution.shipping.verification.support.controllers.ShipmentTestingController;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
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
}
