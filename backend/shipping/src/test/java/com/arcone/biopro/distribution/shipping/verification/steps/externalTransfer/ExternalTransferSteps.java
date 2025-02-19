package com.arcone.biopro.distribution.shipping.verification.steps.externalTransfer;

import com.arcone.biopro.distribution.shipping.verification.support.ApiHelper;
import com.arcone.biopro.distribution.shipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.shipping.verification.support.controllers.ExternalTransferController;
import com.arcone.biopro.distribution.shipping.verification.support.graphql.GraphQLMutationMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;


@Slf4j
public class ExternalTransferSteps {

    @Autowired
    private ExternalTransferController externalTransferController;

    @Autowired
    private ApiHelper apiHelper;

    @Autowired
    private SharedContext context;

    private Map response;


    @Given("I have shipped the following products.")
    public void createProductLocationHistory(DataTable table) {
        var headers = table.row(0);
        for (var i = 1; i < table.height(); i++) {
            var row = table.row(i);
            //| Unit Number   | Product Code | Customer Code | Customer Name              | Shipped Date |
            var result = externalTransferController.createProductLocationHistory(row.get(headers.indexOf("Customer Code"))
                , row.get(headers.indexOf("Customer Name")), row.get(headers.indexOf("Unit Number"))
                , row.get(headers.indexOf("Product Code")), row.get(headers.indexOf("Shipped Date")));
            Assertions.assertNotNull(result);
        }
    }

    @When("I create an external transfer request to the customer {string}, hospital transfer id {string} and transfer date {string}.")
    public void createExternalTransferRequest(String customerCode, String hospitalTransferId ,String transferDate) {
        String hospitalTransferIdParam = "NULL_VALUE".equals(hospitalTransferId) ? "" : hospitalTransferId;
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


}
