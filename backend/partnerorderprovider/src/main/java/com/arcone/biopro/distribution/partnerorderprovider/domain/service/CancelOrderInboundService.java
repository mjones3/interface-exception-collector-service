package com.arcone.biopro.distribution.partnerorderprovider.domain.service;

import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto.CancelOrderInboundDTO;
import com.arcone.biopro.distribution.partnerorderprovider.application.dto.ValidationResponseDTO;

public interface CancelOrderInboundService {

    ValidationResponseDTO receiveCancelOrderInbound(CancelOrderInboundDTO cancelOrderInboundDTO);
}
