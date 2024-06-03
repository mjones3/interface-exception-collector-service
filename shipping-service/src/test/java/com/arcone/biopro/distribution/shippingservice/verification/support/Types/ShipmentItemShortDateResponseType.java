package com.arcone.biopro.distribution.shippingservice.verification.support.Types;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.ZonedDateTime;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class ShipmentItemShortDateResponseType implements Serializable {

    Long id;
    Long shipmentItemId;
    String unitNumber;
    String productCode;
    String storageLocation;
    String comments;
    ZonedDateTime createDate;
    ZonedDateTime modificationDate;

}
