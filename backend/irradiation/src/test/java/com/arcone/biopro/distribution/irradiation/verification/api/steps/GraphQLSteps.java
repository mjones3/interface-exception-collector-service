package com.arcone.biopro.distribution.irradiation.verification.api.steps;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.dto.CheckDigitResponseDTO;
import com.arcone.biopro.distribution.irradiation.application.dto.IrradiationInventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateDeviceUseCase;
import com.arcone.biopro.distribution.irradiation.verification.api.support.IrradiationContext;
import com.arcone.biopro.distribution.irradiation.verification.common.GraphQlHelper;
import com.arcone.biopro.distribution.irradiation.verification.utils.CheckDigitUtil;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@ContextConfiguration
public class GraphQLSteps {

    @Autowired
    private GraphQlHelper graphQlHelper;

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private RepositorySteps repositorySteps;

    @Autowired
    private ValidateDeviceUseCase validateDeviceUseCase;

    @Autowired
    private IrradiationContext irradiationContext;

    @When("I enter the unit number {string} in irradiation and the check digit")
    public void iScanTheUnitNumberInIrradiationAndACheckDigit(String unitNumber) {
        String checkDigit = CheckDigitUtil.calculateDigitCheck(unitNumber);

        try {
            CheckDigitResponseDTO response = graphQlTester
                    .documentName("checkDigit")
                    .variable("unitNumber", unitNumber)
                    .variable("checkDigit", checkDigit)
                    .execute()
                    .path("checkDigit")
                    .entity(CheckDigitResponseDTO.class)
                    .get();
            irradiationContext.setCheckDigitResponse(response);
            if (response.isValid()) {
                iScanTheUnitNumberInIrradiation(unitNumber);
                List<IrradiationInventoryOutput> filteredList = irradiationContext.getInventoryList().stream()
                    .filter(item -> item.unitNumber().equals(unitNumber))
                    .toList();
                irradiationContext.setInventoryList(filteredList);
            }
            repositorySteps.setValidationResult(response.isValid());
        } catch (AssertionError e) {
            repositorySteps.setValidationResult(false);
        }
    }

    @When("I scan the unit number {string} in irradiation")
    public void iScanTheUnitNumberInIrradiation(String unitNumber) {
        var location = "123456789";
        if(!Strings.isEmpty(irradiationContext.getLocation())) {
            location = irradiationContext.getLocation();
        }
        Map<String, Object> variables = Map.of(
            "unitNumber", unitNumber,
            "location", location
        );

        var response = graphQlHelper.executeQuery("validateUnit", variables, "validateUnit", IrradiationInventoryOutput[].class);
        if(response.getErrors().isEmpty()) {
            List<IrradiationInventoryOutput> inventoryList = Arrays.asList(response.getData());
            irradiationContext.setInventoryList(inventoryList);
        } else {
            irradiationContext.setResponseErrors(response.getErrors());
        }
    }

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

    @When("I submit the batch for irradiation")
    public void iSubmitTheBatchForIrradiation() {
        String deviceId = repositorySteps.getBatchDeviceId();
        String startTime = repositorySteps.getBatchStartTime();
        List<Map<String, String>> batchItems = repositorySteps.getBatchItems();

        Map<String, Object> input = Map.of(
            "deviceId", deviceId,
            "startTime", startTime.replace("Z", ""),
            "batchItems", batchItems
        );

        try {
            String result = graphQlTester
                    .documentName("submitBatch")
                    .variable("input", input)
                    .execute()
                    .path("submitBatch.message")
                    .entity(String.class)
                    .get();
            repositorySteps.setBatchSubmissionResult(result);
        } catch (AssertionError e) {
            repositorySteps.setBatchSubmissionError(e.getMessage());
        }
    }
}
