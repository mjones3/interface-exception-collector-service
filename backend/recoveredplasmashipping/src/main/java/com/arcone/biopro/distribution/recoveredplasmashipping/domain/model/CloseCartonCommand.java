package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class CloseCartonCommand implements Validatable {

    private Long cartonId;
    private String employeeId;
    private String locationCode;

    public CloseCartonCommand(Long cartonId, String employeeId, String locationCode) {
        this.cartonId = cartonId;
        this.employeeId = employeeId;
        this.locationCode = locationCode;
        checkValid();
    }

    @Override
    public void checkValid() {
        if(cartonId == null ){
            throw new IllegalArgumentException("Carton ID is required");
        }

        if(employeeId == null || employeeId.isBlank()){
            throw new IllegalArgumentException("Employee ID is required");
        }

        if(locationCode == null || locationCode.isBlank()){
            throw new IllegalArgumentException("Location code is required");
        }
    }
}
