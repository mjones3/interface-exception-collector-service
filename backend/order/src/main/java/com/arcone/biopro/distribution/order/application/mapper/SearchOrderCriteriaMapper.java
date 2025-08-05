package com.arcone.biopro.distribution.order.application.mapper;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.*;
import com.arcone.biopro.distribution.order.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchOrderCriteriaMapper {

    public SearchOrderCriteriaDTO mapToDTO(final SearchOrderCriteria searchCriteriaValues ) {
        return new SearchOrderCriteriaDTO(
            searchCriteriaValues.getOrderStatus().stream().map(orderStatus ->
                LookupDTO.builder()
                .type(orderStatus.getId().getType())
                .optionValue(orderStatus.getId().getOptionValue())
                .descriptionKey(orderStatus.getDescriptionKey())
                .orderNumber(orderStatus.getOrderNumber())
                .active(orderStatus.isActive())
                .build()).toList(),
            searchCriteriaValues.getOrderPriorities().stream().map(orderPriority ->
                LookupDTO.builder()
                .type(orderPriority.getId().getType())
                .optionValue(orderPriority.getId().getOptionValue())
                .descriptionKey(orderPriority.getDescriptionKey())
                .orderNumber(orderPriority.getOrderNumber())
                .active(orderPriority.isActive())
                .build()).toList(),
            searchCriteriaValues.getCustomers().stream().map(customer -> OrderCustomerReportDTO.builder()
                .code(customer.getCode())
                .name(customer.getName())
                .build()).toList(),
            searchCriteriaValues.getShipmentTypes().stream().map(shipmenType ->
                LookupDTO.builder()
                    .type(shipmenType.getId().getType())
                    .optionValue(shipmenType.getId().getOptionValue())
                    .descriptionKey(shipmenType.getDescriptionKey())
                    .orderNumber(shipmenType.getOrderNumber())
                    .active(shipmenType.isActive())
                    .build()).toList(),
            searchCriteriaValues.getLocations().stream().map(location ->
                LocationFilterDTO.builder()
                    .code(location.getCode())
                    .name(location.getName())
                    .build()).toList()
            );
    }

}
