package com.arcone.biopro.distribution.order.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@EqualsAndHashCode
@ToString
public class CancelOrderCommand implements Validatable {

    private String externalId;
    private String employeeId;
    private String reason;
    private String cancelDate;
    private static final String CANCEL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public CancelOrderCommand(String externalId, String employeeId, String reason , String cancelDate ) {
        this.externalId = externalId;
        this.employeeId = employeeId;
        this.reason = reason;
        this.cancelDate = cancelDate;
        checkValid();
    }

    @Override
    public void checkValid() {

        if (this.externalId == null) {
            throw new IllegalArgumentException("External ID cannot be null");
        }

        if (this.reason == null || this.reason.isEmpty()) {
            throw new IllegalArgumentException("Reason cannot be null or empty");
        }

        if (this.cancelDate == null || this.cancelDate.isEmpty()) {
            throw new IllegalArgumentException("Cancel Date cannot be null or empty");
        }

        try{
            LocalDateTime.parse(this.cancelDate,DateTimeFormatter.ofPattern(CANCEL_DATE_FORMAT));
        }catch (Exception e){
            throw new IllegalArgumentException("Cancel Date is not a valid date");
        }

    }
}
