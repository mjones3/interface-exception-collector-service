package com.arcone.biopro.distribution.shipping.domain.service;

import com.arcone.biopro.distribution.shipping.domain.model.UnitNumberWithCheckDigit;
import reactor.core.publisher.Mono;

public interface UnitNumberWithCheckDigitService {

    Mono<UnitNumberWithCheckDigit> verifyCheckDigit(String unitNumber, String checkDigit);

}
