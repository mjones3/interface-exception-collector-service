package com.arcone.biopro.distribution.shipping.application.usecase;

import com.arcone.biopro.distribution.shipping.domain.model.vo.UnitNumberWithCheckDigit;
import com.arcone.biopro.distribution.shipping.domain.service.UnitNumberWithCheckDigitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UnitNumberWithCheckDigitUseCase implements UnitNumberWithCheckDigitService {

    @Override
    public Mono<UnitNumberWithCheckDigit> verifyCheckDigit(String unitNumber, String checkDigit) {
        return Mono.just(new UnitNumberWithCheckDigit(unitNumber, checkDigit));
    }

}
