package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.ApiHelper;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.DatabaseService;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.graphql.GraphQLQueryMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Slf4j
public class LocationConfigurationSteps {

    private DataTable definedLocations;
    private List<Map> locationResponse;

    @Autowired
    ApiHelper apiHelper;

    @Autowired
    DatabaseService databaseService;

    @Autowired
    SharedContext context;

    @Given("The following locations are defined as Recovered Plasma Shipping Locations")
    public void theFollowingLocationsAreDefinedAsRecoveredPlasmaShippingLocations(DataTable dataTable) {
        definedLocations = dataTable;
        Assertions.assertNotNull(definedLocations);
    }

    @When("I request to list all Recovered Plasma Shipping Locations")
    public void iRequestToListAllRecoveredPlasmaShippingLocations() {
        this.locationResponse = apiHelper.graphQlListRequest(GraphQLQueryMapper.findAllLocations(), "findAllLocations");
        log.debug("Response: {}", this.locationResponse);
        Assertions.assertNotNull(this.locationResponse);
    }


    @Then("the response should contain all Recovered Plasma Shipping Locations")
    public void theResponseShouldContainAllRecoveredPlasmaShippingLocations() {
        var headers = definedLocations.row(0);
        for (var i = 1; i < definedLocations.height(); i++) {
            var row = definedLocations.row(i);
            var result = this.locationResponse.stream().filter(map -> map.get("code").equals(row.get(headers.indexOf("Location Code")))).findFirst();
            Assertions.assertNotNull(result);
            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals(result.get().get("name"), row.get(headers.indexOf("Location Name")));
        }
    }


    @Given("The location {string} is configured with prefix {string}, shipping code {string}, carton prefix {string} and prefix configuration {string}.")
    public void configureLocation(String location, String prefix, String shippingCode , String cartonPrefix, String prefixConfig) {

        int randomId = (int) (Math.random() * 1000) +1;
        // lk_location
        var createConfigurationSQL = "INSERT INTO lk_location (id, external_id, code, name, city, state, postal_code, address_line_1, active, create_date, modification_date) " +
            "VALUES ("+ randomId + ", '" + location + "', '" + location + "', '" + location + "', 'city', 'state', '000000', 'address_line_1', true, now(), now())";
        databaseService.executeSql(createConfigurationSQL).block();

        // lk_location_property
        var locationPropertySQL = "INSERT INTO lk_location_property (location_id, property_key, property_value) " +
            "VALUES (" + randomId + ", 'RPS_PARTNER_PREFIX', '" + prefix + "'), " +
            " ((" + randomId + "), 'RPS_LOCATION_SHIPMENT_CODE', '" + shippingCode + "'), " +
            " ((" + randomId + "), 'RPS_LOCATION_CARTON_CODE', '" + "MH1" + "'), " +
            " ((" + randomId + "), 'RPS_CARTON_PARTNER_PREFIX', '" + cartonPrefix + "'), " +
            " ((" + randomId + "), 'TZ', 'America/New_York'), " +
            " ((" + randomId + "), 'PHONE_NUMBER', '123-456-7894'), " +
            " ((" + randomId + "), 'RPS_USE_PARTNER_PREFIX', '" + prefixConfig + "');";
        databaseService.executeSql(locationPropertySQL).block();

        log.info("Configuring location: {} with prefix: {}, shipping code: {}, prefix config: {}",
            location, prefix, shippingCode, prefixConfig);

        context.setLocationCode(location);
    }

    @Given("I have removed from the database all the configurations for the location {string}.")
    public void iHaveRemovedFromTheDatabaseAllTheConfigurationsForTheLocation(String external_id) {
        var deletePropertySQL = "DELETE FROM lk_location_property WHERE location_id = (SELECT id FROM lk_location WHERE external_id = '" + external_id + "');";
        databaseService.executeSql(deletePropertySQL).block();

        var deleteConfigurationSQL = "DELETE FROM lk_location WHERE external_id = '" + external_id + "';";
        databaseService.executeSql(deleteConfigurationSQL).block();
    }
}
