package com.arcone.biopro.distribution.shippingservice.verification.support.types;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ListShipmentsResponseType {
    Long id;
    Long orderNumber;
    String priority;
    String status;
    ZonedDateTime createDate;
}
