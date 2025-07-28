package com.arcone.biopro.distribution.receiving.application.mapper;

import com.arcone.biopro.distribution.receiving.domain.model.InternalTransfer;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.ShipmentCompletedMessage;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ShipmentCompletedMessageMapper {

    default InternalTransfer toModel(ShipmentCompletedMessage shipmentCompletedMessage){
        var payload = shipmentCompletedMessage.getPayload();
        var internalTransfer = InternalTransfer.create(payload.getOrderNumber(), payload.getExternalOrderId(), payload.getProductCategory(), payload.getLocationCode(), payload.getCustomerCode()
            , payload.getLabelStatus(), payload.getQuarantinedProducts() , payload.getPerformedBy());
        payload.getLineItems()
            .forEach(lineItem -> lineItem.getProducts()
                .forEach(product -> internalTransfer.addItem(product.getUnitNumber(), product.getProductCode(), product.getProductDescription())));

        return internalTransfer;
    }

}
