package com.arcone.biopro.distribution.shippingservice.verification.steps.shipment;

import com.arcone.biopro.distribution.shippingservice.verification.pages.distribution.HomePage;
import com.arcone.biopro.distribution.shippingservice.verification.pages.distribution.ShipmentDetailPage;
import com.arcone.biopro.distribution.shippingservice.verification.support.ScreenshotService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;


@Slf4j
@SpringBootTest
public class ShipmentDetailPageSteps {

    @Autowired
    private ShipmentDetailPage shipmentDetailPage;

    @Autowired
    private ScreenshotService screenshot;

    @Value("${save.all.screenshots}")
    private boolean saveAllScreenshots;

    @Then("I should have an option to view the Pick List.")
    public void viewPickList() {
        shipmentDetailPage.viewPickListButton();
        screenshot.attachConditionalScreenshot(saveAllScreenshots);
    }

    @Then("I should have an option to fill the products in the shipment.")
    public void viewFillProduct() {
        shipmentDetailPage.viewFillProduct();

    screenshot.attachConditionalScreenshot(saveAllScreenshots);}

    @Then("I should not be able to see any product shipped details.")
    public void viewProductShippingDetails() {
        shipmentDetailPage.viewProductShippingDetails();
        screenshot.attachConditionalScreenshot(saveAllScreenshots);
    }

    @Then("I should see zero products are filled out of the total number of products to be filled.")
    public void viewAmountOfProductsFilled() throws Exception {
        shipmentDetailPage.viewAmountOfProductsFilled();
        screenshot.attachConditionalScreenshot(saveAllScreenshots);
        // TODO change this from percentage to amount of products filled once selector is added
    }

    @Then("I can see the Order Information, the Shipping Information, and Order Criteria.")
    public void checkPageContent(){
        shipmentDetailPage.viewPageContent();
        screenshot.attachConditionalScreenshot(saveAllScreenshots);
    }

    @And("no products have been filled.")
    public void checkNoProductsAreFilled() {
        // TODO there is no option to validate this now, update with fill order implementation
    }

    @And("no products have been shipped.")
    public void checkNoProductAreShipped() {
        // TODO there is no option to validate this now, update with fill order implementation
    }
}
