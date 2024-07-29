package com.arcone.biopro.distribution.shipping.verification.support.types;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class ListShipmentsResponseType {
    Long id;
    Long orderNumber;
    String priority;
    String status;
    ZonedDateTime createDate;
}
