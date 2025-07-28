package com.arcone.biopro.distribution.receiving.application.usecase;

import com.arcone.biopro.distribution.receiving.application.mapper.ShipmentCompletedMessageMapper;
import com.arcone.biopro.distribution.receiving.domain.model.InternalTransfer;
import com.arcone.biopro.distribution.receiving.domain.repository.InternalTransferRepository;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.ShipmentCompletedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentCompletedUseCase {

    private final InternalTransferRepository internalTransferRepository;
    private final ShipmentCompletedMessageMapper shipmentCompletedMessageMapper;
    private static final String INTERNAL_TRANSFER_SHIPMENT_TYPE = "INTERNAL_TRANSFER";

    public Mono<InternalTransfer> processShipmentCompletedMessage(final ShipmentCompletedMessage shipmentCompletedMessage){
        log.debug("Processing shipment completed message: {}", shipmentCompletedMessage);
        if(INTERNAL_TRANSFER_SHIPMENT_TYPE.equals(shipmentCompletedMessage.getPayload().getShipmentType())){
            return Mono.just(shipmentCompletedMessage)
                .map(shipmentCompletedMessageMapper::toModel)
                .flatMap(internalTransferRepository::create)
                .onErrorResume(error -> {
                    log.error("Not able to process Shipment Completed Message {}",error.getMessage());
                    return Mono.error(error);
                });
        }else{
            log.debug("Shipment type not supported: {}", shipmentCompletedMessage.getPayload().getShipmentType());
            return Mono.empty();
        }
    }

}
