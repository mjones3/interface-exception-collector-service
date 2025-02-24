package com.arcone.biopro.distribution.order.domain.service;

import com.arcone.biopro.distribution.order.application.dto.CancelOrderReceivedDTO;
import reactor.core.publisher.Mono;

public interface CancelOrderService {

    Mono<Void> processCancelOrderReceivedEvent(CancelOrderReceivedDTO cancelOrderReceivedDTO);
}
