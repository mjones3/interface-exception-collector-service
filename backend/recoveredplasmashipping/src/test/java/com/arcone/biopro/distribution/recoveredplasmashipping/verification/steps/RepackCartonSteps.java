package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CartonTestingController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages.ShipmentDetailsPage;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.TestUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@Slf4j
public class RepackCartonSteps {

    @Autowired
    private CartonTestingController cartonTestingController;

    @Autowired
    private SharedContext sharedContext;

    @Autowired
    private TestUtils testUtils;

    @Value("${default.employee.id}")
    private String employeeId;

    private Map apiResponse;

    @Autowired
    private ShipmentDetailsPage shipmentDetailsPage;


    @And("I have a closed carton with the unit numbers as {string} and product codes as {string} and product types {string} which were flagged as repack.")
    public void iHaveAClosedCartonWithTheUnitNumbersAsAndProductCodesAsAndProductTypesWhichWereFlaggedAsRepack(String unitNumbers, String productCodes, String productTypes) {

        createCarton("REPACK",unitNumbers, productCodes, productTypes);
    }

    private void createCarton(String status , String unitNumbers, String productCodes, String productTypes) {
        cartonTestingController.createCarton(sharedContext.getShipmentCreateResponse().get("id").toString());
        Assertions.assertNotNull(sharedContext.getCreateCartonResponseList());

        String cartonId = sharedContext.getCreateCartonResponseList().getFirst().get("id").toString();
        String[] unitNumbersArray = testUtils.getCommaSeparatedList(unitNumbers);
        String[] productCodesArray = testUtils.getCommaSeparatedList(productCodes);
        String[] productTypesArray = testUtils.getCommaSeparatedList(productTypes);

        for (int i = 0; i < unitNumbersArray.length; i++) {
            cartonTestingController.insertVerifiedProduct(cartonId, unitNumbersArray[i], productCodesArray[i], productTypesArray[i]);
        }
        cartonTestingController.updateCartonStatus(cartonId,status);
    }


    @When("I request to repack the carton with reason {string}.")
    public void iRequestToRepackTheCartonWithReason(String reason) {

        if("<comment_greater_than_250>".equals(reason)){
            reason = "a".repeat(251);
        }else if("<null>".equals(reason)){
            reason = null;
        }

       this.apiResponse =  cartonTestingController.repackCarton(
            sharedContext.getCreateCartonResponseList().getFirst().get("id").toString(),
            employeeId,
            sharedContext.getLocationCode(),
            reason
        );

        Assert.assertNotNull(apiResponse);

    }

    @And("I have a closed carton with the unit numbers as {string} and product codes as {string} and product types {string}.")
    public void iHaveAClosedCartonWithTheUnitNumbersAsAndProductCodesAsAndProductTypes(String unitNumbers, String productCodes, String productTypes) {
        createCarton("CLOSED",unitNumbers, productCodes, productTypes);
    }

    @When("I request the last carton created.")
    public void iRequestTheLastCartonCreated() {
        this.apiResponse = cartonTestingController.findCartonById(Integer.parseInt(sharedContext.getCreateCartonResponseList().getFirst().get("id").toString()));
        Assert.assertNotNull(apiResponse);
    }

    @And("The products unit number {string} and product code {string} {string} be packed in the carton.")
    public void theProductsUnitNumberAndProductCodeBePackedInTheCarton(String unitNumbers, String productCodes, String option) {

        String[] unitNumbersArray = testUtils.getCommaSeparatedList(unitNumbers);
        String[] productCodesArray = testUtils.getCommaSeparatedList(productCodes);

        for (int i = 0; i < unitNumbersArray.length; i++) {

            if (option.equals("should")) {
                Assert.assertTrue(cartonTestingController.checkProductIsPacked(unitNumbersArray[i], productCodesArray[i]));
            } else if (option.equals("should not")) {
                Assert.assertFalse(cartonTestingController.checkProductIsPacked(unitNumbersArray[i], productCodesArray[i]));
            } else {
                Assert.fail("The option " + option + " is not valid.");
            }
        }
    }

    @And("The repack option should be available for the carton number prefix {string} and sequence number {string} and status {string}.")
    public void theRepackOptionShouldBeAvailableForTheCartonSequenceNumber(String cartonPrefix , String cartonNumber , String status) {
        Assert.assertTrue(shipmentDetailsPage.isRepackButtonEnabled(cartonPrefix,cartonNumber,status) );
    }

    @When("I choose to repack the carton number prefix {string} and sequence number {string} and status {string}.")
    public void iChooseToRepackTheCartonNumberPrefixAndSequenceNumberAndStatus(String cartonPrefix , String cartonNumber , String status) {
        shipmentDetailsPage.clickRepackButton(cartonPrefix,cartonNumber,status);
    }

    @When("I choose cancel the repack carton.")
    public void iChooseCancelTheRepackCarton() {
        shipmentDetailsPage.clickCancelButton();
    }

    @Then("The status of the carton number prefix {string} and sequence number {string} should be {string}.")
    public void theStatusOfTheCartonNumberPrefixAndSequenceNumberShouldBe(String cartonPrefix , String cartonNumber , String status) {
        Assert.assertEquals(shipmentDetailsPage.getCartonStatus(cartonPrefix,cartonNumber), status );
    }

    @And("I enter reason comments {string}.")
    public void iEnterReasonComments(String comments) {
    shipmentDetailsPage.enterRepackComments(comments);
    }

    @When("I confirm to repack the carton.")
    public void iConfirmToRepackTheCarton() {
    shipmentDetailsPage.clickConfirmRepackCarton();
    }
}

