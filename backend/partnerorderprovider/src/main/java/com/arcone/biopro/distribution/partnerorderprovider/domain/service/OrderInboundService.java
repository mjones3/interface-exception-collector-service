package com.arcone.biopro.distribution.partnerorderprovider.domain.service;

import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto.OrderInboundDTO;
import com.arcone.biopro.distribution.partnerorderprovider.application.dto.ValidationResponseDTO;

public interface OrderInboundService {

    ValidationResponseDTO receiveOrderInbound(OrderInboundDTO orderInboundDTO);
}
