package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class CreateCartonCommand implements Validatable {

    private final Long recoveredPlasmaShipmentId;
    private final String createEmployeeId;

    public CreateCartonCommand(Long recoveredPlasmaShipmentId, String createEmployeeId) {
        this.recoveredPlasmaShipmentId = recoveredPlasmaShipmentId;
        this.createEmployeeId = createEmployeeId;
        checkValid();
    }

    @Override
    public void checkValid() {

        if(recoveredPlasmaShipmentId == null ){
            throw new IllegalArgumentException("Shipment is required");
        }

        if(createEmployeeId == null || createEmployeeId.isBlank()){
            throw new IllegalArgumentException("Create employee ID is required");
        }
    }
}
