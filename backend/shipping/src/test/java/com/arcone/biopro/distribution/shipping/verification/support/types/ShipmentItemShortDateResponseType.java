package com.arcone.biopro.distribution.shipping.verification.support.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.ZonedDateTime;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
