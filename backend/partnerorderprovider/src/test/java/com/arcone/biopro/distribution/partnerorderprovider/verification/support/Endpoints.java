package com.arcone.biopro.distribution.partnerorderprovider.verification.support;

public interface Endpoints {
    String ORDER_INBOUND = "/v1/partner-order-provider/orders";
    String CANCEL_ORDER_INBOUND = "/v1/partner-order-provider/orders/{externalId}/cancel";
    String MODIFY_ORDER_INBOUND = "/v1/partner-order-provider/orders/{externalId}/modify";
    String CHECK_HEALTH = "/management/health";
}
