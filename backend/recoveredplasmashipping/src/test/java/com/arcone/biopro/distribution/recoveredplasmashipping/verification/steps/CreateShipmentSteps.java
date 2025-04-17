package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CreateShipmentController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages.CreateShipmentPage;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.DatabaseQueries;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.DatabaseService;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
public class CreateShipmentSteps {

    @Autowired
    private CreateShipmentPage createShipmentPage;
    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private CreateShipmentController createShipmentController;
    @Autowired
    private SharedContext sharedContext;
    @Autowired
    private TestUtils testUtils;

    @Given("I have removed from the database all shipments which code contains with {string}.")
    public void removeShipmentsFromDatabase(String code) {
        // Delete carton items
        var deleteCartonItemsQuery = DatabaseQueries.DELETE_CARTON_ITEMS_BY_SHIPMENT_CODE(code);
        databaseService.executeSql(deleteCartonItemsQuery).block();
        // Delete cartons
        var deleteCartonsQuery = DatabaseQueries.DELETE_CARTONS_BY_SHIPMENT_CODE(code);
        databaseService.executeSql(deleteCartonsQuery).block();
        log.info("Removing cartons from shipments containing code: {}", code);
        // Delete Shipments
        var deleteShipmentsQuery = DatabaseQueries.DELETE_SHIPMENTS_BY_CODE(code);
        databaseService.executeSql(deleteShipmentsQuery).block();
        log.info("Removing shipments containing code: {}", code);
    }

    @Given("I am on the Shipment Create/List Page.")
    public void navigateToShipmentCreatePage() throws InterruptedException {
        createShipmentPage.goTo();
    }

    @When("I choose to create a shipment.")
    public void initiateShipmentCreation() {
        createShipmentPage.clickCreateShipment();
        log.info("Initiating shipment creation");
    }

    @And("I have entered all the fields:")
    public void enterShipmentFields(DataTable dataTable) throws InterruptedException {
        Thread.sleep(1000);
        Map<String, String> fields = dataTable.asMap(String.class, String.class);

        String customer = fields.get("Customer");
        createShipmentPage.selectCustomer(customer);


        String cartonTareWeight = fields.get("Carton Tare Weight");
        createShipmentPage.setCartonTareWeight(cartonTareWeight);

        String shipmentDate = fields.get("Shipment Date");
        if (shipmentDate.equals("<tomorrow>")) {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            shipmentDate = tomorrow.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            createShipmentPage.setShipmentDate(shipmentDate);
        }
        String productType = fields.get("Product Type");
        createShipmentPage.selectProductType(productType);

        String transportationRefNumber = fields.get("Transportation Reference Number");
        createShipmentPage.setTransportationRefNumber(transportationRefNumber);
    }

    @When("I choose to submit the shipment.")
    public void submitShipment() {
        createShipmentPage.submitShipment();
    }

    @And("I should be redirected to the Shipment Details page.")
    public void verifyRedirectToShipmentDetails() {
        // Implementation will be added in the DIS-334 story
        log.info("Verifying redirect to Shipment Details page");
    }

    @When("I request to create a new shipment with the values:")
    public void iRequestToCreateANewShipmentWithTheValues(DataTable dataTable) {
        Map<String, String> fields = dataTable.asMap(String.class, String.class);


        String shipmentDate = fields.get("Shipment Date");
        shipmentDate = testUtils.parseDataKeyword(shipmentDate);

        String transportationRefNumber = fields.get("Transportation Reference Number");
        if (transportationRefNumber.equals("<null>")) {
            transportationRefNumber = null;
        } else {
            transportationRefNumber = "\"" + transportationRefNumber + "\"";
        }

        sharedContext.setLocationCode(fields.get("Location Code"));

        createShipmentController.createShipment(
            "\"" + fields.get("Customer Code") + "\"",
            "\"" + fields.get("Product Type") + "\"",
            Float.valueOf(fields.get("Carton Tare Weight")),
            "\"" + shipmentDate + "\"",
            transportationRefNumber,
            "\"" + fields.get("Location Code") + "\""
        );
    }

    @When("I request to create {int} new shipments with the values:")
    public void iRequestToCreateANewShipmentsWithTheValues(int quantity, DataTable dataTable) throws InterruptedException {
        Map<String, String> fields = dataTable.asMap(String.class, String.class);


        for (int i = 0; i < quantity; i++) {

            String shipmentDate = LocalDate.now().plusDays(i).toString();
            if (i == 0) {
                sharedContext.setInitialShipmentDate(shipmentDate);
            } else if (i == quantity - 1) {
                sharedContext.setFinalShipmentDate(shipmentDate);
            }


            String shipmentNumber = fields.get("Shipment Number Prefix") + i;
            // Get OPEN or CLOSED randomly
            List<String> statuses = List.of("OPEN", "CLOSED");
            String status = statuses.get(new Random().nextInt(statuses.size()));

            String createShipmentQuery = DatabaseQueries.INSERT_SHIPMENT(
                fields.get("Customer Code"),
                fields.get("Location Code"),
                fields.get("Product Type"),
                status,
                shipmentNumber,
                shipmentDate
            );

            databaseService.executeSql(createShipmentQuery).block();


            Thread.sleep(500);
        }
    }

