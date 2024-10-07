package com.arcone.biopro.distribution.shipping.domain.service;

import reactor.core.publisher.Mono;

public interface ConfigService {

    String LOOKUP_KEY_SHIPPING_CHECK_DIGIT_ACTIVE = "SHIPPING_CHECK_DIGIT_ACTIVE";

    Mono<Boolean> findShippingCheckDigitActive();

}
