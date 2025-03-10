package com.arcone.biopro.distribution.shipping.domain.service;

import com.arcone.biopro.distribution.shipping.application.dto.CancelExternalTransferRequest;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import reactor.core.publisher.Mono;

public interface CancelExternalTransferService {

    Mono<RuleResponseDTO> cancelExternalTransfer(CancelExternalTransferRequest cancelExternalTransferRequest);

    Mono<RuleResponseDTO> confirmCancelExternalTransfer(CancelExternalTransferRequest cancelExternalTransferRequest);


}
