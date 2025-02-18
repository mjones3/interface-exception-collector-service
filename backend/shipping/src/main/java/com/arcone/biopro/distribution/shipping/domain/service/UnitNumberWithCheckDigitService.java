package com.arcone.biopro.distribution.shipping.domain.service;

import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import reactor.core.publisher.Mono;

public interface UnitNumberWithCheckDigitService {

    Mono<RuleResponseDTO> verifyCheckDigit(String unitNumber, String checkDigit);

}
