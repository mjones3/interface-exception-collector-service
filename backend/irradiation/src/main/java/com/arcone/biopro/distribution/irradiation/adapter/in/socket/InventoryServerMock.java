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

    @MessageMapping("getInventoryByUnitNumber")
    public Flux<InventoryOutput> getInventoryByUnitNumber(String unitNumber) {
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
