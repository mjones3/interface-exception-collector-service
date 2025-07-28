package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.dto.CheckDigitResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service class for Check Digit.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CheckDigitUseCase {

    public Mono<CheckDigitResponseDTO> checkDigit(String unitNumber, String checkDigit) {
        log.info("Starting check digit process for unit number: {}", unitNumber);
        return Mono.just(new CheckDigitResponseDTO(calculateDigitCheck(unitNumber).equals(checkDigit)));
    }

    private String calculateDigitCheck(String unitNumber) {
        var UNIT_NUMBER_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ*";

        var sum = unitNumber.chars()
            .map(UNIT_NUMBER_ALPHABET::indexOf)
            .reduce(0, (acc, val) -> ((acc + val) * 2) % 37);

        return String.valueOf(UNIT_NUMBER_ALPHABET.charAt((38 - sum) % 37));
    }
}