    @And("The shipment should be created with the following information:")
    public void theShipmentShouldBeCreatedWithTheFollowingInformation(DataTable datatable) {
        Map<String, String> fields = datatable.asMap(String.class, String.class);

        String customerCode = fields.get("customer_code");
        String productType = fields.get("product_type");
        String cartonTareWeight = fields.get("carton_tare_weight");
        String shipmentDate = fields.get("shipment_date");
        String transportationRefNumber = fields.get("transportation_reference_number");
        String locationCode = fields.get("location_code");
        String status = fields.get("status");
        String createDate = fields.get("create_date");

        var newShipment = databaseService.fetchData(DatabaseQueries.FETCH_SHIPMENT_BY_ID(sharedContext.getLastShipmentId() + 1)).first().block();

        Assert.assertNotNull(newShipment);
        Assert.assertEquals(customerCode, newShipment.get("customer_code").toString());
        Assert.assertEquals(productType, newShipment.get("product_type").toString());
        Assert.assertEquals(cartonTareWeight, newShipment.get("carton_tare_weight").toString());
        Assert.assertEquals(locationCode, newShipment.get("location_code").toString());
        Assert.assertEquals(status, newShipment.get("status").toString());

        Assert.assertTrue(
            createShipmentController.verifyIfNullNotNullOrValue(createDate, newShipment.get("create_date")));
        Assert.assertTrue(
            createShipmentController.verifyIfNullNotNullOrValue(shipmentDate, newShipment.get("shipment_date")));
        Assert.assertTrue(
            createShipmentController.verifyIfNullNotNullOrValue(transportationRefNumber, newShipment.get("transportation_reference_number")));

    }

    @Given("I attempt to create a shipment with the attribute {string} as {string}.")
    public void iAttemptToCreateAShipmentWithTheAttributeAs(String attribute, String value) {
        createShipmentController.createShipmentWithInvalidData(attribute, value);
    }

    @And("The shipment {string} be created.")
    public void theShipmentBeCreated(String option) {
        if (option.equals("should")) {
            Assert.assertTrue(createShipmentController.wasNewShipmentWasCreated());
        } else if (option.equals("should not")) {
            Assert.assertFalse(createShipmentController.wasNewShipmentWasCreated());
        }
    }

    @Then("The generated shipment number should starts with {string} and ends with the next shipment count number.")
    public void theGeneratedShipmentNumberShouldStartsWithAndEndsWithTheNextShipmentCountNumber(String shipentNumberPrefix) {
        var lastShipmentCount = sharedContext.getLastShipmentNumber();
        var responseShipmentNumber = sharedContext.getShipmentCreateResponse().get("shipmentNumber");

        Assert.assertEquals(responseShipmentNumber, shipentNumberPrefix + (lastShipmentCount + 1));

    }

    @And("I have removed from the database all shipments from location {string} with transportation ref number {string}.")
    public void iHaveRemovedFromTheDatabaseAllShipmentsFromLocationWithTransportationRefNumber(String location, String transportationRefNumber) {
        // Delete Carton Items
        var deleteCartonItemsQuery = DatabaseQueries.REMOVE_CARTON_ITEMS_BY_LOCATION_AND_TRANSPORTATION_REF_NUMBER(location, transportationRefNumber);
        databaseService.executeSql(deleteCartonItemsQuery).block();

        // Delete Cartons
        var deleteCartonsQuery = DatabaseQueries.REMOVE_CARTONS_BY_LOCATION_AND_TRANSPORTATION_REF_NUMBER(location, transportationRefNumber);
        databaseService.executeSql(deleteCartonsQuery).block();

        // Delete shipments
        var deleteShipmentsQuery = DatabaseQueries.REMOVE_SHIPMENTS_BY_LOCATION_AND_TRANSPORTATION_REF_NUMBER(location, transportationRefNumber);
        databaseService.executeSql(deleteShipmentsQuery).block();
    }

    @And("I request to add {int} carton(s) to the shipment.")
    public void iRequestToAddCartonsToTheShipment(int quantity) {
        for (var n = 0; n < quantity; n++)
            createShipmentController.createCarton(sharedContext.getShipmentCreateResponse().get("id").toString());
    }

    @And("I request to add {int} carton(s) to the shipment {int}.")
    public void iRequestToAddCartonsToTheShipmentNumber(int quantity, int shipmentId) {
        for (var n = 0; n < quantity; n++)
            createShipmentController.createCarton(shipmentId);
    }

    @Given("I have an empty carton created with the Customer Code as {string} , Product Type as {string}, Carton Tare Weight as {string}, Shipment Date as {string}, Transportation Reference Number as {string} and Location Code as {string}.")
    public void createShipmentAndCarton(String customerCode, String productType, String cartonTare, String shipmentDate, String transportationRefNumber, String locationCode) {
        createShipmentController.createShipment(customerCode, productType, Float.parseFloat(cartonTare), testUtils.parseDataKeyword(shipmentDate), transportationRefNumber, locationCode);
        iRequestToAddCartonsToTheShipment(1);
    }
}
