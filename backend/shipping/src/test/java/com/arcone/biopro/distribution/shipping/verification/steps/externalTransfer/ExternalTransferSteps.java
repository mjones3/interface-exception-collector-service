package com.arcone.biopro.distribution.shipping.verification.steps.externalTransfer;

import com.arcone.biopro.distribution.shipping.verification.pages.distribution.ExternalTransferPage;
import com.arcone.biopro.distribution.shipping.verification.support.ApiHelper;
import com.arcone.biopro.distribution.shipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.shipping.verification.support.controllers.ExternalTransferController;
import com.arcone.biopro.distribution.shipping.verification.support.graphql.GraphQLMutationMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Slf4j
public class ExternalTransferSteps {

    @Autowired
    private ExternalTransferController externalTransferController;

    @Autowired
    private ApiHelper apiHelper;

    @Autowired
    private SharedContext context;

    @Autowired
    private ExternalTransferPage page;

    private Map response;

    private static final String NULL_VALUE = "NULL_VALUE";
    private static final String SHOULD = "should";


    @Given("I have shipped the following products.")
    public void createProductLocationHistory(DataTable table) {
        var headers = table.row(0);
        for (var i = 1; i < table.height(); i++) {
            var row = table.row(i);
            //| Unit Number   | Product Code | Customer Code | Customer Name              | Shipped Date |
            var result = externalTransferController.createProductLocationHistory(row.get(headers.indexOf("Customer Code"))
                , row.get(headers.indexOf("Customer Name")), row.get(headers.indexOf("Unit Number"))
                , row.get(headers.indexOf("Product Code"))
                , row.get(headers.indexOf("Product Family"))
                , row.get(headers.indexOf("Shipped Date")));
            Assertions.assertNotNull(result);
        }
    }

    @When("I create an external transfer request to the customer {string}, hospital transfer id {string} and transfer date {string}.")
    public void createExternalTransferRequest(String customerCode, String hospitalTransferId ,String transferDate) {
        String hospitalTransferIdParam = NULL_VALUE.equals(hospitalTransferId) ? "" : hospitalTransferId;
        String createExternalTransferMutation = GraphQLMutationMapper.createExternalTransferInformationMutation(customerCode,transferDate , hospitalTransferIdParam ,context.getEmployeeId());
        this.response = apiHelper.graphQlRequest(createExternalTransferMutation, "createExternalTransfer");
        log.debug("Response: {}", response);
        Assertions.assertNotNull(response);
    }

    @Then("I should have an external transfer request created.")
    public void shouldHaveExternalTransferRequest() {
        Assertions.assertEquals("200 OK", response.get("ruleCode"));
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(context.getApiMessageResponse());
        var message = context.getApiMessageResponse().getFirst();
        Assertions.assertEquals("SUCCESS", message.get("notificationType"));
    }

    @And("I navigate to the external transfer page.")
    public void iNavigateToTheExternalTransferPage() throws InterruptedException {
        page.goTo();
    }

    @When("I choose customer name {string}.")
    public void iChooseCustomerName(String customerName) throws InterruptedException {
        page.selectCustomer(customerName);
    }

    @And("I fill hospital transfer Id {string} and {string} as transfer Date.")
    public void fillHospitalTransferIdAndTransferDate(String hospitalTransferId, String transferDate) throws InterruptedException {
        var hospitalTransferIdValue = NULL_VALUE.equals(hospitalTransferId) ? "" : hospitalTransferId;
        page.defineHospitalTransferIdAndTransferDate(hospitalTransferIdValue, transferDate);
    }

    @Then("I {string} be able to add products to the external transfer request.")
    public void iBeAbleToAddProductsToTheExternalTransferRequest(String shouldFlag) {
        page.checkUnitNumberProductCodeFieldVisibilityIs(SHOULD.equals(shouldFlag));
    }

    @And("I have entered the external transfer information for the customer {string} , transfer date {string} successfully.")
    public void iHaveEnteredTheExternalTransferInformationSuccessfully(String customerCode, String transferDate) {
        String createExternalTransferMutation = GraphQLMutationMapper.createExternalTransferInformationMutation(customerCode,transferDate , "" ,context.getEmployeeId());
        this.response = apiHelper.graphQlRequest(createExternalTransferMutation, "createExternalTransfer");
        log.debug("Response: {}", response);

        Assertions.assertNotNull(context.getApiMessageResponse());

        var message = context.getApiMessageResponse().getFirst();
        Assertions.assertEquals("SUCCESS", message.get("notificationType"));

        var results = (List<Map>) ((LinkedHashMap) this.response.get("results")).get("results");
        Assertions.assertNotNull(results);
        context.setExternalTransferId(Long.parseLong(results.getFirst().get("id").toString()));

    }

    @When("I add the product with {string} and {string}.")
    public void iAddTheProductWithAnd(String unitNumber, String productCode) {
        String addProductToExternalTransferMutation = GraphQLMutationMapper.addProductToExternalTransferInformationMutation(context.getExternalTransferId(), unitNumber,productCode , context.getEmployeeId());
        this.response = apiHelper.graphQlRequest(addProductToExternalTransferMutation, "addExternalTransferProduct");
        log.debug("Response: {}", response);
        context.setProductCode(productCode);
        context.setUnitNumber(unitNumber);
        Assertions.assertNotNull(response);

    }

    @Then("The product {string} be added in the list of products to be transferred.")
    public void theProductShouldBeAddedInTheListOfProductsToBeTransferred(String shouldFlag) {
        Assertions.assertNotNull(response);
        if(SHOULD.equals(shouldFlag)){
            var results = (List<Map>) ((LinkedHashMap) this.response.get("results")).get("results");
            var items = (List<Map>) results.getFirst().get("externalTransferItems");
            Assertions.assertFalse(items.isEmpty());
            Assertions.assertTrue(items.stream().anyMatch(item -> item.get("productCode").toString().equals(context.getProductCode())
                && item.get("unitNumber").toString().equals(context.getUnitNumber())));
        }else{
            Assertions.assertNull(this.response.get("results"));
        }
    }

    @When("I submit the external transfer process.")
    public void iSubmitTheExternalTransferProcess() {
        String mutation = GraphQLMutationMapper.completeExternalTransferInformationMutation(context.getExternalTransferId(), "",context.getEmployeeId());
        this.response = apiHelper.graphQlRequest(mutation, "completeExternalTransfer");
        log.debug("Response Submit {}",response);
    }

}
