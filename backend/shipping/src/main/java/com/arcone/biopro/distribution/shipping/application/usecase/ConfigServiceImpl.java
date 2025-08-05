package com.arcone.biopro.distribution.shipping.application.usecase;

import com.arcone.biopro.distribution.shipping.domain.model.Reason;
import com.arcone.biopro.distribution.shipping.domain.repository.LookupRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ReasonRepository;
import com.arcone.biopro.distribution.shipping.domain.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final LookupRepository lookupRepository;
    private final ReasonRepository reasonRepository;

    @Override
    public Mono<Boolean> findShippingCheckDigitActive() {
        return this.lookupRepository.findFirstConfigAsBoolean(LOOKUP_KEY_SHIPPING_CHECK_DIGIT_ACTIVE);
    }

    @Override
    public Mono<Boolean> findShippingVisualInspectionActive() {
        return this.lookupRepository.findFirstConfigAsBoolean(LOOKUP_KEY_SHIPPING_VISUAL_INSPECTION_ACTIVE);
    }

    @Override
    public Flux<Reason> findVisualInspectionFailedDiscardReasons() {
        return reasonRepository.findAllByType(REASON_KEY_VISUAL_INSPECTION_FAILED);
    }

    @Override
    public Mono<Boolean> findShippingSecondVerificationActive() {
        return this.lookupRepository.findFirstConfigAsBoolean(LOOKUP_KEY_SHIPPING_SECOND_VERIFICATION_ACTIVE);
    }

}
