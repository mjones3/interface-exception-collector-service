package com.arcone.biopro.distribution.receiving.infrastructure.service;

import com.arcone.biopro.distribution.receiving.domain.model.BarcodePattern;
import com.arcone.biopro.distribution.receiving.domain.model.BarcodeTranslation;
import com.arcone.biopro.distribution.receiving.domain.model.FinNumber;
import com.arcone.biopro.distribution.receiving.domain.model.Product;
import com.arcone.biopro.distribution.receiving.domain.model.enumeration.ParseType;
import com.arcone.biopro.distribution.receiving.domain.service.ConfigurationService;
import com.arcone.biopro.distribution.receiving.infrastructure.mapper.BarcodePatternEntityMapper;
import com.arcone.biopro.distribution.receiving.infrastructure.mapper.BarcodeTranslationEntityMapper;
import com.arcone.biopro.distribution.receiving.infrastructure.mapper.FinNumberEntityMapper;
import com.arcone.biopro.distribution.receiving.infrastructure.mapper.ProductEntityMapper;
import com.arcone.biopro.distribution.receiving.infrastructure.persistence.BarcodePatternRepository;
import com.arcone.biopro.distribution.receiving.infrastructure.persistence.BarcodeTranslationRepository;
import com.arcone.biopro.distribution.receiving.infrastructure.persistence.FinNumberEntityRepository;
import com.arcone.biopro.distribution.receiving.infrastructure.persistence.ProductEntityRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConfigurationServiceImpl implements ConfigurationService {

    BarcodePatternRepository barcodePatternRepository;

    BarcodePatternEntityMapper barcodePatternEntityMapper;

    BarcodeTranslationRepository barcodeTranslationRepository;

    BarcodeTranslationEntityMapper barcodeTranslationEntityMapper;

    FinNumberEntityRepository finNumberEntityRepository;

    FinNumberEntityMapper finNumberEntityMapper;

    ProductEntityRepository productEntityRepository;

    ProductEntityMapper productEntityMapper;


    @Override
    public Mono<BarcodePattern> findByParseType(ParseType parseType) {
        return barcodePatternRepository.findByParseType(parseType).map(barcodePatternEntityMapper::toDomain);
    }

    @Override
    public Mono<BarcodeTranslation> findByFromValueAndSixthDigit(String foundText, String sixthDigit) {
        return barcodeTranslationRepository.findByFromValueAndSixthDigit(foundText, sixthDigit).map(barcodeTranslationEntityMapper::toDomain);
    }

    @Override
    public Mono<FinNumber> findByFinNumber(String finNumber) {
        return finNumberEntityRepository.findFirstByFinNumberAndActiveIsTrue(finNumber)
            .map(finNumberEntityMapper::toDomain);
    }

    @Override
    public Mono<Product> findByCodeAndTemperatureCategory(String productCode, String temperatureCategory) {
        return productEntityRepository.findByProductCodeAndTemperatureCategory(productCode,temperatureCategory)
            .map(productEntityMapper::toModel);
    }

}
