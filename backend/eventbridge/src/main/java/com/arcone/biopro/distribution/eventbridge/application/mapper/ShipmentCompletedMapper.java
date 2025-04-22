package com.arcone.biopro.distribution.eventbridge.application.mapper;

import com.arcone.biopro.distribution.eventbridge.application.dto.ShipmentCompletedPayload;
import com.arcone.biopro.distribution.eventbridge.domain.model.ShipmentCompletedOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.model.ShipmentLineItem;
import com.arcone.biopro.distribution.eventbridge.domain.model.vo.ShipmentCustomer;
import com.arcone.biopro.distribution.eventbridge.domain.model.vo.ShipmentLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShipmentCompletedMapper {

    public ShipmentCompletedOutbound toDomain(ShipmentCompletedPayload shipmentCompletedPayload){

        var shipmentCompletedOutbound = new ShipmentCompletedOutbound(shipmentCompletedPayload.shipmentId()
            ,shipmentCompletedPayload.externalOrderId()
            ,shipmentCompletedPayload.createDate()
            , new ShipmentCustomer(shipmentCompletedPayload.customerCode(), shipmentCompletedPayload.customerType() , shipmentCompletedPayload.customerName() , shipmentCompletedPayload.departmentCode())
            , new ShipmentLocation(shipmentCompletedPayload.locationCode(), shipmentCompletedPayload.locationName()), shipmentCompletedPayload.deliveryType()
        );

        if(shipmentCompletedPayload.lineItems() != null && !shipmentCompletedPayload.lineItems().isEmpty()){
            shipmentCompletedPayload.lineItems().forEach(lineItem -> {
                var outboundLineItem = new ShipmentLineItem(lineItem.productFamily(), lineItem.quantity());
                if(lineItem.products()!= null && !lineItem.products().isEmpty()){
                    lineItem.products().forEach(product -> outboundLineItem.addProduct(product.unitNumber(), product.productCode(), product.aboRh()
                        , product.expirationDate(),product.collectionDate()));
                }
                shipmentCompletedOutbound.addLineItem(outboundLineItem);
            });
        }
        if(shipmentCompletedPayload.services() != null && !shipmentCompletedPayload.services().isEmpty()){
            shipmentCompletedPayload.services().forEach(service -> {
               shipmentCompletedOutbound.addService(service.code(), service.quantity());
            });
        }
        return shipmentCompletedOutbound;
    }
}
