package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class FindShipmentCommand implements Validatable {

    private final Long shipmentId;
    private final String locationCode;
    private final String employeeId;

    public FindShipmentCommand(Long shipmentId, String locationCode, String employeeId) {
        this.shipmentId = shipmentId;
        this.locationCode = locationCode;
        this.employeeId = employeeId;

        checkValid();
    }

    @Override
    public void checkValid() {

        if(shipmentId == null){
            throw new IllegalArgumentException("Shipment ID is required");
        }

        if(locationCode == null || locationCode.isBlank()){
            throw new IllegalArgumentException("Location code is required");
        }

        if(employeeId == null || employeeId.isBlank()){
            throw new IllegalArgumentException("Employee ID is required");
        }
    }
}
