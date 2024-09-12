package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.application.dto.AddQuarantineInput;
import com.arcone.biopro.distribution.inventory.application.dto.Product;
import com.arcone.biopro.distribution.inventory.application.dto.ProductRecoveredInput;
import com.arcone.biopro.distribution.inventory.application.dto.RemoveQuarantineInput;
import com.arcone.biopro.distribution.inventory.application.usecase.AddQuarantinedUseCase;
import com.arcone.biopro.distribution.inventory.application.usecase.ProductRecoveredUseCase;
import com.arcone.biopro.distribution.inventory.application.usecase.RemoveQuarantinedUseCase;
import com.arcone.biopro.distribution.inventory.verification.common.ScenarioContext;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Slf4j
public class UseCaseSteps {

    @Autowired
    private AddQuarantinedUseCase addQuarantinedUseCase;

    @Autowired
    private RemoveQuarantinedUseCase removeQuarantinedUseCase;

    @Autowired
    private ProductRecoveredUseCase productRecoveredUseCase;

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

       Product product = Product.builder()
            .unitNumber(unitNumber)
            .productCode(productCode)
            .build();

        addQuarantinedUseCase.execute(new AddQuarantineInput(product, Long.parseLong(quarantineReasonId), quarantineReasonMap.get(quarantineReason), null)).block();
    }

    @When("I received a Remove Quarantine event for unit {string} and product {string} with reason {string} and id {string}")
    public void iReceivedARemoveQuarantineEventForUnitAndProductWithReason(String unitNumber, String productCode, String quarantineReason, String quarantineReasonId) {
        Product product = Product.builder()
            .unitNumber(unitNumber)
            .productCode(productCode)
            .build();

        removeQuarantinedUseCase.execute(new RemoveQuarantineInput(product, Long.parseLong(quarantineReasonId))).block();
    }

    @When("I received a Product Recovered event")
    public void iReceivedAEvent() {
        productRecoveredUseCase.execute(new ProductRecoveredInput(scenarioContext.getUnitNumber(), scenarioContext.getProductCode() )).block();
    }
}
