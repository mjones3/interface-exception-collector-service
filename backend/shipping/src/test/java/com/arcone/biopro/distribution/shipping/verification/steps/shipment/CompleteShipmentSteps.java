package com.arcone.biopro.distribution.shipping.verification.steps.shipment;

import com.arcone.biopro.distribution.shipping.verification.support.ApiHelper;
import com.arcone.biopro.distribution.shipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.shipping.verification.support.TestUtils;
import com.arcone.biopro.distribution.shipping.verification.support.graphql.GraphQLMutationMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class CompleteShipmentSteps {

    @Autowired
    private SharedContext context;

    @Autowired
    private ApiHelper apiHelper;


    @When("I request to complete a shipment.")
    public void iRequestToCompleteAShipment() {
        var response = apiHelper.graphQlRequest(GraphQLMutationMapper.completeShipmentMutation(context.getShipmentId(), "test-emplyee-id"), "completeShipment");
        Assert.assertNotNull(response);
    }


}
