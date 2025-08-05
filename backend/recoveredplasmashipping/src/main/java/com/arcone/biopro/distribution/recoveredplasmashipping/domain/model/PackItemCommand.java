package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class PackItemCommand implements Validatable {

    private final Long cartonId;
    private final String unitNumber;
    private final String productCode;
    private final String employeeId;
    private final String locationCode;

    public PackItemCommand(Long cartonId, String unitNumber, String productCode, String employeeId , String locationCode) {
        this.cartonId = cartonId;
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        this.employeeId = employeeId;
        this.locationCode = locationCode;

        checkValid();
    }

    @Override
    public void checkValid() {

        if (cartonId == null) {
            throw new IllegalArgumentException("Carton ID is required");
        }

        if (unitNumber == null || unitNumber.isBlank()) {
            throw new IllegalArgumentException("Unit Number is required");
        }

        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("Product Code is required");
        }

        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Employee ID is required");
        }
        if(locationCode == null || locationCode.isBlank()){
            throw new IllegalArgumentException("Location Code is required");
        }
    }
}
