package com.arcone.biopro.distribution.shippingservice.verification.support.Types;

import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.BloodType;
import lombok.Data;

@Data
public class OrderItemResponseType {
    Long id;
    Long orderId;
    String productFamily;
    BloodType bloodType;
    Integer quantity;
    String comments;
}
