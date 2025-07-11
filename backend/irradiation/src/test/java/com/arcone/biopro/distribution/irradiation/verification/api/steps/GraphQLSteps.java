package com.arcone.biopro.distribution.irradiation.verification.api.steps;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.controller.errors.DeviceValidationFailureException;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateDeviceUseCase;
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

    @Autowired
    private ValidateDeviceUseCase validateDeviceUseCase;

    @When("I scan the device {string} at location {string}")
    public void iScanTheDeviceAtLocation(String deviceId, String location) {
        try {
            Boolean result = graphQlTester
                    .document("query { validateDevice(deviceId: \"" + deviceId + "\", location: \"" + location + "\") }")
                    .execute()
                    .path("validateDevice")
                    .entity(Boolean.class)
                    .get();
            repositorySteps.setValidationResult(result);
        } catch (AssertionError e) {
            repositorySteps.setValidationResult(false);
        }
    }

    @When("I scan the device {string}")
    public void iScanTheDevice(String deviceId) {
        try {
            Boolean result = graphQlTester
                    .document("query { validateDevice(deviceId: \"" + deviceId + "\", location: \"DEFAULT_LOCATION\") }")
                    .execute()
                    .path("validateDevice")
                    .entity(Boolean.class)
                    .get();
            repositorySteps.setValidationResult(result);
        } catch (AssertionError e) {
            repositorySteps.setValidationResult(false);
        }
    }
}
