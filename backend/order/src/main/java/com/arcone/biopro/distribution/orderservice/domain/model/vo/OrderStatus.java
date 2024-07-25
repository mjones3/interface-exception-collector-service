package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
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
        if(!isValidStatus(orderStatus,lookupService)){
            throw new IllegalArgumentException("orderStatus is not a valid order status");
        }
    }

    private static boolean isValidStatus(String orderStatus , LookupService lookupService) {

        var list = lookupService.findAllByType(ORDER_STATUS_TYPE_CODE).collectList().block();
        if(list == null || list.isEmpty()) {
            return false;
        }

        return list.stream().anyMatch(lookup -> lookup.getId().getOptionValue().equals(orderStatus));
    }

}
