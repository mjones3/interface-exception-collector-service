package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.application.dto.*;
import com.arcone.biopro.distribution.inventory.application.usecase.*;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ShipmentType;
import com.arcone.biopro.distribution.inventory.domain.model.vo.InputProduct;
import com.arcone.biopro.distribution.inventory.verification.common.ScenarioContext;
import com.arcone.biopro.distribution.inventory.verification.utils.InventoryUtil;
import com.arcone.biopro.distribution.inventory.verification.utils.LogMonitor;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UseCaseSteps {

    private final AddQuarantinedUseCase addQuarantinedUseCase;

    private final LabelAppliedUseCase labelAppliedUseCase;

    private final RemoveQuarantinedUseCase removeQuarantinedUseCase;

    private final ProductRecoveredUseCase productRecoveredUseCase;

    private final ShipmentCompletedUseCase shipmentCompletedUseCase;

    private final ProductCreatedUseCase productCreatedUseCase;

    private final CheckInCompletedUseCase checkInCompletedUseCase;

    private final ProductDiscardedUseCase productDiscardedUseCase;

    private final ProductStoredUseCase productStoredUseCase;

    private final UnsuitableUseCase unsuitableUseCase;

    private final RecoveredPlasmaCartonPackedUseCase recoveredPlasmaCartonPackedUseCase;

    private final ProductCompletedUseCase productCompletedUseCase;

    private final ScenarioContext scenarioContext;

    private final InventoryUtil inventoryUtil;

    private final LogMonitor logMonitor;

    @Value("${default.location}")
    private String defaultLocation;

    public static final Map<String, String> quarantineReasonMap = Map.of(
        "ABS Positive", "ABS_POSITIVE",
        "BCA Unit Needed", "BCA_UNIT_NEEDED",
        "CCP Eligible", "CCP_ELIGIBLE",
        "Failed Visual Inspection", "FAILED_VISUAL_INSPECTION",
        "Hold Until Expiration", "HOLD_UNTIL_EXPIRATION",
        "In Process Hold", "IN_PROCESS_HOLD",
        "Pending Further Review Inspection", "PENDING_FURTHER_REVIEW_INSPECTION",
        "Save Plasma for CTS", "SAVE_PLASMA_FOR_CTS",
        "Other", "OTHER_SEE_COMMENTS",
        "Under Investigation", "UNDER_INVESTIGATION"
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
        productRecoveredUseCase.execute(new ProductRecoveredInput(scenarioContext.getUnitNumber(), scenarioContext.getProductCode())).block();
    }

    @When("I received a Label Applied event for the following products:")
    public void iReceivedALabelAppliedEventForTheFollowingProducts(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> product : products) {
            String unitNumber = product.get("Unit Number");
            String productCode = product.get("Product Code");
            boolean isLicensed = Boolean.parseBoolean(product.get("Is licensed"));
            InventoryInput inventoryInput = inventoryUtil.newInventoryInput(unitNumber, productCode, isLicensed);
            labelAppliedUseCase.execute(inventoryInput).block();
        }
    }

    @When("I received a Shipment Completed event with shipment type {string} for the following units:")
    public void iReceivedAShipmentCompletedEventForTheFollowingUnits(String shipmentType, DataTable dataTable) {
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
            ShipmentType.valueOf(shipmentType),
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
            Boolean hasExpDate = BooleanUtils.toBoolean(product.get("Has Expiration Date").toLowerCase());
            var inputProducts = List.of(new InputProduct(unitNumber, parentProductCode));
            ProductCreatedInput productCreatedInput = inventoryUtil.newProductCreatedInput(unitNumber, productCode, inputProducts, hasExpDate);
            productCreatedUseCase.execute(productCreatedInput).block();
            logMonitor.await("Product converted.*");
        }
    }

    @When("I received a CheckIn Completed event for the following products:")
    public void iReceivedACheckInCompletedEventForTheFollowingProducts(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> product : products) {
            String unitNumber = product.get("Unit Number");
            String productCode = product.get("Product Code");
            checkInCompletedUseCase.execute(inventoryUtil.newCheckInCompletedInput(unitNumber, productCode)).block();
        }
    }

    @When("I received a Discard Created event for the following products:")
    public void iReceivedADiscardCreatedEventForTheFollowingProducts(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> product : products) {
            String unitNumber = product.get("Unit Number");
            String productCode = product.get("Product Code");
            String reason = product.get("Reason");
            String comments = null;
            if (product.containsKey("Comments")) {
                comments = product.get("Comments");
            }
            if (product.containsKey("Comment Length")) {
                comments = RandomStringUtils.randomAlphabetic(Integer.parseInt(product.get("Comment Length")));
            }
            productDiscardedUseCase.execute(inventoryUtil.newProductDiscardedInput(unitNumber, productCode, reason, comments)).block();
        }
    }


    @When("I received a Unit Unsuitable event with unit number {string} and reason {string}")
    public void iReceivedAUnitUnsuitableEventWithUnitNumberAndReason(String unitNumber, String reason) {
        unsuitableUseCase.execute(new UnsuitableInput(unitNumber, null, reason)).block();
    }

    @When("I received a Recovered Plasma Carton Packed Event for carton number {string}")
    public void IReceivedRecoveredPlasmaCartonPacked(String cartonNumber, DataTable dataTable) {

        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        List<RecoveredPlasmaCartonPackedInput.PackedProduct> packedProducts = new ArrayList<>();
        for (Map<String, String> product : products) {

            var unitNumber = product.get("Unit Number");
            var productCode = product.get("Product Code");
            packedProducts.add(RecoveredPlasmaCartonPackedInput.PackedProduct.builder()
                .unitNumber(unitNumber)
                .productCode(productCode)
                .status("PACKED")
                .build());
        }

        RecoveredPlasmaCartonPackedInput input = RecoveredPlasmaCartonPackedInput.builder()
            .cartonNumber(cartonNumber)
            .packedProducts(packedProducts)
            .build();
        recoveredPlasmaCartonPackedUseCase.execute(input).block();


    }

    @When("I received a Product Unsuitable event with unit number {string}, product code {string} and reason {string}")
    public void iReceivedAProductUnsuitableEventWithUnitNumberProductCodeAndReason(String unitNumber, String productCode, String reason) {
        unsuitableUseCase.execute(new UnsuitableInput(unitNumber, productCode, reason)).block();
    }

    @When("I received a Product Storage event for unit {string} and product {string} with device {string} and storageLocations {string} at location {string}")
    public void iReceivedAProductStorageEventWithUnitProductDeviceStorageLocationAndLocation(String unitNumber, String productCode, String device, String storageLocations, String location) {
        ProductStorageInput productInput = ProductStorageInput.builder()
            .unitNumber(unitNumber)
            .productCode(productCode)
            .location(location)
            .deviceStored(device)
            .storageLocation(storageLocations)
            .build();
        productStoredUseCase.execute(productInput).block();
    }

    @When("I received a {string} event for the following products:")
    public void iReceivedAEventForTheFollowingProducts(String eventType, DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> product : products) {
            var unitNumber = product.get("Unit Number");
            var productCode = product.get("Product Code");
            var reason = product.get("Reason");
            var reasonId = product.get("Reason Id");
            var shipmentType = product.get("Shipment type");
            var deviceStorage = product.get("Device Storage");
            var storageLocation = product.get("Storage Location");
            var location = product.get("Location");

            switch (eventType) {
                case "Label Applied":
                    iReceivedALabelAppliedEventForTheFollowingProducts(dataTable);
                    break;
                case "Apply Quarantine":
                    iReceiveApplyQuarantineWithReasonToTheUnitAndTheProduct(unitNumber, productCode, reason, reasonId);
                    break;
                case "Remove Quarantine":
                    iReceiveApplyQuarantineWithReasonToTheUnitAndTheProduct(unitNumber, productCode, reason, reasonId);
                    iReceivedARemoveQuarantineEventForUnitAndProductWithReason(unitNumber, productCode, reason, reasonId);
                    break;
                case "Shipment Completed":
                    iReceivedAShipmentCompletedEventForTheFollowingUnits(shipmentType, dataTable);
                    break;
                case "Discard Created":
                    iReceivedADiscardCreatedEventForTheFollowingProducts(dataTable);
                    break;
                case "Product Stored":
                    iReceivedAProductStorageEventWithUnitProductDeviceStorageLocationAndLocation(unitNumber, productCode, deviceStorage, storageLocation, location);
                default:
                    break;
            }
        }
    }

    @When("I received a Product Completed event for the following products:")
    public void iReceivedAProductCompletedEventForTheFollowingProducts(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> product : products) {
            String unitNumber = product.get("Unit Number");
            String productCode = product.get("Product Code");
            Integer volume = Integer.valueOf(product.get("Volume"));
            Integer anticoagulantVolume = Integer.valueOf(product.get("Anticoagulant Volume"));
            productCompletedUseCase.execute(inventoryUtil.newProductCompletedInput(unitNumber, productCode, volume, anticoagulantVolume, "ml")).block();
        }
    }
}
