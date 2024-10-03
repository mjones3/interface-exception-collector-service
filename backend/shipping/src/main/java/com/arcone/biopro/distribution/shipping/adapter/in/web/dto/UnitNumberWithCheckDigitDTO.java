package com.arcone.biopro.distribution.shipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record UnitNumberWithCheckDigitDTO (
    String unitNumber,
    String verifiedCheckDigit
) implements Serializable {}
