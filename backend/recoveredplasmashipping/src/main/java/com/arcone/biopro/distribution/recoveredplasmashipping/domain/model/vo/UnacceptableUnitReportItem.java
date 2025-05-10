package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class UnacceptableUnitReportItem implements Validatable {

    private Long shipmentId;
    private String cartonNumber;
    private Integer cartonSequenceNumber;
    private String unitNumber;
    private String productCode;
    private String failureReason;
    private ZonedDateTime createDate;

    public UnacceptableUnitReportItem(Long shipmentId, String cartonNumber, Integer cartonSequenceNumber, String unitNumber
        , String productCode, String failureReason, ZonedDateTime createDate) {
        this.shipmentId = shipmentId;
        this.cartonNumber = cartonNumber;
        this.cartonSequenceNumber = cartonSequenceNumber;
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        this.failureReason = failureReason;
        this.createDate = createDate;

        checkValid();

    }

    @Override
    public void checkValid() {
        if (cartonNumber == null || cartonNumber.isBlank()) {
            throw new IllegalArgumentException("Carton Number is null or blank");
        }

        if (cartonSequenceNumber == null) {
            throw new IllegalArgumentException("Carton Sequence Number is null");
        }

        if (unitNumber == null || unitNumber.isBlank()) {
            throw new IllegalArgumentException("Unit Number is null or blank");
        }

        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("Product Code is null or blank");
        }

        if (failureReason == null || failureReason.isBlank()) {
            throw new IllegalArgumentException("Failure Reason is null or blank");
        }

        if (createDate == null) {
            throw new IllegalArgumentException("Create Date is null");
        }

    }
}
