package com.arcone.biopro.distribution.shipping.application.usecase;

import com.arcone.biopro.distribution.shipping.domain.repository.LookupRepository;
import com.arcone.biopro.distribution.shipping.domain.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final LookupRepository lookupRepository;

    @Override
    public Mono<Boolean> findShippingCheckDigitActive() {
        return this.lookupRepository.findFirstConfigAsBoolean(LOOKUP_KEY_SHIPPING_CHECK_DIGIT_ACTIVE);
    }

    @Override
    public Mono<Boolean> findShippingVisualInspectionActive() {
        return this.lookupRepository.findFirstConfigAsBoolean(LOOKUP_KEY_SHIPPING_VISUAL_INSPECTION_ACTIVE);
    }

}
