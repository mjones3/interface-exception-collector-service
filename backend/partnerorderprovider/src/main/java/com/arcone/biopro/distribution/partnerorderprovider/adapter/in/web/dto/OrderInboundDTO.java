package com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class OrderInboundDTO implements Serializable {

    private String externalId;
    private String locationCode;
    private String createDateTimezone;
    private String createDate;
    private String createEmployeeCode;
    private String orderStatus;
    private String shipmentType;
    private String deliveryType;
    private String shippingMethod;
    private String productCategory;
    private String desiredShippingDate;
    private String shippingCustomerCode;
    private String billingCustomerCode;
    private String comments;
    private OrderPickTypeDTO orderPickType;
    private List<OrderItemDTO> orderItems = new ArrayList<>();
    private Boolean quarantinedProducts;
    private String labelStatus;

}
