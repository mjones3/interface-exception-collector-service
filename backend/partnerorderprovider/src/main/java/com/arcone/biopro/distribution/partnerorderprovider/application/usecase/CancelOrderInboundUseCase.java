package com.arcone.biopro.distribution.partnerorderprovider.application.usecase;

import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto.CancelOrderInboundDTO;
import com.arcone.biopro.distribution.partnerorderprovider.application.dto.ValidationResponseDTO;
import com.arcone.biopro.distribution.partnerorderprovider.domain.event.CancelOrderInboundReceived;
import com.arcone.biopro.distribution.partnerorderprovider.domain.model.CancelOrder;
import com.arcone.biopro.distribution.partnerorderprovider.domain.service.CancelOrderInboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class CancelOrderInboundUseCase implements CancelOrderInboundService {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public ValidationResponseDTO receiveCancelOrderInbound(CancelOrderInboundDTO cancelOrderInboundDTO) {

        var cancelOrder = new CancelOrder(cancelOrderInboundDTO.getExternalId()
            , cancelOrderInboundDTO.getCancelDate()
            , cancelOrderInboundDTO.getCancelEmployeeCode()
            , cancelOrderInboundDTO.getCancelReason()
        );

        applicationEventPublisher.publishEvent(new CancelOrderInboundReceived(cancelOrder));
        return ValidationResponseDTO
            .builder()
            .id(cancelOrder.getId())
            .status("ACCEPTED")
            .timestamp(ZonedDateTime.now())
            .build();
    }
}
