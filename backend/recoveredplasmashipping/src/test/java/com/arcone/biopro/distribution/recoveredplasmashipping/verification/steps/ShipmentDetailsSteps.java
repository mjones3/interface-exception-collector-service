package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.FilterShipmentsController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages.CreateShipmentPage;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages.ShipmentDetailsPage;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.TestUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class ShipmentDetailsSteps {

    @Autowired
    private ShipmentDetailsPage shipmentDetailsPage;
    @Autowired
    private CreateShipmentPage createShipmentPage;
    @Autowired
    private SharedContext sharedContext;
    @Autowired
    private FilterShipmentsController filterShipmentsController;
    @Autowired
    TestUtils utils;

    @When("I navigate to the shipment details page for the last shipment created.")
    public void iNavigateToTheShipmentDetailsPageForTheLastShipmentCreated() throws InterruptedException {
        shipmentDetailsPage.goTo(sharedContext.getShipmentCreateResponse().get("id").toString());
    }

    @Then("I should see the following shipment information:")
    public void iShouldSeeTheFollowingShipmentInformation(DataTable dataTable) {
        // Expected values
        Map<String, String> table = dataTable.asMap(String.class, String.class);

        // Parse keywords from feature data table
        var expectedDate = table.get("Shipment Date").equals("<tomorrow>")
            ? LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            : table.get("Shipment Date");

        // Verify
        Assert.assertTrue(shipmentDetailsPage.getShipmentNumber().contains(table.get("Shipment Number Prefix")));
        Assert.assertTrue(shipmentDetailsPage.getCustomerCode().equalsIgnoreCase(table.get("Customer Code")));
        Assert.assertTrue(shipmentDetailsPage.getCustomerName().equalsIgnoreCase(table.get("Customer Name")));
        Assert.assertTrue(shipmentDetailsPage.getProductType().equalsIgnoreCase(table.get("Product Type")));
        Assert.assertTrue(shipmentDetailsPage.getShipmentStatus().equalsIgnoreCase(table.get("Shipment Status")));
        Assert.assertTrue(shipmentDetailsPage.getShipmentDate().equalsIgnoreCase(expectedDate));
        Assert.assertTrue(shipmentDetailsPage.getTotalCartons().equalsIgnoreCase(table.get("Total Cartons")));
        Assert.assertTrue(shipmentDetailsPage.getTotalProducts().equalsIgnoreCase(table.get("Total Products")));
        Assert.assertTrue(shipmentDetailsPage.getTransportationNumber().equalsIgnoreCase(table.get("Transportation Ref. Number")));

    }

    @And("I should have an option to add a carton to the shipment.")
    public void iShouldHaveAnOptionToAddACartonToTheShipment() {
        Assert.assertTrue(shipmentDetailsPage.isAddCartonButtonVisible());
    }

    @And("It should be possible to navigate back to the Search page.")
    public void itShouldBePossibleToNavigateBackToTheSearchPage() {
        Assert.assertTrue(shipmentDetailsPage.isBackToSearchButtonVisible());
        shipmentDetailsPage.clickBackToSearchButton();
        createShipmentPage.waitForLoad();
    }

    @When("I request the last created shipment data.")
    public void iRequestTheLastCreatedShipmentData() {
        filterShipmentsController.findShipmentByIdAndLocation(sharedContext.getShipmentCreateResponse().get("id").toString(), sharedContext.getLocationCode());
    }

    @Then("The find shipment response should contain the following information:")
    public void theFindShipmentResponseShouldContainTheFollowingInformation(DataTable dataTable) {
        Map<String, String> table = dataTable.asMap(String.class, String.class);

        var shipmentResponse = sharedContext.getFindShipmentApiResponse();
        Assert.assertTrue(shipmentResponse.get("shipmentNumber").toString().contains(table.get("shipmentNumber")));
        Assert.assertEquals(shipmentResponse.get("locationCode"), table.get("locationCode"));
        Assert.assertEquals(shipmentResponse.get("customerCode"), table.get("customerCode"));
        Assert.assertEquals(shipmentResponse.get("customerName"), table.get("customerName"));
        Assert.assertEquals(shipmentResponse.get("productType"), table.get("productType"));
        Assert.assertEquals(shipmentResponse.get("status"), table.get("shipmentStatus"));
        Assert.assertEquals(shipmentResponse.get("transportationReferenceNumber"), table.get("transportationReferenceNumber"));
        Assert.assertEquals(Integer.parseInt(shipmentResponse.get("totalCartons").toString()), Integer.parseInt(table.get("totalCartons")));
        Assert.assertEquals(Boolean.getBoolean(shipmentResponse.get("canAddCartons").toString()), Boolean.getBoolean(table.get("canAddCartons")));
    }

    @When("I request to find the shipment {string} at location {string}.")
    public void iRequestToFindTheShipmentAtLocation(String shipmentId, String locationCode) {
        filterShipmentsController.findShipmentByIdAndLocation(shipmentId, locationCode);
    }

    @When("I choose to add a carton to the shipment.")
    public void iChooseToAddACartonToTheShipment() {
        shipmentDetailsPage.clickAddCarton();
    }

    @Then("The Add Carton button should be {string}.")
    public void theAddCartonButtonShouldBe(String enabledDisabled) {
        if (enabledDisabled.equalsIgnoreCase("enabled")){
            Assert.assertTrue(shipmentDetailsPage.isAddCartonButtonEnabled());
        } else if (enabledDisabled.equalsIgnoreCase("disabled")){
            Assert.assertFalse(shipmentDetailsPage.isAddCartonButtonEnabled());
        } else {
            Assert.fail("Wrong option for button enabledDisabled");
        }
    }

    @Then("The find shipment response should have the following information:")
    public void theFindShipmentResponseShouldHaveTheFollowingInformation(DataTable dataTable) {
        Map<String, String> table = dataTable.asMap(String.class, String.class);
        var shipmentResponse = sharedContext.getFindShipmentApiResponse();
        List<Map> cartonResponseList = (List<Map>) shipmentResponse.get("cartonList");
        Assert.assertEquals(table.get("Total Cartons"), shipmentResponse.get("totalCartons").toString());
        Assert.assertEquals(Integer.parseInt(table.get("Total Cartons")), cartonResponseList.size());

        AtomicReference<Integer> index = new AtomicReference<>(0);
        cartonResponseList.forEach(carton -> {
                Assert.assertTrue(carton.get("cartonNumber").toString().contains(utils.getCommaSeparatedList(table.get("Carton Number Prefix"))[index.get()]));
                Assert.assertEquals(carton.get("cartonSequence").toString(),utils.getCommaSeparatedList(table.get("Sequence Number"))[index.get()]);
                index.getAndSet(index.get() + 1);
            });
    }

    @Then("I should see the list of cartons added to the shipment containing:")
    public void iShouldSeeTheListOfCartonsAddedToTheShipmentContaining(DataTable dataTable) {
        var tableHeaders = dataTable.row(0);
        for (int i = 1; i < dataTable.height(); i++) {
            var row = dataTable.row(i);
            shipmentDetailsPage.verifyCartonIsListed(
                row.get(tableHeaders.indexOf("Carton Number Prefix")),
                row.get(tableHeaders.indexOf("Sequence")),
                row.get(tableHeaders.indexOf("Status")));
        }
    }
}
