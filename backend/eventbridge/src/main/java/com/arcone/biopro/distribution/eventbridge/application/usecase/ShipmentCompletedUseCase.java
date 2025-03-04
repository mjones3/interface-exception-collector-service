package com.arcone.biopro.distribution.eventbridge.application.usecase;

import com.arcone.biopro.distribution.eventbridge.application.dto.ShipmentCompletedPayload;
import com.arcone.biopro.distribution.eventbridge.application.mapper.ShipmentCompletedMapper;
import com.arcone.biopro.distribution.eventbridge.domain.event.ShipmentCompletedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.domain.model.ShipmentCompletedOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.service.ShipmentCompletedService;
import io.github.springwolf.core.asyncapi.annotations.AsyncMessage;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentCompletedUseCase implements ShipmentCompletedService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ShipmentCompletedMapper shipmentCompletedMapper;

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "ShipmentCompletedOutbound",
        description = "Shipment Completed Outbound Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "import com.arcone.biopro.distribution.eventbridge.domain.event.ShipmentCompletedOutboundEvent"
        )),
        message = @AsyncMessage(
            name = "ShipmentCompletedOutbound",
            title = "ShipmentCompletedOutbound",
            description = "Shipment Completed Outbound Event"
        )
    ))
    @Override
    public Mono<Void> processCompletedShipmentEvent(ShipmentCompletedPayload shipmentCompletedEventPayloadDTO) {
        return publishShipmentCompletedOutboundEvent(shipmentCompletedMapper.toDomain(shipmentCompletedEventPayloadDTO));
    }

    private Mono<Void> publishShipmentCompletedOutboundEvent(ShipmentCompletedOutbound shipmentCompletedOutbound){
        applicationEventPublisher.publishEvent(new ShipmentCompletedOutboundEvent(shipmentCompletedOutbound));
        return Mono.empty();
    }
}
