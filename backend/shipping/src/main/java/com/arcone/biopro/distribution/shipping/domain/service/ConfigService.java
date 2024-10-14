package com.arcone.biopro.distribution.shipping.domain.service;

import com.arcone.biopro.distribution.shipping.domain.model.Reason;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ConfigService {

    String LOOKUP_KEY_SHIPPING_CHECK_DIGIT_ACTIVE = "SHIPPING_CHECK_DIGIT_ACTIVE";
    String LOOKUP_KEY_SHIPPING_VISUAL_INSPECTION_ACTIVE = "SHIPPING_VISUAL_INSPECTION_ACTIVE";
    String REASON_KEY_VISUAL_INSPECTION_FAILED = "VISUAL_INSPECTION_FAILED";

    Mono<Boolean> findShippingCheckDigitActive();
    Mono<Boolean> findShippingVisualInspectionActive();

    Flux<Reason> findVisualInspectionFailedDiscardReasons();

}
