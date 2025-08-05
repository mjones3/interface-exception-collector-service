package com.arcone.biopro.distribution.eventbridge.infrastructure.mapper;

import com.arcone.biopro.distribution.eventbridge.domain.model.RecoveredPlasmaShipmentClosedCartonItemOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.model.RecoveredPlasmaShipmentClosedCartonOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.model.RecoveredPlasmaShipmentClosedOutbound;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.RecoveredPlasmaShipmentClosedCartonItemOutboundPayload;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.RecoveredPlasmaShipmentClosedCartonOutboundPayload;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.RecoveredPlasmaShipmentClosedOutboundPayload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring" , unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RecoveredPlasmaShipmentClosedOutboundEventMapper {

    @Mapping(target = "cartonList", source = "cartonOutboundList")
    @Mapping(target = "shipmentDate", source = "shipmentDateFormatted")
    @Mapping(target = "shipmentCloseDate", source = "closeDateFormatted")
    @Mapping(target = "totalShipmentProducts", expression = "java(recoveredPlasmaShipmentClosedOutbound.getTotalShipmentProducts())")
    RecoveredPlasmaShipmentClosedOutboundPayload modelToCloseEventDTO(RecoveredPlasmaShipmentClosedOutbound recoveredPlasmaShipmentClosedOutbound);

    @Mapping(target = "cartonNumber", source = "cartonNumber")
    @Mapping(target = "packedProducts", expression = "java(mapClosedProducts(closedCartonOutbound.getPackedProducts()))")
    RecoveredPlasmaShipmentClosedCartonOutboundPayload cartonModelToEventDTO(RecoveredPlasmaShipmentClosedCartonOutbound closedCartonOutbound);

    default List<RecoveredPlasmaShipmentClosedCartonItemOutboundPayload> mapClosedProducts(List<RecoveredPlasmaShipmentClosedCartonItemOutbound> cartonItems){
        if(cartonItems == null){
            return null;
        }
        return cartonItems.stream().map(this::productModelToEventDTO).toList();
    }

    @Mapping(target = "collectionDate", source = "collectionDateFormatted")
    @Mapping(target = "isbt128Flag", constant = "00")
    RecoveredPlasmaShipmentClosedCartonItemOutboundPayload productModelToEventDTO(RecoveredPlasmaShipmentClosedCartonItemOutbound cartonItem);

}
