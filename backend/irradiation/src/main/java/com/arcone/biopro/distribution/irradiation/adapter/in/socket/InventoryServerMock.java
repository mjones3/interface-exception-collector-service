package com.arcone.biopro.distribution.irradiation.adapter.in.socket;

import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.client.InventoryOutput;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.client.InventoryQuarantineOutput;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.List;

@Controller
@Slf4j
@Profile("rsocket-server")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryServerMock {

    private static final String SCAN_UNIT_FEATURE_UN = "W777725001";
    private static final String IRRADIATION_START_FEATURE_UN = "W777725002";

    @MessageMapping("getInventoryByUnitNumber")
    public Flux<InventoryOutput> getInventoryByUnitNumber(String unitNumber) {
        if (unitNumber.startsWith("W777725001011")) {
            return Flux.just(
                createInventory("W777725001011","E003200", "AVAILABLE", "123456789")
            );
        }
        if (unitNumber.startsWith("W777725001012")) {
            return Flux.just(
                createInventory("W777725001012","E0869V00", "AVAILABLE", "123456789")
            );
        }
        if (unitNumber.startsWith("W777725001013")) {
            return Flux.just(
                createInventory("W777725001013","E003300", "AVAILABLE", "123456789")
            );
        }
        if (unitNumber.startsWith(IRRADIATION_START_FEATURE_UN)) {
            return Flux.just(
                createInventory("W777725002001","E033600", "AVAILABLE", "123456789","AS1 LR RBC"),
                createInventory("W777725002002","E068600", "AVAILABLE", "123456789","APH AS3 LR RBC C2")
            );
        }
        if (unitNumber.startsWith(SCAN_UNIT_FEATURE_UN)) {
            return Flux.just(
                createInventory("W777725001001","E0869V00", "AVAILABLE", "123456789","APH FFP"),
                createInventory("W777725001001","E1624V00", "IN_TRANSIT", "123456789","APH PF24"),
                createInventory("W777725001001","E4689V00", "SHIPPED", "123456789","APH FFP C1"),
                createInventory("W777725001001","E0686V00", "CONVERTED", "123456789","APH AS3 LR RBC C2"),
                createInventory("W777725001001","E2555V00", "MODIFIED", "123456789","PF24"),
                createInventory("W777725001001","E7644V00", "AVAILABLE", "234567891","APH RT24 PF24")
            );
        } else {
            return Flux.just(
                InventoryOutput.builder()
                    .unitNumber("W777725001001")
                    .productCode("E0869V00")
                    .location("123456789")
                    .inventoryStatus("AVAILABLE")
                    .productDescription("Blood Sample Type A")
                    .productFamily("WHOLE_BLOOD")
                    .shortDescription("Type A Sample")
                    .isLabeled(true)
                    .statusReason("In Stock")
                    .unsuitableReason(null)
                    .expired(false)
                    .quarantines(List.of(InventoryQuarantineOutput.builder()
                            .reason("UNDER_INVESTIGATION")
                            .stopsManufacturing(true)
                            .build(),
                        InventoryQuarantineOutput.builder()
                            .reason("WHATEVER")
                            .stopsManufacturing(false)
                            .build()))
                    .build(),

                InventoryOutput.builder()
                    .unitNumber("W777725001001")
                    .productCode("E0868V00")
                    .location("123456789")
                    .inventoryStatus("AVAILABLE")
                    .productDescription("Blood Sample Type A")
                    .productFamily("WHOLE_BLOOD")
                    .shortDescription("Type A Sample")
                    .isLabeled(true)
                    .statusReason("Quality Check In Progress")
                    .unsuitableReason(null)
                    .expired(false)
                    .quarantines(List.of(
                        InventoryQuarantineOutput.builder()
                            .reason("WHATEVER")
                            .stopsManufacturing(false)
                            .build()))
                    .build(),

                InventoryOutput.builder()
                    .unitNumber("W777725001001")
                    .productCode("E0867V00")
                    .location("123456789")
                    .inventoryStatus("DISCARDED")
                    .productDescription("Blood Sample Type DISCARDED")
                    .productFamily("WHOLE_BLOOD")
                    .shortDescription("Type A Sample")
                    .isLabeled(true)
                    .statusReason("Temperature deviation detected")
                    .unsuitableReason(null)
                    .expired(false)
                    .build(),

                InventoryOutput.builder()
                    .unitNumber("W777725001001")
                    .productCode("E0867V02")
                    .location("123456789")
                    .inventoryStatus("AVAILABLE")
                    .productDescription("Blood Sample Type UNSUITABLE")
                    .productFamily("WHOLE_BLOOD")
                    .shortDescription("Type A Sample")
                    .isLabeled(true)
                    .statusReason("Temperature deviation detected")
                    .unsuitableReason("Temperature out of range")
                    .expired(false)
                    .build(),

                InventoryOutput.builder()
                    .unitNumber("W777725001001")
                    .productCode("E0867V03")
                    .location("123456789")
                    .inventoryStatus("AVAILABLE")
                    .productDescription("Blood Sample Type EXPIRED")
                    .productFamily("WHOLE_BLOOD")
                    .shortDescription("Type A Sample")
                    .isLabeled(true)
                    .statusReason("Temperature deviation detected")
                    .unsuitableReason(null)
                    .expired(true)
                    .build()
            );
        }
    }

    private static InventoryOutput createInventory(String unitNumber, String productCode, String status, String location) {
        return createInventory(unitNumber, productCode, status, location, "A product description");
    }

    private static InventoryOutput createInventory(String unitNumber, String productCode, String status, String location, String productDescription) {
        return InventoryOutput.builder()
            .unitNumber(unitNumber)
            .productCode(productCode)
            .location(location)
            .inventoryStatus(status)
            .productDescription(productDescription)
            .productFamily("WHOLE_BLOOD")
            .shortDescription("Type A Sample")
            .isLabeled(true)
            .statusReason("In Stock")
            .unsuitableReason(null)
            .expired(false)
            .build();
    }
}
