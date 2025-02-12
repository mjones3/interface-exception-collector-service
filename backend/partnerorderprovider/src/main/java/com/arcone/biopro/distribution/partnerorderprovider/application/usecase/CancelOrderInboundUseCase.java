package com.arcone.biopro.distribution.partnerorderprovider.application.usecase;

import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto.CancelOrderInboundDTO;
import com.arcone.biopro.distribution.partnerorderprovider.application.dto.ValidationResponseDTO;
import com.arcone.biopro.distribution.partnerorderprovider.domain.service.CancelOrderInboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelOrderInboundUseCase implements CancelOrderInboundService {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public ValidationResponseDTO receiveCancelOrderInbound(CancelOrderInboundDTO cancelOrderInboundDTO) {
        return null;
    }
}
