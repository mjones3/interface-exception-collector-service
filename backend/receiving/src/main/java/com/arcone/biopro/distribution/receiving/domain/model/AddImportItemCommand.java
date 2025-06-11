package com.arcone.biopro.distribution.receiving.domain.model;


import com.arcone.biopro.distribution.receiving.domain.model.vo.AboRh;
import com.arcone.biopro.distribution.receiving.domain.model.vo.LicenseStatus;
import com.arcone.biopro.distribution.receiving.domain.model.vo.VisualInspection;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class AddImportItemCommand implements Validatable {

    private final Long importId;
    private final String unitNumber;
    private final String productCode;
    private final AboRh aboRh;
    private final LocalDateTime expirationDate;
    private final VisualInspection visualInspection;
    private final LicenseStatus licenseStatus;
    private final String employeeId;

    public AddImportItemCommand(Long importId, String unitNumber, String productCode, String aboRh, LocalDateTime expirationDate
        ,String visualInspection, String licenseStatus, String employeeId) {
        this.importId = importId;
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        this.aboRh = AboRh.getInstance(aboRh);
        this.expirationDate = expirationDate;
        this.visualInspection = VisualInspection.getInstance(visualInspection);
        this.licenseStatus = LicenseStatus.getInstance(licenseStatus);
        this.employeeId = employeeId;

        checkValid();
    }

    @Override
    public void checkValid() {

        if (importId == null) {
            throw new IllegalArgumentException("Import Id is required");
        }
        if (visualInspection == null) {
            throw new IllegalArgumentException("Visual Inspection is required");
        }
        if (licenseStatus == null) {
            throw new IllegalArgumentException("License Status is required");
        }
        if (unitNumber == null || unitNumber.isBlank()) {
            throw new IllegalArgumentException("Unit Number is required");
        }
        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("Product Code is required");
        }
        if (aboRh == null) {
            throw new IllegalArgumentException("ABO/RH is required");
        }
        if (expirationDate == null) {
            throw new IllegalArgumentException("Expiration Date is required");
        }

    }
}
