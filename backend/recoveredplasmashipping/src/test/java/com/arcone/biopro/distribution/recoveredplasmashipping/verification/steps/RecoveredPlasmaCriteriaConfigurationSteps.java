package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.ApiHelper;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.DatabaseQueries;
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
import org.springframework.r2dbc.core.FetchSpec;

import java.util.List;
import java.util.Map;

@Slf4j
public class RecoveredPlasmaCriteriaConfigurationSteps {

    private DataTable definedProductTypes;
    private List<Map> productTypeResponse;
    private DataTable definedShippingCriteriaConfigurations;
    private List<Map<String, Object>> shippingCriteriaConfigurationsResponse;
    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private SharedContext sharedContext;

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

    @Given("The following Recovered Plasma Shipping Criteria are defined:")
    public void theFollowingRecoveredPlasmaShippingCriteriaAreDefined(DataTable criteriaConfigurations) {
        this.definedShippingCriteriaConfigurations = criteriaConfigurations;
        Assertions.assertNotNull(definedShippingCriteriaConfigurations);
    }

    @When("I request to list Recovered Plasma Shipping Criteria by customer code {string} and product type {string}")
    public void iRequestToListRecoveredPlasmaShippingCriteriaByCustomerCodeAndProductType(String customerCode, String productType) {
        log.debug("Fetching shipment criteria by : {} {}", customerCode,productType);
        var response = databaseService.fetchData(DatabaseQueries.FETCH_SHIPMENT_CRITERIA_BY_CUSTOMER_AND_PRODUCT_TYPE(customerCode,productType)).all().collectList().blockOptional();
        Assertions.assertTrue(response.isPresent());
        this.shippingCriteriaConfigurationsResponse = response.get();
        this.sharedContext.setRecoveredPlasmaCriteriaConfigurationCustomerCode(customerCode);
    }

    @Then("the response should contain the Recovered Plasma Shipping Criteria Configurations like Product Code as {string}  Min Vol as {string} Min. Number of Units in Carton as {string} and Max. Number of Units in Carton as {string}")
    public void theResponseShouldContainTheRecoveredPlasmaShippingCriteriaConfigurationsLikeProductCodeAsMinVolAsMinNumberOfUnitsInCartonAsAndMaxNumberOfUnitsInCartonAs(String productCode, String minVolume, String minUnits, String maxUnits) {

        log.debug("response {}",this.shippingCriteriaConfigurationsResponse);


        var result = this.shippingCriteriaConfigurationsResponse.stream().filter(map -> map.get("customer_code").equals(sharedContext.getRecoveredPlasmaCriteriaConfigurationCustomerCode())).findFirst();
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(productCode,result.get().get("product_code"));

        var minUnitsResponse = this.shippingCriteriaConfigurationsResponse.stream().filter(map -> map.get("type").equals("MINIMUM_UNITS_BY_CARTON") && map.get("value").equals(minUnits) ).findFirst();
        Assertions.assertNotNull(minUnitsResponse);
        Assertions.assertTrue(minUnitsResponse.isPresent());

        if(!"<null>".equals(minVolume)){
            var volumeResponse = this.shippingCriteriaConfigurationsResponse.stream().filter(map -> map.get("type").equals("MINIMUM_VOLUME") && map.get("value").equals(minVolume)).findFirst();
            Assertions.assertNotNull(volumeResponse);
            Assertions.assertTrue(volumeResponse.isPresent());


        }

        var maxUnitsResponse = this.shippingCriteriaConfigurationsResponse.stream().filter(map -> map.get("type").equals("MAXIMUM_UNITS_BY_CARTON") && map.get("value").equals(maxUnits)).findFirst();
        Assertions.assertNotNull(maxUnitsResponse);
        Assertions.assertTrue(maxUnitsResponse.isPresent());

    }
}
