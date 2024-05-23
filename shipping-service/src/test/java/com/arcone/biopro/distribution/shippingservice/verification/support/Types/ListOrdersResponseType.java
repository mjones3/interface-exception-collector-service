package com.arcone.biopro.distribution.shippingservice.verification.support.Types;

import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.OrderPriority;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.OrderStatus;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ListOrdersResponseType {
    Long id;
    Long orderNumber;
    OrderPriority priority;
    OrderStatus status;
    ZonedDateTime createDate;
}
