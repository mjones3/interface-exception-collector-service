package com.arcone.biopro.distribution.order.application.mapper;

import com.arcone.biopro.distribution.order.domain.model.GeneratePickListCommand;
import com.arcone.biopro.distribution.order.domain.model.GeneratePickListProductCriteria;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.PickList;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static java.util.Optional.ofNullable;

@Component
public class PickListCommandMapper {


    public GeneratePickListCommand mapToDomain(PickList pickList){
        return new GeneratePickListCommand(pickList.getLocationCode(),mapCriteriaList(pickList));
    }

    private List<GeneratePickListProductCriteria> mapCriteriaList(PickList pickList) {
        return ofNullable(pickList.getPickListItems())
            .filter(pickListItems -> !pickListItems.isEmpty())
            .orElseGet(Collections::emptyList)
            .stream()
            .map(pickListItem -> new GeneratePickListProductCriteria(pickListItem.getProductFamily()
                , pickListItem.getBloodType(), pickList.getTemperatureCategory()))
            .toList();
    }

    public GeneratePickListCommand mapToDomain(Order order){
        return new GeneratePickListCommand(order.getLocationFrom().getCode(),mapCriteriaList(order));
    }

    private List<GeneratePickListProductCriteria> mapCriteriaList(Order order) {
        return ofNullable(order.getOrderItems())
            .filter(orderItems -> !orderItems.isEmpty())
            .orElseGet(Collections::emptyList)
            .stream()
            .map(orderItem -> new GeneratePickListProductCriteria(orderItem.getProductFamily().getProductFamily()
                , orderItem.getBloodType().getBloodType() , order.getProductCategory().getProductCategory()))
            .toList();
    }
}
