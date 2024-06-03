package com.arcone.biopro.distribution.shippingservice.verification.support.Types;

import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.BloodType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class ShipmentFulfillmentRequest {
    Long id;
    Long shipmentId;
    String productFamily;
    BloodType bloodType;
    Integer quantity;
    String comments;
    List<ShipmentItemShortDateResponseType> shortDateProducts;
}
