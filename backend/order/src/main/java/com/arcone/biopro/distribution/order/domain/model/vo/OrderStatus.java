package com.arcone.biopro.distribution.order.domain.model.vo;

import com.arcone.biopro.distribution.order.domain.model.Validatable;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class OrderStatus implements Validatable {

    private String orderStatus;
    private static final String ORDER_STATUS_TYPE_CODE = "ORDER_STATUS";
    private final LookupService lookupService;

    public OrderStatus(String orderStatus, LookupService lookupService) {
        this.orderStatus = orderStatus;
        this.lookupService = lookupService;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (orderStatus == null || orderStatus.isBlank()) {
            throw new IllegalArgumentException("orderStatus cannot be null or blank");
        }
        isValidStatus(orderStatus,lookupService);
    }
    private static void isValidStatus(String orderStatus , LookupService lookupService) {

        var types = lookupService.findAllByType(ORDER_STATUS_TYPE_CODE).collectList().block();

        if(types == null || types.isEmpty()) {
            throw new IllegalArgumentException("Order Status " + orderStatus + " is not valid");
        }

        if (types.stream().noneMatch(lookup -> lookup.getId().getOptionValue().equals(orderStatus))) {
            throw new IllegalArgumentException("Order Status " + orderStatus + " is not valid");
        }

    }

    public void setStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}
