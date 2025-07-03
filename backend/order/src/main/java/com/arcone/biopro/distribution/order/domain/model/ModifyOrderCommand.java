package com.arcone.biopro.distribution.order.domain.model;

import com.arcone.biopro.distribution.order.domain.model.vo.ModifyByProcess;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@ToString
@Builder
@Getter
public class ModifyOrderCommand {

    private ModifyByProcess modifyByProcess;
    private String externalId;
    private String locationCode;
    private String modifyDate;
    private String modifyReason;
    private String modifyEmployeeCode;
    private String deliveryType;
    private String shippingMethod;
    private String productCategory;
    private String desiredShippingDate;
    private boolean willPickUp;
    private String willPickUpPhoneNumber;
    private String comments;
    private List<ModifyOrderItem> orderItems;
    private UUID transactionId;
    private Boolean quarantinedProducts;
    private String labelStatus;

}
