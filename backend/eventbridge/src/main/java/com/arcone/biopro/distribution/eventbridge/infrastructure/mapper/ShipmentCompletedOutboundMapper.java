package com.arcone.biopro.distribution.eventbridge.infrastructure.mapper;

import com.arcone.biopro.distribution.eventbridge.domain.model.ShipmentCompletedOutbound;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.ShipmentCompletedOutboundPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShipmentCompletedOutboundMapper {

    public ShipmentCompletedOutboundPayload toDto(ShipmentCompletedOutbound shipmentCompletedOutbound){
        return ShipmentCompletedOutboundPayload
            .builder()
            .shipmentId(1L)
            .build();

    }
}
