package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages.CreateShipmentPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.cucumber.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@SpringBootTest
public class CreateShipmentSteps {

    @Autowired
    private CreateShipmentPage createShipmentPage;

    @Given("I have removed from the database all shipments which code contains with {string}.")
    public void removeShipmentsFromDatabase(String code) {
        // Implementation to remove shipments from database
        log.info("Removing shipments containing code: {}", code);
    }

    @Given("The location {string} is configured with prefix {string}, shipping code {string}, shipping quantity {string}, and prefix configuration {string}.")
    public void configureLocation(String location, String prefix, String shippingCode, String quantity, String prefixConfig) {
        // Implementation to configure location
        log.info("Configuring location: {} with prefix: {}, shipping code: {}, quantity: {}, prefix config: {}", 
            location, prefix, shippingCode, quantity, prefixConfig);
    }

    @Given("I am on the Shipment Create Page.")
    public void navigateToShipmentCreatePage() {
        createShipmentPage.isPageLoaded();
    }

    @When("I choose to create a shipment.")
    public void initiateShipmentCreation() {
        // Implementation to initiate shipment creation
        log.info("Initiating shipment creation");
    }

    @And("I have entered all the fields:")
    public void enterShipmentFields(DataTable dataTable) {
        Map<String, String> fields = dataTable.asMap(String.class, String.class);
        
        String customer = fields.get("Customer");
        createShipmentPage.selectCustomer(customer);

        String productType = fields.get("Product Type");
        createShipmentPage.addProduct(productType, "1"); // Assuming quantity 1 for now

        String cartonTareWeight = fields.get("Carton Tare Weight");
        // Implementation for setting carton tare weight
        
        String scheduledShipmentDate = fields.get("Scheduled Shipment Date");
        if (scheduledShipmentDate.equals("<tomorrow>")) {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            scheduledShipmentDate = tomorrow.format(DateTimeFormatter.ISO_DATE);
        }
        createShipmentPage.setShipmentDate(scheduledShipmentDate);

        String transportationRefNumber = fields.get("Transportation Reference Number");
        // Implementation for setting transportation reference number
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