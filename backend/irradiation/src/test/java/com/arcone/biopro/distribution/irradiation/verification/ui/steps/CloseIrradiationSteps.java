package com.arcone.biopro.distribution.irradiation.verification.ui.steps;

import com.arcone.biopro.distribution.irradiation.verification.ui.pages.CloseIrradiationPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.AllArgsConstructor;
import org.junit.Assert;

@AllArgsConstructor
public class CloseIrradiationSteps {
    private final CloseIrradiationPage closeIrradiationPage;

    @When("On the Close Irradiation page, I click to select all units in the batch")
    public void onTheRecordInspectionWindowIClickToSelectAllUnitsInTheBatch() throws Exception {
        closeIrradiationPage.selectAllUnits();
    }

    @And("On the Close Irradiation page, I click on Record Inspection")
    public void onTheRecordInspectionWindowIClickOnReCordInspection() throws Exception {
        closeIrradiationPage.openRecordInspection();
    }

    @Then("On the Record Inspection window, I verify that Record Inspection window is displayed")
    public void onTheRecordInspectionWindowIVerifyThatRecordInspectionWindowIsDisplayed() {
        Assert.assertTrue("Record Inspection window not displayed", closeIrradiationPage.recordInspectionWindowIsDisplayed());
    }

    @When("On the Record Inspection window, I select Irradiated status")
    public void onTheRecordInspectionWindowISelectIrradiatedStatus() throws Exception {
        closeIrradiationPage.selectIrradiatedStatus();
    }

    @And("On the Record Inspection window, I click on Submit")
    public void onTheRecordInspectionWindowIClickOnSubmit() throws Exception {
        closeIrradiationPage.submitRecordInspection();
    }

    @When("On the Record Inspection window, I select Not Irradiated status")
    public void onTheRecordInspectionWindowISelectNotIrradiatedStatus() throws Exception {
        closeIrradiationPage.selectNotIrradiatedStatus();
    }
}
