package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record PickListDTO(
    Long orderNumber,
    PickListCustomerDTO customer,
    List<PickListItemDTO> pickListItems,
    String orderComments,
    String shipmentType,
    Boolean quarantinedProducts,
    String labelStatus
) implements Serializable {
}
