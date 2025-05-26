package com.arcone.biopro.distribution.receiving.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class EnterShippingInformationCommand implements Validatable {

    private String productCategory;
    private String employeeId;
    private String locationCode;

    public EnterShippingInformationCommand(String productCategory, String employeeId, String locationCode) {
        this.productCategory = productCategory;
        this.employeeId = employeeId;
        this.locationCode = locationCode;
        checkValid();
    }

    @Override
    public void checkValid() {

        if (productCategory == null || productCategory.isBlank()) {
            throw new IllegalArgumentException("Product category is required");
        } else if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Employee ID is required");
        } else if (locationCode == null || locationCode.isBlank()) {
            throw new IllegalArgumentException("Location code is required");
        }

    }
}
