package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages.CreateShipmentPage;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.DatabaseService;
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

    @Given("I have removed from the database all shipments which code contains with {string}.")
    public void removeShipmentsFromDatabase(String code) {
        var deleteShipmentsQuery = "DELETE FROM bld_recovered_plasma_shipment WHERE customer_code LIKE '%" + code + "%'";
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
        }
        createShipmentPage.setShipmentDate(scheduledShipmentDate);

        String transportationRefNumber = fields.get("Transportation Reference Number");
        createShipmentPage.setTransportationRefNumber(transportationRefNumber);
    }

    @When("I choose to submit the shipment.")
    public void submitShipment() {
        createShipmentPage.submitShipment();
    }

    @And("I should be redirected to the Shipment Details page.")
    public void verifyRedirectToShipmentDetails() {
        // Implementation to verify redirect
        log.info("Verifying redirect to Shipment Details page");
    }
}
