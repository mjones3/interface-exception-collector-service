package com.arcone.biopro.distribution.irradiation.verification.api.steps;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.dto.CheckDigitResponseDTO;
import com.arcone.biopro.distribution.irradiation.application.dto.IrradiationInventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.irradiation.dto.BatchSubmissionResultDTO;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateDeviceUseCase;
import com.arcone.biopro.distribution.irradiation.verification.api.support.IrradiationContext;
import com.arcone.biopro.distribution.irradiation.verification.common.GraphQlHelper;
import com.arcone.biopro.distribution.irradiation.verification.common.GraphQlResponse;
import com.arcone.biopro.distribution.irradiation.verification.utils.CheckDigitUtil;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.HashMap;
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
    private BatchSubmissionResultDTO result;
    private String errorMessage;

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

    @When("I submit the batch for irradiation of non-imported products")
    public void iSubmitTheBatchForIrradiationNonImportedProducts() {
        String deviceId = repositorySteps.getBatchDeviceId();
        String startTime = repositorySteps.getBatchStartTime();
        List<Map<String, String>> batchItems = repositorySteps.getBatchItems();

        List<Map<String, Object>> batchItemsWithBloodCenter = batchItems.stream()
            .map(item -> Map.<String, Object>of(
                "unitNumber", item.get("unitNumber"),
                "productCode", item.get("productCode"),
                "lotNumber", item.get("lotNumber")
            ))
            .toList();

        Map<String, Object> input = Map.of(
            "deviceId", deviceId,
            "startTime", startTime.replace("Z", ""),
            "batchItems", batchItemsWithBloodCenter
        );
        Map<String, Object> variables = new HashMap<>();
        variables.put("input", input);

        GraphQlResponse<BatchSubmissionResultDTO> response = graphQlHelper.executeQuery(
                "submitBatch", variables, "submitBatch", BatchSubmissionResultDTO.class);

        if (response.hasErrors()) {
           errorMessage = response.getErrors().get(0).getMessage();
           repositorySteps.setBatchSubmissionError(errorMessage);
        } else {
           result = response.getData();
           repositorySteps.setBatchSubmissionResult(result);
        }
    }
}
