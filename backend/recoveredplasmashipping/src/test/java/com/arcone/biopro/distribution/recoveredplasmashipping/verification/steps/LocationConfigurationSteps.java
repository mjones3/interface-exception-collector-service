package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.ApiHelper;
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
}
