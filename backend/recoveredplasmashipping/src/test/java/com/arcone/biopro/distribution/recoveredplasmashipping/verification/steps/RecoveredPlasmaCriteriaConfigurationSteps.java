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
public class RecoveredPlasmaCriteriaConfigurationSteps {

    private DataTable definedProductTypes;
    private List<Map> productTypeResponse;

    @Autowired
    ApiHelper apiHelper;


    @Given("The following product types are defined as Recovered Plasma Product Type Criteria")
    public void theFollowingProductTypesAreDefinedAsRecoveredPlasmaProductTypeCriteria(DataTable definedProductTypes) {
        this.definedProductTypes = definedProductTypes;
        Assertions.assertNotNull(definedProductTypes);
    }

    @When("I request to list all Product Types by customer code {string}")
    public void iRequestToListAllProductTypesByCustomerCode(String customerCode) {

        this.productTypeResponse = apiHelper.graphQlListRequest(GraphQLQueryMapper.findAllProductTypeCustomer(customerCode), "findAllProductTypeByCustomer");
        log.debug("Response: {}", this.productTypeResponse);
        Assertions.assertNotNull(this.productTypeResponse);

    }

    @Then("the response should contain the following product types")
    public void theResponseShouldContainTheFollowingProductTypes(DataTable productTypes) {

        var headers = productTypes.row(0);
        for (var i = 1; i < productTypes.height(); i++) {
            var row = productTypes.row(i);
            var result = this.productTypeResponse.stream().filter(map -> map.get("productType").equals(row.get(headers.indexOf("Product Type")))).findFirst();
            Assertions.assertNotNull(result);
            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals(result.get().get("productTypeDescription"), row.get(headers.indexOf("Product Type Description")));
        }
    }
}
