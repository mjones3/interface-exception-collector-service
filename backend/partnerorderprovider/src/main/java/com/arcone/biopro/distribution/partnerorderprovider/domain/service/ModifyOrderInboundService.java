package com.arcone.biopro.distribution.partnerorderprovider.domain.service;

import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto.ModifyOrderInboundDTO;
import com.arcone.biopro.distribution.partnerorderprovider.application.dto.ValidationResponseDTO;

public interface ModifyOrderInboundService {

    ValidationResponseDTO receiveModifyOrderInbound(ModifyOrderInboundDTO modifyOrderInboundDTO);
}
