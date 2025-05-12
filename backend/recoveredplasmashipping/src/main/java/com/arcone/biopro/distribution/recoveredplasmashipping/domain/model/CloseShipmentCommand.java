package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class CloseShipmentCommand implements Validatable {

    private Long shipmentId;
    private String employeeId;
    private String locationCode;
    private LocalDate shipDate;

    public CloseShipmentCommand(Long shipmentId, String employeeId, String locationCode, LocalDate shipDate) {
        this.shipmentId = shipmentId;
        this.employeeId = employeeId;
        this.locationCode = locationCode;
        this.shipDate = shipDate;

        checkValid();
    }

    @Override
    public void checkValid() {
        if (shipmentId == null) {
            throw new IllegalArgumentException("Shipment ID is required");
        }

        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Employee ID is required");
        }

        if (locationCode == null || locationCode.isBlank()) {
            throw new IllegalArgumentException("Location code is required");
        }

        if (shipDate == null) {
            throw new IllegalArgumentException("Ship date is required");
        }
        if (shipDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ship date cannot be in the past");
        }
    }
}
