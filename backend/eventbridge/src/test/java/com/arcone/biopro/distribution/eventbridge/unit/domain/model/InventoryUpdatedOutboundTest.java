package com.arcone.biopro.distribution.eventbridge.unit.domain.model;

import com.arcone.biopro.distribution.eventbridge.domain.model.InventoryUpdatedOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.model.ShipmentCompletedOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.model.ShipmentLineItem;
import com.arcone.biopro.distribution.eventbridge.domain.model.vo.ShipmentCustomer;
import com.arcone.biopro.distribution.eventbridge.domain.model.vo.ShipmentLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

class InventoryUpdatedOutboundTest {

    @Test
    public void shouldCreateDomain(){
        var target = new InventoryUpdatedOutbound(
            "CREATED",
            "W035625205983",
            "E067800",
            "APH AS3 LR",
            "RED_BLOOD_CELLS_LEUKOREDUCED",
             "OP",
            LocalDate.now(),
            "MIAMI",
            "REFRIG 1",
            List.of("AVAILABLE"),
            Map.of("HGBS","N"));
        Assertions.assertNotNull(target);
    }

    @Test
    public void shouldNotCreateDomain(){
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new InventoryUpdatedOutbound(
                null,
                "W035625205983",
                "E067800",
                "APH AS3 LR",
                "RED_BLOOD_CELLS_LEUKOREDUCED",
                "OP",
                LocalDate.now(),
                "MIAMI",
                "REFRIG 1",
                List.of("AVAILABLE"),
                Map.of("HGBS","N")));
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new InventoryUpdatedOutbound(
                "CREATED",
                null,
                "E067800",
                "APH AS3 LR",
                "RED_BLOOD_CELLS_LEUKOREDUCED",
                "OP",
                LocalDate.now(),
                "MIAMI",
                "REFRIG 1",
                List.of("AVAILABLE"),
                Map.of("HGBS","N")));
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new InventoryUpdatedOutbound(
                "CREATED",
                "W035625205983",
                null,
                "APH AS3 LR",
                "RED_BLOOD_CELLS_LEUKOREDUCED",
                "OP",
                LocalDate.now(),
                "MIAMI",
                "REFRIG 1",
                List.of("AVAILABLE"),
                Map.of("HGBS","N")));
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new InventoryUpdatedOutbound(
                "CREATED",
                "W035625205983",
                "E067800",
                "APH AS3 LR",
                null,
                "OP",
                LocalDate.now(),
                "MIAMI",
                "REFRIG 1",
                List.of("AVAILABLE"),
                Map.of("HGBS","N")));
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new InventoryUpdatedOutbound(
                "CREATED",
                "W035625205983",
                "E067800",
                "APH AS3 LR",
                "RED_BLOOD_CELLS_LEUKOREDUCED",
                "OP",
                null,
                "MIAMI",
                "REFRIG 1",
                List.of("AVAILABLE"),
                Map.of("HGBS","N")));
    }


}
