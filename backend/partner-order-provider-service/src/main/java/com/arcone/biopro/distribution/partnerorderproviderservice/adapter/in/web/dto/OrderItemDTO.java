package com.arcone.biopro.distribution.partnerorderproviderservice.adapter.in.web.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class OrderItemDTO implements Serializable {

    private String productFamily;
    private String bloodType;
    private Integer quantity;
    private String comments;

}
