package com.arcone.biopro.distribution.shipping.verification.support.types;

import com.arcone.biopro.distribution.shipping.domain.model.enumeration.BloodType;
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
