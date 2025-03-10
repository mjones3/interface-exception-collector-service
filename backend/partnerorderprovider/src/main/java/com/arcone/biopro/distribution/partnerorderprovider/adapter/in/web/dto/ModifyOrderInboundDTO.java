package com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ModifyOrderInboundDTO implements Serializable {

    private String externalId;
    private String locationCode;
    private String modifyReason;
    private String modifyDate;
    private String modifyEmployeeCode;
    private String shipToLocation;
    private String deliveryType;
    private String shippingMethod;
    private String productCategory;
    private String desiredShippingDate;
    private String comments;
    private OrderPickTypeDTO orderPickType;
    private List<OrderItemDTO> orderItems = new ArrayList<>();

}
