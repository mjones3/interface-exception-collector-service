package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.ApiHelper;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.DatabaseQueries;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.DatabaseService;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.graphql.GraphQLQueryMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Slf4j
public class CustomerConfigurationSteps {

    private DataTable definedCustomers;
    private List<Map> customersResponse;

    @Autowired
    ApiHelper apiHelper;
    @Autowired
    DatabaseService databaseService;


    @Given("The following customers are defined as Recovered Plasma Shipping customers.")
    public void theFollowingLocationsAreDefinedAsRecoveredPlasmaShippingCustomers(DataTable dataTable) {
        definedCustomers = dataTable;
        Assertions.assertNotNull(definedCustomers);
    }


    @When("I request to list all Recovered Plasma Shipping Customers.")
    public void iRequestToListAllRecoveredPlasmaShippingCustomers() {
        this.customersResponse = apiHelper.graphQlListRequest(GraphQLQueryMapper.findAllCustomers(), "findAllCustomers");
        log.debug("Response: {}", this.customersResponse);
        Assertions.assertNotNull(this.customersResponse);

    }

    @Then("the response should contain all Recovered Plasma Shipping Customers.")
    public void theResponseShouldContainAllRecoveredPlasmaShippingCustomers() {

        var headers = definedCustomers.row(0);
        for (var i = 1; i < definedCustomers.height(); i++) {
            var row = definedCustomers.row(i);
            var result = this.customersResponse.stream().filter(map -> map.get("code").equals(row.get(headers.indexOf("Code")))).findFirst();
            Assertions.assertNotNull(result);
            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals(result.get().get("name"), row.get(headers.indexOf("Name")));
            Assertions.assertEquals(result.get().get("customerType"), row.get(headers.indexOf("Customer Type")));
            Assertions.assertEquals(result.get().get("state"), row.get(headers.indexOf("State")));
            Assertions.assertEquals(result.get().get("city"), row.get(headers.indexOf("City")));
        }

    }

    @And("The Minimum acceptable Volume of Units in Carton is configured as {string} milliliters for the customer code {string} and product type {string}.")
    public void theMinimumAcceptableVolumeOfUnitsInCartonIsConfiguredAsMillilitersForTheCustomerCodeAndProductType(String volume, String customerCode, String productType) {
        databaseService.executeSql(DatabaseQueries.UPDATE_MIN_VOLUME_CUSTOMER_PRODUCT_CRITERIA(customerCode, productType, "MINIMUM_VOLUME",volume)).block();
    }

    @And("The Maximum Number of Units in Carton is configured as {string} products for the customer code {string} and product type {string}.")
    public void theMaximumNumberOfUnitsInCartonIsConfiguredAsProductsForTheCustomerCodeAndProductType(String maxValue, String customerCode, String productType) {
        databaseService.executeSql(DatabaseQueries.UPDATE_MAX_PRODUCTS_CUSTOMER_CRITERIA(customerCode, productType, maxValue)).block();
    }
    @And("The Minimum Number of Units in Carton is configured as {string} products for the customer code {string} and product type {string}.")
    public void theMinimumNumberOfUnitsInCartonIsConfiguredAsProductsForTheCustomerCodeAndProductType(String maxValue, String customerCode, String productType) {
        databaseService.executeSql(DatabaseQueries.UPDATE_MIN_PRODUCTS_CUSTOMER_CRITERIA(customerCode, productType, maxValue)).block();
    }
}
