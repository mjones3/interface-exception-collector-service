package com.arcone.biopro.distribution.eventbridge.application.mapper;

import com.arcone.biopro.distribution.eventbridge.application.dto.ShipmentCompletedPayload;
import com.arcone.biopro.distribution.eventbridge.domain.model.ShipmentCompletedOutbound;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShipmentCompletedMapper {

    public ShipmentCompletedOutbound toDomain(ShipmentCompletedPayload shipmentCompletedPayload){
        return new ShipmentCompletedOutbound();
    }
}
