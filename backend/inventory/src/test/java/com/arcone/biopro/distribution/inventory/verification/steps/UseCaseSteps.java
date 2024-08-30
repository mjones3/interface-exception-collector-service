package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.verification.common.ScenarioContext;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
public class UseCaseSteps {

    @Autowired
    private ScenarioContext scenarioContext;

    public static final Map<String, String> quarantineReasonMap = Map.of(
        "ABS Positive","ABS_POSITIVE",
        "BCA Unit Needed","BCA_UNIT_NEEDED",
        "CCP Eligible","CCP_ELIGIBLE",
        "Failed Visual Inspection","FAILED_VISUAL_INSPECTION",
        "Hold Until Expiration","HOLD_UNTIL_EXPIRATION",
        "In Process Hold","IN_PROCESS_HOLD",
        "Pending Further Review Inspection","PENDING_FURTHER_REVIEW_INSPECTION",
        "Save Plasma for CTS","SAVE_PLASMA_FOR_CTS",
        "Other","OTHER_SEE_COMMENTS",
        "Under Investigation","UNDER_INVESTIGATION"
    );

    @When("I received an Apply Quarantine event for unit {string} and product {string} with reason {string} and id {string}")
    public void iReceiveApplyQuarantineWithReasonToTheUnitAndTheProduct(String unitNumber, String productCode, String quarantineReason, String quarantineReasonId) {

//        Product product = Product.builder()
//            .unitNumber(new UnitNumber(unitNumber))
//            .productCode(new ProductCode(productCode, scenarioContext.getProduct().getShortDescription()))
//            .build();
//        applyQuarantineProductUseCase.execute(new QuarantineProductInput(product, QuarantineReason.valueOf(quarantineReasonMap.get(quarantineReason)),quarantineReasonId)).block();
        fail("Not yet implemented");
    }

    @When("I received a Remove Quarantine event for unit {string} and product {string} with reason {string} and id {string}")
    public void iReceivedARemoveQuarantineEventForUnitAndProductWithReason(String unitNumber, String productCode, String quarantineReason, String quarantineReasonId) {
//        Product product = Product.builder()
//            .unitNumber(new UnitNumber(unitNumber))
//            .productCode(new ProductCode(productCode, ""))
//            .build();
//        removeQuarantinedProductUseCase.execute(new QuarantineProductInput(product, QuarantineReason.valueOf(quarantineReasonMap.get(quarantineReason)),quarantineReasonId)).block();
        fail("Not yet implemented");
    }
}
