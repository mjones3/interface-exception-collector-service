package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CreateShipmentController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages.ShipmentDetailsPage;
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
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Slf4j
public class ModifyShipmentSteps {
    @Autowired
    private SharedContext sharedContext;
    @Autowired
    private TestUtils testUtils;
    @Autowired
    CreateShipmentController createShipmentController;
    @Autowired
    ShipmentDetailsPage shipmentDetailsPage;

    @Value("${default.employee.id}")
    String employeeId;

    @Given("I request to edit a shipment with the values:")
    public void iRequestToEditAShipmentWithTheValues(DataTable dataTable) {
        var tableRows = dataTable.asMap(String.class, String.class);
        var modifyResponse = createShipmentController.modifyShipment(
            Integer.parseInt(sharedContext.getShipmentCreateResponse().get("id").toString()),
            tableRows.get("Customer Code"),
            tableRows.get("Product Type"),
            tableRows.get("Transportation Reference Number"),
            tableRows.get("Shipment Date"),
            Integer.parseInt(tableRows.get("Carton Tare Weight")),
            employeeId,
            tableRows.get("Comments")
        );
    }

    @And("The following fields should be updated {string}.")
    public void theFollowingFieldsShouldBeUpdated(String modifiedFields) {
        var originalShipment = sharedContext.getShipmentCreateResponse();
        var modifiedShipment = sharedContext.getLastShipmentModifyResponse();
        List<String> fieldList = List.of(testUtils.getCommaSeparatedList(modifiedFields));

        for (String field : fieldList) {
            log.debug("Verifying field: {}", field);
            log.debug("Original value: {} | Modified value: {}", originalShipment.get(field), modifiedShipment.get(field));
            Assert.assertNotEquals(originalShipment.get(field), modifiedShipment.get(field));
        }
    }

    @When("I request the shipment modify history.")
    public void iRequestTheShipmentModifyHistory() {
        createShipmentController.requestShipmentModifyHistory(Integer.parseInt(sharedContext.getShipmentCreateResponse().get("id").toString()));
    }

    @Then("The modify history should contain {string}.")
    public void theModifyHistoryShouldContain(String historyContent) {
        // Break history entries by ';'
        var historyEntries = historyContent.split(";");

        for (String entry : historyEntries) {
            var historyEntryValues = testUtils.getCommaSeparatedList(entry);
            var historyResponse = sharedContext.getLastShipmentModifyHistoryResponse();
            log.debug("Verifying history entry: {}", entry);
            log.debug("History response: {}", historyResponse);
            Assert.assertTrue(
                historyResponse.stream().anyMatch(e -> e.get("createEmployeeId").equals(historyEntryValues[0])
                    && e.get("createDate").toString().contains(testUtils.parseDateKeyword(historyEntryValues[1]))
                &&  e.get("comments").equals(historyEntryValues[2]))
            );
        }
    }

    @And("The edit shipment option should be {string}.")
    public void theEditShipmentOptionShouldBeEnabled(String option) {
        if (option.equals("enabled")) {
            Assert.assertTrue(shipmentDetailsPage.isEditShipmentButtonEnabled());
        } else if (option.equals("disabled")) {
            Assert.assertFalse(shipmentDetailsPage.isEditShipmentButtonEnabled());
        } else {
            throw new RuntimeException("Invalid option");
        }
    }


    @When("I choose to edit the shipment.")
    public void iChooseToEditTheShipment() {
        shipmentDetailsPage.clickEditShipmentButton();
    }

    @Then("I should see the following fields in edit form:")
    public void iShouldSeeTheFollowingFields(DataTable dataTable) throws InterruptedException {
        var headers = dataTable.row(0);
        for (int i = 1; i < dataTable.height(); i++) {
            var row = dataTable.row(i);
            shipmentDetailsPage.verifyEditShipmentFields(row.get(headers.indexOf("Field")), row.get(headers.indexOf("Value")), row.get(headers.indexOf("Status")));
        }
    }

    @When("I switch to the Shipment History tab.")
    public void iSwitchToTheShipmentHistoryTab() {
        shipmentDetailsPage.switchToCommentsTab();
    }

    @Then("I should see the following rows in the history table:")
    public void iShouldSeeTheFollowingRowsInTheHistoryTable(DataTable dataTable) {
        var headers = dataTable.row(0);
        for (int i = 1; i < dataTable.height(); i++) {
            var row = dataTable.row(i);
            shipmentDetailsPage.verifyShipmentHistoryRow(row.get(headers.indexOf("User")), row.get(headers.indexOf("Date")), row.get(headers.indexOf("Comments")));
        }
    }
}
