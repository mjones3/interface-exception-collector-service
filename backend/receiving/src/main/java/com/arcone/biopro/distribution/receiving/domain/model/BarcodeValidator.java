package com.arcone.biopro.distribution.receiving.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.vo.AboRh;
import com.arcone.biopro.distribution.receiving.domain.model.vo.ValidationResult;
import com.arcone.biopro.distribution.receiving.domain.service.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class BarcodeValidator {

    private static final DateTimeFormatter JULIAN_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("0yyDDDHHmm", Locale.US);
    private static final DateTimeFormatter EXPIRATION_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US);
    private static final String DONATION_TYPE_V = "V";

    public static ValidationResult validateBarcode(ValidateBarcodeCommand validateBarcodeCommand , ConfigurationService configurationService) {
        if (validateBarcodeCommand == null) {
            throw new IllegalArgumentException("Barcode command is required");
        }

        if (configurationService == null) {
            throw new IllegalArgumentException("Configuration service is required");
        }

        var pattern = configurationService.findByParseType(validateBarcodeCommand.getParseType())
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Barcode Pattern is required")))
            .block();

        Pattern regexPattern = Pattern.compile(pattern.getPattern());
        var matchingGroup = pattern.getMatchGroups();
        Matcher matcher = regexPattern.matcher(validateBarcodeCommand.getBarcodeValue());
        if (matcher.find()) {
            String foundText = matcher.group(matchingGroup).trim();

            return switch (validateBarcodeCommand.getParseType()) {
                case BARCODE_UNIT_NUMBER -> configurationService.findByFinNumber(foundText.substring(0,5))
                    .flatMap(translation -> Mono.just(ValidationResult.builder().valid(true).result(foundText).build()))
                    .switchIfEmpty(Mono.just( ValidationResult.builder().valid(false).message("FIN is not associated with a registered facility").build()))
                    .block();
                case BARCODE_PRODUCT_CODE ->
                    configurationService.findByCodeAndTemperatureCategory(foundText, validateBarcodeCommand.getTemperatureCategory())
                        .flatMap(translation -> Mono.just(ValidationResult.builder().valid(true).result(foundText).build()))
                        .switchIfEmpty(Mono.just( ValidationResult.builder().valid(false).message("Product type does not match").build()))
                        .block();
                case BARCODE_EXPIRATION_DATE -> checkExpirationDateValue(foundText);
                case BARCODE_BLOOD_GROUP ->
                    configurationService.findByFromValueAndSixthDigit(foundText, DONATION_TYPE_V)
                        .flatMap(translation -> Mono.just(ValidationResult.builder().valid(true)
                                .result(AboRh.getInstance(translation.getToValue()).value())
                            .resultDescription(AboRh.getInstance(translation.getToValue()).description()).build()))
                        .switchIfEmpty(Mono.just( ValidationResult.builder().valid(false).message("Invalid Blood Type").build()))
                        .block();
            };
        }

        return ValidationResult
            .builder()
            .valid(false)
            .message("Barcode is not valid")
            .build();
    }

    private static ValidationResult checkExpirationDateValue(String foundText) {
        try {
            var expirationDate = LocalDateTime.parse(foundText, JULIAN_DATE_TIME_FORMATTER);
            return ValidationResult.builder().valid(true)
                .resultDescription(EXPIRATION_DATE_FORMAT.format(expirationDate))
                .result(expirationDate.toString()).build();
        }catch (Exception e){
            log.error("Not able to parse Expiration Date {}",e.getMessage());
            return ValidationResult.builder().valid(false).message("Invalid Expiration Date").build();
        }
    }
}
