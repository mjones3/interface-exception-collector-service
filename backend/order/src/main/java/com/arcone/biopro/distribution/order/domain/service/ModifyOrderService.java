package com.arcone.biopro.distribution.order.domain.service;

import com.arcone.biopro.distribution.order.application.dto.ModifyOrderReceivedDTO;
import reactor.core.publisher.Mono;

public interface ModifyOrderService {

    Mono<Void> processModifyOrderEvent(ModifyOrderReceivedDTO modifyOrderReceivedDTO);
}
