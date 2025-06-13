package com.arcone.biopro.distribution.eventbridge.application.mapper;

import com.arcone.biopro.distribution.eventbridge.application.dto.RecoveredPlasmaShipmentCartonItemClosedDTO;
import com.arcone.biopro.distribution.eventbridge.application.dto.RecoveredPlasmaShipmentClosedPayload;
import com.arcone.biopro.distribution.eventbridge.domain.model.RecoveredPlasmaShipmentClosedCartonItemOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.model.RecoveredPlasmaShipmentClosedOutbound;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RecoveredPlasmaShipmentClosedMapper {

    public RecoveredPlasmaShipmentClosedOutbound toDomain(RecoveredPlasmaShipmentClosedPayload recoveredPlasmaShipmentClosedPayload){
        var outbound =  new RecoveredPlasmaShipmentClosedOutbound(
            recoveredPlasmaShipmentClosedPayload.shipmentNumber(),
            recoveredPlasmaShipmentClosedPayload.locationShipmentCode(),
            recoveredPlasmaShipmentClosedPayload.locationCartonCode(),
            recoveredPlasmaShipmentClosedPayload.customerCode(),
            recoveredPlasmaShipmentClosedPayload.shipmentDate(),
            recoveredPlasmaShipmentClosedPayload.closeDate(),
            recoveredPlasmaShipmentClosedPayload.locationCode()
        );

        if(recoveredPlasmaShipmentClosedPayload.cartonList() != null){
            recoveredPlasmaShipmentClosedPayload.cartonList().forEach(cartonClosed -> {
                outbound.addCarton(cartonClosed.cartonNumber(), cartonClosed.totalProducts(), cartonClosed.packedProducts().stream().map(this::toDomain).toList());
            });
        }
        return outbound;
    }

    public RecoveredPlasmaShipmentClosedCartonItemOutbound toDomain(RecoveredPlasmaShipmentCartonItemClosedDTO recoveredPlasmaShipmentCartonItemClosedDTO){
        return new RecoveredPlasmaShipmentClosedCartonItemOutbound(
            recoveredPlasmaShipmentCartonItemClosedDTO.unitNumber(),
            recoveredPlasmaShipmentCartonItemClosedDTO.productCode(),
            recoveredPlasmaShipmentCartonItemClosedDTO.collectionFacility(),
            recoveredPlasmaShipmentCartonItemClosedDTO.donationDate(),
            recoveredPlasmaShipmentCartonItemClosedDTO.volume(),
            recoveredPlasmaShipmentCartonItemClosedDTO.aboRh(),
            recoveredPlasmaShipmentCartonItemClosedDTO.collectionTimeZone()
        );
    }

}
