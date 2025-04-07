package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.FilterShipmentsController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages.CreateShipmentPage;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
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
import java.util.Map;

@Slf4j
public class FilterShipmentsSteps {
    @Autowired
    private FilterShipmentsController filterShipmentsController;

    @Autowired
    private SharedContext context;

    @Autowired
    CreateShipmentPage createShipmentPage;

    private Map<String, String> filterTable;

    @Given("I requested the list of shipments filtering by {string} as {string}.")
    public void iRequestedTheListOfShipmentsFilteringByAs(String filter, String value) {
        value = value.replace("<today>", LocalDate.now().toString());
        value = value.replace("<tomorrow>", LocalDate.now().plusDays(1).toString());
        if (value.equals("<currentShipmentNumber>")) {
            value = context.getShipmentCreateResponse().get("shipmentNumber").toString();
        }


        if (filter.equals("locationCodeList")) {
            filterShipmentsController.filterShipmentsByLocationList(value);
        } else {
            filterShipmentsController.filterShipmentsByLocationListAndCustomAttribute(context.getLocationCode(), filter, value);
        }
    }

    @Then("The list shipment response should contains {string} items.")
    public void theListShipmentResponseShouldContainsItems(String expectedQuantity) {
        Assert.assertEquals(Integer.parseInt(expectedQuantity), context.getApiShipmentListResponse().size());
    }

    @When("I receive the shipment list response.")
    public void iReceiveTheShipmentListResponse() {
        // Empty step on purpose
    }

    @And("I open the filter panel.")
    public void iOpenTheFilterPanel() {
        createShipmentPage.openFilterPanel();
    }

    @Then("The Filter Apply button should be {string}.")
    public void theFilterApplyButtonShouldBe(String option) {
        if (option.equals("enabled")) {
            Assert.assertTrue(createShipmentPage.isFilterApplyButtonEnabled());
        } else if (option.equals("disabled")) {
            Assert.assertFalse(createShipmentPage.isFilterApplyButtonEnabled());
        } else {
            Assert.fail("Invalid option");
        }
    }

    @When("I select the following filter criteria:")
    public void iSelectTheFollowingFilterCriteria(DataTable dataTable) {
        filterTable = dataTable.asMap(String.class, String.class);

        createShipmentPage.selectCustomers(filterTable.get("Customer"));
        createShipmentPage.selectLocations(filterTable.get("Location"));
        createShipmentPage.selectShipmentStatuses(filterTable.get("Shipment Status"));
        createShipmentPage.selectProductTypes(filterTable.get("Product Type"));
        createShipmentPage.setFilterTransportationReferenceNumber(filterTable.get("Transportation Reference Number"));
    }

    @And("I enter shipment date range from {string} to {string}.")
    public void iEnterShipmentDateRangeFromTo(String dateFrom, String dateTo) {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        if (dateFrom.equals("<today>")) {
            dateFrom = today.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        } else if (dateFrom.equals("<tomorrow>")) {
            dateFrom = tomorrow.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        }

        if (dateTo.equals("<today>")) {
            dateTo = today.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        } else if (dateTo.equals("<tomorrow>")) {
            dateTo = tomorrow.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        }

        createShipmentPage.setFilterDateRange(dateFrom, dateTo);
    }

    @When("I click the Filter Apply button.")
    public void iClickTheFilterApplyButton() {
        createShipmentPage.clickFilterApplyButton();
    }

    @Then("I should see filtered shipments matching the criteria.")
    public void iShouldSeeFilteredShipmentsMatchingTheCriteria() {
        log.info("I should see filtered shipments matching the criteria.");
        createShipmentPage.verifyFilterResult(
            filterTable.get("Location"),
            filterTable.get("Transportation Reference Number"),
            filterTable.get("Customer"),
            filterTable.get("Product Type"),
            filterTable.get("Shipment Status")
        );

    }

    @And("I should see {string} filter criteria applied.")
    public void iShouldSeeFilterCriteriaApplied(String quantity) {
        createShipmentPage.verifyFilterCriteriaApplied(Integer.parseInt(quantity));
    }
}
