package com.arcone.biopro.distribution.order.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public class PickListItem implements Validatable {
    private String productFamily;
    private String bloodType;
    private Integer quantity;
    private String comments;
    private List<PickListItemShortDate> shortDateList;

    public PickListItem(String productFamily, String bloodType, Integer quantity, String comments) {
        this.productFamily = productFamily;
        this.bloodType = bloodType;
        this.quantity = quantity;
        this.comments = comments;

        checkValid();
    }

    @Override
    public void checkValid() {
        if (this.productFamily == null) {
            throw new IllegalArgumentException("productFamily cannot be null");
        }
        if (this.bloodType == null) {
            throw new IllegalArgumentException("bloodType cannot be null or blank");
        }
        if (this.quantity == null) {
            throw new IllegalArgumentException("quantity cannot be null");
        }
    }

    public void addShortDate(PickListItemShortDate shortDate) {
        if (this.shortDateList == null) {
            this.shortDateList = new ArrayList<>();
        }

        this.shortDateList.add(shortDate);
    }
}
