package com.arcone.biopro.distribution.irradiation.verification.api.steps;

import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration
public class GraphQLSteps {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private RepositorySteps repositorySteps;

    @When("I scan the device {string} at location {string}")
    public void iScanTheDeviceAtLocation(String deviceId, String location) {
        Boolean result = graphQlTester
                .document("query { validateDevice(deviceId: \"" + deviceId + "\", location: \"" + location + "\") { valid errorMessage } }")
                .execute()
                .path("validateDevice.valid")
                .entity(Boolean.class)
                .get();

        repositorySteps.setValidationResult(result);
    }

    @When("I scan the device {string}")
    public void iScanTheDevice(String deviceId) {
        Boolean result = graphQlTester
                .document("query { validateDevice(deviceId: \"" + deviceId + "\", location: \"DEFAULT_LOCATION\") { valid errorMessage } }")
                .execute()
                .path("validateDevice.valid")
                .entity(Boolean.class)
                .get();

        repositorySteps.setValidationResult(result);
    }
}
