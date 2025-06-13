package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class GenerateCartonPackingSlipCommand implements Validatable {

    private String cartonId;
    private String employeeId;
    private String locationCode;


    @Override
    public void checkValid() {
        if(cartonId == null){
            throw new IllegalArgumentException("Carton id is required");
        }
        if(employeeId == null || employeeId.isBlank()){
            throw new IllegalArgumentException("Employee id is required");
        }
        if(locationCode == null || locationCode.isBlank()){
            throw new IllegalArgumentException("Location code is required");
        }
    }
}
