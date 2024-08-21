package com.arcone.biopro.distribution.order.domain.model;

import com.arcone.biopro.distribution.order.domain.model.vo.PickListCustomer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public class PickList implements Validatable {

    private Long orderNumber;
    private String locationCode;
    private PickListCustomer customer;
    private String orderStatus;
    private List<PickListItem> pickListItems;
    private String orderComments;

    public PickList(Long orderNumber , String locationCode , String orderStatus, PickListCustomer customer , String orderComments) {
        this.orderNumber = orderNumber;
        this.locationCode = locationCode;
        this.customer = customer;
        this.orderStatus = orderStatus;
        this.orderComments = orderComments;

        checkValid();
    }

    @Override
    public void checkValid() {

        if (this.orderNumber == null) {
            throw new IllegalArgumentException("orderNumber cannot be null");
        }

        if (this.locationCode == null) {
            throw new IllegalArgumentException("locationCode cannot be null");
        }

        if (this.customer == null) {
            throw new IllegalArgumentException("customer cannot be null or blank");
        }
    }

    public void addPickListItem(PickListItem item) {
        if (this.pickListItems == null) {
            this.pickListItems = new ArrayList<>();
        }
        this.pickListItems.add(item);
    }
}
