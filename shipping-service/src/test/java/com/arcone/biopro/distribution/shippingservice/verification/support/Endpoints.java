package com.arcone.biopro.distribution.shippingservice.verification.support;

public interface Endpoints {


    String CHECK_HEALTH = "/management/health";

    // Shipment endpoints
    String LIST_SHIPMENTS = "/v1/shipments";
    String GET_SHIPMENT = "/v1/shipments/{shipment.id}";
    String PACK_ITEM = "/v1/shipments/pack-item";
}
