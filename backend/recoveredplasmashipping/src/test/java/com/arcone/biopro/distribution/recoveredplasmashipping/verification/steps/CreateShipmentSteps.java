package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CreateShipmentController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages.CreateShipmentPage;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.DatabaseQueries;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.DatabaseService;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
import graphql.Assert;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@SpringBootTest
public class CreateShipmentSteps {

    @Autowired
    private CreateShipmentPage createShipmentPage;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private CreateShipmentController createShipmentController;
    @Autowired
    private SharedContext sharedContext;

    @Given("I have removed from the database all shipments which code contains with {string}.")
    public void removeShipmentsFromDatabase(String code) {
        var deleteShipmentsQuery = DatabaseQueries.DELETE_SHIPMENTS_BY_CODE(code);
        databaseService.executeSql(deleteShipmentsQuery).block();
        log.info("Removing shipments containing code: {}", code);
    }

    @Given("I am on the Shipment Create Page.")
    public void navigateToShipmentCreatePage() throws InterruptedException {
        createShipmentPage.goTo();
    }

    @When("I choose to create a shipment.")
    public void initiateShipmentCreation() {
        createShipmentPage.clickCreateShipment();
        log.info("Initiating shipment creation");
    }

    @And("I have entered all the fields:")
    public void enterShipmentFields(DataTable dataTable) {
        Map<String, String> fields = dataTable.asMap(String.class, String.class);

        String customer = fields.get("Customer");
        createShipmentPage.selectCustomer(customer);

        String productType = fields.get("Product Type");
        createShipmentPage.selectProductType(productType); // Assuming quantity 1 for now

        String cartonTareWeight = fields.get("Carton Tare Weight");
        createShipmentPage.setCartonTareWeight(cartonTareWeight);

        String scheduledShipmentDate = fields.get("Scheduled Shipment Date");
        if (scheduledShipmentDate.equals("<tomorrow>")) {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            scheduledShipmentDate = tomorrow.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            createShipmentPage.setShipmentDate(scheduledShipmentDate);

            String transportationRefNumber = fields.get("Transportation Reference Number");
            createShipmentPage.setTransportationRefNumber(transportationRefNumber);
        }
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


        String scheduledShipmentDate = fields.get("Scheduled Shipment Date");
        if (scheduledShipmentDate.equals("<tomorrow>")) {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            scheduledShipmentDate = tomorrow.toString();
        }

        String transportationRefNumber = fields.get("Transportation Reference Number");
        if (transportationRefNumber.equals("<null>")) {
            transportationRefNumber = null;
        } else {
            transportationRefNumber = "\"" + transportationRefNumber + "\"";
        }

        var response = createShipmentController.createShipment(
            fields.get("Customer Code"),
            fields.get("Product Type"),
            Float.valueOf(fields.get("Carton Tare Weight")),
            scheduledShipmentDate,
            transportationRefNumber,
            fields.get("Location Code")
        );
    }

    @And("The shipment should be created with the following information:")
    public void theShipmentShouldBeCreatedWithTheFollowingInformation(DataTable datatable) {
        Map<String, String> fields = datatable.asMap(String.class, String.class);

        String customerCode = fields.get("customer_code");
        String productType = fields.get("product_type");
        String cartonTareWeight = fields.get("carton_tare_weight");
        String scheduledShipmentDate = fields.get("scheduled_shipment_date");
        String transportationRefNumber = fields.get("transportation_reference_number");
        String locationCode = fields.get("location_code");
        String status = fields.get("status");
        String createDate = fields.get("create_date");

        var newShipment = databaseService.fetchData(DatabaseQueries.FETCH_SHIPMENT_BY_ID(sharedContext.getLastShipmentId() + 1)).first().block();

        assert newShipment != null;
        assert customerCode.equals(newShipment.get("customer_code").toString());
        assert productType.equals(newShipment.get("product_type").toString());
        assert cartonTareWeight.equals(newShipment.get("carton_tare_weight").toString());
        assert locationCode.equals(newShipment.get("location_code").toString());
        assert status.equals(newShipment.get("status").toString());

        Assert.assertTrue(
            createShipmentController.verifyIfNullNotNullOrValue(createDate, newShipment.get("create_date")));
        Assert.assertTrue(
            createShipmentController.verifyIfNullNotNullOrValue(scheduledShipmentDate, newShipment.get("schedule_date")));
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
}
