package com.arcone.biopro.distribution.shipping.domain.service;

import com.arcone.biopro.distribution.shipping.application.dto.CancelSecondVerificationRequest;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import reactor.core.publisher.Mono;

public interface CancelSecondVerificationService {

    Mono<RuleResponseDTO> cancelSecondVerification(CancelSecondVerificationRequest cancelSecondVerificationRequest);

    Mono<RuleResponseDTO> confirmCancelSecondVerification(CancelSecondVerificationRequest cancelSecondVerificationRequest);

}
