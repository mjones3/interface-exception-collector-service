package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.application.dto.*;
import com.arcone.biopro.distribution.inventory.application.usecase.*;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.vo.InputProduct;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntity;
import com.arcone.biopro.distribution.inventory.verification.common.ScenarioContext;
import com.arcone.biopro.distribution.inventory.verification.utils.ISBTProductUtil;
import com.arcone.biopro.distribution.inventory.verification.utils.LogMonitor;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UseCaseSteps {

    private final AddQuarantinedUseCase addQuarantinedUseCase;

    private final LabelAppliedUseCase labelAppliedUseCase;

    private final RemoveQuarantinedUseCase removeQuarantinedUseCase;

    private final ProductRecoveredUseCase productRecoveredUseCase;

    private final ShipmentCompletedUseCase shipmentCompletedUseCase;

    private final ProductCreatedUseCase productCreatedUseCase;

    private final ScenarioContext scenarioContext;

    private final LogMonitor logMonitor;

    @Value("${default.location}")
    private String defaultLocation;

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

    @When("I received a Label Applied event for the following products:")
    public void iReceivedALabelAppliedEventForTheFollowingProducts(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> product : products) {
            String unitNumber = product.get("Unit Number");
            String productCode = product.get("Product Code");
            boolean isLicensed = Boolean.parseBoolean(product.get("Is licensed"));
            labelAppliedUseCase.execute(this.newInventoryInput(unitNumber, productCode, isLicensed)).block();
        }
    }

    @When("I received a Shipment Completed event for the following units:")
    public void iReceivedAShipmentCompletedEventForTheFollowingUnits(DataTable dataTable) {
        List<ShipmentCompletedInput.LineItem> lines = new ArrayList<>();
        List<ShipmentCompletedInput.LineItem.Product> products = new ArrayList<>();
        List<Map<String, String>> inventories = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> inventory : inventories) {
            String unitNumber = inventory.get("Unit Number");
            String productCode = inventory.get("Product Code");
            products.add(new ShipmentCompletedInput.LineItem.Product(unitNumber, productCode));
        }
        lines.add(new ShipmentCompletedInput.LineItem(products));
        var input = new ShipmentCompletedInput(
            "a-shipment-id",
            "an-order-number",
            "a-performed-by",
            lines);
        shipmentCompletedUseCase.execute(input).block();

    }

    @When("I received a Product Created event for the following products:")
    public void iReceivedAProductCreatedEventForTheFollowingProducts(DataTable dataTable) throws InterruptedException {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> product : products) {
            String unitNumber = product.get("Unit Number");
            String productCode = product.get("Product Code");
            String parentProductCode = product.get("Parent Product Code");
            var inputProducts = List.of(new InputProduct(unitNumber, parentProductCode));
            productCreatedUseCase.execute(this.newProductCreatedInput(unitNumber, productCode, inputProducts)).block();
            logMonitor.await("Product converted.*");
        }
    }

    private InventoryInput newInventoryInput(String unitNumber, String productCode, Boolean isLicensed) {
        return InventoryInput.builder()
            .isLicensed(isLicensed)
            .productFamily(ISBTProductUtil.getProductFamily(productCode))
            .aboRh(AboRhType.OP)
            .location(defaultLocation)
            .collectionDate(ZonedDateTime.now())
            .expirationDate(LocalDateTime.now().plusDays(1))
            .weight(100)
            .unitNumber(unitNumber)
            .productCode(productCode)
            .shortDescription(ISBTProductUtil.getProductDescription(productCode))
            .build();
    }

    private ProductCreatedInput newProductCreatedInput(String unitNumber, String productCode, List<InputProduct> inputProducts) {
        return ProductCreatedInput.builder()
            .productFamily(ISBTProductUtil.getProductFamily(productCode))
            .aboRh(AboRhType.OP)
            .location(defaultLocation)
            .collectionDate(ZonedDateTime.now())
            .expirationDate(DateTimeFormatter.ofPattern("MM/dd/yyyy").format(LocalDate.now().plusDays(1)))
            .expirationTime(LocalTime.now().toString())
            .weight(100)
            .unitNumber(unitNumber)
            .productCode(productCode)
            .productDescription(ISBTProductUtil.getProductDescription(productCode))
            .inputProducts(inputProducts)
            .build();
    }
}
