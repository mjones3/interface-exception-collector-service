package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.imported.ProductsImported;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.imported.ProductsImportedMessageMapper;
import com.arcone.biopro.distribution.inventory.application.dto.*;
import com.arcone.biopro.distribution.inventory.application.usecase.*;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UseCaseSteps {

    private final AddQuarantinedUseCase addQuarantinedUseCase;

    private final LabelAppliedUseCase labelAppliedUseCase;

    private final LabelInvalidatedUseCase labelInvalidatedUseCase;

    private final RemoveQuarantinedUseCase removeQuarantinedUseCase;

    private final UpdateQuarantinedUseCase updateQuarantinedUseCase;

    private final ProductRecoveredUseCase productRecoveredUseCase;

    private final ShipmentCompletedUseCase shipmentCompletedUseCase;

    private final ProductCreatedUseCase productCreatedUseCase;

    private final ProductsImportedUseCase productImportedUseCase;

    private final CheckInCompletedUseCase checkInCompletedUseCase;

    private final ProductDiscardedUseCase productDiscardedUseCase;

    private final ProductStoredUseCase productStoredUseCase;

    private final UnsuitableUseCase unsuitableUseCase;

    private final RecoveredPlasmaCartonPackedUseCase recoveredPlasmaCartonPackedUseCase;

    private final RecoveredPlasmaCartonRemovedUseCase recoveredPlasmaCartonRemovedUseCase;

    private final RecoveredPlasmaCartonUnpackedUseCase recoveredPlasmaCartonUnpackedUseCase;

    private final RecoveredPlasmaShipmentClosedUseCase recoveredPlasmaShipmentClosedUseCase;

    private final ProductCompletedUseCase productCompletedUseCase;

    private final ProductsReceivedUseCase productsReceivedUseCase;

    private final ProductModifiedUseCase productModifiedUseCase;

    private final ScenarioContext scenarioContext;

    private final InventoryUtil inventoryUtil;

    private final LogMonitor logMonitor;

    private final ProductsImportedMessageMapper importedMessageMapper;

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

        addQuarantinedUseCase.execute(new AddQuarantineInput(product, Long.parseLong(quarantineReasonId), quarantineReasonMap.get(quarantineReason), null, false)).block();
    }

    @When("I received an Apply Quarantine event for unit {string} and product {string} with reason {string}, id {string} and stopManufacturing mark as {string}")
    public void iReceiveApplyQuarantineWithReasonToTheUnitAndTheProduct(String unitNumber, String productCode, String quarantineReason, String quarantineReasonId, String stopManufacturing) {

        Product product = Product.builder()
            .unitNumber(unitNumber)
            .productCode(productCode)
            .build();
        boolean stopManufacturingInput = Boolean.parseBoolean(stopManufacturing);
        addQuarantinedUseCase.execute(new AddQuarantineInput(product, Long.parseLong(quarantineReasonId), quarantineReasonMap.get(quarantineReason), null, stopManufacturingInput)).block();
    }

    @When("I received a Remove Quarantine event for unit {string} and product {string} with reason {string} and id {string}")
    public void iReceivedARemoveQuarantineEventForUnitAndProductWithReason(String unitNumber, String productCode, String quarantineReason, String quarantineReasonId) {
        Product product = Product.builder()
            .unitNumber(unitNumber)
            .productCode(productCode)
            .build();

        removeQuarantinedUseCase.execute(new RemoveQuarantineInput(product, Long.parseLong(quarantineReasonId))).block();
    }

    @When("I received a Update Quarantine event for unit {string} and product {string} with reason {string}, id {string} and stopManufacturing {string}")
    public void iReceivedAUpdateQuarantineEventForUnitAndProductWithReason(String unitNumber, String productCode, String quarantineReason, String quarantineReasonId, String stopManufacturing) {
        Product product = Product.builder()
            .unitNumber(unitNumber)
            .productCode(productCode)
            .build();

        updateQuarantinedUseCase.execute(new UpdateQuarantineInput(product, Long.parseLong(quarantineReasonId),quarantineReason,"", Boolean.parseBoolean(stopManufacturing))).block();
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

    @When("I received a Label Invalidated event for the following products:")
    public void iReceivedALabelInvalidatedEventForTheFollowingProducts(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> product : products) {
            String unitNumber = product.get("Unit Number");
            String productCode = product.get("Product Code");

            LabelInvalidatedInput input = LabelInvalidatedInput.builder().unitNumber(unitNumber).productCode(productCode).build();
            labelInvalidatedUseCase.execute(input).block();
        }
    }

    @When("I received a Shipment Completed event with shipment type {string} for the following units:")
    public void iReceivedAShipmentCompletedEventForTheFollowingUnits(String shipmentType, DataTable dataTable) {
        this.iReceivedAShipmentCompletedEventWithShipmentTypeAndLocationCodeForTheFollowingUnits(shipmentType, null, dataTable);
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

    @When("I received a Product Imported event for the following products:")
    public void iReceivedAProductImportedEventForTheFollowingProducts(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> product : products) {

            List<ProductsImported.ImportedConsequence> consequences = null;
            if (product.get("Quarantines") != null) {
                consequences = List.of(ProductsImported.ImportedConsequence.builder()
                    .consequenceType("QUARANTINE")
                    .consequenceReasons(Arrays.stream(product.get("Quarantines").split(",")).map(String::trim).toList())
                    .build());

            }

            Map<String, String> properties = null;
            if(product.get("Licensed") != null && product.get("Licensed").equals("true")) {
                properties = Map.of("LICENSE_STATUS", "LICENSED");
            }

            ProductsImported.ImportedProduct importedProduct = ProductsImported.ImportedProduct.builder()
                .unitNumber(product.get("Unit Number"))
                .productCode(product.get("Product Code"))
                .aboRh(AboRhType.valueOf(product.get("Abo Rh")))
                .productDescription(product.get("Product Description"))
                .productFamily(product.get("Product Family"))
                .expirationDate(LocalDateTime.parse(product.get("Expiration Date"), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .consequences(consequences)
                .properties(properties)
                .build();
            ProductsImported message = ProductsImported.builder()
                .products(List.of(importedProduct))
                .temperatureCategory(product.get("Temperature Category"))
                .locationCode(product.get("Location"))
                .build();

            productImportedUseCase.execute(importedMessageMapper.toInput(message)).block();
        }
    }

    @When("I received a CheckIn Completed event for the following products:")
    public void iReceivedACheckInCompletedEventForTheFollowingProducts(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> product : products) {
            String unitNumber = product.get("Unit Number");
            String productCode = product.get("Product Code");
            String collectionLocation = product.get("Collection Location");
            String collectionTimeZone = product.get("Collection TimeZone");
            String checkinLocation = product.get("Checkin Location");
            checkInCompletedUseCase.execute(inventoryUtil.newCheckInCompletedInput(unitNumber, productCode, collectionLocation, collectionTimeZone, checkinLocation)).block();
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

    @When("I received a Product Stored event for the following products:")
    public void iReceivedAProductStoredEventForTheFollowingProducts(DataTable dataTable) {
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
    public void iReceivedRecoveredPlasmaCartonPacked(String cartonNumber, DataTable dataTable) {

        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        List<PackedProductInput> packedProducts = new ArrayList<>();
        for (Map<String, String> product : products) {

            var unitNumber = product.get("Unit Number");
            var productCode = product.get("Product Code");
            packedProducts.add(PackedProductInput.builder()
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

    @When("I received a Recovered Plasma Carton Removed Event for carton number {string}")
    public void iReceivedRecoveredPlasmaCartonRemoved(String cartonNumber, DataTable dataTable) {

        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        List<PackedProductInput> packedProducts = new ArrayList<>();
        for (Map<String, String> product : products) {

            var unitNumber = product.get("Unit Number");
            var productCode = product.get("Product Code");
            packedProducts.add(PackedProductInput.builder()
                .unitNumber(unitNumber)
                .productCode(productCode)
                .status("UNPACKED")
                .build());
        }

        RecoveredPlasmaCartonRemovedInput input = RecoveredPlasmaCartonRemovedInput.builder()
            .cartonNumber(cartonNumber)
            .packedProducts(packedProducts)
            .build();
        recoveredPlasmaCartonRemovedUseCase.execute(input).block();
    }

    @When("I received a Recovered Plasma Carton Unpacked Event for carton number {string}")
    public void iReceivedRecoveredPlasmaCartonUnpacked(String cartonNumber, DataTable dataTable) {

        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        List<PackedProductInput> unpackedProducts = new ArrayList<>();
        for (Map<String, String> product : products) {

            var unitNumber = product.get("Unit Number");
            var productCode = product.get("Product Code");
            unpackedProducts.add(PackedProductInput.builder()
                .unitNumber(unitNumber)
                .productCode(productCode)
                .status("UNPACKED")
                .build());
        }

        RecoveredPlasmaCartonUnpackedInput input = RecoveredPlasmaCartonUnpackedInput.builder()
            .cartonNumber(cartonNumber)
            .unpackedProducts(unpackedProducts)
            .build();
        recoveredPlasmaCartonUnpackedUseCase.execute(input).block();
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
                case "Label Invalidated":
                    iReceivedALabelInvalidatedEventForTheFollowingProducts(dataTable);
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
                    break;
                case "Recovered Plasma Carton Packed":
                    iReceivedRecoveredPlasmaCartonPacked("CN001", dataTable);
                    break;
                case "Recovered Plasma Carton Removed":
                    iReceivedRecoveredPlasmaCartonRemoved("CN001", dataTable);
                    break;
                case "Recovered Plasma Carton Unpacked":
                    iReceivedRecoveredPlasmaCartonUnpacked("CN001", dataTable);
                    break;
                case "Recovered Plasma Carton Closed":
                    iReceivedARecoveredPlasmaShipmentClosedEvent(dataTable);
                    break;
                case "Product Modified":
                    iReceivedAProductModifiedEventForTheFollowingProducts(dataTable);
                    break;
                case "Product Imported":
                    iReceivedAProductImportedEventForTheFollowingProducts(dataTable);
                    break;
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

    @When("I received a Recovered Plasma Shipment Closed Event")
    public void iReceivedARecoveredPlasmaShipmentClosedEvent(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        List<ShipmentPackedProductInput> packedProducts = new ArrayList<>();
        for (Map<String, String> product : products) {
            var unitNumber = product.get("Unit Number");
            var productCode = product.get("Product Code");
            packedProducts.add(ShipmentPackedProductInput.builder()
                .unitNumber(unitNumber)
                .productCode(productCode)
                .status("SHIPPED")
                .build());
        }
        List<CartonInput> cartons = new ArrayList<>();
        cartons.add(CartonInput.builder()
            .cartonNumber("CN001")
            .packedProducts(packedProducts)
            .build());
        RecoveredPlasmaShipmentClosedInput input = RecoveredPlasmaShipmentClosedInput.builder()
            .cartonList(cartons)
            .shipmentNumber("CN001")
            .build();
        recoveredPlasmaShipmentClosedUseCase.execute(input).block();
    }

    @When("I received a Product Modified event for the following products:")
    public void iReceivedAProductModifiedEventForTheFollowingProducts(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> product : products) {
            String unitNumber = product.get("Unit Number");
            String productCode = product.get("Product Code");
            String productDescription = product.get("Product Description");
            String parentProductCode = product.get("Parent Product Code");
            String productFamily = product.get("Product Family");
            String expirationDate = product.get("Expiration Date");
            String expirationTime = product.get("Expiration Time");
            String modificationLocation = product.get("Modification Location");
            String modificationDateStr = product.get("Modification Date");
            String modificationTimeZone = product.get("Modification Time Zone");
            Integer volume = null;
            Integer weight = null;

            if (product.containsKey("Volume")) {
                volume = Integer.valueOf(product.get("Volume"));
            }

            if (product.containsKey("Weight")) {
                weight = Integer.valueOf(product.get("Weight"));
            }
            Map<String, String> properties = new HashMap<>();
            if (product.get("Properties") != null) {
                properties.put(product.get("Properties").split("=")[0], product.get("Properties").split("=")[1]);
            }
            // Parse the modification date
            ZonedDateTime modificationDate;
            try {
                // Parse the date in MM/dd/yyyy format
                java.time.LocalDate localDate = java.time.LocalDate.parse(
                    modificationDateStr,
                    java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy")
                );
                // Convert to ZonedDateTime
                modificationDate = localDate.atStartOfDay(ZoneOffset.UTC);
            } catch (Exception e) {
                // Fallback to current time if parsing fails
                modificationDate = java.time.ZonedDateTime.now();
                log.warn("Failed to parse modification date: {}, using current time instead", modificationDateStr, e);
            }

            // Create ProductModifiedInput object
            ProductModifiedInput productModifiedInput = new ProductModifiedInput(
                unitNumber,
                productCode,
                productDescription,
                parentProductCode,
                productFamily,
                expirationDate,
                expirationTime,
                modificationLocation,
                modificationDate,
                volume,
                weight,
                modificationTimeZone,
                properties
            );

            // Execute the use case
            productModifiedUseCase.execute(productModifiedInput).block();
        }
    }

    @When("I received a Shipment Completed event with shipment type {string} and location code {string} for the following units:")
    public void iReceivedAShipmentCompletedEventWithShipmentTypeAndLocationCodeForTheFollowingUnits(String shipmentType, String locationCode, DataTable dataTable) {
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
            locationCode,
            lines);
        shipmentCompletedUseCase.execute(input).block();
    }

    @When("I received a Product Completed event with location code {string} for the following units:")
    public void iReceivedAProductCompletedEventWithStatusAsAndLocationCodeForTheFollowingUnits(String location, DataTable dataTable) {
        List<Map<String, String>> inventories = dataTable.asMaps(String.class, String.class);
        List<ProductReceivedInput> products = new ArrayList<>();
        for (Map<String, String> inventory : inventories) {
            String unitNumber = inventory.get("Unit Number");
            String productCode = inventory.get("Product Code");
            products.add(ProductReceivedInput.builder()
                .unitNumber(unitNumber)
                .productCode(productCode)
                .inventoryLocation(location)
                .quarantines("")
                .build());
        }
        ProductsReceivedInput input = ProductsReceivedInput.builder()
            .locationCode(location)
            .products(products)
            .build();
        productsReceivedUseCase.execute(input).block();
    }
}
