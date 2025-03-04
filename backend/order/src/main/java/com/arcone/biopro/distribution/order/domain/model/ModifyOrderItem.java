package com.arcone.biopro.distribution.order.domain.model;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ModifyOrderItem {

    private String productFamily;
    private String bloodType;
    private Integer quantity;
    private String comment;
}
