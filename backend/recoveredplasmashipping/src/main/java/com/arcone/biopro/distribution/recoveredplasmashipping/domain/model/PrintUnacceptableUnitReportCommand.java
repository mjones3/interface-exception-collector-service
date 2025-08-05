package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class PrintUnacceptableUnitReportCommand implements Validatable {
    private Long shipmentId;
    private String employeeId;
    private String locationCode;

    public PrintUnacceptableUnitReportCommand(Long shipmentId, String employeeId, String locationCode) {
        this.shipmentId = shipmentId;
        this.employeeId = employeeId;
        this.locationCode = locationCode;

        checkValid();
    }

    @Override
    public void checkValid() {
        if (shipmentId == null) {
            throw new IllegalArgumentException("Shipment id is required");
        }

        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Employee id is required");
        }

        if (locationCode == null || locationCode.isBlank()) {
            throw new IllegalArgumentException("Location code is required");
        }

    }
}
