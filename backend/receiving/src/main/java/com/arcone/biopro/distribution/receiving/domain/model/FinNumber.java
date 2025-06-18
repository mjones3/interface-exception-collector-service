package com.arcone.biopro.distribution.receiving.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
@Builder(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FinNumber implements Validatable {


    private Long id;
    private String finNumber;
    private Integer orderNumber;
    private boolean active;
    private ZonedDateTime createDate;
    private ZonedDateTime modificationDate;

    public FinNumber(Long id, String finNumber, Integer orderNumber, boolean active, ZonedDateTime createDate, ZonedDateTime modificationDate) {
        this.id = id;
        this.finNumber = finNumber;
        this.orderNumber = orderNumber;
        this.active = active;
        this.createDate = createDate;
        this.modificationDate = modificationDate;

        checkValid();
    }

    public void checkValid() {

        if (finNumber == null || finNumber.isEmpty()) {
            throw new IllegalArgumentException("Fin Number is required.");
        }
        if (orderNumber == null) {
            throw new IllegalArgumentException("Order Number is required.");
        }
        if (createDate == null) {
            throw new IllegalArgumentException("Create Date is required.");
        }
        if (modificationDate == null) {
            throw new IllegalArgumentException("Modification Date is required.");
        }

    }
}

