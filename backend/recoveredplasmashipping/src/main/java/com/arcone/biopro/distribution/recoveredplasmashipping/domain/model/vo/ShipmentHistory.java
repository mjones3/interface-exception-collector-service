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
public class ShipmentHistory implements Validatable {

    private Long id;
    private Long shipmentId;
    private String comments;
    private String createEmployeeId;
    private ZonedDateTime createDate;

    public ShipmentHistory(Long id, Long shipmentId, String comments, String createEmployeeId, ZonedDateTime createDate) {
        this.id = id;
        this.shipmentId = shipmentId;
        this.comments = comments;
        this.createEmployeeId = createEmployeeId;
        this.createDate = createDate;

        checkValid();
    }

    @Override
    public void checkValid() {
        if (shipmentId == null) {
            throw new IllegalArgumentException("Shipment Id is required");
        }
        if (createEmployeeId == null || createEmployeeId.isBlank()) {
            throw new IllegalArgumentException("Create Employee Id is required");
        }
        if (createDate == null) {
            throw new IllegalArgumentException("Create Date is required");
        }
        if (comments == null || comments.isBlank()) {
            throw new IllegalArgumentException("Comments is required");
        }
    }
}
