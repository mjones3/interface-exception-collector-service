package com.arcone.biopro.distribution.shipping.application.usecase;

import com.arcone.biopro.distribution.shipping.domain.model.UnitNumberWithCheckDigit;
import com.arcone.biopro.distribution.shipping.domain.service.UnitNumberWithCheckDigitService;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UnitNumberWithCheckDigitUseCase implements UnitNumberWithCheckDigitService {

    @Override
    @WithSpan("verifyCheckDigit")
    public Mono<UnitNumberWithCheckDigit> verifyCheckDigit(String unitNumber, String checkDigit) {
        return Mono.just(new UnitNumberWithCheckDigit(unitNumber, checkDigit));
    }

}
