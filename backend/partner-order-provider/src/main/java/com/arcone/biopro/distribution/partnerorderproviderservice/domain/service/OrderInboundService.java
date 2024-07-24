package com.arcone.biopro.distribution.partnerorderproviderservice.domain.service;

import com.arcone.biopro.distribution.partnerorderproviderservice.adapter.in.web.dto.OrderInboundDTO;
import com.arcone.biopro.distribution.partnerorderproviderservice.application.dto.ValidationResponseDTO;

public interface OrderInboundService {

    ValidationResponseDTO receiveOrderInbound(OrderInboundDTO orderInboundDTO);
}
