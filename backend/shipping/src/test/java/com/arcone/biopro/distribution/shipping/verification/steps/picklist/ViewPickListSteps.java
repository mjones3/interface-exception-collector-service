package com.arcone.biopro.distribution.shipping.verification.steps.picklist;

import com.arcone.biopro.distribution.shipping.verification.pages.distribution.HomePage;
import com.arcone.biopro.distribution.shipping.verification.pages.distribution.ShipmentDetailPage;
import com.arcone.biopro.distribution.shipping.verification.pages.distribution.ViewPickListPage;
import com.arcone.biopro.distribution.shipping.verification.support.ScreenshotService;
import com.arcone.biopro.distribution.shipping.verification.support.controllers.ShipmentTestingController;
import com.arcone.biopro.distribution.shipping.verification.support.types.ShipmentRequestDetailsResponseType;
import graphql.Assert;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@Slf4j
@SpringBootTest
public class ViewPickListSteps {

    @Autowired
    private ViewPickListPage viewPickListPage;

    @Autowired
    private ShipmentDetailPage shipmentDetailPage;

    private ShipmentRequestDetailsResponseType shipmentDetailType;

    @Autowired
    private ShipmentTestingController shipmentTestingController;

    @Autowired
    private HomePage homePage;

    @Autowired
    private ScreenshotService screenshot;

    @Value("${save.all.screenshots}")
    private boolean saveAllScreenshots;

    private long orderNumber;


    private ShipmentRequestDetailsResponseType setupOrderFulfillmentRequest(String orderNumber, String customerId, String customerName, String quantities, String bloodTypes
        , String productFamilies, String unitNumbers, String productCodes) {
        return shipmentTestingController.buildShipmentRequestDetailsResponseType(Long.valueOf(orderNumber),
            "ASAP",
            "OPEN",
            Long.valueOf(customerId),
            0L,
            "MDL_HUB_1",
            "TEST",
            "TEST",
            "Frozen",
            LocalDate.now(),
            "Blood Bank",
            customerName,
            "",
            "3056778756",
            "FL",
            "33016",
            "US",
            "1",
            "Miami",
            "Miami-Dade",
            "36544 SW 27th St",
            "North Miami",
            quantities,
            bloodTypes,
            productFamilies, unitNumbers, productCodes);


    }

    private void goToDetailsPage(long orderNumber) throws Exception {
        Long shipmentId = shipmentTestingController.getOrderShipmentId(orderNumber);
        homePage.goTo();
        this.shipmentDetailPage.goTo(shipmentId);
        screenshot.attachConditionalScreenshot(saveAllScreenshots);
    }

    @Given("The shipment details are order Number {string}, customer ID {string}, Customer Name {string}, Product Details: Quantities {string}, Blood Types: {string}, Product Families {string}.")
    public void buildOrderFulfilmentRequest(String orderNumber, String customerId, String customerName
        , String quantities, String bloodTypes, String productFamilies) {

        this.shipmentDetailType = setupOrderFulfillmentRequest(orderNumber, customerId, customerName, quantities, bloodTypes, productFamilies, null, null);

        Assert.assertNotNull(this.shipmentDetailType);

    }

    @And("I have received a shipment fulfillment request with above details.")
    public void triggerOrderFulfillmentEvent() throws Exception {
        this.orderNumber = shipmentTestingController.createShippingRequest(this.shipmentDetailType);
        Assert.assertNotNull(this.orderNumber);
    }

    @And("I am on the Shipment Fulfillment Details page.")
    public void goToShipmentDetailsPage() throws Exception {
        this.goToDetailsPage(this.orderNumber);
    }

    @And("I am on the Shipment Fulfillment Details page for order {int}.")
    public void goToShipmentDetailsPageByOrderNumber(long customOrderNumber) throws Exception {
        this.goToDetailsPage(customOrderNumber);
    }

    @And("I enter the Shipment Fulfillment Details page for order {int}.")
    public void enterShipmentDetailsPageByOrderNumber(long customOrderNumber) throws Exception {
        this.goToDetailsPage(customOrderNumber);
    }

