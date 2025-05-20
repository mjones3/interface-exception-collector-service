package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CreateShipmentController;
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
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
    @Autowired
    private CreateShipmentController createShipmentController;

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
        Assert.assertTrue(shipmentDetailsPage.getTransportationNumber().equalsIgnoreCase(table.get("Transportation Ref. Number")));
        if(table.get("Carton Status") != null){
            var cartonDetailsSplit = table.get("Carton Status").split(",");
            shipmentDetailsPage.verifyCartonIsListed(cartonDetailsSplit[0],cartonDetailsSplit[1],cartonDetailsSplit[2]);
        }
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
        if (enabledDisabled.equalsIgnoreCase("enabled")) {
            Assert.assertTrue(shipmentDetailsPage.isAddCartonButtonEnabled());
        } else if (enabledDisabled.equalsIgnoreCase("disabled")) {
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

        if(table.get("Shipment Status")!=null){
            Assert.assertEquals(table.get("Shipment Status"), shipmentResponse.get("status").toString());
        }

        if(table.get("Shipment Number") != null){
            Assert.assertTrue(shipmentResponse.get("shipmentNumber").toString().contains(table.get("Shipment Number")));
        }

        if(table.get("Carton Number Prefix") != null){
            AtomicReference<Integer> index = new AtomicReference<>(0);
            cartonResponseList.forEach(carton -> {
                Assert.assertTrue(carton.get("cartonNumber").toString().contains(utils.getCommaSeparatedList(table.get("Carton Number Prefix"))[index.get()]));
                Assert.assertEquals(carton.get("cartonSequence").toString(),utils.getCommaSeparatedList(table.get("Sequence Number"))[index.get()]);
                if(table.get("Carton Status") != null){
                    Assert.assertEquals(carton.get("status").toString(),utils.getCommaSeparatedList(table.get("Carton Status"))[index.get()]);
                }
                index.getAndSet(index.get() + 1);
            });
        }
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

    @Then("I should see a list of all cartons.")
    public void iShouldSeeAListOfAllCartons() {
        shipmentDetailsPage.verifyCartonsAreVisible(sharedContext.getCreateCartonResponseList());
    }

    @When("I choose to expand the row for the carton sequence number {string}.")
    public void iChooseToExpandTheRowForTheCartonSequenceNumber(String sequenceNumber) {
        shipmentDetailsPage.clickExpandCarton(sequenceNumber);
    }

    @And("I should see the unit(s) {string} added to the carton sequence {string}.")
    public void iShouldSeeTheUnitAddedToTheCartonSequence(String unitNumber, String cartonSequence) {
        shipmentDetailsPage.verifyUnitsAreListed(cartonSequence, unitNumber);
    }

    @And("I should see the total number of products as {string}.")
    public void iShouldSeeTheTotalNumberOfProductsAs(String totalProducts) {
        Assert.assertEquals(totalProducts, shipmentDetailsPage.getTotalProducts());
    }

    @When("I request to print the Unacceptable Products Report.")
    public void iRequestToPrintTheUnacceptableProductsReport() {
        createShipmentController.printUnacceptableUnitsReport(sharedContext.getShipmentCreateResponse().get("id").toString(), sharedContext.getShipmentCreateResponse().get("locationCode").toString());
        Assertions.assertNotNull(sharedContext.getLastUnacceptableUnitsReportResponse());
    }

    @Then("The Unacceptable Products Report status should be {string}")
    public void theUnacceptableProductsReportStatusShouldBe(String status) {
        Assertions.assertEquals(sharedContext.getFindShipmentApiResponse().get("unsuitableUnitReportDocumentStatus").toString(), status);
    }

    @And("The Unacceptable Products Report should contain:")
    public void theUnacceptableProductsReportShouldContain(DataTable dataTable) {
        Assertions.assertNotNull(dataTable);
        Map<String, String> table = dataTable.asMap(String.class, String.class);
        var reportResponse = sharedContext.getLastUnacceptableUnitsReportResponse();

        var products = (List) reportResponse.get("failedProducts");

        if(table.get("Shipment Number Prefix") != null){
            Assert.assertTrue(reportResponse.get("shipmentNumber").toString().contains(table.get("Shipment Number Prefix")));
        }

        if(table.get("Unit Number") != null){
            Assert.assertEquals(products.stream().map(s -> ((LinkedHashMap) s ).get("unitNumber")).sorted().collect(Collectors.joining(",")),table.get("Unit Number"));
        }

        if(table.get("Product Code") != null){
            Assert.assertEquals(products.stream().map(s -> ((LinkedHashMap) s ).get("productCode")).collect(Collectors.joining(",")),table.get("Product Code"));
        }

       /* if(table.get("Carton Number Prefix") != null){
            Assert.assertEquals(products.stream().map(s -> ((LinkedHashMap) s ).get("cartonNumber")).collect(Collectors.joining(",")),table.get("Carton Number Prefix"));
        }*/

        if(table.get("Carton Sequence") != null){
            Assert.assertTrue(products.stream().map(s -> ((LinkedHashMap) s ).get("cartonSequenceNumber").toString()).collect(Collectors.joining(",")).toString().contains(table.get("Carton Sequence")));
        }

        if(table.get("Reason for Failure") != null){
            Assert.assertEquals(products.stream().map(s -> ((LinkedHashMap) s ).get("failureReason")).sorted().collect(Collectors.joining(",")),table.get("Reason for Failure"));
        }
    }

    @Then("I should a message {string} indicating there are not unacceptable products in the shipment.")
    public void iShouldSeeAMessageIndicatingThereAreNotUnacceptableProductsInTheShipment(String message) {
        var reportResponse = sharedContext.getLastUnacceptableUnitsReportResponse();

        var products = (List) reportResponse.get("failedProducts");

        Assert.assertTrue(products.isEmpty());

        Assert.assertEquals(reportResponse.get("noProductsFlaggedMessage"),message);

    }

    @And("I should see the unacceptable units report information:")
    public void iShouldSeeTheUnacceptableUnitsReportInformation(DataTable dataTable) {

        Assertions.assertNotNull(dataTable);
        Map<String, String> table = dataTable.asMap(String.class, String.class);

        if(table.get("Last Run") != null){
            var expectedDate = table.get("Last Run").equals("<today>")
                ? LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                : table.get("Last Run");

            var pageDateSplit = shipmentDetailsPage.getLastUnacceptableRunDate().split(" ");
            Assert.assertEquals(pageDateSplit[0],expectedDate);
        }

        if(table.get("View  Icon") != null){
            if(table.get("View  Icon").equalsIgnoreCase("enabled")){
                Assert.assertTrue(shipmentDetailsPage.isUnacceptableReportButtonEnabled());
            }else if(table.get("View  Icon").equalsIgnoreCase("disabled")){
                Assert.assertFalse(shipmentDetailsPage.isUnacceptableReportButtonEnabled());
            }
        }

    }

    @When("I choose to open the unacceptable units report.")
    public void iChooseToOpenTheUnacceptableUnitsReport() {
        shipmentDetailsPage.clickUnacceptableReportButton();
    }

    @Then("I should see the following unacceptable units report information:")
    public void iShouldSeeTheFollowingUnacceptableUnitsReportInformation(DataTable dataTable) {
        Assertions.assertNotNull(dataTable);
        Map<String, String> table = dataTable.asMap(String.class, String.class);

        shipmentDetailsPage.verifyUnacceptableProductsReportIsVisible();

        if(table.get("Shipment Number Prefix") != null){
            Assert.assertTrue(shipmentDetailsPage.getUnacceptableTableHeader().contains(table.get("Shipment Number Prefix")));
        }

    }

    @And("I should see the following rows in the units report information:")
    public void iShouldSeeTheFollowingRowsInTheUnitsReportInformation(DataTable dataTable) {
        var tableHeaders = dataTable.row(0);
        for (int i = 1; i < dataTable.height(); i++) {
            var row = dataTable.row(i);
            var line = row.get(tableHeaders.indexOf("Row Content")).split(",");
            log.debug("checking the report row line {}",row.get(tableHeaders.indexOf("Row Number")));
            log.debug("checking the report row content {}",line);
            shipmentDetailsPage.verifyProductIsListed(line[0],line[1],line[2],line[3],line[4]);
        }
    }
}
