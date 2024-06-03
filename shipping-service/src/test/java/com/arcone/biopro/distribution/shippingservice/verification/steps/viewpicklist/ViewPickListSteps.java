package com.arcone.biopro.distribution.shippingservice.verification.steps.viewpicklist;

import com.arcone.biopro.distribution.shippingservice.verification.pages.distribution.HomePage;
import com.arcone.biopro.distribution.shippingservice.verification.pages.distribution.LoginPage;
import com.arcone.biopro.distribution.shippingservice.verification.pages.distribution.ShipmentDetailPage;
import com.arcone.biopro.distribution.shippingservice.verification.pages.distribution.ViewPickListPage;
import com.arcone.biopro.distribution.shippingservice.verification.support.Controllers.ShipmentTestingController;
import com.arcone.biopro.distribution.shippingservice.verification.support.Types.ShipmentRequestDetailsResponseType;
import graphql.Assert;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.annotation.AfterTestExecution;

import java.time.LocalDate;

@Slf4j
public class ViewPickListSteps {

    @Autowired
    private ViewPickListPage viewPickListPage;

    @Autowired
    private LoginPage loginPage;

    @Autowired
    private ShipmentDetailPage shipmentDetailPage;

    private ShipmentRequestDetailsResponseType shipmentDetailType;

    @Autowired
    private ShipmentTestingController shipmentTestingController;

    private Long shipmentId;

    @Autowired
    private HomePage homePage;

    @Autowired
    private WebDriver driver;

    @Given("The shipment details are order Number {string} , customer ID {string} , Customer Name {string} , Product Details : Quantities {string} , Blood Types : {string} , Product Families {string}.")
    public void buildOrderFulfilmentRequest(String orderNumber, String customerId , String customerName
        , String quantities , String bloodTypes , String productFamilies) throws  Exception{

        this.shipmentDetailType = setupOrderFulfillmentRequest(orderNumber, customerId, customerName, quantities, bloodTypes, productFamilies,null,null);

        Assert.assertNotNull(this.shipmentDetailType);

    }

    private ShipmentRequestDetailsResponseType setupOrderFulfillmentRequest(String orderNumber, String customerId, String customerName, String quantities, String bloodTypes
        , String productFamilies , String unitNumbers , String productCodes) {
        return shipmentTestingController.buildShipmentRequestDetailsResponseType(Long.valueOf(orderNumber),
            "ASAP",
            "OPEN",
            Long.valueOf(customerId),
            0L,
            1L,
            "TEST",
            "TEST",
            "Frozen",
            LocalDate.now(),
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
            productFamilies, unitNumbers , productCodes);


    }

    @And("I have received a shipment fulfillment request with above details.")
    public void triggerOrderFulFillmentEvent() throws Exception{
        var orderNumber = shipmentTestingController.createShippingRequest(this.shipmentDetailType);
        Assert.assertNotNull(orderNumber);
    }

    @And("I am on the Shipment Fulfillment Details page.")
    public void goToShipmentDetailsPage() throws Exception{

        var orders = shipmentTestingController.parseShipmentList(shipmentTestingController.listShipments());
        var orderFilter = orders.stream().filter(x -> x.getOrderNumber().equals(this.shipmentDetailType.getOrderNumber())).findAny().orElse(null);
        if (orderFilter != null) {
            this.shipmentId = orderFilter.getId();
            log.info("Found Shipment by Order Number");
        }
        homePage.goTo();
        this.shipmentDetailPage.goTo(this.shipmentId);
    }

    @When("I choose to view the Pick List.")
    public void whenIChooseViewPickList(){
        shipmentDetailPage.openViewPickListModal();
    }

    @Then("I am able to view the correct Order Details.")
    public void matchOrderDetails(){
        var shipmentDetails = this.viewPickListPage.getShipmentDetailsTableContent();
        Assert.assertNotNull(shipmentDetails);
        Assert.assertTrue(this.shipmentDetailType.getOrderNumber().equals(Long.valueOf(shipmentDetails.get("orderNumber"))));
        Assert.assertTrue(this.shipmentDetailType.getShippingCustomerCode().equals(Long.valueOf(shipmentDetails.get("customerId"))));
        Assert.assertTrue(this.shipmentDetailType.getShippingCustomerName().equals(shipmentDetails.get("customerName")));
    }

    @And("I am able to view the correct Shipment Details.")
    public void matchProductDetails(){
        var productDetails = this.viewPickListPage.getProductDetailsTableContent();
        log.info("productDetails {}", this.shipmentDetailType.getItems());
        log.info("Map Details {}", productDetails);
        Assert.assertNotNull(productDetails);
        if(this.shipmentDetailType.getItems() != null && !this.shipmentDetailType.getItems().isEmpty()){
            this.shipmentDetailType.getItems().forEach(item -> {
                var mapKey = item.getQuantity()+":"+item.getProductFamily()+":"+item.getBloodType();
                log.info("comparing key {}", mapKey);
                Assert.assertNotNull(productDetails.get(mapKey));
            });
        }
    }

    @Given("The shipment details are order Number {string} , customer ID {string} , Customer Name {string} , Product Details : Quantities {string} , Blood Types : {string} , Product Families {string} , Short Date Products {string} , Product Code {string}.")
    public void buildOrderFulfilmentRequestWithShortDate(String orderNumber, String customerId , String customerName
        , String quantities , String bloodTypes , String productFamilies , String unitNumbers , String productCodes) throws  Exception{

       this.shipmentDetailType =  setupOrderFulfillmentRequest(orderNumber, customerId , customerName
            , quantities ,  bloodTypes ,  productFamilies ,  unitNumbers ,  productCodes);

        Assert.assertNotNull(shipmentDetailType);
    }

    @And("I am able to view the correct Shipment Details with short date products.")
    public void matchProductDetailsWithShortDate(){
        var productDetails = this.viewPickListPage.getProductDetailsTableContent();
        var shortDateDetails = this.viewPickListPage.getShortDateProductDetailsTableContent();

        log.info("productDetails {}", this.shipmentDetailType.getItems());
        log.info("Map Details {}", productDetails);
        log.info("Short Date Map Details {}", shortDateDetails);
        Assert.assertNotNull(productDetails);
        if(this.shipmentDetailType.getItems() != null && !this.shipmentDetailType.getItems().isEmpty()){
            this.shipmentDetailType.getItems().forEach(item -> {
                var mapKey = item.getQuantity()+":"+item.getProductFamily()+":"+item.getBloodType();
                log.info("comparing key {}", mapKey);
                Assert.assertNotNull(productDetails.get(mapKey));
                if(item.getShortDateProducts() != null && !item.getShortDateProducts().isEmpty()){
                    item.getShortDateProducts().forEach(shortDateItem -> {
                        var mapShortDateKey = shortDateItem.getUnitNumber()+":"+shortDateItem.getProductCode()+":"+item.getBloodType();
                        log.info("Comparing Short Date key {}", mapShortDateKey);
                        Assert.assertNotNull(shortDateDetails.get(mapShortDateKey));
                    });
                }
            });
        }
    }

    @And("I should see a message {string} indicating There are no suggested short-dated products.")
    public void matchNoShortDateProductsMessage(String message){
        Assert.assertTrue(message.equals(this.viewPickListPage.getNoShortDateMessageContent()));
    }

}
