package com.arcone.biopro.distribution.receiving.domain.service;

import com.arcone.biopro.distribution.receiving.domain.model.BarcodePattern;
import com.arcone.biopro.distribution.receiving.domain.model.BarcodeTranslation;
import com.arcone.biopro.distribution.receiving.domain.model.FinNumber;
import com.arcone.biopro.distribution.receiving.domain.model.Product;
import com.arcone.biopro.distribution.receiving.domain.model.enumeration.ParseType;
import reactor.core.publisher.Mono;

public interface ConfigurationService {

    Mono<BarcodePattern> findByParseType(ParseType parseType);

    Mono<BarcodeTranslation> findByFromValueAndSixthDigit(String foundText, String sixthDigit);

    Mono<FinNumber> findByFinNumber(String finNumber);

    Mono<Product> findByCodeAndTemperatureCategory(String productCode , String temperatureCategory);

    Mono<Product> findProductByCode(String productCode);

}