    @When("I choose to view the Pick List.")
    public void whenIChooseViewPickList() {
        shipmentDetailPage.openViewPickListModal();
    }

    @Then("I am able to view the correct Order Details.")
    public void matchOrderDetails() {
        var shipmentDetails = this.viewPickListPage.getShipmentDetailsTableContent();
        screenshot.attachConditionalScreenshot(saveAllScreenshots);
        Assert.assertNotNull(shipmentDetails);
        Assert.assertTrue(this.shipmentDetailType.getOrderNumber().equals(Long.valueOf(shipmentDetails.get("orderNumber"))));
        Assert.assertTrue(this.shipmentDetailType.getShippingCustomerCode().equals(Long.valueOf(shipmentDetails.get("shippingCustomerCode"))));
        Assert.assertTrue(this.shipmentDetailType.getShippingCustomerName().equals(shipmentDetails.get("customerName")));
    }

    @And("I am able to view the correct Shipment Details for the {string} product.")
    public void matchProductDetails(String familyDescription) {
        var productDetails = this.viewPickListPage.getProductDetailsTableContent();
        log.info("productDetails {}", this.shipmentDetailType.getItems());
        log.info("Map Details {}", productDetails);
        screenshot.attachConditionalScreenshot(saveAllScreenshots);
        Assert.assertNotNull(productDetails);
        if (this.shipmentDetailType.getItems() != null && !this.shipmentDetailType.getItems().isEmpty()) {
            this.shipmentDetailType.getItems().forEach(item -> {
                var mapKey = item.getQuantity() + ":" + familyDescription + ":" + item.getBloodType();
                log.info("comparing key {}", mapKey);
                Assert.assertNotNull(productDetails.get(mapKey));
            });
        }
    }

    @Given("The shipment details are order Number {string}, customer ID {string}, Customer Name {string}, Product Details: Quantities {string}, Blood Types: {string}, Product Families {string}, Short Date Products {string}, Product Code {string}.")
    public void buildOrderFulfilmentRequestWithShortDate(String orderNumber, String customerId, String customerName
        , String quantities, String bloodTypes, String productFamilies, String unitNumbers, String productCodes) {

        this.shipmentDetailType = setupOrderFulfillmentRequest(orderNumber, customerId, customerName
            , quantities, bloodTypes, productFamilies, unitNumbers, productCodes);

        Assert.assertNotNull(shipmentDetailType);
    }

    @And("I am able to view the correct Shipment Details with short date products for the {string} family.")
    public void matchProductDetailsWithShortDate(String familyDescription) {
        var productDetails = this.viewPickListPage.getProductDetailsTableContent();
        var shortDateDetails = this.viewPickListPage.getShortDateProductDetailsTableContent();

        log.info("productDetails {}", this.shipmentDetailType.getItems());
        log.info("Map Details {}", productDetails);
        log.info("Short Date Map Details {}", shortDateDetails);
        screenshot.attachConditionalScreenshot(saveAllScreenshots);
        Assert.assertNotNull(productDetails);
        if (this.shipmentDetailType.getItems() != null && !this.shipmentDetailType.getItems().isEmpty()) {
            this.shipmentDetailType.getItems().forEach(item -> {
                var mapKey = item.getQuantity() + ":" + familyDescription + ":" + item.getBloodType();
                log.info("comparing key {}", mapKey);
                Assert.assertNotNull(productDetails.get(mapKey));
                if (item.getShortDateProducts() != null && !item.getShortDateProducts().isEmpty()) {
                    item.getShortDateProducts().forEach(shortDateItem -> {
                        var mapShortDateKey = shortDateItem.getUnitNumber() + ":" + shortDateItem.getProductCode() + ":" + item.getBloodType();
                        log.info("Comparing Short Date key {}", mapShortDateKey);
                        Assert.assertNotNull(shortDateDetails.get(mapShortDateKey));
                    });
                }
            });
        }
    }

    @And("I should see a message {string} indicating There are no suggested short-dated products.")
    public void matchNoShortDateProductsMessage(String message) {
        Assert.assertTrue(message.equals(this.viewPickListPage.getNoShortDateMessageContent()));
    }
}
